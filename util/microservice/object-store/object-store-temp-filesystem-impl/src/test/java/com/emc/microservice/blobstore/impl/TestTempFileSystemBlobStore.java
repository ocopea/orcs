// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2014 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.blobstore.impl;

import com.emc.microservice.blobstore.BlobStore;
import com.emc.microservice.blobstore.IllegalStoreStateException;
import com.emc.microservice.blobstore.ObjectKeyFormatException;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class TestTempFileSystemBlobStore {
    @Test
    public void testCrud() throws Exception {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("filename", "brain_slug.png");
        headers.put("retention", "1000");

        File originalBsImage =
                new File(TestTempFileSystemBlobStore.class.getClassLoader().getResource("brain_slug.png").getFile());

        BlobStore bs = new TempFileSystemBlobStore();
        String key = UUID.randomUUID().toString();
        bs.create("Z", key, headers, new FileInputStream(originalBsImage));

        Map<String, String> storedHeaders = bs.readHeaders("Z", key);
        assertEquals(
                "'filename' property doesn't exists or not equals to stored metadata ",
                "brain_slug.png",
                storedHeaders.get("filename"));

        Assert.assertTrue(bs.isExists("Z", key));
        ByteArrayOutputStream originalByteArryOutputStream = new ByteArrayOutputStream();
        bs.readBlob("Z", key, originalByteArryOutputStream);
        assertEquals(
                "Stored file in blob store is not the same as received from blob store",
                originalByteArryOutputStream.toByteArray().length,
                originalBsImage.length());
        Map<String, String> originalHeadersFromBs = bs.readHeaders("Z", key);
        Assert.assertEquals("brain_slug.png", originalHeadersFromBs.get("filename"));

        headers.put("filename", "elysium.jpg");
        File updatedBsImage =
                new File(TestTempFileSystemBlobStore.class.getClassLoader().getResource("elysium.jpg").getFile());
        bs.update("Z", key, headers, new FileInputStream(updatedBsImage));

        bs.delete("Z", key);
    }

    @Test
    public void testMoveNamespace() throws Exception {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("filename", "brain_slug.png");
        headers.put("retention", "1000");

        File bsImage =
                new File(TestTempFileSystemBlobStore.class.getClassLoader().getResource("brain_slug.png").getFile());

        BlobStore bs = new TempFileSystemBlobStore();
        String key = UUID.randomUUID().toString();
        String originalNameSpace = "ashish";
        bs.update(originalNameSpace, key, headers, new FileInputStream(bsImage));

        Map<String, String> storedHeaders = bs.readHeaders(originalNameSpace, key);
        assertEquals(
                "'filename' property doesn't exists or not equals to stored metadata ",
                "brain_slug.png",
                storedHeaders.get("filename"));

        Assert.assertTrue(bs.isExists(originalNameSpace, key));
        ByteArrayOutputStream originalByteArrayOutputStream = new ByteArrayOutputStream();
        bs.readBlob(originalNameSpace, key, originalByteArrayOutputStream);

        assertEquals(
                "Stored file in blob store is not the same as received from blob store",
                originalByteArrayOutputStream.toByteArray().length,
                bsImage.length());

        String newNamespace = "chen";
        bs.moveNameSpace(originalNameSpace, key, newNamespace);

        Assert.assertTrue(bs.isExists(newNamespace, key));

        Assert.assertTrue(bs.isExists(newNamespace, key));
        ByteArrayOutputStream newByteArrayOutputStream = new ByteArrayOutputStream();
        bs.readBlob(newNamespace, key, newByteArrayOutputStream);

        assertEquals(
                "Stored file in blob store is not the same as received from blob store",
                newByteArrayOutputStream.toByteArray().length,
                bsImage.length());

        Assert.assertFalse(bs.isExists(originalNameSpace, key));

        bs.delete(newNamespace, key);
    }

    @Test
    public void testIllegalKey() {
        BlobStore bs = new TempFileSystemBlobStore();
        InputStream inputStream = null;
        try {
            bs.update(".", "!", null, inputStream);
            assertTrue("illegal key passed as valid", false);
        } catch (ObjectKeyFormatException e) {
            assertTrue(true);
        } catch (IllegalStoreStateException e) {
            assertTrue("IllegalStoreStateException should not be thrown", false);
        }
    }

    @Test
    public void testListBlob() throws FileNotFoundException {
        Map<String, String> headers = new HashMap<>();
        File originalBsImage =
                new File(TestTempFileSystemBlobStore.class.getClassLoader().getResource("brain_slug.png").getFile());

        BlobStore bs = new TempFileSystemBlobStore();
        String key = "k1";
        bs.create("ns1", key, headers, new FileInputStream(originalBsImage));

        bs.list().forEach(System.out::println);
    }

    // todo: what should this test method do? Decide, and then make it do that.
    /*@Test
    public void testDel() throws FileNotFoundException {
        Map <String,String> headers = new HashMap<>();
        File originalBsImage = new File (TestTempFileSystemBlobStore.class.getClassLoader().getResource("brain_slug.png").getFile());

        BlobStore bs = new TempFileSystemBlobStore();
        String key = "k1";
        bs.create("ns1", key, headers, new FileInputStream(originalBsImage));
        bs.update("ns1", key, headers, new FileInputStream(originalBsImage));
        bs.update("ns1", key, headers, new FileInputStream(originalBsImage));
        bs.update("ns1", key, headers, new FileInputStream(originalBsImage));
        bs.update("ns1", key, headers, new FileInputStream(originalBsImage));
        bs.update("ns1", key, headers, new FileInputStream(originalBsImage));
        bs.readBlob("ns1", key, System.out);

        bs.list().forEach(System.out::println);
    }*/

}
