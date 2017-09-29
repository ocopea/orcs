// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.blobstore;

/**
 * Signals object key already exists
 */
public class DuplicateObjectKeyException extends StoreException {
    public DuplicateObjectKeyException(String message) {
        super(message);
    }
}
