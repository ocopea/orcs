// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.dpa.dev.messaging;

import com.emc.microservice.messaging.MessagingStatsConnection;
import com.emc.microservice.messaging.MessagingStatsResourceDescriptor;
import com.emc.microservice.messaging.QueueConfiguration;
import com.emc.microservice.messaging.QueueStats;
import com.emc.microservice.registry.ServiceRegistryApi;

/**
 * Created by liebea on 2/22/15.
 * Drink responsibly
 */
public class DevMessagingStatsConnection extends
                                         MessagingStatsConnection<
                                                 DevMessagingProviderConfiguration,
                                                 QueueConfiguration> {

    private final DevMessagingServer messagingServer;

    protected DevMessagingStatsConnection(
            MessagingStatsResourceDescriptor descriptor,
            DevMessagingProviderConfiguration messagingConfiguration,
            ServiceRegistryApi registryAPI,
            DevMessagingServer messagingServer) {
        super(descriptor, messagingConfiguration, registryAPI,
                DevMessagingProviderConfiguration.class, QueueConfiguration.class);
        this.messagingServer = messagingServer;
    }

    @Override
    protected QueueStats getQueueStats(
            String queueName,
            DevMessagingProviderConfiguration destinationConfiguration,
            QueueConfiguration devQueueConfiguration) {
        DevMessagingServer.DevMessagingStats devMessageStats = messagingServer.getMessageStats(queueName);
        return new QueueStats(
                devMessageStats.getName(),
                devMessageStats.getMessagesInQueue(),
                devMessageStats.getMessagesSinceRestart());
    }
}
