// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.blobstore;

/**
 * Signals that key supplied to an operation is invalid and doesn't match the pattern
 */
public class ObjectKeyFormatException extends StoreException {

    public ObjectKeyFormatException(String message) {
        super(message);
    }
}
