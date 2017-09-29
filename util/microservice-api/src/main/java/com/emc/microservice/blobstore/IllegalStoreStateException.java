// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.blobstore;

/**
 * Signals that underlying database where we keep headers and blob body is in illegal state.
 * E.g. exception happened when we execute search query or read/write object.
 */
public class IllegalStoreStateException extends StoreException {

    public IllegalStoreStateException(String message) {
        super(message);
    }

    public IllegalStoreStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
