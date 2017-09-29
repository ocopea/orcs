// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.io;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: rosenr5
 * Date: 12/1/13
 * Time: 10:09 AM
 */
public class PrefixReader extends Reader {
    private final char[] prefix;
    private final Reader delegate;
    private int prefixLocation = 0;

    public PrefixReader(char[] prefix, Reader delegate) {
        if (prefix != null) {
            this.prefix = Arrays.copyOf(prefix, prefix.length);
        } else {
            this.prefix = null;
        }
        this.delegate = delegate;
    }

    public PrefixReader(Object lock, char[] prefix, Reader delegate) {
        super(lock);
        if (prefix != null) {
            this.prefix = Arrays.copyOf(prefix, prefix.length);
        } else {
            this.prefix = null;
        }
        this.delegate = delegate;
    }

    @Override
    public int read(CharBuffer target) throws IOException {
        int len = target.remaining();
        int remainingInPrefix = prefix.length - prefixLocation;
        if (remainingInPrefix >= len) {
            //can satisfy request from prefix
            target.put(prefix, prefixLocation, len);
            prefixLocation += len;
            return len;
        }
        //logical else - not enough in prefix
        if (remainingInPrefix > 0) { //empty what remains in prefix
            target.put(prefix, prefixLocation, remainingInPrefix);
            prefixLocation = prefix.length;
        }
        int readFromDelegate = delegate.read(target); //and move on
        if (remainingInPrefix > 0 && readFromDelegate < 0) {
            //underlying stream is dry yet we can still return stuff from the prefix.
            //return from prefix and let the next call return the -1 (EOF)
            return remainingInPrefix;
        }
        return remainingInPrefix + readFromDelegate;
    }

    @Override
    public int read() throws IOException {
        int remainingInPrefix = prefix.length - prefixLocation;
        if (remainingInPrefix > 0) { //still reading from prefix
            char toReturn = prefix[prefixLocation++];
            return toReturn;
        }
        return delegate.read();
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int remainingInPrefix = prefix.length - prefixLocation;
        if (remainingInPrefix >= len) {
            //can satisfy request from prefix
            System.arraycopy(prefix, prefixLocation, cbuf, off, len);
            prefixLocation += len;
            return len;
        }
        //logical else - not enough in prefix
        if (remainingInPrefix > 0) { //empty what remains in prefix
            System.arraycopy(prefix, prefixLocation, cbuf, off, remainingInPrefix);
            prefixLocation = prefix.length;
        }
        int readFromDelegate = delegate.read(cbuf, off + remainingInPrefix, len - remainingInPrefix);
        if (readFromDelegate < 0 && remainingInPrefix > 0) {
            //underlying stream is dry yet we can still return stuff from the prefix.
            //return from prefix and let the next call return the -1 (EOF)
            return remainingInPrefix;
        }
        return remainingInPrefix + readFromDelegate;
    }

    @Override
    public long skip(long n) throws IOException {
        int remainingInPrefix = prefix.length - prefixLocation;
        if (remainingInPrefix > n) {
            prefixLocation += n;
            return n;
        }
        prefixLocation = prefix.length; //skip all of prefix
        return remainingInPrefix + delegate.skip(n - remainingInPrefix);
    }

    @Override
    public boolean ready() throws IOException {
        return delegate.ready();
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        throw new IOException("mark() not supported");
    }

    @Override
    public void reset() throws IOException {
        throw new IOException("reset() not supported");
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
