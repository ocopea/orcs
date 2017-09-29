// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.blobstore;

import com.emc.microservice.resource.ResourceConfiguration;
import com.emc.microservice.resource.ResourceConfigurationProperty;

import java.util.List;

/**
 * Created by liebea on 9/28/2014. Enjoy it
 */
public abstract class BlobStoreConfiguration extends ResourceConfiguration {
    protected BlobStoreConfiguration(String configurationName, List<ResourceConfigurationProperty> properties) {
        super(configurationName, properties);
    }
}
