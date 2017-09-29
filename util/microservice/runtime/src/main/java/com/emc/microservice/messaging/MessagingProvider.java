// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging;

import com.emc.microservice.Context;

import java.util.Map;

/**
 * Provides messaging and queues services. Implementations should be registered in META-INF/services.
 * TODO: consider breaking apart or significantly changing API.
 */
public interface MessagingProvider<
        QueueConfT extends QueueConfiguration,
        MessageConfT extends MessagingProviderConfiguration> {

    RuntimeMessageSender getMessageSender(
            MessageConfT messageConfiguration,
            DestinationConfiguration destinationConfiguration,
            QueueConfT queueConfiguration,
            Context context);

    void createQueue(
            MessageConfT messagingConfiguration,
            QueueConfT queueConf);

    QueueReceiverImpl createQueueReceiver(
            MessageConfT messagingConfiguration,
            InputQueueConfiguration inputQueueConfiguration,
            QueueConfT queueConf,
            Map<String, QueueConfT> deadLetterQueueConfs,
            ManagedMessageListener messageListener,
            Context context,
            String consumerName);

    Class<QueueConfT> getQueueConfClass();

    Class<MessageConfT> getMessageConfClass();
}
