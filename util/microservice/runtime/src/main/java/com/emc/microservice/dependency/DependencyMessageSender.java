// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.dependency;

import com.emc.microservice.messaging.MessageRoutingPlan;
import com.emc.microservice.messaging.MessageSender;
import com.emc.microservice.messaging.MessageWriter;

import java.util.Map;
import java.util.Objects;

/**
 * Created with true love by liebea on 10/14/2014.
 */
public class DependencyMessageSender implements MessageSender {

    private final MessageSender messageSender;
    private final Map<String, RoutInfo> routingTable;

    public DependencyMessageSender(
            String dependentServiceURI,
            MessageSender messageSender,
            Map<String, RoutInfo> routingTable) {
        Objects.requireNonNull(dependentServiceURI, "Must provide dependentServiceURI");
        this.messageSender = Objects.requireNonNull(messageSender, "Must provide message sender");
        this.routingTable = routingTable;
        if (routingTable != null && !routingTable.isEmpty()) {
            Objects.requireNonNull(
                    routingTable.get(dependentServiceURI),
                    "Routing table must contain the dependent service destination " + dependentServiceURI);
        }
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
        MessageRoutingPlan routingPlan = getMessageRoutingPlan(messageRoutingPlan);
        this.messageSender.streamMessage(messageWriter, messageHeaders, messageContext, routingPlan, messageGroup);
    }

    @Override
    public <T> void sendMessage(Class<T> format, T objectForSerialization, Map<String, String> messageHeaders) {
        sendMessage(format, objectForSerialization, messageHeaders, null, null, null);
    }

    @Override
    public <T> void sendMessage(
            Class<T> format,
            T objectForSerialization,
            Map<String, String> messageHeaders,
            String messageGroup) {
        sendMessage(format, objectForSerialization, messageHeaders, null, null, messageGroup);
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
            T objectForSerialization,
            Map<String, String> messageHeaders,
            Map<String, String> messageContext,
            MessageRoutingPlan messageRoutingPlan) {
        sendMessage(format, objectForSerialization, messageHeaders, messageContext, messageRoutingPlan, null);
    }

    @Override
    public <T> void sendMessage(
            Class<T> format,
            T objectForSerialization,
            Map<String, String> messageHeaders,
            Map<String, String> messageContext,
            MessageRoutingPlan messageRoutingPlan,
            String messageGroup) {
        MessageRoutingPlan routingPlan = getMessageRoutingPlan(messageRoutingPlan);
        this.messageSender.sendMessage(
                format,
                objectForSerialization,
                messageHeaders,
                messageContext,
                routingPlan,
                messageGroup);
    }

    private MessageRoutingPlan getMessageRoutingPlan(MessageRoutingPlan messageRoutingPlan) {
        MessageRoutingPlan.Builder builder = MessageRoutingPlan.builder();
        // In case of async callback we need to supply destination configuration
        if (routingTable != null && !routingTable.isEmpty()) {

            // Initializing message routing via context
            for (Map.Entry<String, RoutInfo> currRoute : routingTable.entrySet()) {
                builder.withRoute(
                        currRoute.getKey(),
                        currRoute.getValue().getDestinationConfiguration().getDestinationQueueURI(),
                        currRoute.getValue().getDestinationConfiguration().getBlobNamespace(),
                        currRoute.getValue().getDestinationConfiguration().getBlobKeyHeaderName(),
                        currRoute.getValue().getDestinationConfiguration().isLogContentWhenInDebug());
            }
        }

        // Overwrite with manually set routing plan if exists
        if (messageRoutingPlan != null) {
            for (Map.Entry<String, MessageRoutingPlan.MessagingRoutInfo> currRoute : messageRoutingPlan
                    .getRoutingTable()
                    .entrySet()) {
                builder.withRoute(
                        currRoute.getKey(),
                        currRoute.getValue().getDestinationConfiguration().getDestinationQueueURI(),
                        currRoute.getValue().getDestinationConfiguration().getBlobNamespace(),
                        currRoute.getValue().getDestinationConfiguration().getBlobKeyHeaderName(),
                        currRoute.getValue().getDestinationConfiguration().isLogContentWhenInDebug());
            }
        }
        return builder.build();
    }
}
