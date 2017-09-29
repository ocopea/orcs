// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.io;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: rosenr5
 * Date: 12/3/13
 * Time: 10:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class CountingReader extends Reader {
    private final Reader delegate;
    private int charCount = 0;

    public CountingReader(Reader delegate) {
        this.delegate = delegate;
    }

    @Override
    public int read(CharBuffer target) throws IOException {
        int charsRead = delegate.read(target);
        if (charsRead > 0) { //disregard EOF
            charCount += charsRead;
        }
        return charsRead;
    }

    @Override
    public int read() throws IOException {
        int c = delegate.read();
        if (c >= 0) {
            charCount++;
        }
        return c;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int charsRead = delegate.read(cbuf, off, len);
        if (charsRead > 0) {
            charCount += charsRead;
        }
        return charsRead;
    }

    @Override
    public long skip(long n) throws IOException {
        final long skipped = delegate.skip(n);
        charCount += skipped; //we count those actually skipped
        return skipped;
    }

    @Override
    public boolean ready() throws IOException {
        return delegate.ready();
    }

    @Override
    public boolean markSupported() {
        return false; //why make my life more difficult than it already is? :-)
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

    public int getCharCount() {
        return charCount;
    }
}
