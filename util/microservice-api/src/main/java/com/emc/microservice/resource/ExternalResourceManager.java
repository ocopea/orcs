// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2015-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.resource;

import com.emc.microservice.Context;
import com.emc.microservice.health.HealthCheck;

import java.util.Collection;

/**
 * Created by liebea on 4/5/15.
 * Drink responsibly
 */
public interface ExternalResourceManager<D extends ResourceDescriptor,
        C extends ResourceConfiguration,
        R extends ManagedResource<D, C>> {

    /**
     * Get resource description name for logging
     *
     * @return name of resource being managed (in plural form)
     */
    String getResourceTypeNamePlural();

    /**
     * Get resource description name for logging
     *
     * @return name of resource being managed (in singular form)
     */
    String getResourceTypeName();

    /**
     * Initialize the resource, including validating physical connectivity and checking resource is available for
     * immediate consumption when out of this method.
     *
     * @param resourceDescriptor    design time descriptor for resource
     * @param resourceConfiguration run-time physical configuration for resource
     * @param context               instance of micro-service context
     * @return physical resource instantiated and validated
     */
    R initializeResource(
            D resourceDescriptor,
            C resourceConfiguration,
            Context context);

    /***
     * Called once the resource has initialized usually for things like dependency injection
     * @param resourceDescriptor descriptor
     * @param resourceConfiguration configuration
     * @param initializedResource the initialized resource
     * @param context ms context
     */
    void postInitResource(
            D resourceDescriptor,
            C resourceConfiguration,
            R initializedResource,
            Context context);

    /**
     * Release all resources for service
     *
     * @param resourceToCleanUp resource to clean up
     */
    void cleanUpResource(R resourceToCleanUp);

    /**
     * Pausing resource, making it temporarily unavailable to consume by services - e.g. pause queue,
     * reply to http request with server busy etc...
     *
     * @param resourceToPause resource to pause
     */
    void pauseResource(R resourceToPause);

    /**
     * Mark the resource as "serving" after all initializations and validations already handled by
     * "initializeResource" method.
     *
     * @param resourceToStart resource to mark as started
     */
    void startResource(R resourceToStart);

    Class<C> getResourceConfigurationClass();

    Class<D> getDescriptorClass();

    /**
     * Create resource health check for adding to health check manager
     *
     * @param managedResource resource to create health check for
     */
    Collection<HealthCheck> getResourceHealthChecks(R managedResource);
}
