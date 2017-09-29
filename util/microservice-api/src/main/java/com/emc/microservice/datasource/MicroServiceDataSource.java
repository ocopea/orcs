// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2015-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.datasource;

import javax.sql.DataSource;
import java.io.Closeable;

/**
 * Created by liebea on 4/7/15.
 * Drink responsibly
 */
public interface MicroServiceDataSource extends DataSource, Closeable {
    void beginTransaction();

    void commitTransaction();

    void rollbackTransaction();

    boolean isInTransaction();
}
