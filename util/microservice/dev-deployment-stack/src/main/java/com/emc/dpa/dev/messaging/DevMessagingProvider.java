// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.dpa.dev.messaging;

import com.emc.dpa.dev.DevQueueConfiguration;
import com.emc.microservice.Context;
import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.microservice.blobstore.BlobStoreConfiguration;
import com.emc.microservice.messaging.DestinationConfiguration;
import com.emc.microservice.messaging.InputQueueConfiguration;
import com.emc.microservice.messaging.ManagedMessageListener;
import com.emc.microservice.messaging.MessagingProvider;
import com.emc.microservice.messaging.QueueConfiguration;
import com.emc.microservice.messaging.QueueReceiverImpl;
import com.emc.microservice.messaging.RuntimeMessageSender;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.microservice.resource.ResourceProviderManager;

import java.util.Map;

/**
 * A Messaging provider for dev environment. This class contains an entanglement of messaging and queueing.
 * Should be redesigned.
 * <p>
 * TODO: move to a separate module (with META-INF/services file)? break up and redesign completely?
 */
public class DevMessagingProvider
        implements MessagingProvider<DevQueueConfiguration, DevMessagingProviderConfiguration> {

    private static BlobStoreAPI initBlobStoreAPI(
            String blobstoreName, ResourceProvider resourceProvider,
            Context context) {
        String name = blobstoreName == null ? DevMessagingServer.DEV_MESSAGING_BLOBSTORE_NAME : blobstoreName;
        BlobStoreConfiguration blobStoreConfiguration = resourceProvider.getServiceRegistryApi()
                .getBlobStoreConfiguration(resourceProvider.getBlobStoreConfigurationClass(), name);

        return resourceProvider.getBlobStore(blobStoreConfiguration, context);
    }

    @Override
    public RuntimeMessageSender getMessageSender(
            DevMessagingProviderConfiguration messageConfiguration,
            DestinationConfiguration destinationConfiguration,
            DevQueueConfiguration queueConfiguration,
            Context context) {
        return new DevMessageSender(
                DevMessagingServer.getInstance(),
                destinationConfiguration.getDestinationQueueURI(),
                initBlobStoreAPI(
                        queueConfiguration.getBlobstoreName(),
                        ResourceProviderManager.getResourceProvider(),
                        context),
                destinationConfiguration.getBlobNamespace(),
                destinationConfiguration.getBlobKeyHeaderName(),
                queueConfiguration.isGzip());
    }

    @Override
    public void createQueue(
            DevMessagingProviderConfiguration messagingConfiguration,
            DevQueueConfiguration devQueueConfiguration) {
        throw new UnsupportedOperationException("No!");
    }

    @Override
    public QueueReceiverImpl createQueueReceiver(
            DevMessagingProviderConfiguration messagingConfiguration,
            InputQueueConfiguration inputQueueConfiguration,
            DevQueueConfiguration queueConfiguration,
            Map<String, DevQueueConfiguration> deadLetterQueueConfs,
            ManagedMessageListener messageListener,
            Context context,
            String consumerName) {
        final DevMessagingServer devMessagingServer = DevMessagingServer.getInstance();
        Integer topicId = null;
        if (queueConfiguration.getMessageDestinationType() == QueueConfiguration.MessageDestinationType.TOPIC) {
            topicId = devMessagingServer.subscribeToTopic(inputQueueConfiguration.getInputQueueURI());
        }
        String blobstoreName = queueConfiguration.getBlobstoreName() == null ?
                DevMessagingServer.DEV_MESSAGING_BLOBSTORE_NAME :
                queueConfiguration.getBlobstoreName();
        return new DevQueueReceiver(
                inputQueueConfiguration,
                queueConfiguration,
                messageListener,
                context,
                devMessagingServer,
                topicId,
                initBlobStoreAPI(blobstoreName, ResourceProviderManager.getResourceProvider(), context),
                consumerName);

    }

    @Override
    public Class<DevQueueConfiguration> getQueueConfClass() {
        return DevQueueConfiguration.class;
    }

    @Override
    public Class<DevMessagingProviderConfiguration> getMessageConfClass() {
        return DevMessagingProviderConfiguration.class;
    }
}
