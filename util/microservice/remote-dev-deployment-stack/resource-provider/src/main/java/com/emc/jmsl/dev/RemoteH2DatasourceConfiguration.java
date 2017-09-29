// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.jmsl.dev;

import com.emc.microservice.datasource.DatasourceConfiguration;
import com.emc.microservice.resource.ResourceConfigurationProperty;
import com.emc.microservice.resource.ResourceConfigurationPropertyType;

import java.util.Arrays;
import java.util.List;

/**
 * Created by liebea on 2/6/15.
 * Drink responsibly
 */
public class RemoteH2DatasourceConfiguration extends DatasourceConfiguration {
    private static final String CONFIGURATION_NAME = "H2 Remote Datasource";
    private static final ResourceConfigurationProperty PROPERTY_DBNAME = new ResourceConfigurationProperty("dbName",
            ResourceConfigurationPropertyType.STRING,
            "Database Name",
            true,
            false);
    private static final ResourceConfigurationProperty PROPERTY_USER = new ResourceConfigurationProperty("userName",
            ResourceConfigurationPropertyType.STRING,
            "User Name",
            true,
            true);
    private static final ResourceConfigurationProperty PROPERTY_PASSWORD = new ResourceConfigurationProperty("password",
            ResourceConfigurationPropertyType.STRING,
            "password",
            false,
            true);
    private static final ResourceConfigurationProperty PROPERTY_URL = new ResourceConfigurationProperty("url",
            ResourceConfigurationPropertyType.STRING,
            "DB Connection String",
            true,
            false);

    private static final List<ResourceConfigurationProperty> PROPERTIES =
            Arrays.asList(PROPERTY_DBNAME, PROPERTY_URL, PROPERTY_PASSWORD, PROPERTY_URL);

    public RemoteH2DatasourceConfiguration() {
        super(CONFIGURATION_NAME, PROPERTIES);
    }

    public RemoteH2DatasourceConfiguration(String dbName, String userName, String password, String url) {
        this();
        setPropertyValues(propArrayToMap(new String[]{
                PROPERTY_DBNAME.getName(), dbName,
                PROPERTY_USER.getName(), userName,
                PROPERTY_PASSWORD.getName(), password,
                PROPERTY_URL.getName(), url
        }));
    }

    @Override
    public String getDatabaseSchema() {
        return getDBName();
    }

    public String getDBName() {
        return getProperty(PROPERTY_DBNAME.getName());
    }

    public String getUserName() {
        return getProperty(PROPERTY_USER.getName());
    }

    public String getPassword() {
        return getProperty(PROPERTY_PASSWORD.getName());
    }

    public String getURL() {
        return getProperty(PROPERTY_URL.getName());
    }
}
