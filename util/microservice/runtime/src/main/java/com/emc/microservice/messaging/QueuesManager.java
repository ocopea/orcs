// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging;

import com.emc.microservice.Context;
import com.emc.microservice.blobstore.BlobStoreConfiguration;
import com.emc.microservice.blobstore.BlobStoreDescriptor;
import com.emc.microservice.resource.AbstractResourceManager;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created with love by liebea on 5/26/2014.
 * This class manages the receivers for consuming input messages into the micro-service
 */
public class QueuesManager extends
                           AbstractResourceManager<InputQueueDescriptor, InputQueueConfiguration, ManagedInputQueue> {

    public QueuesManager(Logger logger, List<InputQueueDescriptor> managedQueueDescriptors) {
        super(managedQueueDescriptors, logger);
    }

    @Override
    public ManagedInputQueueImpl initializeResource(
            InputQueueDescriptor queueDescriptor,
            InputQueueConfiguration inputQueueConfiguration,
            Context context) {

        // Reading global messaging configuration
        MessagingProviderConfiguration messagingProviderConfiguration = Objects.requireNonNull(
                resourceProvider.getServiceRegistryApi().getMessagingProviderConfiguration(
                        resourceProvider.getMessagingConfigurationClass(),
                        MessagingProviderConfiguration.DEFAULT_MESSAGING_SYSTEM_NAME),
                "Messaging system not available from registry required for initializing queues");

        // Reading queue configuration
        QueueConfiguration queueConfiguration = Objects.requireNonNull(
                resourceProvider.getServiceRegistryApi().getQueueConfiguration(
                        resourceProvider.getQueueConfigurationClass(),
                        inputQueueConfiguration.getInputQueueURI(),
                        context),
                "Queue not found in registry " + inputQueueConfiguration.getInputQueueURI());

        // Reading dead letter queue configurations
        List<String> dlqURIs = inputQueueConfiguration.getDeadLetterQueues();
        Map<String, QueueConfiguration> dlqConfigurations = new HashMap<>(dlqURIs.size());
        for (String dlqURI : dlqURIs) {
            dlqConfigurations.put(dlqURI, Objects.requireNonNull(
                    resourceProvider.getServiceRegistryApi().getQueueConfiguration(
                            resourceProvider.getQueueConfigurationClass(), dlqURI, context),
                    "Queue not found in registry " + dlqURI));
        }

        // When queue configured to read via blobstore, verifying blobstore is initialized
        verifyBlobStoreIsInitialized(queueConfiguration.getBlobstoreName(), context);

        List<QueueReceiverImpl> receivers = new ArrayList<>(inputQueueConfiguration.getNumberOfListeners());
        MessageListener listener = MessageListenerFactory.createMessageListener(
                queueDescriptor.getMessageListener(),
                context);

        ManagedInputQueueImpl managedQueue = new ManagedInputQueueImpl(
                queueDescriptor,
                inputQueueConfiguration,
                receivers);

        // Creating message listeners according to the concurrency level requested
        for (int i = 0; i < inputQueueConfiguration.getNumberOfListeners(); ++i) {
            // Initializing the message listener
            final int ordinal = i + 1;
            ManagedMessageListener messageListener = new ManagedMessageListenerImpl(
                    context,
                    listener,
                    managedQueue,
                    ordinal);

            @SuppressWarnings("unchecked")
            QueueReceiverImpl currReceiver = resourceProvider.createQueueReceiver(
                    messagingProviderConfiguration,
                    managedQueue.getConfiguration(),
                    queueConfiguration,
                    dlqConfigurations,
                    messageListener,
                    context,
                    queueDescriptor.getName() + "#" + ordinal);
            try {
                currReceiver.init();
                receivers.add(currReceiver);
            } catch (Exception e) {
                logger.warn("Could not initialize queue receiver: {}", e.getMessage());
                logger.debug("Could not initialize queue receiver. Trace: {}", e);
            }

        }

        logger.debug(
                "Initialized {} Consumers on queue {}, ({})",
                receivers.size(),
                queueDescriptor.getQueueDescription(),
                inputQueueConfiguration.toString());

        return managedQueue;

    }

    @Override
    public void postInitResource(
            InputQueueDescriptor resourceDescriptor,
            InputQueueConfiguration resourceConfiguration,
            ManagedInputQueue initializedResource,
            Context context) {
    }

    private synchronized void verifyBlobStoreIsInitialized(String blobstoreName, Context context) {
        if (blobstoreName != null && !blobstoreName.isEmpty()) {
            if (!context.getBlobStoreManager().hasResource(blobstoreName)) {
                // Getting blobstore configuration from registry
                BlobStoreConfiguration messagingBlobStoreConfiguration =
                        resourceProvider.getServiceRegistryApi().getBlobStoreConfiguration(
                                resourceProvider.getBlobStoreConfigurationClass(),
                                blobstoreName);

                context.getBlobStoreManager().addResourceDynamically(
                        new BlobStoreDescriptor(blobstoreName),
                        messagingBlobStoreConfiguration,
                        context);
            }
        }
    }

    @Override
    public String getResourceTypeNamePlural() {
        return "Managed Messaging Input Queues";
    }

    @Override
    public String getResourceTypeName() {
        return "Managed Messaging Input queue";
    }

    @Override
    public void pauseResource(ManagedInputQueue resourceToPause) {
        List<QueueReceiver> activeQueues = resourceToPause.getReceivers();
        int successCount = 0;
        for (QueueReceiver currReceiver : activeQueues) {
            try {
                currReceiver.pause();
                successCount++;
            } catch (Exception ignored) {
                logger.error("exception while pausing receiver for {}",
                        resourceToPause.getDescriptor().getName(), ignored);
            }
        }
        logger.debug("Paused listening on {} out of {} consumers on queue {}",
                successCount, activeQueues.size(), resourceToPause.getQueueName());
    }

    @Override
    public void startResource(ManagedInputQueue resourceToStart) {
        List<QueueReceiver> activeQueues = resourceToStart.getReceivers();
        int successCount = 0;
        for (QueueReceiver currReceiver : activeQueues) {
            try {
                currReceiver.start();
                successCount++;
            } catch (Exception ignored) {
                logger.error(
                        "exception while starting receiver for {}",
                        resourceToStart.getDescriptor().getName(),
                        ignored);
            }
        }
        logger.info(
                "Started {} out of {} consumers on queue {}, ({})",
                successCount,
                activeQueues.size(),
                resourceToStart.getQueueName(),
                resourceToStart.getConfiguration().toString());
    }

    @Override
    public Class<InputQueueConfiguration> getResourceConfigurationClass() {
        return InputQueueConfiguration.class;
    }

    /**
     * Cleaning up all open connections
     */
    @Override
    public void cleanUpResource(ManagedInputQueue resourceToCleanUp) {
        List<QueueReceiver> activeQueues = resourceToCleanUp.getReceivers();
        int successCount = 0;
        for (QueueReceiver currReceiver : activeQueues) {
            try {
                currReceiver.cleanUp();
                successCount++;
            } catch (Exception ignored) {
                logger.error("exception while cleaning up resources", ignored);
            }
        }
        logger.info(
                "Closed {} out of {} consumers on queue {}, ({})",
                successCount,
                activeQueues.size(),
                resourceToCleanUp.getQueueName(),
                resourceToCleanUp.getConfiguration().toString());
    }

    @Override
    public Class<InputQueueDescriptor> getDescriptorClass() {
        return InputQueueDescriptor.class;
    }
}
