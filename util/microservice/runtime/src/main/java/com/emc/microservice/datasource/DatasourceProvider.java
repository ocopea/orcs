// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.datasource;

/**
 * Created by liebea on 11/21/16.
 * Drink responsibly
 */
public interface DatasourceProvider<C extends DatasourceConfiguration> {

    MicroServiceDataSource getDatasource(C conf);

    Class<C> getConfClass();
}
