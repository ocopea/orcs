// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.messaging;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Implementation of persistent messaging, allows robust "at least once" messaging using external persistence
 */
public class PersistentMessagingServer implements Connection {

    private final MessagePersister messagePersister;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private Map<String, Queue> queues = new ConcurrentHashMap<>();

    public PersistentMessagingServer(MessagePersister persister) {
        messagePersister = persister;
    }

    @Override
    public void createQueue(
            String queueName,
            int memoryBufferMaxMessages,
            int secondsToSleepBetweenMessageRetries,
            int maxRetries) {

        queues.computeIfAbsent(
                queueName,
                s -> new Queue(
                        queueName,
                        executor,
                        messagePersister,
                        memoryBufferMaxMessages,
                        secondsToSleepBetweenMessageRetries,
                        maxRetries));
    }

    @Override
    public void sendMessage(String queueName, InputStream messageBody, Map<String, String> headers) {
        getQueue(queueName).sendMessage(messageBody, headers);
    }

    @Override
    public void sendMessage(String queueName, Consumer<OutputStream> writer, Map<String, String> headers) {
        getQueue(queueName).sendMessage(writer, headers);
    }

    private Queue getQueue(String queueName) {
        // Getting queue, verifying it exists
        return Objects.requireNonNull(queues.get(queueName), "queue " + queueName + " does not exist");
    }

    @Override
    public void subscribe(String queueName, String consumerName, Consumer<Message> messageConsumer) {
        getQueue(queueName).addConsumer(consumerName, messageConsumer);
    }

    @Override
    public void unsubscribe(String queueName, String consumerName) {
        getQueue(queueName).removeConsumer(consumerName);
    }

    @Override
    public void pause() {
        queues.values().forEach(Queue::pause);
    }

    @Override
    public void pause(String queueName) {
        getQueue(queueName).pause();
    }

    @Override
    public void start() {
        queues.values().forEach(Queue::start);
    }

    @Override
    public void start(String queueName) {
        getQueue(queueName).start();
    }
}
