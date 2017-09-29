// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.io;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CountingReaderTest {

    private Reader delegate = Mockito.mock(Reader.class);
    private CountingReader cut = new CountingReader(delegate);

    @Test
    public void readWithEOF() throws IOException {
        when(delegate.read()).thenReturn(-1);

        assertEquals(-1, cut.read());

        assertEquals(0, cut.getCharCount());
    }

    @Test
    public void read3Chars() throws IOException {
        when(delegate.read()).thenReturn(101, 102, 103, 104, 105, -1);

        assertEquals(101, cut.read());
        assertEquals(102, cut.read());
        assertEquals(103, cut.read());

        assertEquals(3, cut.getCharCount());
    }

    @Test
    public void readWithCharBuffer() throws IOException {
        when(delegate.read(Mockito.any(CharBuffer.class))).thenReturn(88);

        assertEquals(88, cut.read(CharBuffer.allocate(100)));
        assertEquals(88, cut.getCharCount());
    }

    @Test
    public void readWithCharBufferAndEOF() throws IOException {
        when(delegate.read(Mockito.any(CharBuffer.class))).thenReturn(88, -1);

        assertEquals(88, cut.read(CharBuffer.allocate(100)));
        assertEquals(-1, cut.read(CharBuffer.allocate(100)));
        assertEquals(88, cut.getCharCount());
    }

    @Test
    public void readWithCharArray() throws IOException {
        when(delegate.read(Mockito.any(char[].class))).thenReturn(88);
        when(delegate.read(Mockito.any(char[].class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(88);

        assertEquals(88, cut.read(new char[100]));
        assertEquals(88, cut.getCharCount());
    }

    @Test
    public void readWithCharArrayAndEOF() throws IOException {
        when(delegate.read(Mockito.any(char[].class))).thenReturn(88, -1);
        when(delegate.read(Mockito.any(char[].class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(88, -1);

        assertEquals(88, cut.read(new char[100]));
        assertEquals(-1, cut.read(new char[100]));
        assertEquals(88, cut.getCharCount());
    }

    @Test
    public void readWithCharArrayAndInts() throws IOException {
        when(delegate.read(Mockito.any(char[].class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(88);

        assertEquals(88, cut.read(new char[100], 10, 90));
        assertEquals(88, cut.getCharCount());
    }

    @Test
    public void readWithCharArrayAndIntsAndEOF() throws IOException {
        when(delegate.read(Mockito.any(char[].class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(88, -1);

        assertEquals(88, cut.read(new char[100], 10, 90));
        assertEquals(-1, cut.read(new char[100], 10, 90));
        assertEquals(88, cut.getCharCount());
    }

    @Test
    public void skipWithDelegateThatSkipsFully() throws IOException {
        when(delegate.skip(1000L)).thenReturn(1000L);

        assertEquals(1000L, cut.skip(1000L));
        assertEquals(1000L, cut.getCharCount());
    }

    @Test
    public void skipWithDelegateThatSkipsPartially() throws IOException {
        when(delegate.skip(1000L)).thenReturn(500L);

        assertEquals(500L, cut.skip(1000L));
        assertEquals(500L, cut.getCharCount());
    }

    @Test
    public void ready() throws IOException {
        when(delegate.ready()).thenReturn(true);

        assertTrue(cut.ready());

        verify(delegate).ready();
    }

    @Test
    public void markSupported() {
        assertFalse(cut.markSupported());
    }

    @Test(expected = IOException.class)
    public void mark() throws IOException {
        cut.mark(1000);
    }

    @Test(expected = IOException.class)
    public void reset() throws IOException {
        cut.reset();
    }

    @Test
    public void close() throws IOException {
        cut.close();

        verify(delegate).close();
    }

    @Test
    public void getCharCountWithNoCharactersRead() {
        assertEquals(0, cut.getCharCount());
    }
}
