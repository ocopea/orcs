// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.bootstrap;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by kuruvt on 16/03/2016.
 */
public class SchemaBootstrapScriptTest {
    private SchemaBootstrapScript schemaBootstrapScript;

    private static final String EXPECTED = "--DROP TABLE tableName;\n" +
            "drop table tableName;\n" +
            "\n" +
            "ALTER TABLE tableName ADD COLUMN colName VARCHAR(1024);";

    @After
    public void tearDown() {
        schemaBootstrapScript = null;
    }

    @Test
    public void testGetScriptReaderFromFile() throws Exception {
        schemaBootstrapScript = new SchemaBootstrapScript("test.sql", "test", 1);

        final Reader reader = schemaBootstrapScript.getScriptReader();
        final List<String> lines = IOUtils.readLines(reader);

        assertTrue(reader instanceof StringReader);
        assertTrue(lines.equals(Arrays.asList(EXPECTED.split("\n"))));
    }

    @Test
    public void testGetScriptReaderFromStringBuilder() throws Exception {
        final StringBuilder stringBuilder = new StringBuilder(EXPECTED);
        schemaBootstrapScript = new SchemaBootstrapScript(stringBuilder, "test", 1);

        final Reader reader = schemaBootstrapScript.getScriptReader();

        assertTrue(reader instanceof StringReader);
        assertEquals(EXPECTED, IOUtils.toString(reader));
    }
}
