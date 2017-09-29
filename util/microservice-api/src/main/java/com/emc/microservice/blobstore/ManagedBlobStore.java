// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.blobstore;

import com.emc.microservice.resource.ManagedResource;

/**
 * Created by liebea on 9/28/2014. Enjoy it
 */
public interface ManagedBlobStore extends ManagedResource<BlobStoreDescriptor, BlobStoreConfiguration> {

    BlobStoreAPI getBlobStoreAPI();
}
