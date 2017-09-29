// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.datasource;

import com.emc.microservice.Context;
import com.emc.microservice.health.HealthCheck;
import com.emc.microservice.resource.AbstractResourceManager;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created with love by liebea on 6/3/2014.
 */
public class DatasourceManager extends
                               AbstractResourceManager<
                                       DatasourceDescriptor,
                                       DatasourceConfiguration,
                                       ManagedDatasource> {

    public DatasourceManager(List<DatasourceDescriptor> descriptors, Logger logger) {
        super(descriptors, logger);
    }

    @Override
    public String getResourceTypeNamePlural() {
        return "Managed Data Sources";
    }

    @Override
    public String getResourceTypeName() {
        return "Managed Data Source";
    }

    @Override
    public ManagedDatasource initializeResource(
            DatasourceDescriptor resourceDescriptor,
            DatasourceConfiguration resourceConfiguration,
            Context context) {

        MicroServiceDataSource dataSource = resourceProvider.getDataSource(
                resourceConfiguration.asSpecificConfiguration(resourceProvider.getDatasourceConfigurationClass()));

        return new ManagedDatasourceImpl(resourceDescriptor, resourceConfiguration, dataSource);
    }

    @Override
    public void postInitResource(
            DatasourceDescriptor resourceDescriptor,
            DatasourceConfiguration resourceConfiguration,
            ManagedDatasource initializedResource,
            Context context) {
    }

    @Override
    public Collection<HealthCheck> getResourceHealthChecks(ManagedDatasource managedDatasource) {
        return Collections.singletonList(
                new ManagedDatasourceHealthCheck(managedDatasource));
    }

    @Override
    public void cleanUpResource(ManagedDatasource resourceToCleanUp) {
    }

    @Override
    public void pauseResource(ManagedDatasource resourceToPause) {
    }

    @Override
    public void startResource(ManagedDatasource resourceToStart) {
    }

    @Override
    public Class<DatasourceConfiguration> getResourceConfigurationClass() {
        return DatasourceConfiguration.class;
    }

    @Override
    public Class<DatasourceDescriptor> getDescriptorClass() {
        return DatasourceDescriptor.class;
    }
}
