// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.jmsl.dev.messaging;

import com.emc.microservice.messaging.MessagingProviderConfiguration;
import com.emc.microservice.resource.ResourceConfigurationProperty;

import java.util.Collections;

/**
 * Created by liebea on 2/22/15.
 * Drink responsibly
 */
public class DevMessagingProviderConfiguration extends MessagingProviderConfiguration {
    private static final String CONFIGURATION_NAME = "Dev Messaging Statistics";

    public DevMessagingProviderConfiguration() {
        super(CONFIGURATION_NAME, Collections.<ResourceConfigurationProperty>emptyList());
    }

    @Override
    public String getMessagingNode() {
        return "localhost";
    }
}
