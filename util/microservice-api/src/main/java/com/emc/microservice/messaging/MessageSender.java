// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.messaging;

import java.util.Map;

/**
 * Created with love by liebea on 6/22/2014.
 * Interface used to send messages between different services
 */
public interface MessageSender {

    /***
     * Stream message to remote service
     * @param messageWriter message writer to use when steaming the message
     * @param messageHeaders message headers to pass to the remote service
     */
    void streamMessage(MessageWriter messageWriter, Map<String, String> messageHeaders);

    /***
     * Stream message to remote service
     * @param messageWriter message writer to use when steaming the message
     * @param messageHeaders message headers to pass to the remote service
     * @param messageGroup Optional. Supply if using message grouping
     */
    void streamMessage(MessageWriter messageWriter, Map<String, String> messageHeaders, String messageGroup);

    /***
     * Stream message to remote service
     * @param messageWriter message writer to use when steaming the message
     * @param messageHeaders message headers to pass to the remote service
     *                       Data sent via messageHeaders should be small strings only
     * @param messageContext message context key value set. used to pass in all further communication chain between
     *                       services useful when using a callback on a remote service and we want to pass context to
     *                       the when the callback returns useful when there is more than one service on the call chain
     *                       and we want to pass information to services down in the chain Data sent via messageContext
     *                       should be small strings only, anything big should use blobstore and pass only reference
     */
    void streamMessage(MessageWriter messageWriter,
                       Map<String, String> messageHeaders,
                       Map<String, String> messageContext);

    /***
     * Stream message to remote service
     * @param messageWriter message writer to use when steaming the message
     * @param messageHeaders message headers to pass to the remote service
     *                       Data sent via messageHeaders should be small strings only
     * @param messageContext message context key value set. used to pass in all further communication chain between
     *                       services
     *                       useful when using a callback on a remote service and we want to pass context to the when
     *                       the callback returns useful when there is more than one service on the call chain and we
     *                       want to pass information to services down in the chain Data sent via messageContext should
     *                       be small strings only, anything big should use blobstore and pass only reference
     * @param messageGroup Optional. Supply if using message grouping
     */
    void streamMessage(MessageWriter messageWriter,
                       Map<String, String> messageHeaders,
                       Map<String, String> messageContext,
                       String messageGroup);

    /***
     * Stream message to remote service
     * @param messageWriter  message writer to use when steaming the message
     * @param messageHeaders message headers to pass to the remote service
     *                       Data sent via messageHeaders should be small strings only
     * @param messageContext message context key value set. used to pass in all further communication chain between
     *                       services
     *                       useful when using a callback on a remote service and we want to pass context to the when
     *                       the callback returns useful when there is more than one service on the call chain and we
     *                       want to pass information to services down in the chain Data sent via messageContext should
     *                       be small strings only, anything big should use blobstore and pass only reference
     * @param messageRoutingPlan route message via multiple hops using routing plan
     */
    void streamMessage(MessageWriter messageWriter,
                       Map<String, String> messageHeaders,
                       Map<String, String> messageContext,
                       MessageRoutingPlan messageRoutingPlan);

    /***
     * Stream message to remote service
     * @param messageWriter  message writer to use when steaming the message
     * @param messageHeaders message headers to pass to the remote service
     *                       Data sent via messageHeaders should be small strings only
     * @param messageContext message context key value set. used to pass in all further communication chain between
     *                       services
     *                       useful when using a callback on a remote service and we want to pass context to the when
     *                       the callback returns useful when there is more than one service on the call chain and we
     *                       want to pass information to services down in the chain Data sent via messageContext should
     *                       be small strings only, anything big should use blobstore and pass only reference
     * @param messageRoutingPlan route message via multiple hops using routing plan
     * @param messageGroup Optional. Supply if using message grouping
     */
    void streamMessage(MessageWriter messageWriter,
                       Map<String, String> messageHeaders,
                       Map<String, String> messageContext,
                       MessageRoutingPlan messageRoutingPlan,
                       String messageGroup);

    /***
     * Sends a message represented by a java POJO to a remote service, using micro-service serialization library
     * for serializing
     * @param format message object format. as registered in @see SerializationManager
     * @param objectForSerialization java object to send. will use registered serializer to format it
     * @param messageHeaders message headers to pass to the remote service
     *                       Data sent via messageHeaders should be small strings only
     */
    <T> void sendMessage(Class<T> format, T objectForSerialization, Map<String, String> messageHeaders);

