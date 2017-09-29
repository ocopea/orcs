// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

package com.emc.microservice.bootstrap;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by kuruvt on 10/03/2016.
 */
@RunWith(Parameterized.class)
public class SchemaBootstrapValidateScriptTest {

    /*
     * fileName should be without the suffix.
     * Rules for fileName are,
      * 1. file names containing 'drop' is for the drop statements only
      * 2. file names containing 'alter' is for the alter statements only
      * 3. file names containing 'success' should be used for cases that are allowed. eg: only add or create
     */
    private String fileName;
    private String content;

    public SchemaBootstrapValidateScriptTest(String fileName, String content) {
        this.fileName = fileName;
        this.content = content;
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"dropTable", "DROP TABLE tableName;"},
                {"dropTableLowercase", "drop Table name"},
                {"dropWithComments", "--some comments;\r\nDROP TABLE tableName\r\n--more comments\r\n"},
                {"dropWithHashComments", "#some comments;\r\nDROP TABLE tableName;"},
                {"dropWithSpaces", "DROP    TABLE\ttableName;"},
                {"dropWithMultiple", "DROP    TABLE\ttableName;\nDROP table\n table2;"},
                {"dropWithAllowed",
                 "CREATE TABLE tableName \n(\nf_id character varying(1024) NOT NULL\n);\n\nDROP TABLE table2;"},

                {"alterTable", "ALTER TABLE tableName RENAME TO newName;"},
                {"alterTableIfExists", "ALTER TABLE IF EXISTS tableName RENAME TO newName;"},
                {"alterColumn", "ALTER TABLE tableName\nRENAME COLUMN name TO newName;"},
                {"alterColumnDropColumn", "ALTER TABLE tableName\nDROP COLUMN drop_column;"},
                {"alterColumnIfExists", "ALTER TABLE IF EXISTS\ntableName RENAME COLUMN name TO newName;"},
                {"alterColumnOnly", "ALTER TABLE ONLY tableName RENAME TO newName;"},

                {"successCreateTable", "CREATE TABLE tableName \n(\nf_id character varying(1024) NOT NULL\n);"},
                {"successIndexIfExists", "DROP INDEX IF EXISTS name"},
                {"successAddColumn", "alter table add column newCol character varying(1024); "}
        });
    }

    @Test
    public void testValidateScript() throws Exception {
        final File dropFile = createFile(fileName, content);
        try {
            SchemaBootstrap.validateScript(new StringReader(IOUtils.toString(new FileInputStream(dropFile))));
            if (!fileName.contains("success")) {
                fail();
            }
        } catch (IOException e) {
            if (fileName.contains("success")) {
                fail();
            } else {
                assertTrue(e
                        .getMessage()
                        .contains(
                                "The script contains unsupported statements! Dropping or renaming a table, column or constraint is not allowed."));
            }
        }
    }

    private File createFile(String fileName, String content) throws Exception {
        //create a temp file
        File file = File.createTempFile(fileName, ".sql");

        //write to it
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(content);
        bw.close();

        return file;
    }
}
