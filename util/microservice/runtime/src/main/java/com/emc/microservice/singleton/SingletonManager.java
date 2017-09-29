// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.singleton;

import com.emc.microservice.Context;
import com.emc.microservice.resource.AbstractResourceManager;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by liebea on 3/22/15.
 * Drink responsibly
 */
public class SingletonManager extends
                              AbstractResourceManager<SingletonDescriptor, SingletonConfiguration, ManagedSingleton> {

    protected static final String NAME = "Singleton";
    private static final String NAME_PLURAL = "Singletons";

    /**
     * Initialize the resource manager
     *
     * @param descriptors Static descriptor list describing the resource (defined on design time)
     * @param microServiceLogger logger that belongs to the micro-service instance we're attached to
     */
    public SingletonManager(List<SingletonDescriptor> descriptors, Logger microServiceLogger) {
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
    public ManagedSingletonImpl initializeResource(
            SingletonDescriptor resourceDescriptor,
            SingletonConfiguration resourceConfiguration,
            Context context) {
        try {

            // Instantiating singleton class
            Object singletonInstance = resourceDescriptor.getClazz().newInstance();
            return new ManagedSingletonImpl(resourceDescriptor, resourceConfiguration, singletonInstance);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Failed instantiating singleton " + resourceDescriptor.getName() +
                    " of type " + resourceDescriptor.getClazz().getCanonicalName(), e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed initializing singleton " + resourceDescriptor.getName() +
                    " of type " + resourceDescriptor.getClazz().getCanonicalName(), e);
        }
    }

    @Override
    public void postInitResource(
            SingletonDescriptor resourceDescriptor,
            SingletonConfiguration resourceConfiguration,
            ManagedSingleton initializedResource,
            Context context) {
        doIfLifecycle(initializedResource.getInstance(), serviceLifecycle -> serviceLifecycle.init(context));

    }

    private void doIfLifecycle(Object o, Consumer<ServiceLifecycle> consumer) {
        if (o instanceof ServiceLifecycle) {
            consumer.accept((ServiceLifecycle) o);
        }
    }

    @Override
    public void cleanUpResource(ManagedSingleton resourceToCleanUp) {
        doIfLifecycle(resourceToCleanUp.getInstance(), ServiceLifecycle::shutDown);
    }

    @Override
    public void pauseResource(ManagedSingleton resourceToPause) {
        doIfLifecycle(resourceToPause.getInstance(), ServiceLifecycle::pause);
    }

    @Override
    public void startResource(ManagedSingleton resourceToStart) {
        doIfLifecycle(resourceToStart.getInstance(), ServiceLifecycle::resume);
    }

    @Override
    public Class<SingletonConfiguration> getResourceConfigurationClass() {
        return SingletonConfiguration.class;
    }

    @Override
    public Class<SingletonDescriptor> getDescriptorClass() {
        return SingletonDescriptor.class;
    }
}
