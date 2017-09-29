// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.io;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: rosenr5
 * Date: 11/28/13
 * Time: 4:57 PM
 */
public class EscapingReader extends Reader {
    // CHECKSTYLE:OFF
    private static final Pattern invalidCharsPattern = Pattern.compile("[^"
            + "\u0009\r\n"
            + "\u0020-\uD7FF"
            + "\uE000-\uFFFD"
            + "\ud800\udc00-\udbff\udfff"
            + "]");
    // CHECKSTYLE:ON

    private final Reader delegate;

    public EscapingReader(Reader delegate) {
        this.delegate = delegate;
    }

    public EscapingReader(Object lock, Reader delegate) {
        super(lock);
        this.delegate = delegate;
    }

    @Override
    public int read() throws IOException {
        int fromDelegate = delegate.read();
        if (fromDelegate < 0) {
            return fromDelegate;
        }
        //not EOF. see if like this char
        char c = (char) fromDelegate;
        while (invalidCharsPattern.matcher(String.valueOf(c)).find()) {
            //skip any characters we dont like
            fromDelegate = delegate.read();
            if (fromDelegate < 0) {
                return fromDelegate; //eof
            }
            c = (char) fromDelegate;
        }
        return fromDelegate;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int count = delegate.read(cbuf, off, len);
        if (count <= 0) {
            return count;
        }
        String read = String.valueOf(cbuf, off, count);
        String possiblyReplaced = invalidCharsPattern.matcher(read).replaceAll("");
        if (read.equals(possiblyReplaced)) {
            return count; //nothing to remove
        }
        char[] corrected = possiblyReplaced.toCharArray();
        System.arraycopy(corrected, 0, cbuf, off, corrected.length);
        return corrected.length;
    }

    @Override
    public long skip(long n) throws IOException {
        return delegate.skip(n);
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

