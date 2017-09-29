// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.messaging;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
  * Defines an interface required for messaging system in order to persist messages
 */
public interface MessagePersister {

    /**
     * Stores a message
     */
    Message persistMessage(String queueName, InputStream messageBody, Map<String, String> headers);

    /**
     * Stores a message
     */
    Message persistMessage(String queueName, Consumer<OutputStream> writer, Map<String, String> headers);

    /**
     * Loads ordered messages for a queue. order is by the date messages were persisted
     * @param queueName name of the queue to load messages for
     * @param maxCount maximum amount of messages to load
     * @param skipMessageIds message ids to ignore
     */
    List<Message> loadNextMessages(String queueName, int maxCount, Set<String> skipMessageIds);

    /**
     * Delete a specific message from a queue
     */
    void deleteMessage(String queueName, String messageId);

}
