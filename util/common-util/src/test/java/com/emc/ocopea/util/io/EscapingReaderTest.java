// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.io;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: rosenr5
 * Date: 12/1/13
 * Time: 3:33 PM
 */
public class EscapingReaderTest {
    private Reader mockDelegate = Mockito.mock(Reader.class);
    private EscapingReader cut = new EscapingReader(mockDelegate);

    @Test(expected = java.util.NoSuchElementException.class)
    @Ignore
    public void testWithoutEscaping() throws Exception {
        InputStream fileIS = getClass().getClassLoader().getResourceAsStream("badchars.xml");
        Reader normalReader = new InputStreamReader(fileIS);
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = inputFactory.createXMLEventReader(normalReader);
        while (eventReader.hasNext()) {
            eventReader.next();
        }
    }

    @Test
    @Ignore
    public void testWithEscaping() throws Exception {
        InputStream fileIS = getClass().getClassLoader().getResourceAsStream("badchars.xml");
        Reader escapingReader = new EscapingReader(new InputStreamReader(fileIS));
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = inputFactory.createXMLEventReader(escapingReader);
        while (eventReader.hasNext()) {
            eventReader.next();
        }
    }

    @Test
    public void read() throws IOException {
        when(mockDelegate.read()).thenReturn((int) 'y', (int) 'a', (int) 'y', -1);

        assertEquals((int) 'y', cut.read());
        assertEquals((int) 'a', cut.read());
        assertEquals((int) 'y', cut.read());
        assertEquals(-1, cut.read());
    }

    @Test
    public void readWithLock() throws IOException {
        when(mockDelegate.read()).thenReturn((int) 'y', (int) 'a', (int) 'y', -1);

        final Object lock = new Object();
        cut = new EscapingReader(lock, mockDelegate);

        assertEquals((int) 'y', cut.read());
        assertEquals((int) 'a', cut.read());
        assertEquals((int) 'y', cut.read());
        assertEquals(-1, cut.read());
    }

    @Test
    public void readWithIllegalAtEnd() throws IOException {
        when(mockDelegate.read()).thenReturn((int) 'y', 0x00, -1);

        assertEquals((int) 'y', cut.read());
        assertEquals(-1, cut.read());
    }

    @Test
    public void readWithIllegalNotAtEnd() throws IOException {
        when(mockDelegate.read()).thenReturn((int) 'y', 0x00, (int) 'y', -1);

        assertEquals((int) 'y', cut.read());
        assertEquals((int) 'y', cut.read());
        assertEquals(-1, cut.read());
    }

    @Test
    public void readWithCharArrayAndDelegateEOF() throws IOException {
        when(mockDelegate.read((char[]) Mockito.anyVararg(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(-1);

        final char[] buffer = new char[10];
        assertEquals(-1, cut.read(buffer));
    }

    @Test
    public void readWithCharArrayAndLegalText() throws IOException {
        cut = new EscapingReader(new StringReader("legal"));

        final char[] buffer = new char[10];
        assertEquals(5, cut.read(buffer));
        assertEquals("legal", String.valueOf(buffer, 0, 5));
    }

    @Test
    public void readWithCharArrayAndIllegalText() throws IOException {
        cut = new EscapingReader(new StringReader("bad\0char"));

        final char[] buffer = new char[10];
        assertEquals(7, cut.read(buffer));
        assertEquals("badchar", String.valueOf(buffer, 0, 7));
    }

    @Test
    public void readWithCharBufferAndLegalText() throws IOException {
        cut = new EscapingReader(new StringReader("legal"));

        final CharBuffer buffer = CharBuffer.allocate(10);
        assertEquals(5, cut.read(buffer));
        buffer.flip();
        assertEquals("legal", buffer.toString());
    }

    @Test
    public void readWithCharBufferAndIllegalText() throws IOException {
        cut = new EscapingReader(new StringReader("bad\0char"));

        final CharBuffer buffer = CharBuffer.allocate(10);
        assertEquals(7, cut.read(buffer));
        buffer.flip();
        assertEquals("badchar", buffer.toString());
    }

    @Test
    public void skip() throws IOException {
        cut.skip(999);

        verify(mockDelegate).skip(Mockito.anyLong());
    }

    @Test
    public void ready() throws IOException {
        cut.ready();

        verify(mockDelegate).ready();
    }

    @Test
    public void markSupported() throws IOException {
        assertFalse(cut.markSupported());
    }

    @Test(expected = IOException.class)
    public void mark() throws IOException {
        cut.mark(0);
    }

    @Test(expected = IOException.class)
    public void reset() throws IOException {
        cut.reset();
    }

    @Test
    public void close() throws IOException {
        cut.close();

        verify(mockDelegate).close();
    }
}
