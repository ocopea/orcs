// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2015 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.messaging.error.management.model;

/**
 * @author nivenb
 */
public class Queue {

    // NOTE: There are many other fields returned in the REST API. We can add more to the model as we need them

    private final String name;

    private final String virtualHost;

    private final boolean durable;

    private final boolean autoDelete;

    private final int consumers;

    public Queue(String name, String virtualHost, boolean durable, boolean autoDelete, int consumers) {
        this.name = name;
        this.virtualHost = virtualHost;
        this.durable = durable;
        this.autoDelete = autoDelete;
        this.consumers = consumers;
    }

    public String getName() {
        return name;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public boolean isDurable() {
        return durable;
    }

    public boolean isAutoDelete() {
        return autoDelete;
    }

    public int getConsumers() {
        return consumers;
    }
}
