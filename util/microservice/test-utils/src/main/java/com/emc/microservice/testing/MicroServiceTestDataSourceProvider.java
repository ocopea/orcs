// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2016 EMC Corporation
 * All rights reserved
 */
package com.emc.microservice.testing;

import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nivenb on 19/01/2016.
 */
public class MicroServiceTestDataSourceProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicroServiceTestDataSourceProvider.class);

    private static final String CONNECTION_STRING = "jdbc:h2:mem:@databaseName@;DB_CLOSE_DELAY=-1;MODE=PostgreSQL";

    private static Map<String, HikariDataSource> pooledDataSources = new HashMap<>();

    @NoJavadoc
    public static synchronized DataSource getDataSource(
            MockTestingResourceProvider.MockDatasourceConfiguration configuration) {

        String databaseName = configuration.getDatabaseSchema();

        String poolName = databaseName;

        HikariDataSource pooledDataSource = pooledDataSources.get(poolName);

        if (pooledDataSource != null) {
            return pooledDataSource;
        }

        loadH2DriverClass();

        String connectionString = CONNECTION_STRING.replace("@databaseName@", databaseName);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(connectionString);
        config.setMaximumPoolSize(10);
        config.setPoolName("pool_" + databaseName);

        pooledDataSource = new HikariDataSource(config);
        pooledDataSources.put(poolName, pooledDataSource);

        return pooledDataSource;
    }

    private static void loadH2DriverClass() {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.error("Could not load H2 driver", e);
        }
    }

}