    /***
     * Sends a message represented by a java POJO to a remote service, using micro-service serialization library
     * for serializing
     * @param format message object format. as registered in @see SerializationManager
     * @param objectForSerialization java object to send. will use registered serializer to format it
     * @param messageHeaders message headers to pass to the remote service
     *                       Data sent via messageHeaders should be small strings only
     * @param messageGroup Optional. Supply if using message grouping
     */
    <T> void sendMessage(Class<T> format,
                         T objectForSerialization,
                         Map<String, String> messageHeaders,
                         String messageGroup);

    /***
     * Sends a message represented by a java POJO to a remote service, using micro-service serialization library
     * for serializing
     * @param format message object format. as registered in @see SerializationManager
     * @param objectForSerialization java object to send. will use registered serializer to format it
     * @param messageHeaders message headers to pass to the remote service
     *                       Data sent via messageHeaders should be small strings only
     * @param messageContext message context key value set. used to pass in all further communication chain between
     *                       services
     *                       useful when using a callback on a remote service and we want to pass context to the when
     *                       the callback returns useful when there is more than one service on the call chain and we
     *                       want to pass information to services down in the chain Data sent via messageContext should
     *                       be small strings only, anything big should use blobstore and pass only reference
     */
    <T> void sendMessage(Class<T> format,
                         T objectForSerialization,
                         Map<String, String> messageHeaders,
                         Map<String, String> messageContext);

    /***
     * Sends a message represented by a java POJO to a remote service, using micro-service serialization library
     * for serializing
     * @param format message object format. as registered in @see SerializationManager
     * @param objectForSerialization java object to send. will use registered serializer to format it
     * @param messageHeaders message headers to pass to the remote service
     *                       Data sent via messageHeaders should be small strings only
     * @param messageContext message context key value set. used to pass in all further communication chain between
     *                       services
     *                       useful when using a callback on a remote service and we want to pass context to the when
     *                       the callback returns useful when there is more than one service on the call chain and we
     *                       want to pass information to services down in the chain Data sent via messageContext should
     *                       be small strings only, anything big should use blobstore and pass only reference
     * @param messageGroup Optional. Supply if using message grouping
     */
    <T> void sendMessage(Class<T> format,
                         T objectForSerialization,
                         Map<String, String> messageHeaders,
                         Map<String, String> messageContext,
                         String messageGroup);

    /***
     * Sends a message represented by a java POJO to a remote service, using micro-service serialization library
     * for serializing
     * @param format message object format. as registered in @see SerializationManager
     * @param objectForSerialization java object to send. will use registered serializer to format it
     * @param messageHeaders message headers to pass to the remote service
     *                       Data sent via messageHeaders should be small strings only
     * @param messageContext message context key value set. used to pass in all further communication chain between
     *                       services
     *                       useful when using a callback on a remote service and we want to pass context to the when
     *                       the callback returns useful when there is more than one service on the call chain and we
     *                       want to pass information to services down in the chain Data sent via messageContext should
     *                       be small strings only, anything big should use blobstore and pass only reference
     * @param messageRoutingPlan route message via multiple hops using routing plan
     */
    <T> void sendMessage(Class<T> format,
                         T objectForSerialization,
                         Map<String, String> messageHeaders,
                         Map<String, String> messageContext,
                         MessageRoutingPlan messageRoutingPlan);

    /***
     * Sends a message represented by a java POJO to a remote service, using micro-service serialization library for
     * serializing
     * @param format message object format. as registered in @see SerializationManager
     * @param objectForSerialization java object to send. will use registered serializer to format it
     * @param messageHeaders message headers to pass to the remote service
     *                       Data sent via messageHeaders should be small strings only
     * @param messageContext message context key value set. used to pass in all further communication chain between
     *                       services
     *                       useful when using a callback on a remote service and we want to pass context to the when
     *                       the callback returns useful when there is more than one service on the call chain and we
     *                       want to pass information to services down in the chain Data sent via messageContext should
     *                       be small strings only, anything big should use blobstore and pass only reference
     * @param messageRoutingPlan route message via multiple hops using routing plan
     * @param messageGroup Optional. Supply if using message grouping
     */
    <T> void sendMessage(Class<T> format,
                         T objectForSerialization,
                         Map<String, String> messageHeaders,
                         Map<String, String> messageContext,
                         MessageRoutingPlan messageRoutingPlan,
                         String messageGroup);
}
