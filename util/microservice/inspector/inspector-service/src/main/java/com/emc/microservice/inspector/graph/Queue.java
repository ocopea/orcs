// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.inspector.graph;

import com.emc.microservice.messaging.QueueConfiguration;
import com.emc.microservice.messaging.QueueStats;

/**
 * Created by liebea on 2/22/15.
 * Drink responsibly
 */
public class Queue {
    private final String name;
    private final QueueConfiguration inputQueueConfiguration;
    private final QueueStats queueStats;

    public Queue(String name, QueueConfiguration inputQueueConfiguration, QueueStats queueStats) {
        this.name = name;
        this.inputQueueConfiguration = inputQueueConfiguration;
        this.queueStats = queueStats;
    }

    public String getName() {
        return name;
    }

    public QueueConfiguration getInputQueueConfiguration() {
        return inputQueueConfiguration;
    }

    public QueueStats getQueueStats() {
        return queueStats;
    }
}
