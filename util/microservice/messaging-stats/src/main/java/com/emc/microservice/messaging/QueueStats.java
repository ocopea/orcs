// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging;

/**
 * Created by liebea on 2/22/15.
 * Drink responsibly
 */
public class QueueStats {
    private final String name;
    private final long numberOfMessages;
    private final long totalMessagesSinceStartup;

    /***
     * Required by  jackson
     */
    private QueueStats() {
        this(null, 0L, 0L);
    }

    public QueueStats(String name, long numberOfMessages, long totalMessagesSinceStartup) {
        this.name = name;
        this.numberOfMessages = numberOfMessages;
        this.totalMessagesSinceStartup = totalMessagesSinceStartup;
    }

    public String getName() {
        return name;
    }

    public long getNumberOfMessages() {
        return numberOfMessages;
    }

    public long getTotalMessagesSinceStartup() {
        return totalMessagesSinceStartup;
    }
}
