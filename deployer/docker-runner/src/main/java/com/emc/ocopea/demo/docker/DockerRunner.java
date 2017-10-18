// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.demo.docker;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceController;
import com.emc.microservice.blobstore.PGBlobstoreSchemaBootstrap;
import com.emc.microservice.bootstrap.AbstractSchemaBootstrap;
import com.emc.microservice.bootstrap.SchemaBootstrapRunner;
import com.emc.microservice.config.ConfigurationAPI;
import com.emc.microservice.configuration.EnvironmentVariableConfigurationImpl;
import com.emc.microservice.postgres.StandalonePostgresDatasourceConfiguration;
import com.emc.microservice.registry.ServiceRegistryImpl;
import com.emc.microservice.resource.DefaultWebApiResolver;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.microservice.runner.MicroServiceRunner;
import com.emc.microservice.webclient.WebAPIResolver;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Helper Runner for running microservices within docker containers
 */
public class DockerRunner {

    /**
     * Run services in docker container
     */
    public static Map<String, MicroServiceController> runServices(
            String serviceEnvVarPrefix,
            Map<String, AbstractSchemaBootstrap> schemaBootstrapMap,
            MicroService... services) {

        ResourceProvider resourceProvider = getResourceProvider(serviceEnvVarPrefix);

        schemaBootstrapMap.entrySet().forEach(currDsEntry -> {

            final StandalonePostgresDatasourceConfiguration pgConfiguration = resourceProvider.getServiceRegistryApi()
                    .getDataSourceConfiguration(StandalonePostgresDatasourceConfiguration.class, currDsEntry.getKey());

            DataSource dataSource = resourceProvider.getDataSource(pgConfiguration);
            try {
                final String schemaName = currDsEntry.getValue().getSchemaName();
                SchemaBootstrapRunner.dropSchemaIfExist(dataSource, schemaName);
                SchemaBootstrapRunner.runBootstrap(
                        dataSource,
                        currDsEntry.getValue(),
                        schemaName,
                        null);
            } catch (SQLException | IOException e) {
                throw new IllegalStateException("Failed initializing schema " + currDsEntry.getKey(), e);
            }
        });

        final StandalonePostgresDatasourceConfiguration blobStoreConfiguration = resourceProvider
                .getServiceRegistryApi()
                .getDataSourceConfiguration(StandalonePostgresDatasourceConfiguration.class, "hub-db");
        DataSource dataSource = resourceProvider.getDataSource(blobStoreConfiguration);
        try {
            final String schemaName = "central_blobstore";
            SchemaBootstrapRunner.dropSchemaIfExist(dataSource, schemaName);
            SchemaBootstrapRunner.runBootstrap(
                    dataSource,
                    new PGBlobstoreSchemaBootstrap(schemaName),
                    schemaName,
                    null);
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Failed initializing central_blobstore schema", e);
        }

        return new MicroServiceRunner().run(
                resourceProvider,
                services
        );
    }

    private static ResourceProvider getResourceProvider(String serviceEnvVarPrefix) {
        ConfigurationAPI staticEnvConfiguration = getConfigurationAPI();

        // reading the cluster ip of the current service
        final String serviceHost = System.getenv(serviceEnvVarPrefix.toUpperCase() + "_SERVICE_HOST");
        final String servicePort = System.getenv(serviceEnvVarPrefix.toUpperCase() + "_SERVICE_PORT");
        if (serviceHost == null || servicePort == null) {
            throw new IllegalStateException("Failed locating cluster service ip and port using " +
                    serviceEnvVarPrefix.toUpperCase() + "_SERVICE_HOST and " +
                    serviceEnvVarPrefix.toUpperCase() + "_SERVICE_PORT");
        }

        // In some cases, docker does not expose the service cluster ip within the same container. so cheating it...
        final String urlToReplace = "://" + serviceHost + ":" + servicePort;

        return new ResourceProvider(
                staticEnvConfiguration,
                new ServiceRegistryImpl(staticEnvConfiguration)) {
            @Override
            public WebAPIResolver getWebAPIResolver() {
                return new DefaultWebApiResolver(false) {
                    @Override
                    protected ResteasyWebTarget getResteasyWebTarget(String url) {

                        return super.getResteasyWebTarget(
                                url.replaceFirst(urlToReplace, "://localhost:8080")
                        );
                    }
                };
            }
        };

    }

    private static ConfigurationAPI getConfigurationAPI() {
        return new EnvironmentVariableConfigurationImpl("NAZ_MS_CONF");
    }

}
