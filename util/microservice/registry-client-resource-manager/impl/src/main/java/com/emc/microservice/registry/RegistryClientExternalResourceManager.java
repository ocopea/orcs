// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.registry;

import com.emc.microservice.Context;
import com.emc.microservice.health.HealthCheck;
import com.emc.microservice.resource.ExternalResourceManager;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by liebea on 4/18/17.
 * Drink responsibly
 */
public class RegistryClientExternalResourceManager
        implements ExternalResourceManager<RegistryClientDescriptor, RegistryClientConfiguration, RegistryClientImpl> {

    @Override
    public String getResourceTypeNamePlural() {
        return "registry clients";
    }

    @Override
    public String getResourceTypeName() {
        return "registry client";
    }

    @Override
    public RegistryClientImpl initializeResource(
            RegistryClientDescriptor descriptor, RegistryClientConfiguration configuration, Context context) {
        return new RegistryClientImpl();
    }

    @Override
    public void postInitResource(
            RegistryClientDescriptor descriptor,
            RegistryClientConfiguration configuration,
            RegistryClientImpl client,
            Context context) {
    }

    @Override
    public void cleanUpResource(RegistryClientImpl client) {
    }

    @Override
    public void pauseResource(RegistryClientImpl client) {
    }

    @Override
    public void startResource(RegistryClientImpl client) {
    }

    @Override
    public Class<RegistryClientConfiguration> getResourceConfigurationClass() {
        return RegistryClientConfiguration.class;
    }

    @Override
    public Class<RegistryClientDescriptor> getDescriptorClass() {
        return RegistryClientDescriptor.class;
    }

    @Override
    public Collection<HealthCheck> getResourceHealthChecks(RegistryClientImpl client) {
        return Collections.emptyList();
    }
}
