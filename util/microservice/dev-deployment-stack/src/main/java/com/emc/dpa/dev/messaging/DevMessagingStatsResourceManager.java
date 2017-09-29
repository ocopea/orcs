// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.dpa.dev.messaging;

import com.emc.microservice.Context;
import com.emc.microservice.health.HealthCheck;
import com.emc.microservice.messaging.MessagingStatsResourceDescriptor;
import com.emc.microservice.resource.ExternalResourceManager;
import com.emc.microservice.resource.ResourceProviderManager;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by liebea on 2/22/15.
 * Drink responsibly
 */
public class DevMessagingStatsResourceManager implements
                                              ExternalResourceManager<
                                                      MessagingStatsResourceDescriptor,
                                                      DevMessagingProviderConfiguration,
                                                      DevMessagingStatsConnection> {

    @Override
    public Class<DevMessagingProviderConfiguration> getResourceConfigurationClass() {
        return DevMessagingProviderConfiguration.class;
    }

    @Override
    public Class<MessagingStatsResourceDescriptor> getDescriptorClass() {
        return MessagingStatsResourceDescriptor.class;
    }

    @Override
    public Collection<HealthCheck> getResourceHealthChecks(DevMessagingStatsConnection managedResource) {
        return Collections.emptyList();
    }

    @Override
    public String getResourceTypeNamePlural() {
        return getResourceTypeName();
    }

    @Override
    public String getResourceTypeName() {
        return "Dev Messaging Statistics";
    }

    @Override
    public DevMessagingStatsConnection initializeResource(
            MessagingStatsResourceDescriptor resourceDescriptor,
            DevMessagingProviderConfiguration resourceConfiguration,
            Context context) {

        //noinspection unchecked
        return new DevMessagingStatsConnection(
                resourceDescriptor,
                resourceConfiguration,
                ResourceProviderManager.getResourceProvider().getServiceRegistryApi(),
                DevMessagingServer.getInstance());
    }

    @Override
    public void postInitResource(
            MessagingStatsResourceDescriptor resourceDescriptor,
            DevMessagingProviderConfiguration resourceConfiguration,
            DevMessagingStatsConnection initializedResource,
            Context context) {
    }

    @Override
    public void cleanUpResource(DevMessagingStatsConnection resourceToCleanUp) {
    }

    @Override
    public void pauseResource(DevMessagingStatsConnection resourceToPause) {
    }

    @Override
    public void startResource(DevMessagingStatsConnection resourceToStart) {
    }

}
