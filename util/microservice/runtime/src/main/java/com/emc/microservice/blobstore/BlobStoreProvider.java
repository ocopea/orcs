// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.blobstore;

import com.emc.microservice.serialization.SerializationManager;

/**
 * Provides BlobStore services. Implementations should be registered in META-INF/services.
 */
public interface BlobStoreProvider<C extends BlobStoreConfiguration> {
    /**
     * Returns a BlobStoreAPI. It may create one if necessary. Created object should take all definitions from
     * configuration, serialization should be handled by serialization Manager.
     *
     * @param configuration configuration of the BlobStore
     * @param serializationManager handles all serialization
     */
    BlobStoreAPI getBlobStore(C configuration, SerializationManager serializationManager);

    /**
     * @return The expected configuration class.
     */
    Class<C> getConfClass();
}
