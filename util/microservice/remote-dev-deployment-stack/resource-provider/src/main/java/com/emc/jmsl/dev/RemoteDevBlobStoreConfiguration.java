// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.jmsl.dev;

import com.emc.microservice.blobstore.BlobStoreConfiguration;
import com.emc.microservice.resource.ResourceConfigurationProperty;
import com.emc.microservice.resource.ResourceConfigurationPropertyType;

import java.util.Arrays;

/**
 * Created by liebea on 1/18/15.
 * Drink responsibly
 */
public class RemoteDevBlobStoreConfiguration extends BlobStoreConfiguration {
    private static final ResourceConfigurationProperty PROPERTY_NAME =
            new ResourceConfigurationProperty("name", ResourceConfigurationPropertyType.STRING, "name", true, false);

    public RemoteDevBlobStoreConfiguration() {
        super("Dev Blobstore configuration", Arrays.asList(PROPERTY_NAME));
    }

    public RemoteDevBlobStoreConfiguration(String name) {
        this();
        setPropertyValues(propArrayToMap(new String[]{
                PROPERTY_NAME.getName(), name
        }));
    }

    public String getName() {
        return getProperty(PROPERTY_NAME.getName());
    }

}
