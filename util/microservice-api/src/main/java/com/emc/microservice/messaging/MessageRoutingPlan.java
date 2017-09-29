// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2015-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.messaging;

import com.emc.microservice.MicroserviceIdentifier;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by liebea on 6/28/15.
 * Drink responsibly
 */
public class MessageRoutingPlan {
    private final Map<String, MessagingRoutInfo> routingTable;

    protected MessageRoutingPlan(Map<String, MessagingRoutInfo> routingTable) {
        this.routingTable = routingTable;
    }

    public Map<String, MessagingRoutInfo> getRoutingTable() {
        return routingTable;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Builder() {
        }

        final Map<String, MessagingRoutInfo> routingTable = new LinkedHashMap<>();

        /**
         * Add route to builder
         */
        public Builder withRoute(String serviceURI,
                                 String destinationQueueURI,
                                 String blobStoreNameSpace,
                                 String blobstoreKeyHeaderName,
                                 boolean logInDebug) {
            this.routingTable.put(serviceURI,
                    new MessagingRoutInfo(
                            new DestinationConfiguration(
                                    destinationQueueURI,
                                    blobStoreNameSpace,
                                    blobstoreKeyHeaderName,
                                    logInDebug)));
            return this;
        }

        /***
         * Add route to builder
         */
        public Builder withRoute(String serviceURI, String destinationServiceShortName) {
            return withRoute(
                    serviceURI,
                    new MicroserviceIdentifier(destinationServiceShortName).getDefaultInputQueueName(),
                    null,
                    null,
                    true);
        }

        public MessageRoutingPlan build() {
            return new MessageRoutingPlan(routingTable);
        }
    }

    // TODO - remove this class and replace by DestinationConfiguration where it is used
    public static class MessagingRoutInfo {
        private final DestinationConfiguration destinationConfiguration;

        private MessagingRoutInfo(DestinationConfiguration destinationConfiguration) {
            this.destinationConfiguration = destinationConfiguration;
        }

        public DestinationConfiguration getDestinationConfiguration() {
            return destinationConfiguration;
        }

    }
}
