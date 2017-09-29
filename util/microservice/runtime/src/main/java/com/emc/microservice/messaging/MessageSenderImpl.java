// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging;

import com.emc.microservice.Context;
import com.emc.microservice.ContextImpl;
import com.emc.microservice.resource.ResourceConfiguration;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.microservice.serialization.SerializationManager;
import com.emc.microservice.serialization.SerializationWriter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by liebea on 6/28/15.
 * Drink responsibly
 */
public class MessageSenderImpl implements MessageSender {

    private final RuntimeMessageSender runtimeMessageSender;
    private final SerializationManager serializationManager;
    private final ResourceProvider resourceProvider;
    private final Context context;

    public MessageSenderImpl(
            RuntimeMessageSender runtimeMessageSender,
            SerializationManager serializationManager,
            ResourceProvider resourceProvider,
            Context context) {
        this.runtimeMessageSender = runtimeMessageSender;
        this.serializationManager = serializationManager;
        this.resourceProvider = resourceProvider;
        this.context = context;
    }

    @Override
    public void streamMessage(MessageWriter messageWriter, Map<String, String> messageHeaders) {
        streamMessage(messageWriter, messageHeaders, null, null, null);
    }

    @Override
    public void streamMessage(MessageWriter messageWriter, Map<String, String> messageHeaders, String messageGroup) {
        streamMessage(messageWriter, messageHeaders, null, null, messageGroup);
    }

    @Override
    public void streamMessage(
            MessageWriter messageWriter,
            Map<String, String> messageHeaders,
            Map<String, String> messageContext) {
        streamMessage(messageWriter, messageHeaders, messageContext, null, null);
    }

    @Override
    public void streamMessage(
            MessageWriter messageWriter,
            Map<String, String> messageHeaders,
            Map<String, String> messageContext,
            String messageGroup) {
        streamMessage(messageWriter, messageHeaders, messageContext, null, messageGroup);
    }

    @Override
    public void streamMessage(
            MessageWriter messageWriter,
            Map<String, String> messageHeaders,
            Map<String, String> messageContext,
            MessageRoutingPlan messageRoutingPlan) {
        streamMessage(messageWriter, messageHeaders, messageContext, messageRoutingPlan, null);
    }

    @Override
    public void streamMessage(
            MessageWriter messageWriter,
            Map<String, String> messageHeaders,
            Map<String, String> messageContext,
            MessageRoutingPlan messageRoutingPlan,
            String messageGroup) {

        // Adding Message routing plan to message context headers
        if (messageRoutingPlan != null) {

            // Expanding message context map in order to add additional routing values
            if (messageContext == null || messageContext.isEmpty()) {
                messageContext = new HashMap<>();
            } else {
                messageContext = new HashMap<>(messageContext);
            }

            // Adding each route instruction as a message context header
            StringBuilder messageRoutingPlanBuilder = new StringBuilder();
            for (Map.Entry<String, MessageRoutingPlan.MessagingRoutInfo> currRoute : messageRoutingPlan
                    .getRoutingTable()
                    .entrySet()) {

                // Getting actual queue configuration from registry
                String destinationQueueURI =
                        currRoute.getValue().getDestinationConfiguration().getDestinationQueueURI();
                QueueConfiguration dependentServiceQueueConfiguration =
                        Objects.requireNonNull(
                                resourceProvider.getServiceRegistryApi().getQueueConfiguration(
                                        resourceProvider.getQueueConfigurationClass(), destinationQueueURI, context),
                                "Failed retrieving queue configuration for " + destinationQueueURI +
                                        " required for sending message from " + currRoute.getKey() +
                                        " as message route");

                // Merging both destination and queue configurations
                Map<String, String> routeContextProperties = new HashMap<>();
                routeContextProperties.putAll(currRoute.getValue().getDestinationConfiguration().getPropertyValues());
                routeContextProperties.putAll(dependentServiceQueueConfiguration.getPropertyValues());

                // Serializing destination + queue configurations into message context header
                messageContext.put(
                        ContextImpl.MS_API_OUTPUT_QUEUE_NAME_HEADER + "_" + currRoute.getKey(),
                        ResourceConfiguration.propsToPersistentFormat(routeContextProperties));

                messageRoutingPlanBuilder.append(currRoute.getKey()).append(",");
            }

            if (messageRoutingPlanBuilder.length() > 0) {
                // remove last comma
                messageRoutingPlanBuilder.setLength(messageRoutingPlanBuilder.length() - 1);
            }

            // Add routing plan header
            messageContext.put(ContextImpl.MS_API_ROUTING_PLAN_HEADER, messageRoutingPlanBuilder.toString());
        }

        // Appending context as additional prefixed headers
        if (messageContext != null) {
            messageHeaders =
                    MessagingSerializationHelper.appendContextToHeadersUsingPrefix(messageHeaders, messageContext);
        }

        // Streaming the message
        runtimeMessageSender.streamMessage(messageWriter, messageHeaders, messageGroup);
    }

    @Override
    public <T> void sendMessage(Class<T> format, T objectForSerialization, Map<String, String> messageHeaders) {
        sendMessage(format, objectForSerialization, messageHeaders, Collections.<String, String>emptyMap(), null, null);
    }

    @Override
    public <T> void sendMessage(
            Class<T> format,
            T objectForSerialization,
            Map<String, String> messageHeaders,
            String messageGroup) {
        sendMessage(
                format,
                objectForSerialization,
                messageHeaders,
                Collections.<String, String>emptyMap(),
                null,
                messageGroup);
    }

    @Override
    public <T> void sendMessage(
            Class<T> format,
            T objectForSerialization,
            Map<String, String> messageHeaders,
            Map<String, String> messageContext) {
        sendMessage(format, objectForSerialization, messageHeaders, messageContext, null, null);
    }

    @Override
    public <T> void sendMessage(
            Class<T> format,
            T objectForSerialization,
            Map<String, String> messageHeaders,
            Map<String, String> messageContext,
            String messageGroup) {
        sendMessage(format, objectForSerialization, messageHeaders, messageContext, null, messageGroup);
    }

    @Override
    public <T> void sendMessage(
            Class<T> format,
            final T objectForSerialization,
            Map<String, String> messageHeaders,
            Map<String, String> messageContext,
            MessageRoutingPlan messageRoutingPlan) {
        sendMessage(format, objectForSerialization, messageHeaders, messageContext, messageRoutingPlan, null);
    }

    @Override
    public <T> void sendMessage(
            Class<T> format,
            final T objectForSerialization,
            Map<String, String> messageHeaders,
            Map<String, String> messageContext,
            MessageRoutingPlan messageRoutingPlan,
            String messageGroup) {
        final SerializationWriter<T> writer = serializationManager.getWriter(format);

        streamMessage(
                outputStream ->
                        writer.writeObject(objectForSerialization, outputStream),
                messageHeaders,
                messageContext,
                messageRoutingPlan,
                messageGroup
        );
    }
}
