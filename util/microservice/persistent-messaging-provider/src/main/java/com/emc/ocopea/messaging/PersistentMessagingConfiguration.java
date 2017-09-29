// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.messaging;

import com.emc.microservice.messaging.MessagingProviderConfiguration;
import com.emc.microservice.resource.ResourceConfigurationProperty;
import com.emc.microservice.resource.ResourceConfigurationPropertyType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PersistentMessagingConfiguration extends MessagingProviderConfiguration {
    private static final String CONFIGURATION_NAME = "Persistent Messaging Configuration";

    private static final ResourceConfigurationProperty PROPERTY_DATASOURCE_NAME =
            new ResourceConfigurationProperty(
                    "datasourceName",
                    ResourceConfigurationPropertyType.STRING,
                    "name of the datasource to use",
                    false,
                    false);

    private static final ResourceConfigurationProperty PERSIST_MESSAGES =
            new ResourceConfigurationProperty(
                    "persistMessage",
                    ResourceConfigurationPropertyType.BOOLEAN,
                    "Whether or not to persist the messages",
                    false,
                    false);

    private static final List<ResourceConfigurationProperty> PROPERTIES =
            Arrays.asList(PROPERTY_DATASOURCE_NAME, PERSIST_MESSAGES);

    public PersistentMessagingConfiguration() {
        super(CONFIGURATION_NAME, PROPERTIES);
    }

    public PersistentMessagingConfiguration(String datasourceName, boolean persistMessages) {
        this();
        setPropertyValues(propArrayToMap(new String[]{
                PROPERTY_DATASOURCE_NAME.getName(), datasourceName,
                PERSIST_MESSAGES.getName(), Boolean.toString(persistMessages)

        }));
    }

    @Override
    public String getMessagingNode() {
        return "localhost";
    }

    public String getDatasourceName() {
        return getProperty(PROPERTY_DATASOURCE_NAME.getName());
    }

    public boolean isPersistMessages() {
        final String strBool = getProperty(PERSIST_MESSAGES.getName());
        return strBool == null ? true : Boolean.valueOf(strBool);
    }
}
