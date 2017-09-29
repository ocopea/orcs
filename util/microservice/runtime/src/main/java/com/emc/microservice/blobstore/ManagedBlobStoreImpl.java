// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.blobstore;

import com.emc.microservice.resource.AbstractManagedResource;

/**
 * Created by liebea on 9/28/2014.
 * Enjoy it.
 */
public class ManagedBlobStoreImpl extends
                                  AbstractManagedResource<BlobStoreDescriptor, BlobStoreConfiguration>
        implements ManagedBlobStore {

    private final BlobStoreAPI blobStoreAPI;

    protected ManagedBlobStoreImpl(
            BlobStoreDescriptor descriptor,
            BlobStoreConfiguration configuration,
            BlobStoreAPI blobStoreAPI) {

        super(descriptor, configuration);
        this.blobStoreAPI = blobStoreAPI;
    }

    @Override
    public BlobStoreAPI getBlobStoreAPI() {
        return blobStoreAPI;
    }
}
