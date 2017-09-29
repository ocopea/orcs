// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.dpa.dev.datasource;

import com.emc.dpa.dev.H2DatasourceConfiguration;
import com.emc.microservice.datasource.DatasourceProvider;
import com.emc.microservice.datasource.MicroServiceDataSource;

import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of {@link DatasourceProvider}. Loads H2 driver and creates data sources using H2 DB in PostgreSQL
 * mode. Always uses user 'sa' and an empty password.
 */
public class H2DatasourceProvider implements DatasourceProvider<H2DatasourceConfiguration> {
    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot load H2 driver", e);
        }
    }

    private static Map<String, DevDataSource> datasourceByName = new HashMap<>();

    public static DevDataSource getDataSource(H2DatasourceConfiguration configuration) {
        return getDataSource(configuration.getDBFileName());
    }

    /**
     * Retrieve pooled data source based on configuration. If data source with the same databaseName and databaseSchema
     * and dbUser already exists method will return existing one.
     *
     * @param dbFileName database file name
     *
     * @return Data Source
     */
    public static DevDataSource getDataSource(String dbFileName) {
        DevDataSource dataSource = datasourceByName.get(dbFileName);
        if (dataSource == null) {
            dataSource =
                    DevDataSource.create("jdbc:h2:mem:" + dbFileName + ";DB_CLOSE_DELAY=-1;MODE=PostgreSQL", "sa", "");
            datasourceByName.put(dbFileName, dataSource);
        }
        return dataSource;
    }

    @Override
    public MicroServiceDataSource getDatasource(H2DatasourceConfiguration h2DatasourceConfiguration) {
        return getDataSource(h2DatasourceConfiguration.getDBFileName());
    }

    @Override
    public Class<H2DatasourceConfiguration> getConfClass() {
        return H2DatasourceConfiguration.class;
    }
}
