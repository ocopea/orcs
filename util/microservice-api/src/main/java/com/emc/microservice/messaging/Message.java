// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.messaging;

import java.util.Map;

/**
 * Created with love by liebea on 6/22/2014.
 */
public interface Message {

    /**
     * Get message header by name
     *
     * @param headerName Message header name
     * @return Message header value
     */
    String getMessageHeader(String headerName);

    /**
     * Get all message headers as key value pairs
     *
     * @return map of header name to values
     */
    Map<String, String> getMessageHeaders();

    /**
     * Get the value for given key in the context
     *
     * @param key Context key
     * @return Value in the context corresponding to the key
     */
    String getContextValue(String key);

    /**
     * Get all the key:value pairs in the context
     *
     * @return map of key:value pairs in the context
     */
    Map<String, String> getMessageContext();

    /***
     * Read message using input stream directly
     * @param messageReader implement message reader interface
     */
    void readMessage(MessageReader messageReader);

    /***
     * Deserialize java object from message. object serializer has to be registered as supported format,
     * either by declaring it in the input descriptor of the service, or by registering a custom serializer
     * @see com.emc.microservice.MicroServiceInitializationHelper
     * @param format object format (Java class to deserialize to)
     * @param <T> generic type for format
     * @return either valid nice object instance or exception
     */
    <T> T readObject(Class<T> format);

    /***
     * Return the technology-specific message implementation
     * @return native message object
     */
    Object getUnderlyingMessageObject();

}
