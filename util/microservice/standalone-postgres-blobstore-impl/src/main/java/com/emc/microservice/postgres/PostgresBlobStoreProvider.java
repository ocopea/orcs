// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.postgres;

import com.emc.microservice.blobstore.BlobStore;
import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.microservice.blobstore.BlobStoreProvider;
import com.emc.microservice.blobstore.MicroServiceBlobStore;
import com.emc.microservice.blobstore.StandalonePostgresBlobStoreConfiguration;
import com.emc.microservice.blobstore.impl.PostgresBlobStoreService;
import com.emc.microservice.serialization.SerializationManager;
import com.emc.microservice.serialization.SerializationManagerImpl;
import com.emc.ocopea.util.PostgresUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation of {@link BlobStoreProvider}. Created {@link BlobStoreAPI} uses a PostgreSQL database to preserve
 * its data.
 */
public class PostgresBlobStoreProvider implements BlobStoreProvider<StandalonePostgresBlobStoreConfiguration> {
    private final Map<String, BlobStore> blobStores = new ConcurrentHashMap<>();

    @Override
    public synchronized BlobStoreAPI getBlobStore(
            StandalonePostgresBlobStoreConfiguration conf,
            SerializationManager serializationManager) {
        String blobstoreIdentifier = conf.getDatabaseName() + "!" + conf.getDatabaseSchema();
        BlobStore blobStore = blobStores.computeIfAbsent(
                blobstoreIdentifier,
                k -> new PostgresBlobStoreService(PostgresUtil.getDataSource(conf.getDatabaseName(), conf.getServer(),
                        conf.getPort(), conf.getDbUser(), conf.getDbPassword(), conf.getMaxConnections(),
                        conf.getDatabaseSchema())));
        if (serializationManager == null) {
            serializationManager = new SerializationManagerImpl();
        }
        return new MicroServiceBlobStore(blobStore, serializationManager);
    }

    @Override
    public Class<StandalonePostgresBlobStoreConfiguration> getConfClass() {
        return StandalonePostgresBlobStoreConfiguration.class;
    }
}
