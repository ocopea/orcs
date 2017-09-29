// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.blobstore;

/**
 * Indicates use-case when operation performed on object that doesn't exists
 */
public class ObjectMissingException extends StoreException {
    public ObjectMissingException(String message) {
        super(message);
    }
}
