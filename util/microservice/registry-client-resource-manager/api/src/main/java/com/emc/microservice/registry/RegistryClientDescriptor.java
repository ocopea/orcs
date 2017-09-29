// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.registry;

import com.emc.microservice.resource.ResourceDescriptor;

/**
 * Created by liebea on 4/18/17.
 * Drink responsibly
 */
public class RegistryClientDescriptor implements ResourceDescriptor {

    public static final String REGISTRY_CLIENT_RESOURCE_NAME = "registry-client";

    @Override
    public String getName() {
        return REGISTRY_CLIENT_RESOURCE_NAME;
    }
}
