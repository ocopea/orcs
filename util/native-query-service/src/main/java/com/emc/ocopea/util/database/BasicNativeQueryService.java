// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.database;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import javax.sql.DataSource;

/**
 * Created with love by liebea on 5/28/2014.
 */
public class BasicNativeQueryService extends AbstractNativeQueryService {
    private DataSource dataSource;

    public BasicNativeQueryService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected DataSource getDataSource() {
        return dataSource;
    }
}
