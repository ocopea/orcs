// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging;

import com.emc.microservice.registry.ServiceRegistryApi;
import com.emc.microservice.resource.AbstractManagedResource;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;

import java.util.Objects;

/**
 * Created by liebea on 2/22/15.
 * Drink responsibly
 */
public abstract class MessagingStatsConnection<
        /**
         * Global Messaging configuration required to access messaging system like port location etc
         * This Configuration class needs to be implemented for each stack supporting messaging stats
         */
        MessagingConfT extends MessagingProviderConfiguration,
        /**
         * Destination configuration class supported by the
         */
        QueueConfT extends QueueConfiguration>
        extends AbstractManagedResource<MessagingStatsResourceDescriptor, MessagingConfT> {

    private final ServiceRegistryApi serviceRegistryApi;
    private final MessagingConfT messagingConfiguration;
    private final Class<MessagingConfT> messagingConfClass;
    private final Class<QueueConfT> queueConfClass;

    protected MessagingStatsConnection(
            MessagingStatsResourceDescriptor descriptor,
            MessagingConfT messagingConfiguration,
            ServiceRegistryApi registryAPI,
            Class<MessagingConfT> messagingConfClass,
            Class<QueueConfT> queueConfClass
    ) {
        super(descriptor, messagingConfiguration);
        this.serviceRegistryApi = registryAPI;
        this.messagingConfiguration = messagingConfiguration;
        this.queueConfClass = queueConfClass;
        this.messagingConfClass = messagingConfClass;
    }

    protected MessagingConfT getMessagingConfiguration() {
        return messagingConfiguration;
    }

    @NoJavadoc
    public QueueStats getQueueStats(String queueName) {
        // todo: 1: need to somewhere store global service tags for this...
        // todo: 2: maybe cache registry results so we won't have to do that all the time? or leave caching to registry?
        //         though we know it is a queue and queue configurations won't change that often...
        MessagingConfT messagingConf =
                Objects.requireNonNull(
                        serviceRegistryApi.getMessagingProviderConfiguration(
                                messagingConfClass,
                                MessagingProviderConfiguration.DEFAULT_MESSAGING_SYSTEM_NAME),
                        "Could not find required messaging system configuration for destination " + queueName);

        QueueConfT queueConf = Objects.requireNonNull(
                serviceRegistryApi.getQueueConfiguration(queueConfClass, queueName, null),
                "Could not find required destination configuration for destination " + queueName);

        return getQueueStats(queueName, messagingConf, queueConf);
    }

    protected abstract QueueStats getQueueStats(
            String queueName,
            MessagingConfT destinationConfiguration,
            QueueConfT queueConf);
}
