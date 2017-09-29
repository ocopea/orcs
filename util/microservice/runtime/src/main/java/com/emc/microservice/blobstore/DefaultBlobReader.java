// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.blobstore;

import com.emc.microservice.serialization.SerializationManager;

import java.io.InputStream;

/**
 * Created by liebea on 11/23/14.
 * Drink responsibly
 */
public class DefaultBlobReader<T> implements BlobReader {
    private final SerializationManager serializationManager;
    private final Class<T> format;
    private T object = null;

    public DefaultBlobReader(SerializationManager serializationManager, Class<T> format) {
        this.serializationManager = serializationManager;
        this.format = format;
    }

    public T getObject() {
        return object;
    }

    @Override
    public void read(InputStream in) {
        this.object = serializationManager.getReader(format).readObject(in);
    }
}
