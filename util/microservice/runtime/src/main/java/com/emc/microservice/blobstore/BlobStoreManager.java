// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.blobstore;

import com.emc.microservice.Context;
import com.emc.microservice.health.HealthCheck;
import com.emc.microservice.resource.AbstractResourceManager;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by liebea on 9/28/2014.
 * Enjoy it.
 */
public class BlobStoreManager extends
                              AbstractResourceManager<BlobStoreDescriptor, BlobStoreConfiguration, ManagedBlobStore> {

    /**
     * Initialize the resource manager.
     *
     * @param descriptors Static descriptor list describing the resource (defined on design time)
     * @param microServiceLogger logger that belongs to the micro-service instance we're attached to
     */
    public BlobStoreManager(List<BlobStoreDescriptor> descriptors, Logger microServiceLogger) {
        super(descriptors, microServiceLogger);
    }

    @Override
    public String getResourceTypeNamePlural() {
        return "Blob Stores";
    }

    @Override
    public String getResourceTypeName() {
        return "Blob Store";
    }

    @Override
    public ManagedBlobStore initializeResource(
            BlobStoreDescriptor resourceDescriptor,
            BlobStoreConfiguration resourceConfiguration,
            Context context) {

        //noinspection unchecked
        return new ManagedBlobStoreImpl(
                resourceDescriptor,
                resourceConfiguration,
                resourceProvider
                        .getBlobStore(
                                resourceConfiguration,
                                context));
    }

    @Override
    public void postInitResource(
            BlobStoreDescriptor resourceDescriptor,
            BlobStoreConfiguration resourceConfiguration,
            ManagedBlobStore initializedResource,
            Context context) {
    }

    @Override
    public void cleanUpResource(ManagedBlobStore resourceToCleanUp) {
        // nothing to do unless deciding to cache
    }

    @Override
    public void pauseResource(ManagedBlobStore resourceToPause) {
        // nothing to do
    }

    @Override
    public void startResource(ManagedBlobStore resourceToStart) {
        // nothing to do
    }

    @Override
    public Class<BlobStoreConfiguration> getResourceConfigurationClass() {
        return BlobStoreConfiguration.class;
    }

    @Override
    public Collection<HealthCheck> getResourceHealthChecks(ManagedBlobStore managedResource) {
        return Collections.emptyList();
    }

    @Override
    public Class<BlobStoreDescriptor> getDescriptorClass() {
        return BlobStoreDescriptor.class;
    }
}
