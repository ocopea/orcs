// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.configuration;

import com.emc.microservice.configuration.bootstrap.ConfigurationSchemaBootstrap;
import com.emc.microservice.testing.MicroServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by shresa on 31/07/15.
 * Test V1 of ConfigService
 */
public class ConfigurationV1ServiceTest {

    private MicroServiceTestHelper helper;
    private ConfigResource resourceUnderTest;

    @Before
    public void setup() throws Exception {
        helper = new MicroServiceTestHelper(new ConfigurationMicroservice());
        helper.createOrUpgrdaeSchema(new ConfigurationSchemaBootstrap());
        helper.startServiceInTestMode();
        resourceUnderTest = helper.getServiceResource(ConfigResource.class);
    }

    @After
    public void tearDown() {
        helper.stopTestMode();
    }

    @Test
    public void testThatGetPathReturnsEmptyListInitially() {
        assertEquals("[]", resourceUnderTest.read("/"));
    }

    @Test
    public void testThatGetPathReturnsListOfFolderAndFiles() {
        resourceUnderTest.write("/level1/file1", "file1");
        resourceUnderTest.write("/level1/file2", "file2");
        resourceUnderTest.write("/level1/level2/file3", "file3");

        final String list = resourceUnderTest.read("/level1");
        // TODO fragile test using string comparison instead of jsonassert
        assertEquals("[\"/level1/file1\",\"/level1/file2\",\"/level1/level2\"]", list);
    }

    @Test
    public void testThatGetPathReturnsEmptyListIfItDoesNotExist() {
        assertEquals("[]", resourceUnderTest.read("/apple/ball/"));
        assertEquals("[]", resourceUnderTest.read("/apple/ball"));
    }

    @Test
    public void testThatDeleteExistingFileDeletesIt() {
        resourceUnderTest.write("/level1/file1", "file1");
        assertEquals("file1", resourceUnderTest.read("/level1/file1"));
        resourceUnderTest.delete("/level1/file1");
        assertEquals("[]", resourceUnderTest.read("/level1/file1"));
    }

    @Test
    public void testThatDeleteOnNonExistingPathDoesNotReturnError() {
        resourceUnderTest.delete("/apple/ball");
        resourceUnderTest.delete("/apple/ball/cat/");
    }

    @Test(expected = WebApplicationException.class)
    public void testThatDeleteOnFolderReturnsErrorIfNotEmpty() {
        resourceUnderTest.write("/level1/file1", "file1");
        resourceUnderTest.delete("/level1/");
    }

    @Test
    public void testThatWritingAFileCreatesIt() {
        String originalContent = "{'name':'test'}";
        resourceUnderTest.write("/apple/ball", originalContent);
        final String loadedContent = resourceUnderTest.read("/apple/ball");
        assertEquals(originalContent, loadedContent);
    }

    @Test(expected = WebApplicationException.class)
    public void testThatWritingExistingFileReturnsError() {
        String originalContent = "{'name':'test'}";
        resourceUnderTest.write("/apple/ball", originalContent);
        assertNotNull(resourceUnderTest.read("/apple/ball"));

        resourceUnderTest.write("/apple/ball", "{}");
    }

    @Test
    public void testThatAppendToExistingFileShouldReplaceExistingFile() {
        resourceUnderTest.write("/apple/ball", "ball");
        assertEquals("ball", resourceUnderTest.read("/apple/ball"));
        resourceUnderTest.overwrite("/apple/ball", "big ball");
        assertEquals("big ball", resourceUnderTest.read("/apple/ball"));
    }

    @Test
    public void testThatAppendingNonExistingFileCreatesIt() {
        resourceUnderTest.overwrite("/apple/ball", "ball");
        assertEquals("ball", resourceUnderTest.read("/apple/ball"));
    }

    @Test(expected = WebApplicationException.class)
    public void testThatOverwriteOnFolderThrowsError() {
        resourceUnderTest.overwrite("/apple/ball/cat", "cat");
        resourceUnderTest.overwrite("/apple/ball", "ball");
    }
}
