// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.dservice;

import com.emc.microservice.Context;
import com.emc.microservice.resource.AbstractResourceManager;
import com.emc.microservice.singleton.ServiceLifecycle;
import org.slf4j.Logger;

import java.util.List;
import java.util.ServiceLoader;

/**
 * Created by liebea on 3/22/15.
 * Drink responsibly
 */
public class DynamicJavaServiceManager extends
                                       AbstractResourceManager<
                                               DynamicJavaServiceDescriptor,
                                               DynamicJavaServiceConfiguration,
                                               ManagedDynamicJavaService> {

    private static final String NAME = "Dynamic Java Service";
    private static final String NAME_PLURAL = "Dynamic Java Services";

    /**
     * Initialize the resource manager
     *
     * @param descriptors Static descriptor list describing the resource (defined on design time)
     * @param microServiceLogger logger that belongs to the micro-service instance we're attached to
     */
    public DynamicJavaServiceManager(List<DynamicJavaServiceDescriptor> descriptors, Logger microServiceLogger) {
        super(descriptors, microServiceLogger);
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
    public ManagedDynamicJavaService initializeResource(
            DynamicJavaServiceDescriptor resourceDescriptor,
            DynamicJavaServiceConfiguration resourceConfiguration,
            Context context) {
        try {

            // Instantiating singleton class
            final ServiceLoader loader = ServiceLoader.load(resourceDescriptor.getClazz());
            if (!loader.iterator().hasNext()) {
                throw new IllegalStateException("Could not find service implementation for " +
                        resourceDescriptor.getClazz().getCanonicalName() + " in classpath");
            }
            final Object next = loader.iterator().next();

            // Initializing if applicable
            if (next instanceof ServiceLifecycle) {
                ((ServiceLifecycle) next).init(context);
            }
            return new ManagedDynamicJavaServiceImpl(resourceDescriptor, resourceConfiguration, next);
        } catch (Exception e) {
            throw new IllegalStateException("Failed initializing dynamic java service " + resourceDescriptor.getName() +
                    " of type " + resourceDescriptor.getClazz().getCanonicalName(), e);
        }
    }

    @Override
    public void postInitResource(
            DynamicJavaServiceDescriptor resourceDescriptor,
            DynamicJavaServiceConfiguration resourceConfiguration,
            ManagedDynamicJavaService initializedResource,
            Context context) {
    }

    @Override
    public void cleanUpResource(ManagedDynamicJavaService resourceToCleanUp) {
        final Object instance = resourceToCleanUp.getInstance();
        if (instance instanceof ServiceLifecycle) {
            ((ServiceLifecycle) instance).shutDown();
        }
    }

    @Override
    public void pauseResource(ManagedDynamicJavaService resourceToPause) {
    }

    @Override
    public void startResource(ManagedDynamicJavaService resourceToStart) {
    }

    @Override
    public Class<DynamicJavaServiceConfiguration> getResourceConfigurationClass() {
        return DynamicJavaServiceConfiguration.class;
    }

    @Override
    public Class<DynamicJavaServiceDescriptor> getDescriptorClass() {
        return DynamicJavaServiceDescriptor.class;
    }
}
