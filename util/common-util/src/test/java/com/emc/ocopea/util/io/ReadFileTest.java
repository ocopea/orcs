// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.io;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by L Braine on 07/11/2015.
 */
public class ReadFileTest {

    private static final String TESTFILE = "ReadFileTest.txt";
    private static final String BADFILE = "NoSuchFileTest.txt";

    @Test(expected = FileNotFoundException.class)
    public void getFileContentsFromResourceTest() throws IOException {

        final String result = ReadFile.getFileContentsFromResource(TESTFILE);
        assertNotNull(result);
        assertTrue(result.contains("wheezles"));

        ReadFile.getFileContentsFromResource(BADFILE);
        assertFalse(true);
    }

    @Test(expected = FileNotFoundException.class)
    public void getFileContentsFromResourceWithClassloaderTest() throws IOException {

        final String result =
                ReadFile.getFileContentsFromResource(TESTFILE, Thread.currentThread().getContextClassLoader());
        assertNotNull(result);
        assertTrue(result.contains("wheezles"));

        ReadFile.getFileContentsFromResource(BADFILE, Thread.currentThread().getContextClassLoader());
        assertFalse(true);
    }

    @Test
    public void getFileContentsFromResourceAsStringBuilderTest() throws IOException {

        final StringBuilder sb = ReadFile.getFileContentsFromResourceAsStringBuilder(
                TESTFILE,
                Thread.currentThread().getContextClassLoader());
        assertFalse(sb.length() == 0);
    }
}
