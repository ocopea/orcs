// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.resource;

import com.emc.microservice.Context;
import org.slf4j.Logger;

import java.util.List;

/**
 * Created with true love by liebea on 11/13/2014.
 */
public class ExternalResourceManagerWrapper<
        D extends ResourceDescriptor,
        C extends ResourceConfiguration,
        R extends ManagedResource<D, C>> extends
                                         AbstractResourceManager<D, C, R> {

    private final ExternalResourceManager<D, C, R> externalResourceManager;

    /**
     * Initialize the resource manager
     *
     * @param descriptors Static descriptor list describing the resource (defined on design time)
     * @param microServiceLogger logger that belongs to the micro-service instance we're attached to
     */
    public ExternalResourceManagerWrapper(
            List<D> descriptors,
            Logger microServiceLogger,
            ExternalResourceManager<D, C, R> externalResourceManager) {
        super(descriptors, microServiceLogger);
        this.externalResourceManager = externalResourceManager;
    }

    @Override
    public String getResourceTypeNamePlural() {
        return externalResourceManager.getResourceTypeNamePlural();
    }

    @Override
    public String getResourceTypeName() {
        return externalResourceManager.getResourceTypeName();
    }

    @Override
    public R initializeResource(D resourceDescriptor, C resourceConfiguration, Context context) {
        return externalResourceManager.initializeResource(resourceDescriptor, resourceConfiguration, context);
    }

    @Override
    public void postInitResource(
            D resourceDescriptor,
            C resourceConfiguration,
            R initializedResource,
            Context context) {
        externalResourceManager.postInitResource(
                resourceDescriptor,
                resourceConfiguration,
                initializedResource,
                context);
    }

    @Override
    public void cleanUpResource(R resourceToCleanUp) {
        externalResourceManager.cleanUpResource(resourceToCleanUp);
    }

    @Override
    public void pauseResource(R resourceToPause) {
        externalResourceManager.pauseResource(resourceToPause);
    }

    @Override
    public void startResource(R resourceToStart) {
        externalResourceManager.startResource(resourceToStart);
    }

    @Override
    public Class<C> getResourceConfigurationClass() {
        return externalResourceManager.getResourceConfigurationClass();
    }

    @Override
    public Class<D> getDescriptorClass() {
        return externalResourceManager.getDescriptorClass();
    }
}
