// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.messaging;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Connection to an underlying shpan messaging system
 */
public interface Connection {

    /**
     * Creates a queue in the shpan messaging system. all queues need to be created every time the connection object
     * is created.
     * @param queueName queue name - has to be unique
     * @param memoryBufferMaxMessages In-memory queue buffer size
     * @param secondsToSleepBetweenMessageRetries seconds to sleep between retries on failed consumers
     * @param maxRetries number of retry attempts when consumer failed consuming the message
     */
    void createQueue(
            String queueName,
            int memoryBufferMaxMessages,
            int secondsToSleepBetweenMessageRetries,
            int maxRetries);

    /**
     * Send a message to a specific queue
     *
     * @param queueName queue name
     * @param messageBody input stream containing full message body
     * @param headers optional headers for the message
     */
    void sendMessage(String queueName, InputStream messageBody, Map<String, String> headers);

    /**
     * Send a message to a specific queue
     *
     * @param queueName queue name
     * @param writer writer that when invokes writes it's content to the output stream provided
     * @param headers optional headers for the message
     */
    void sendMessage(String queueName, Consumer<OutputStream> writer, Map<String, String> headers);

    /**
     * Subscribe for messages in the queue. Each subscriber registered on a queue is guaranteed to only receive a
     * single message at a time. the concurrency level of the queue subscribers associated with this connection
     * object is determined by the number of subscribers. each message will be delivered to one of the subscribers
     * and not to all.
     * @param queueName queue name
     * @param consumerName consumer key - will be printed to as part of log messages key has to be unique in the
     *                     messaging jvm
     * @param messageConsumer consumer function
     */
    void subscribe(String queueName, String consumerName, Consumer<Message> messageConsumer);

    /**
     * Unsubscribe from a queue
     */
    void unsubscribe(String queueName, String consumerName);

    /**
     * Pause all queues - avoid producing messages to consumers until started again. new messages can be sent and
     * persisted, however not consumed
     */
    void pause();

    /**
     * Pause a specific queue - avoid producing messages to consumers until started again. new messages can be sent
     * and persisted, however not consumed
     */
    void pause(String queueName);

    /**
     * Start the queues - start producing messages. can also be used to "un-pause".
     */
    void start();

    /**
     * Start a specific queue - start producing messages. can also be used to "un-pause".
     */
    void start(String queueName);
}
