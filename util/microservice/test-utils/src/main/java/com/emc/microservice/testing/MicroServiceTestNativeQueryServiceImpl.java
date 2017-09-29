// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: ApolloUnitTestNativeQueryServiceImpl.java 77948 2013-08-16 16:39:00Z martiv6 $
 *
 * This computer code is copyright 2013 - 2016 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.testing;

import com.emc.ocopea.util.database.AbstractNativeQueryService;

import javax.sql.DataSource;

/**
 * An Apollo NativeQueryServiceBean implementation suitable for unit tests, using an internal H2 database.
 *
 * @author nivenb
 */
public class MicroServiceTestNativeQueryServiceImpl extends AbstractNativeQueryService {

    private DataSource h2InMemoryDataSource;

    public MicroServiceTestNativeQueryServiceImpl(String databaseName) {
        setupDataSource(databaseName, null);
    }

    public MicroServiceTestNativeQueryServiceImpl(String databaseName, String schema) {
        setupDataSource(databaseName, schema);
    }

    /**
     *
     */
    @Override
    public DataSource getDataSource() {
        return h2InMemoryDataSource;
    }

    /**
     *
     */
    private void setupDataSource(String databaseName, String schema) {
        MockTestingResourceProvider.MockDatasourceConfiguration mockDatasourceConfiguration =
                new MockTestingResourceProvider.MockDatasourceConfiguration(databaseName + "_" + schema);
        h2InMemoryDataSource = MicroServiceTestDataSourceProvider.getDataSource(mockDatasourceConfiguration);
    }

}
