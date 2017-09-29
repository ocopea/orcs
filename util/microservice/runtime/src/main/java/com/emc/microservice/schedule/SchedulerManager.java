// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.schedule;

import com.emc.microservice.Context;
import com.emc.microservice.resource.AbstractResourceManager;
import com.emc.microservice.serialization.JacksonSerializationReader;
import com.emc.microservice.serialization.JacksonSerializationWriter;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

public class SchedulerManager
        extends AbstractResourceManager<SchedulerDescriptor, SchedulerConfiguration, ManagedScheduler> {

    protected static final String NAME = "Scheduler";
    private static final String NAME_PLURAL = "Schedulers";
    private final Map<String, Class<? extends ScheduleListener>> listenerClassMappings;

    /**
     * Initialize the resource manager
     * @param descriptors Static descriptor list describing the resource (defined on design time)
     * @param listenerClassMappings mapping between ScheduleListener identifiers and classes
     * @param microServiceLogger logger that belongs to the micro-service instance we're attached to
     */
    public SchedulerManager(
            List<SchedulerDescriptor> descriptors,
            Map<String, Class<? extends ScheduleListener>> listenerClassMappings,
            Logger microServiceLogger) {
        super(descriptors, microServiceLogger);
        this.listenerClassMappings = listenerClassMappings;
    }

    @Override
    public String getResourceTypeNamePlural() {
        return NAME_PLURAL;
    }

    @Override
    public String getResourceTypeName() {
        return NAME;
    }

    @Override
    public ManagedScheduler initializeResource(
            SchedulerDescriptor resourceDescriptor,
            SchedulerConfiguration resourceConfiguration,
            Context context) {
        context.getSerializationManager().register(
                SchedulerMessage.class,
                new JacksonSerializationReader<>(SchedulerMessage.class),
                new JacksonSerializationWriter<>());

        return new ManagedSchedulerImpl(
                resourceDescriptor,
                resourceConfiguration,
                resourceProvider.getScheduler(resourceConfiguration, context),
                listenerClassMappings,
                context,
                context.getSerializationManager());
    }

    @Override
    public void postInitResource(
            SchedulerDescriptor resourceDescriptor,
            SchedulerConfiguration resourceConfiguration,
            ManagedScheduler initializedResource,
            Context context) {
    }

    @Override
    public void cleanUpResource(ManagedScheduler resourceToCleanUp) {
    }

    @Override
    public void pauseResource(ManagedScheduler resourceToPause) {
    }

    @Override
    public void startResource(ManagedScheduler resourceToStart) {
    }

    @Override
    public Class<SchedulerDescriptor> getDescriptorClass() {
        return SchedulerDescriptor.class;
    }

    @Override
    public Class<SchedulerConfiguration> getResourceConfigurationClass() {
        return SchedulerConfiguration.class;
    }
}
