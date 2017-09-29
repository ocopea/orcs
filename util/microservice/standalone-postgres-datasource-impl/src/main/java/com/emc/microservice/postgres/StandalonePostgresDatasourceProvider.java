// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2014 - 2016 EMC Corporation
 * All rights reserved
 */
package com.emc.microservice.postgres;

import com.emc.microservice.datasource.DatasourceProvider;
import com.emc.microservice.datasource.DatasourceWrapper;
import com.emc.microservice.datasource.MicroServiceDataSource;
import com.emc.microservice.metrics.DPAMetricDescriptor;
import com.emc.microservice.metrics.MetricsRegistryImpl;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.util.PostgresUtil;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with true love by liebea on 10/12/2014.
 */
public class StandalonePostgresDatasourceProvider
        implements DatasourceProvider<StandalonePostgresDatasourceConfiguration> {

    private static final String DPA_METRIC_PREFIX = "jdbc.pool.";

    private static final String METRIC_TAG_SERVER = "server";

    private static final String METRIC_TAG_DATABASE = "database";

    private static final String METRIC_TAG_SCHEMA = "schema";

    private static final String METRIC_TAG_USER = "user";

    @Override
    public Class<StandalonePostgresDatasourceConfiguration> getConfClass() {
        return StandalonePostgresDatasourceConfiguration.class;
    }

    @Override
    public MicroServiceDataSource getDatasource(StandalonePostgresDatasourceConfiguration configuration) {
        return new DatasourceWrapper(getRawDataSource(configuration));
    }

    @NoJavadoc
    public static DataSource getRawDataSource(StandalonePostgresDatasourceConfiguration configuration) {
        return getDataSource(configuration.getDatabaseName(),
                configuration.getServer(),
                configuration.getPort(),
                configuration.getDbUser(),
                configuration.getDbPassword(),
                configuration.getMaxConnections(),
                configuration.getDatabaseSchema());
    }

    /**
     * Retrieve pooled data source based on configuration. If data source with the same databaseName and databaseSchema
     * and dbUser already exists method will return existing one.
     */
    public static DataSource getDataSource(
            String databaseName, String server, int port, String dbUser,
            String dbPassword, int maxConnections, String schemaName) {
        return PostgresUtil.getDataSource(databaseName, server, port, dbUser, dbPassword, maxConnections, schemaName);
    }

    private static void configureMetricDescriptorForPool(
            String poolIdentifier,
            String server,
            String databaseName,
            String dbUser,
            String databaseSchema,
            MetricsRegistryImpl metricsRegistryImpl) {
        Map<String, String> metricTags = new HashMap<>();
        metricTags.put(METRIC_TAG_SERVER, server);
        metricTags.put(METRIC_TAG_DATABASE, databaseName);
        metricTags.put(METRIC_TAG_SCHEMA, databaseSchema);
        metricTags.put(METRIC_TAG_USER, dbUser);

        metricsRegistryImpl.addCodahaleMetricNameToDescriptorMapping(
                poolIdentifier + ".pool.Wait",
                new DPAMetricDescriptor(DPA_METRIC_PREFIX + "Wait", metricTags));
        metricsRegistryImpl.addCodahaleMetricNameToDescriptorMapping(
                poolIdentifier + ".pool.Usage",
                new DPAMetricDescriptor(DPA_METRIC_PREFIX + "Usage", metricTags));
        metricsRegistryImpl.addCodahaleMetricNameToDescriptorMapping(
                poolIdentifier + ".pool.TotalConnections",
                new DPAMetricDescriptor(DPA_METRIC_PREFIX + "TotalConnections", metricTags));
        metricsRegistryImpl.addCodahaleMetricNameToDescriptorMapping(
                poolIdentifier + ".pool.IdleConnections",
                new DPAMetricDescriptor(DPA_METRIC_PREFIX + "IdleConnections", metricTags));
        metricsRegistryImpl.addCodahaleMetricNameToDescriptorMapping(
                poolIdentifier + ".pool.ActiveConnections",
                new DPAMetricDescriptor(DPA_METRIC_PREFIX + "ActiveConnections", metricTags));
        metricsRegistryImpl.addCodahaleMetricNameToDescriptorMapping(
                poolIdentifier + ".pool.PendingConnections",
                new DPAMetricDescriptor(DPA_METRIC_PREFIX + "PendingConnections", metricTags));
    }

}
