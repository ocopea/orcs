// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.datasource;

import com.emc.microservice.resource.ManagedResource;

/**
 * Created with love by liebea on 6/3/2014.
 */
public interface ManagedDatasource extends ManagedResource<DatasourceDescriptor, DatasourceConfiguration> {
    MicroServiceDataSource getDataSource();
}
