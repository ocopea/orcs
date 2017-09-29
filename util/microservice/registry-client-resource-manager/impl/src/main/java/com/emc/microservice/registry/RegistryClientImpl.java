// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.registry;

import com.emc.microservice.ServiceConfig;
import com.emc.microservice.resource.ManagedResource;
import com.emc.microservice.resource.ResourceProviderManager;

public class RegistryClientImpl
        implements ManagedResource<RegistryClientDescriptor, RegistryClientConfiguration>, RegistryClient {

    @Override
    public void registerService(String urn, String url) {

        ResourceProviderManager.getResourceProvider().getServiceRegistryApi().registerServiceConfig(
                urn,
                ServiceConfig.generateServiceConfig(urn, null, url, null, null, null, null, null, null, null, null)
        );
    }

    @Override
    public String getName() {
        return getDescriptor().getName();
    }

    @Override
    public RegistryClientDescriptor getDescriptor() {
        return new RegistryClientDescriptor();
    }

    @Override
    public RegistryClientConfiguration getConfiguration() {
        return new RegistryClientConfiguration();
    }
}
