// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging.error;

import com.emc.microservice.messaging.MessagingProviderConfiguration;
import com.emc.microservice.resource.ResourceConfigurationProperty;
import com.emc.microservice.resource.ResourceConfigurationPropertyType;

import java.util.Arrays;
import java.util.List;

/**
 * Created with true love by liebea on 10/12/2014.
 */
public class RabbitMQMessagingProviderConfiguration extends MessagingProviderConfiguration {

    private static final String CONFIGURATION_NAME = "RabbitMQ";

    private static final ResourceConfigurationProperty PROPERTY_HOST = new ResourceConfigurationProperty("host",
            ResourceConfigurationPropertyType.STRING,
            "RabbitMQ host name",
            true,
            false);
    private static final ResourceConfigurationProperty PROPERTY_VIRTUAL_HOST = new ResourceConfigurationProperty(
            "virtualHost",
            ResourceConfigurationPropertyType.STRING,
            "RabbitMQ virtual host name",
            true,
            false);
    private static final ResourceConfigurationProperty PROPERTY_PORT = new ResourceConfigurationProperty("port",
            ResourceConfigurationPropertyType.INT,
            "RabbitMQ port",
            true,
            false);
    private static final ResourceConfigurationProperty PROPERTY_USER_NAME =
            new ResourceConfigurationProperty("userName",
                    ResourceConfigurationPropertyType.STRING,
                    "RabbitMQ user name",
                    true,
                    true);
    private static final ResourceConfigurationProperty PROPERTY_PASSWORD = new ResourceConfigurationProperty("password",
            ResourceConfigurationPropertyType.STRING,
            "RabbitMQ user password",
            true,
            true);

    private static final ResourceConfigurationProperty PROPERTY_MANAGEMENT_HOST = new ResourceConfigurationProperty(
            "managementHost",
            ResourceConfigurationPropertyType.STRING,
            "RabbitMQ management host name",
            true,
            false);
    private static final ResourceConfigurationProperty PROPERTY_MANAGEMENT_PORT = new ResourceConfigurationProperty(
            "managementPort",
            ResourceConfigurationPropertyType.INT,
            "RabbitMQ management port",
            true,
            false);
    private static final ResourceConfigurationProperty PROPERTY_MANAGEMENT_USER_NAME =
            new ResourceConfigurationProperty("managementUserName",
                    ResourceConfigurationPropertyType.STRING,
                    "RabbitMQ management user name",
                    true,
                    true);
    private static final ResourceConfigurationProperty PROPERTY_MANAGEMENT_PASSWORD = new ResourceConfigurationProperty(
            "managementPassword",
            ResourceConfigurationPropertyType.STRING,
            "RabbitMQ management user password",
            true,
            true);
    private static final ResourceConfigurationProperty PROPERTY_MANAGEMENT_PATH = new ResourceConfigurationProperty(
            "managementPath",
            ResourceConfigurationPropertyType.STRING,
            "RabbitMQ management api path",
            false,
            false);
    private static final ResourceConfigurationProperty PROPERTY_MANAGEMENT_SSL = new ResourceConfigurationProperty(
            "managementSSL",
            ResourceConfigurationPropertyType.BOOLEAN,
            "RabbitMQ management ssl option",
            false,
            false);

    private static final List<ResourceConfigurationProperty> PROPERTIES = Arrays.asList(
            PROPERTY_HOST, PROPERTY_VIRTUAL_HOST,
            PROPERTY_PORT, PROPERTY_USER_NAME, PROPERTY_PASSWORD,
            PROPERTY_MANAGEMENT_HOST, PROPERTY_MANAGEMENT_PORT,
            PROPERTY_MANAGEMENT_USER_NAME, PROPERTY_MANAGEMENT_PASSWORD,
            PROPERTY_MANAGEMENT_PATH, PROPERTY_MANAGEMENT_SSL
    );

    public RabbitMQMessagingProviderConfiguration() {
        super(CONFIGURATION_NAME, PROPERTIES);
    }

    public RabbitMQMessagingProviderConfiguration(
            String host, String vhost, int port, String username, String password,
            String managementHost, int managementPort, String managementUserName,
            String managementPassword, String managementPath, boolean managementSSL) {
        this();
        setPropertyValues(propArrayToMap(new String[]{
                PROPERTY_HOST.getName(), host,
                PROPERTY_PORT.getName(), Integer.toString(port),
                PROPERTY_VIRTUAL_HOST.getName(), vhost,
                PROPERTY_USER_NAME.getName(), username,
                PROPERTY_PASSWORD.getName(), password,
                PROPERTY_MANAGEMENT_HOST.getName(), managementHost,
                PROPERTY_MANAGEMENT_PORT.getName(), Integer.toString(managementPort),
                PROPERTY_MANAGEMENT_USER_NAME.getName(), managementUserName,
                PROPERTY_MANAGEMENT_PASSWORD.getName(), managementPassword,
                PROPERTY_MANAGEMENT_PATH.getName(), managementPath,
                PROPERTY_MANAGEMENT_SSL.getName(), Boolean.toString(managementSSL)
        }));
    }

    public String getHost() {
        return getProperty(PROPERTY_HOST.getName());
    }

    public String getVirtualHost() {
        return getProperty(PROPERTY_VIRTUAL_HOST.getName());
    }

    public int getPort() {
        return Integer.parseInt(getProperty(PROPERTY_PORT.getName()));
    }

    public String getUserName() {
        return getProperty(PROPERTY_USER_NAME.getName());
    }

    public String getPassword() {
        return getProperty(PROPERTY_PASSWORD.getName());
    }

    public String getManagementHost() {
        return getProperty(PROPERTY_MANAGEMENT_HOST.getName());
    }

    public int getManagementPort() {
        String prop = getProperty(PROPERTY_MANAGEMENT_PORT.getName());
        if (prop == null) {
            return -1;
        }
        return Integer.parseInt(prop);
    }

    public String getManagementUserName() {
        return getProperty(PROPERTY_MANAGEMENT_USER_NAME.getName());
    }

    public String getManagementPassword() {
        return getProperty(PROPERTY_MANAGEMENT_PASSWORD.getName());
    }

    public String getManagementPath() {
        return getProperty(PROPERTY_MANAGEMENT_PATH.getName());
    }

    public boolean isManagementSSL() {
        String prop = getProperty(PROPERTY_MANAGEMENT_SSL.getName());
        if (prop == null) {
            return false;
        }
        return Boolean.parseBoolean(prop);
    }

    @Override
    public String getMessagingNode() {
        return getHost();
    }

}
