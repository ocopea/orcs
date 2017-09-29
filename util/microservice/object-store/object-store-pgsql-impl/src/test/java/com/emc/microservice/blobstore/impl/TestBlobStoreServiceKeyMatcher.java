// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2014 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.blobstore.impl;

import com.emc.microservice.blobstore.ObjectKeyFormatException;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * separate class to test isKeyAcceptable() to avoid initialization of PGSQLBlobStore
 */
public class TestBlobStoreServiceKeyMatcher {

    @Test
    public void testIsKeyAcceptable() throws ObjectKeyFormatException {
        String key = "1234567890QWE-RTYU1IOPASDFGH7JKLZXCVBNMq-we_rtyuiopasdfghjklzxcvbnm_";
        try {
            PostgresBlobStoreService.isKeyAcceptable(key);
            assertTrue(true);
        } catch (ObjectKeyFormatException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testIsKeyIsNotAcceptable() {
        String key = "1234567890_-QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm!";
        try {
            PostgresBlobStoreService.isKeyAcceptable(key);
            assertTrue(false);
        } catch (ObjectKeyFormatException e) {
            assertTrue(true);
        }
    }
}
