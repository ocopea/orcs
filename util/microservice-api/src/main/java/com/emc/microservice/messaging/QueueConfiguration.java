// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.messaging;

import com.emc.microservice.resource.ResourceConfiguration;
import com.emc.microservice.resource.ResourceConfigurationProperty;

import java.util.List;

/**
 * Created with love by liebea on 6/1/2014.
 */
public abstract class QueueConfiguration extends ResourceConfiguration {
    public enum MessageDestinationType {
        QUEUE,
        TOPIC
    }

    protected QueueConfiguration(String configurationName, List<ResourceConfigurationProperty> properties) {
        super(configurationName, properties);
    }

    public abstract String getBlobstoreName();

    public abstract MessageDestinationType getMessageDestinationType();

    public abstract boolean isGzip();

}
