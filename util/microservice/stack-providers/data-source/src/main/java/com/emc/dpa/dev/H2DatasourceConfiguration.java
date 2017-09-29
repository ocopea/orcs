// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.dpa.dev;

import com.emc.microservice.datasource.DatasourceConfiguration;
import com.emc.microservice.resource.ResourceConfigurationProperty;
import com.emc.microservice.resource.ResourceConfigurationPropertyType;

import java.util.Arrays;
import java.util.List;

/**
 * See {@link com.emc.dpa.dev.datasource.H2DatasourceProvider}.
 */
public class H2DatasourceConfiguration extends DatasourceConfiguration {
    private static final String CONFIGURATION_NAME = "H2 Datasource";
    private static final ResourceConfigurationProperty PROPERTY_DB_SCHEMA =
            new ResourceConfigurationProperty("dbSchema",
                    ResourceConfigurationPropertyType.STRING,
                    "Database schema",
                    true,
                    false);

    // Configuration Properties
    private static final ResourceConfigurationProperty PROPERTY_DB_FILE_NAME = new ResourceConfigurationProperty(
            "dbFilename",
            ResourceConfigurationPropertyType.STRING,
            "DB File Name",
            true,
            false);

    private static final List<ResourceConfigurationProperty> PROPERTIES = Arrays.asList(PROPERTY_DB_FILE_NAME);

    public H2DatasourceConfiguration() {
        super(CONFIGURATION_NAME, PROPERTIES);
    }

    public H2DatasourceConfiguration(String dbFileName, String dbSchemaName) {
        this();
        setPropertyValues(propArrayToMap(new String[]{
                PROPERTY_DB_FILE_NAME.getName(), dbFileName,
                PROPERTY_DB_SCHEMA.getName(), dbSchemaName
        }));
    }

    public String getDBFileName() {
        return getProperty(PROPERTY_DB_FILE_NAME.getName());
    }

    @Override
    public String getDatabaseSchema() {
        return getProperty(PROPERTY_DB_SCHEMA.getName());
    }

}
