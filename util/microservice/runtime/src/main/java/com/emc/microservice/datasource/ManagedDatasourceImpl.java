// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.datasource;

import com.emc.microservice.resource.AbstractManagedResource;

import java.util.Objects;

/**
 * Created with love by liebea on 6/3/2014.
 */
public class ManagedDatasourceImpl extends AbstractManagedResource<DatasourceDescriptor, DatasourceConfiguration>
        implements ManagedDatasource {
    private final MicroServiceDataSource dataSource;

    public ManagedDatasourceImpl(
            DatasourceDescriptor descriptor,
            DatasourceConfiguration configuration,
            MicroServiceDataSource dataSource) {
        super(descriptor, configuration);
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    @Override
    public MicroServiceDataSource getDataSource() {
        return dataSource;
    }
}
