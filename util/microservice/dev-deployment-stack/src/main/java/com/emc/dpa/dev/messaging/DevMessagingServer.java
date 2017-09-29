// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.dpa.dev.messaging;

import com.emc.ocopea.devtools.checkstyle.NoJavadoc;

import java.util.ArrayList;
import java.util.List;
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
    private final Map<String, List<BlockingQueue<DevMessageDescriptor>>> topicSubscribers = new ConcurrentHashMap<>();

    public static DevMessagingServer getInstance() {
        return instance;
    }

    private DevMessagingServer() {
    }

    public DevMessageDescriptor receive(String queueName) throws InterruptedException {
        return receive(queueName, null);
    }

    @NoJavadoc
    public DevMessageDescriptor receive(String queueName, Integer topicId) throws InterruptedException {
        if (topicId == null) {
            return getQueue(queueName).take();
        } else {
            return topicSubscribers.get(queueName).get(topicId).take();
        }
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

        final List<BlockingQueue<DevMessageDescriptor>> topicSubs = this.topicSubscribers.get(queueName);
        if (topicSubs != null) {
            topicSubs.forEach(currSub -> {
                try {
                    currSub.put(devMessageDescriptor);
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Failed putting message in topic", e);
                }
            });
        } else {
            try {
                queue.put(devMessageDescriptor);
            } catch (InterruptedException e) {
                throw new IllegalStateException("Failed putting message in queue", e);
            }
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

    @NoJavadoc
    public Integer subscribeToTopic(String inputQueueURI) {
        List<BlockingQueue<DevMessageDescriptor>> blockingQueues = topicSubscribers.get(inputQueueURI);
        if (blockingQueues == null) {
            blockingQueues = new ArrayList<>();
            topicSubscribers.put(inputQueueURI, blockingQueues);
        }
        blockingQueues.add(new ArrayBlockingQueue<>(1000));
        return blockingQueues.size() - 1;
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
