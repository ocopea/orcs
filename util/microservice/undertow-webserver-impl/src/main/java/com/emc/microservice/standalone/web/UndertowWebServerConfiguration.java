// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.standalone.web;

import com.emc.microservice.resource.ResourceConfigurationProperty;
import com.emc.microservice.resource.ResourceConfigurationPropertyType;
import com.emc.microservice.restapi.WebServerConfiguration;

import java.util.Arrays;
import java.util.List;

/**
 * Created with true love by liebea on 10/19/2014.
 */
public class UndertowWebServerConfiguration extends WebServerConfiguration {
    private static final String CONFIGURATION_NAME = "Standalone RestEasy Web Server";
    private static final ResourceConfigurationProperty PROPERTY_PORT = new ResourceConfigurationProperty("port",
            ResourceConfigurationPropertyType.INT,
            "Default Webserver Port",
            true,
            false);
    private static final ResourceConfigurationProperty PROPERTY_BASE_PATH =
            new ResourceConfigurationProperty("basePath",
                    ResourceConfigurationPropertyType.INT,
                    "Base Root Path",
                    false,
                    false);
    private static final List<ResourceConfigurationProperty> PROPERTIES =
            Arrays.asList(PROPERTY_PORT, PROPERTY_BASE_PATH);

    public UndertowWebServerConfiguration() {
        super(CONFIGURATION_NAME, PROPERTIES);
    }

    public UndertowWebServerConfiguration(int port) {
        this(port, null);
    }

    public UndertowWebServerConfiguration(int port, String basePath) {
        this();
        setPropertyValues(propArrayToMap(new String[]{
                PROPERTY_PORT.getName(), Integer.toString(port),
                PROPERTY_BASE_PATH.getName(), basePath
        }));
    }

    public int getPort() {
        return Integer.parseInt(getProperty(PROPERTY_PORT.getName()));
    }

    public String getBasePath() {
        return getProperty(PROPERTY_BASE_PATH.getName());
    }
}
