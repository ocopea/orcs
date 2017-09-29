// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.dpa.dev.blobstore;

import com.emc.dpa.dev.DevBlobStoreConfiguration;
import com.emc.microservice.blobstore.BlobStore;
import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.microservice.blobstore.BlobStoreProvider;
import com.emc.microservice.blobstore.MicroServiceBlobStore;
import com.emc.microservice.blobstore.impl.TempFileSystemBlobStore;
import com.emc.microservice.serialization.SerializationManager;
import com.emc.microservice.serialization.SerializationManagerImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation of {@link BlobStoreProvider}. Created {@link BlobStoreAPI} use local temp files to preserve their
 * data. See {@link TempFileSystemBlobStore}.
 */
public class DevBlobStoreProvider implements BlobStoreProvider<DevBlobStoreConfiguration> {
    private final Map<String, BlobStore> blobStores = new ConcurrentHashMap<>();

    @Override
    public synchronized BlobStoreAPI getBlobStore(
            DevBlobStoreConfiguration configuration,
            SerializationManager serializationManager) {
        BlobStore blobStore = blobStores.get(configuration.getName());
        if (blobStore == null) {
            blobStore = new TempFileSystemBlobStore();
            blobStores.put(configuration.getName(), blobStore);
        }
        if (serializationManager == null) {
            serializationManager = new SerializationManagerImpl();
        }
        return new MicroServiceBlobStore(blobStore, serializationManager);
    }

    @Override
    public Class<DevBlobStoreConfiguration> getConfClass() {
        return DevBlobStoreConfiguration.class;
    }
}
