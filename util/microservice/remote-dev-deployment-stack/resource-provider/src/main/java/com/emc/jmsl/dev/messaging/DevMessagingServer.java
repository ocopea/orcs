// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.jmsl.dev.messaging;

import com.emc.ocopea.devtools.checkstyle.NoJavadoc;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by liebea on 1/18/15.
 * Drink responsibly
 */
public class DevMessagingServer {
    public static final String DEV_MESSAGING_BLOBSTORE_NAME = "dev-messaging";
    private static final DevMessagingServer instance = new DevMessagingServer();
    private final Map<String, BlockingQueue<DevMessageDescriptor>> messages = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> messagesSinceStartup = new ConcurrentHashMap<>();

    public static DevMessagingServer getInstance() {
        return instance;
    }

    private DevMessagingServer() {
    }

    public DevMessageDescriptor receive(String queueName) throws InterruptedException {
        BlockingQueue<DevMessageDescriptor> queue = getQueue(queueName);
        return queue.take();
    }

    private synchronized BlockingQueue<DevMessageDescriptor> getQueue(String queueName) {
        BlockingQueue<DevMessageDescriptor> queue = messages.get(queueName);
        if (queue == null) {
            queue = new ArrayBlockingQueue<>(1000);
            messages.put(queueName, queue);
            messagesSinceStartup.put(queueName, new AtomicLong(0));
        }
        return queue;
    }

    @NoJavadoc
    public void sendMessage(String queueName, DevMessageDescriptor devMessageDescriptor) {
        BlockingQueue<DevMessageDescriptor> queue = getQueue(queueName);
        messagesSinceStartup.get(queueName).getAndIncrement();
        try {
            queue.put(devMessageDescriptor);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Failed putting message in queue", e);
        }
    }

    @NoJavadoc
    public DevMessagingStats getMessageStats(String queueName) {

        AtomicLong atomicLong = messagesSinceStartup.get(queueName);
        if (atomicLong == null) {
            return new DevMessagingStats(queueName, 0L, 0L);
        }
        BlockingQueue<DevMessageDescriptor> queue = getQueue(queueName);

        return new DevMessagingStats(queueName, queue.size(), atomicLong.get());
    }

    static final class DevMessagingStats {
        private String name;
        private long messagesInQueue;
        private long messagesSinceRestart;

        DevMessagingStats(String name, long messagesInQueue, long messagesSinceRestart) {
            this.name = name;
            this.messagesInQueue = messagesInQueue;
            this.messagesSinceRestart = messagesSinceRestart;
        }

        public String getName() {
            return name;
        }

        public long getMessagesInQueue() {
            return messagesInQueue;
        }

        public long getMessagesSinceRestart() {
            return messagesSinceRestart;
        }
    }
}
