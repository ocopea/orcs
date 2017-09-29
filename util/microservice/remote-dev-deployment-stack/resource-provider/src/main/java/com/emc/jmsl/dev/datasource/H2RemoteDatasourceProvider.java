// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.jmsl.dev.datasource;

import com.emc.jmsl.dev.RemoteH2DatasourceConfiguration;
import com.emc.microservice.datasource.DatasourceProvider;
import com.emc.microservice.datasource.MicroServiceDataSource;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with true love by liebea on 10/12/2014.
 */
public class H2RemoteDatasourceProvider implements DatasourceProvider<RemoteH2DatasourceConfiguration> {
    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot load H2 driver", e);
        }
    }

    private static Map<String, DevDataSource> datasourceByName = new HashMap<>();

    @Override
    public MicroServiceDataSource getDatasource(RemoteH2DatasourceConfiguration configuration) {
        DevDataSource dataSource = datasourceByName.get(configuration.getDBName());
        if (dataSource == null) {
            dataSource = DevDataSource.create(configuration.getURL(),
                    configuration.getUserName(),
                    configuration.getPassword());
            datasourceByName.put(configuration.getDBName(), dataSource);
        }
        return dataSource;
    }

    @Override
    public Class<RemoteH2DatasourceConfiguration> getConfClass() {
        return RemoteH2DatasourceConfiguration.class;
    }
}
