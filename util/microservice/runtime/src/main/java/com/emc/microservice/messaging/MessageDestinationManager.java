// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging;

import com.emc.microservice.Context;
import com.emc.microservice.blobstore.BlobStoreConfiguration;
import com.emc.microservice.blobstore.BlobStoreDescriptor;
import com.emc.microservice.resource.AbstractResourceManager;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;

/**
 * Created with love by liebea on 5/26/2014.
 */
public class MessageDestinationManager
        extends AbstractResourceManager<DestinationDescriptor, DestinationConfiguration, ManagedMessageDestination> {

    public MessageDestinationManager(Logger logger, List<DestinationDescriptor> microServiceDestinationDescriptors) {
        super(microServiceDestinationDescriptors, logger);
    }

    @Override
    public String getResourceTypeNamePlural() {
        return "Managed Messaging Destinations";
    }

    @Override
    public String getResourceTypeName() {
        return "Managed Messaging Destination";
    }

    @Override
    public ManagedMessageDestination initializeResource(
            DestinationDescriptor resourceDescriptor,
            DestinationConfiguration destinationConfiguration,
            Context context) {

        // Reading global messaging configuration
        MessagingProviderConfiguration messagingProviderConfiguration = Objects.requireNonNull(
                resourceProvider.getServiceRegistryApi().getMessagingProviderConfiguration(
                        resourceProvider.getMessagingConfigurationClass(),
                        MessagingProviderConfiguration.DEFAULT_MESSAGING_SYSTEM_NAME),
                "Messaging system not available from registry required for initializing destinations");

        // Reading queue configuration
        QueueConfiguration queueConfiguration = Objects.requireNonNull(
                resourceProvider.getServiceRegistryApi().getQueueConfiguration(
                        resourceProvider.getQueueConfigurationClass(),
                        destinationConfiguration.getDestinationQueueURI(),
                        context),
                "Queue not found in registry " + destinationConfiguration.getDestinationQueueURI());

        // In case message should be sent via blobstore, making sure blobstore is initialized
        verifyBlobStoreIsInitialized(queueConfiguration.getBlobstoreName(), context);

        @SuppressWarnings("unchecked")
        RuntimeMessageSender runtimeMessageSender = resourceProvider.getMessageSender(
                messagingProviderConfiguration,
                destinationConfiguration,
                queueConfiguration,
                context);
        return new ManagedMessageDestinationImpl(
                resourceDescriptor,
                destinationConfiguration,
                new MessageSenderImpl(
                        runtimeMessageSender,
                        context.getSerializationManager(),
                        resourceProvider,
                        context));
    }

    @Override
    public void postInitResource(
            DestinationDescriptor resourceDescriptor,
            DestinationConfiguration resourceConfiguration,
            ManagedMessageDestination initializedResource,
            Context context) {
    }

    private synchronized void verifyBlobStoreIsInitialized(String blobstoreName, Context context) {
        if (blobstoreName != null && !blobstoreName.isEmpty()) {
            if (!context.getBlobStoreManager().hasResource(blobstoreName)) {
                // Getting blobstore configuration from registry
                BlobStoreConfiguration messagingBlobStoreConfiguration = Objects.requireNonNull(
                        resourceProvider
                                .getServiceRegistryApi()
                                .getBlobStoreConfiguration(
                                        resourceProvider.getBlobStoreConfigurationClass(),
                                        blobstoreName),
                        "No configuration found for blobstore " + blobstoreName + " while loading micro service " +
                                context.getMicroServiceBaseURI());
                context
                        .getBlobStoreManager()
                        .addResourceDynamically(
                                new BlobStoreDescriptor(blobstoreName),
                                messagingBlobStoreConfiguration,
                                context);
            }
        }
    }

    @Override
    public void cleanUpResource(ManagedMessageDestination resourceToCleanUp) {
        // nothing to cleanup - we're not caching the connections in this implementation
    }

    @Override
    public void pauseResource(ManagedMessageDestination resourceToPause) {
    }

    @Override
    public void startResource(ManagedMessageDestination resourceToStart) {
        // Nothing to do, started on init phase will be available as service request message senders
    }

    @Override
    public Class<DestinationConfiguration> getResourceConfigurationClass() {
        return DestinationConfiguration.class;
    }

    @NoJavadoc
    public MessageSender getMessageSender(String destinationName) {
        ManagedMessageDestination messageDestination = managedResources.get(destinationName);
        if (messageDestination == null) {
            throw new IllegalArgumentException("Invalid destination name requested " + destinationName);
        }
        return messageDestination.getMessageSender();
    }

    @Override
    public Class<DestinationDescriptor> getDescriptorClass() {
        return DestinationDescriptor.class;
    }
}
