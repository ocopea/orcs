// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.io;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;

/**
 * Created with IntelliJ IDEA. User: rosenr5 Date: 12/1/13 Time: 3:58 PM
 */
@SuppressWarnings("resource")
public class PrefixReaderTest {

    private static final String DEFAULT_PREFIX = "prefix";
    private static final String DEFAULT_CONTENT = "someString";
    private static final String DEFAULT_COMBINED = DEFAULT_PREFIX + DEFAULT_CONTENT;
    private static final int DEFAULT_TOTAL = DEFAULT_COMBINED.length();
    private Reader delegate = new StringReader(DEFAULT_CONTENT);
    private char[] prefix = DEFAULT_PREFIX.toCharArray();
    private PrefixReader cut = new PrefixReader(prefix, delegate);

    @Test
    public void read() throws Exception {
        assertEquals('p', cut.read());
        assertEquals('r', cut.read());
        assertEquals('e', cut.read());
        assertEquals('f', cut.read());
        assertEquals('i', cut.read());
        assertEquals('x', cut.read());
        assertEquals('s', cut.read());
        assertEquals('o', cut.read());
        assertEquals('m', cut.read());
        assertEquals('e', cut.read());
        assertEquals('S', cut.read());
        assertEquals('t', cut.read());
        assertEquals('r', cut.read());
        assertEquals('i', cut.read());
        assertEquals('n', cut.read());
        assertEquals('g', cut.read());
        assertEquals(-1, cut.read());
    }

    @Test
    public void readWithLockConstructor() throws IOException {
        final Object lock = new Object();
        final PrefixReader cut = new PrefixReader(lock, prefix, delegate);

        assertEquals('p', cut.read());
        assertEquals('r', cut.read());
        assertEquals('e', cut.read());
    }

    @Test
    public void readWithLargeCharBuffer() throws IOException {
        final CharBuffer buffer = CharBuffer.allocate(20);
        assertEquals(DEFAULT_TOTAL, cut.read(buffer));
        buffer.flip();
        assertEquals(DEFAULT_COMBINED, buffer.toString());
    }

    @Test
    public void readWithLargeCharArray() throws IOException {
        final char[] buffer = new char[20];
        final int read = cut.read(buffer);
        assertEquals(DEFAULT_TOTAL, read);
        assertEquals(DEFAULT_COMBINED, String.valueOf(buffer, 0, read));
    }

    @Test
    public void readWithLargeCharBufferAndEmptyDelegate() throws IOException {
        final PrefixReader cut = new PrefixReader(prefix, new StringReader(""));

        final CharBuffer buffer = CharBuffer.allocate(20);

        assertEquals(DEFAULT_PREFIX.length(), cut.read(buffer));
        buffer.flip();
        assertEquals(DEFAULT_PREFIX, buffer.toString());
    }

    @Test
    public void readWithLargeCharArrayAndEmptyDelegate() throws IOException {
        final PrefixReader cut = new PrefixReader(prefix, new StringReader(""));

        final char[] buffer = new char[20];

        int read;
        assertEquals(DEFAULT_PREFIX.length(), read = cut.read(buffer));
        assertEquals(DEFAULT_PREFIX, String.valueOf(buffer, 0, read));
    }

    @Test
    public void readWithSmallCharBuffer() throws IOException {
        final int capacity = 3;
        final CharBuffer buffer = CharBuffer.allocate(capacity);
        assertEquals(capacity, cut.read(buffer));
        buffer.flip();
        assertEquals("pre", buffer.toString());
    }

    @Test
    public void readWithSmallCharArray() throws IOException {
        final int capacity = 3;
        final char[] buffer = new char[capacity];
        final int read = cut.read(buffer);
        assertEquals(capacity, read);
        assertEquals("pre", String.valueOf(buffer, 0, read));
    }

    @Test
    public void readWithExactCharBuffer() throws IOException {
        final int capacity = DEFAULT_PREFIX.length();
        final CharBuffer buffer = CharBuffer.allocate(capacity);

        assertEquals(capacity, cut.read(buffer));
        buffer.clear();
        assertEquals(capacity, cut.read(buffer));

        assertEquals("someSt", buffer.flip().toString());
    }

    @Test
    public void readWithExactCharArray() throws IOException {
        final int capacity = DEFAULT_PREFIX.length();
        final char[] buffer = new char[capacity];

        int read;
        assertEquals(capacity, read = cut.read(buffer));
        assertEquals(capacity, read = cut.read(buffer));

        assertEquals("someSt", String.valueOf(buffer, 0, read));
    }

    @Test
    public void readWithExactCharBufferAndEmptyDelegate() throws IOException {
        final PrefixReader cut = new PrefixReader(prefix, new StringReader(""));

        final int capacity = DEFAULT_PREFIX.length();
        final CharBuffer buffer = CharBuffer.allocate(capacity);

        assertEquals(capacity, cut.read(buffer));
        buffer.clear();
        assertEquals(-1, cut.read(buffer));
    }

    @Test
    public void readWithExactCharArrayAndEmptyDelegate() throws IOException {
        final PrefixReader cut = new PrefixReader(prefix, new StringReader(""));

        final int capacity = DEFAULT_PREFIX.length();
        final char[] buffer = new char[capacity];

        assertEquals(capacity, cut.read(buffer));
        assertEquals(-1, cut.read(buffer));
    }

    @Test
    public void markSupported() throws IOException {
        assertFalse(cut.markSupported());
    }

    @Test(expected = IOException.class)
    public void mark() throws Exception {
        cut.mark(0);
    }

    @Test(expected = IOException.class)
    public void reset() throws Exception {
        cut.reset();
    }

    @Test
    public void close() throws Exception {
        final Reader mockDelegate = Mockito.mock(Reader.class);

        final PrefixReader cut = new PrefixReader(prefix, mockDelegate);
        cut.close();

        verify(mockDelegate).close();
    }

    @Test
    public void ready() throws Exception {
        final Reader mockDelegate = Mockito.mock(Reader.class);

        final PrefixReader cut = new PrefixReader(prefix, mockDelegate);
        cut.ready();

        verify(mockDelegate).ready();
    }

    @Test
    public void skipNothing() throws Exception {
        // prefixsomeString
        // 0123456789012345
        assertEquals(0, cut.skip(0));
        assertEquals('p', cut.read());
    }

    @Test
    public void skipWithinPrefix() throws Exception {
        // prefixsomeString
        // 0123456789012345
        assertEquals(5, cut.skip(5));
        assertEquals('x', cut.read());
    }

    @Test
    public void skipBeyondPrefix() throws Exception {
        // prefixsomeString
        // 0123456789012345
        assertEquals(8, cut.skip(8));
        assertEquals('m', cut.read());
    }

    @Test
    public void skipBeyondEnd() throws Exception {
        // prefixsomeString
        // 0123456789012345
        assertEquals(16, cut.skip(20));
        assertEquals(-1, cut.read());
    }
}
