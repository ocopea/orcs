// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.blobstore;

import com.emc.microservice.resource.ResourceDescriptor;

/**
 * Created by liebea on 9/28/2014. Enjoy it
 */
public class BlobStoreDescriptor implements ResourceDescriptor {

    private final String name;

    public BlobStoreDescriptor(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
