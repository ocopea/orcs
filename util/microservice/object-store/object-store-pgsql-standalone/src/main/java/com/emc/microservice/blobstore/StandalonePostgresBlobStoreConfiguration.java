// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.blobstore;

import com.emc.microservice.resource.ResourceConfigurationProperty;
import com.emc.microservice.resource.ResourceConfigurationPropertyType;

import java.util.Arrays;
import java.util.List;

/**
 * Created by liebea on 9/6/2014. Enjoy it
 */
public class StandalonePostgresBlobStoreConfiguration extends BlobStoreConfiguration {

    private static final String CONFIGURATION_NAME = "Standalone PGSQL Blobstore";

    // Configuration Properties
    protected static final ResourceConfigurationProperty PROPERTY_SERVER = new ResourceConfigurationProperty("server",
            ResourceConfigurationPropertyType.STRING,
            "Database server",
            true,
            false);
    protected static final ResourceConfigurationProperty PROPERTY_PORT = new ResourceConfigurationProperty("port",
            ResourceConfigurationPropertyType.INT,
            "Database port",
            true,
            false);
    protected static final ResourceConfigurationProperty PROPERTY_DATABASE_NAME = new ResourceConfigurationProperty(
            "databaseName",
            ResourceConfigurationPropertyType.STRING,
            "Database name",
            true,
            false);
    protected static final ResourceConfigurationProperty PROPERTY_DB_SCHEMA = new ResourceConfigurationProperty(
            "dbSchema",
            ResourceConfigurationPropertyType.STRING,
            "Database schema",
            true,
            false);
    protected static final ResourceConfigurationProperty PROPERTY_MAX_CONNECTIONS = new ResourceConfigurationProperty(
            "maxConnections",
            ResourceConfigurationPropertyType.INT,
            "Maximum connections to allocate in local connection pool",
            true,
            false);
    protected static final ResourceConfigurationProperty PROPERTY_DB_USER = new ResourceConfigurationProperty("dbUser",
            ResourceConfigurationPropertyType.STRING,
            "Database user name",
            true,
            true);
    protected static final ResourceConfigurationProperty PROPERTY_DB_PASSWORD = new ResourceConfigurationProperty(
            "dbPassword",
            ResourceConfigurationPropertyType.STRING,
            "Database password",
            true,
            true);

    protected static final List<ResourceConfigurationProperty> PROPERTIES = Arrays.asList(PROPERTY_SERVER,
            PROPERTY_PORT,
            PROPERTY_DATABASE_NAME,
            PROPERTY_DB_SCHEMA,
            PROPERTY_MAX_CONNECTIONS,
            PROPERTY_DB_USER,
            PROPERTY_DB_PASSWORD);

    public StandalonePostgresBlobStoreConfiguration() {
        super(CONFIGURATION_NAME, PROPERTIES);
    }

    public StandalonePostgresBlobStoreConfiguration(
            String configurationName,
            List<ResourceConfigurationProperty> properties) {
        super(configurationName, properties);
    }

    public StandalonePostgresBlobStoreConfiguration(
            String server,
            int port,
            String dbName,
            String dbSchemaName,
            int maxConnections,
            String user,
            String password) {
        this();
        setPropertyValues(propArrayToMap(new String[]{
                PROPERTY_SERVER.getName(), server,
                PROPERTY_PORT.getName(), String.valueOf(port),
                PROPERTY_MAX_CONNECTIONS.getName(), String.valueOf(maxConnections),
                PROPERTY_DATABASE_NAME.getName(), dbName,
                PROPERTY_DB_USER.getName(), user,
                PROPERTY_DB_PASSWORD.getName(), password,
                PROPERTY_DB_SCHEMA.getName(), dbSchemaName
        }));
    }

    public String getServer() {
        return getProperty(PROPERTY_SERVER.getName());
    }

    public int getPort() {
        return Integer.parseInt(getProperty(PROPERTY_PORT.getName()));
    }

    public String getDatabaseName() {
        return getProperty(PROPERTY_DATABASE_NAME.getName());
    }

    public String getDatabaseSchema() {
        return getProperty(PROPERTY_DB_SCHEMA.getName());
    }

    public int getMaxConnections() {
        return Integer.parseInt(getProperty(PROPERTY_MAX_CONNECTIONS.getName()));
    }

    public String getDbUser() {
        return getProperty(PROPERTY_DB_USER.getName());
    }

    public String getDbPassword() {
        return getProperty(PROPERTY_DB_PASSWORD.getName());
    }
}
