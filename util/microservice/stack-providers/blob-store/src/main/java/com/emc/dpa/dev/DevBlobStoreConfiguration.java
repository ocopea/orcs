// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.dpa.dev;

import com.emc.microservice.blobstore.BlobStoreConfiguration;
import com.emc.microservice.resource.ResourceConfigurationProperty;
import com.emc.microservice.resource.ResourceConfigurationPropertyType;

import java.util.Arrays;

/**
 * Configuration used by {@link com.emc.dpa.dev.blobstore.DevBlobStoreProvider}
 */
public class DevBlobStoreConfiguration extends BlobStoreConfiguration {
    private static final ResourceConfigurationProperty PROPERTY_NAME =
            new ResourceConfigurationProperty("name", ResourceConfigurationPropertyType.STRING, "name", true, false);

    public DevBlobStoreConfiguration() {
        super("Dev Blobstore configuration", Arrays.asList(PROPERTY_NAME));
    }

    public DevBlobStoreConfiguration(String name) {
        this();
        setPropertyValues(propArrayToMap(new String[]{
                PROPERTY_NAME.getName(), name
        }));
    }

    public String getName() {
        return getProperty(PROPERTY_NAME.getName());
    }

}
