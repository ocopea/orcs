// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.messaging;

import com.emc.ocopea.util.io.StreamUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This MessagePersister implementation uses memory to store messages. this is mainly for use in tests and "singleJar"
 * implementations
 */
public class InMemoryMessagePersister implements MessagePersister {
    private final AtomicLong ordinal = new AtomicLong(0);

    // queueName -> messageId -> message
    private final Map<String, SortedMap<Long, Message>> messages = new ConcurrentHashMap<>();

    @Override
    public Message persistMessage(String queueName, InputStream messageBody, Map<String, String> headers) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            StreamUtil.copy(messageBody, byteArrayOutputStream);

            return persistInternal(queueName, headers, byteArrayOutputStream);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed persisting message in queue " + queueName + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Message persistMessage(
            String queueName, Consumer<OutputStream> writer, Map<String, String> headers) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            writer.accept(byteArrayOutputStream);
            return persistInternal(queueName, headers, byteArrayOutputStream);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed persisting message in queue " + queueName + ": " + e.getMessage(), e);
        }
    }

    private Message persistInternal(
            String queueName,
            Map<String, String> headers,
            ByteArrayOutputStream byteArrayOutputStream) {
        synchronized (this) {
            final long ordinal = this.ordinal.incrementAndGet();
            final InMemoryMessage newMessage = new InMemoryMessage(
                    ordinal,
                    byteArrayOutputStream.toByteArray(),
                    headers);

            // Adding the new message the the queue
            messages.computeIfAbsent(queueName, q -> new TreeMap<>()).put(ordinal, newMessage);
            return newMessage;
        }
    }

    @Override
    public synchronized void deleteMessage(String queueName, String messageId) {
        messages.getOrDefault(queueName, new TreeMap<>()).remove(Long.valueOf(messageId));
    }

    @Override
    public synchronized List<Message> loadNextMessages(
            String queueName,
            int maxCount,
            Set<String> skipMessageIds) {
        final List<Message> list = this.messages.getOrDefault(queueName, Collections.emptySortedMap()).values()
                .stream()
                .sequential()
                .filter(message -> !skipMessageIds.contains(message.getId()))
                .collect(Collectors.toList());
        return list.subList(0, Math.min(maxCount, list.size()));
    }

    private class InMemoryMessage implements Message {
        private final long ordinal;
        private final byte[] data;
        private final Map<String, String> headers;

        private InMemoryMessage(long ordinal, byte[] data, Map<String, String> headers) {
            this.ordinal = ordinal;
            this.data = new byte[data.length];
            System.arraycopy(data, 0, this.data, 0, data.length);
            this.headers = headers;
        }

        @Override
        public String getId() {
            return Long.toString(ordinal);
        }

        @Override
        public Map<String, String> getHeaders() {
            return headers;
        }

        @Override
        public void readMessage(Consumer<InputStream> messageConsumer) {
            messageConsumer.accept(new ByteArrayInputStream(data));
        }
    }
}
