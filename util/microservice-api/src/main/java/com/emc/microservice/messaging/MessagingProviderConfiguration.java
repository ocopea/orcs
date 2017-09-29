// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2015-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.messaging;

import com.emc.microservice.resource.ResourceConfiguration;
import com.emc.microservice.resource.ResourceConfigurationProperty;

import java.util.List;

/**
 * Created by liebea on 5/25/15.
 * Drink responsibly
 */
public abstract class MessagingProviderConfiguration extends ResourceConfiguration {
    public static final String DEFAULT_MESSAGING_SYSTEM_NAME = "default-messaging";

    protected MessagingProviderConfiguration(String configurationName, List<ResourceConfigurationProperty> properties) {
        super(configurationName, properties);
    }

    public abstract String getMessagingNode();
}
