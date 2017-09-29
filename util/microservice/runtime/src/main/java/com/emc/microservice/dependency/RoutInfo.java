// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.dependency;

import com.emc.microservice.messaging.DestinationConfiguration;
import com.emc.microservice.messaging.QueueConfiguration;

/**
 * Created by liebea on 6/4/15.
 * Drink responsibly
 */
public class RoutInfo {
    private final DestinationConfiguration destinationConfiguration;
    private final QueueConfiguration queueConfiguration;

    public RoutInfo(DestinationConfiguration destinationConfiguration, QueueConfiguration queueConfiguration) {
        this.destinationConfiguration = destinationConfiguration;
        this.queueConfiguration = queueConfiguration;
    }

    public DestinationConfiguration getDestinationConfiguration() {
        return destinationConfiguration;
    }

    public QueueConfiguration getQueueConfiguration() {
        return queueConfiguration;
    }
}
