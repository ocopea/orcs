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
public class Binding {

    private final String source;

    private final String virtualHost;

    private final String destination;

    private final String destinationType;

    private final String routingKey;

    private final String propertiesKey;

    public Binding(
            String source,
            String virtualHost,
            String destination,
            String destinationType,
            String routingKey,
            String propertiesKey) {
        this.source = source;
        this.virtualHost = virtualHost;
        this.destination = destination;
        this.destinationType = destinationType;
        this.routingKey = routingKey;
        this.propertiesKey = propertiesKey;
    }

    public String getSource() {
        return source;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public String getDestination() {
        return destination;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getPropertiesKey() {
        return propertiesKey;
    }

    @Override
    public String toString() {
        return "Binding{" +
                "source='" + source + '\'' +
                ", virtualHost='" + virtualHost + '\'' +
                ", destination='" + destination + '\'' +
                ", destinationType='" + destinationType + '\'' +
                ", routingKey='" + routingKey + '\'' +
                ", propertiesKey='" + propertiesKey + '\'' +
                '}';
    }
}
