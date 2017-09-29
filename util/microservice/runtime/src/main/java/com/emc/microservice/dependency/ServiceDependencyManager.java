// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.dependency;

import com.emc.microservice.Context;
import com.emc.microservice.MicroserviceIdentifier;
import com.emc.microservice.ServiceConfig;
import com.emc.microservice.blobstore.BlobStoreConfiguration;
import com.emc.microservice.blobstore.BlobStoreDescriptor;
import com.emc.microservice.messaging.DestinationConfiguration;
import com.emc.microservice.messaging.MessageSender;
import com.emc.microservice.messaging.MessageSenderImpl;
import com.emc.microservice.messaging.MessagingProviderConfiguration;
import com.emc.microservice.messaging.QueueConfiguration;
import com.emc.microservice.resource.AbstractResourceManager;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by liebea on 9/21/2014. Enjoy it
 * This class manages inter-service dependencies, initializes, checks health and clean up
 */
public class ServiceDependencyManager extends AbstractResourceManager<
        ServiceDependencyDescriptor,
        ServiceDependencyConfiguration,
        ManagedDependency> {

    public ServiceDependencyManager(List<ServiceDependencyDescriptor> descriptors, Logger logger) {
        super(descriptors, logger);
    }

    @Override
    public String getResourceTypeNamePlural() {
        return "External Service Dependencies";
    }

    @Override
    public String getResourceTypeName() {
        return "External Service Dependency";
    }

    @Override
    public void cleanUpResource(ManagedDependency resourceToCleanUp) {
        // nothing to clean up for now
    }

    @Override
    public void pauseResource(ManagedDependency resourceToPause) {
        // todo: pause
    }

    @Override
    public void startResource(ManagedDependency resourceToStart) {
    }

    @Override
    public Class<ServiceDependencyConfiguration> getResourceConfigurationClass() {
        return ServiceDependencyConfiguration.class;
    }

    @Override
    public ManagedDependency initializeResource(
            ServiceDependencyDescriptor serviceDependencyDescriptor,
            ServiceDependencyConfiguration serviceDependencyConfiguration,
            Context context) {
        // Looking up the service we depend on in service registry
        String destinationServiceShortName = serviceDependencyDescriptor.getDependentServiceIdentifier().getShortName();
        ServiceConfig dependentServiceConfig =
                Objects.requireNonNull(
                        resourceProvider.getServiceRegistryApi().getServiceConfig(destinationServiceShortName),
                        "Failed getting \"" + destinationServiceShortName +
                                "\" dependent service configuration from registry, " +
                                "required for service dependency from \"" +
                                context.getMicroServiceBaseURI() + "\"");

        MessageSender messageSender = null;
        switch (serviceDependencyDescriptor.getServiceDependencyType()) {
            case SYNC_CALL:
                // For sync call we don't need message sender
                break;
            default:

                // Building message sender for messaging
                messageSender = buildMessageSender(
                        serviceDependencyDescriptor,
                        context,
                        dependentServiceConfig,
                        destinationServiceShortName);
                break;
        }

        return new ManagedDependencyImpl(
                serviceDependencyDescriptor,
                serviceDependencyConfiguration,
                messageSender,
                dependentServiceConfig.getServiceURI(),
                context);
    }

    @Override
    public void postInitResource(
            ServiceDependencyDescriptor resourceDescriptor,
            ServiceDependencyConfiguration resourceConfiguration,
            ManagedDependency initializedResource,
            Context context) {
    }

    private DependencyMessageSender buildMessageSender(
            ServiceDependencyDescriptor serviceDependencyDescriptor,
            Context context,
            ServiceConfig dependentServiceConfiguration,
            String destinationServiceURI) {
        Map<String, RoutInfo> messageRoutingTable = new HashMap<>();

        String dependentServiceInputQueueURI =
                new MicroserviceIdentifier(dependentServiceConfiguration.getServiceURI()).getDefaultInputQueueName();
        QueueConfiguration dependentServiceQueueConfiguration =
                Objects.requireNonNull(
                        resourceProvider.getServiceRegistryApi().getQueueConfiguration(
                                resourceProvider.getQueueConfigurationClass(), dependentServiceInputQueueURI, context),
                        "Failed fetching dependent service queue configuration for queue " +
                                dependentServiceInputQueueURI);

        // In case message should be sent via blobstore, making sure blobstore is initialized
        verifyBlobStoreIsInitialized(dependentServiceQueueConfiguration.getBlobstoreName(), context);

        // Reading global messaging configuration
        MessagingProviderConfiguration messagingProviderConfiguration;

        Map<String, DestinationConfiguration> customDestinationConfiguration =
                serviceDependencyDescriptor.getCustomDestinationConfiguration();

        // Reading global messaging configuration
        messagingProviderConfiguration = Objects.requireNonNull(
                resourceProvider.getServiceRegistryApi().getMessagingProviderConfiguration(
                        resourceProvider.getMessagingConfigurationClass(),
                        MessagingProviderConfiguration.DEFAULT_MESSAGING_SYSTEM_NAME),
                "Messaging system not available from registry required for initializing dependencies");

        // In case we have an async callback then we initialize our message sender object with given conf
        if (serviceDependencyDescriptor.getServiceDependencyType() == ServiceDependencyType.ASYNC_CALL) {

            List<String> routingServices = serviceDependencyDescriptor.getMessageRoutingTable();

            // Getting all queues for non-last-of-the-chain services
            populateMessageRoutingTable(context, messageRoutingTable, routingServices, serviceDependencyDescriptor);

            // For the last service in chain - adding the callback queue
            String lastServiceOnChainShortName = routingServices.get(routingServices.size() - 1);
            String dependencyCallbackQueueName = context
                    .getServiceDescriptor()
                    .getIdentifier()
                    .getDependencyCallbackQueueName(lastServiceOnChainShortName);
            QueueConfiguration dependencyCallbackQueueConfiguration =
                    Objects.requireNonNull(
                            resourceProvider.getServiceRegistryApi().getQueueConfiguration(
                                    resourceProvider.getQueueConfigurationClass(),
                                    dependencyCallbackQueueName,
                                    context),
                            "Service callback queue not registered as destination " + dependencyCallbackQueueName);

            // Adding configuration to routing table
            DestinationConfiguration destinationConfiguration = getDestinationConfiguration(
                    customDestinationConfiguration,
                    lastServiceOnChainShortName,
                    dependencyCallbackQueueName);
            messageRoutingTable.put(
                    lastServiceOnChainShortName,
                    new RoutInfo(destinationConfiguration, dependencyCallbackQueueConfiguration));

        } else if (serviceDependencyDescriptor.getServiceDependencyType() == ServiceDependencyType.SEND_AND_FORGET) {
            // Getting all queues for services
            List<String> routingServices = serviceDependencyDescriptor.getMessageRoutingTable();

            populateMessageRoutingTable(context, messageRoutingTable, routingServices, serviceDependencyDescriptor);
        }

        //noinspection unchecked
        return new DependencyMessageSender(
                destinationServiceURI,
                new MessageSenderImpl(
                        resourceProvider.getMessageSender(
                                messagingProviderConfiguration,
                                getDestinationConfiguration(
                                        serviceDependencyDescriptor.getCustomDestinationConfiguration(),
                                        dependentServiceInputQueueURI,
                                        dependentServiceInputQueueURI),
                                dependentServiceQueueConfiguration,
                                context),
                        context.getSerializationManager(), resourceProvider, context),
                messageRoutingTable);
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

    private DestinationConfiguration getDestinationConfiguration(
            Map<String, DestinationConfiguration> customDestinationConfiguration,
            String lastServiceOnChainBaseURI,
            String dependencyCallbackQueueName) {
        DestinationConfiguration destinationConfiguration =
                customDestinationConfiguration.get(lastServiceOnChainBaseURI);
        if (destinationConfiguration == null) {
            destinationConfiguration = new DestinationConfiguration(dependencyCallbackQueueName, null, null, true);
        } else {
            destinationConfiguration = new DestinationConfiguration(
                    dependencyCallbackQueueName,
                    destinationConfiguration.getBlobNamespace(),
                    destinationConfiguration.getBlobKeyHeaderName(),
                    destinationConfiguration.isLogContentWhenInDebug());
        }
        return destinationConfiguration;
    }

    private void populateMessageRoutingTable(
            Context context,
            Map<String, RoutInfo> messageRoutingTable,
            List<String> routingServices,
            ServiceDependencyDescriptor serviceDependencyDescriptor) {
        for (int routIdx = 0; routIdx < routingServices.size() - 1; routIdx++) {
            String currServiceShortName = routingServices.get(routIdx);
            String nextServiceShortName = routingServices.get(routIdx + 1);

            // Verifying next service is available via registry
            ServiceConfig nextServiceConfig =
                    Objects.requireNonNull(
                            resourceProvider.getServiceRegistryApi().getServiceConfig(nextServiceShortName),
                            "Failed getting \"" + nextServiceShortName +
                                    " \" dependent service configuration from registry, " +
                                    "required in message routing via " +
                                    currServiceShortName);

            /*
            // todo:Amit again - add this to service config - whether it is a callback style etc...
            if (nextServiceConfig.getInputResourceType() !=
            RegisteredServiceConfiguration.RegisteredResourceType.messaging){
                throw new UnsupportedOperationException("Unsupported service route.
                can't chain \"" + nextServiceShortName + "\" to message route via \"" + currServiceShortName + "\"" +
                        "Since it does not support messaging input");
            }
            */

            String nextServiceQueueURI = new MicroserviceIdentifier(nextServiceShortName).getDefaultInputQueueName();
            QueueConfiguration nextQueueConfiguration =
                    Objects.requireNonNull(
                            resourceProvider.getServiceRegistryApi().getQueueConfiguration(
                                    resourceProvider.getQueueConfigurationClass(), nextServiceQueueURI, context),
                            "Failed receiving next chain queue configuration from registry for queue " +
                                    nextServiceQueueURI +
                                    " required as output for service " + currServiceShortName);

            // Adding configuration to routing table
            messageRoutingTable.put(
                    currServiceShortName,
                    new RoutInfo(getDestinationConfiguration(
                            serviceDependencyDescriptor.getCustomDestinationConfiguration(),
                            currServiceShortName,
                            nextServiceQueueURI), nextQueueConfiguration));
        }
    }

    @Override
    public Class<ServiceDependencyDescriptor> getDescriptorClass() {
        return ServiceDependencyDescriptor.class;
    }
}
