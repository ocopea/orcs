// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.messaging;

import com.emc.microservice.Context;
import com.emc.microservice.messaging.DefaultMessageReader;
import com.emc.microservice.messaging.InputQueueConfiguration;
import com.emc.microservice.messaging.ManagedMessageListener;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.messaging.MessageReader;
import com.emc.microservice.messaging.MessagingSerializationHelper;
import com.emc.microservice.messaging.QueueReceiverImpl;

import java.util.Map;

/**
 * Created by liebea on 4/12/17.
 * Drink responsibly
 */
public class PersistentQueueReceiver extends QueueReceiverImpl<PersistentQueueConfiguration> {
    private final PersistentMessagingServer server;

    protected PersistentQueueReceiver(
            InputQueueConfiguration inputQueueConfiguration,
            PersistentQueueConfiguration queueConfiguration,
            Map<String, PersistentQueueConfiguration> deadLetterQueueConfigurations,
            ManagedMessageListener messageListener,
            Context context,
            String consumerName,
            PersistentMessagingServer server) {
        super(
                inputQueueConfiguration,
                queueConfiguration,
                deadLetterQueueConfigurations,
                messageListener,
                context,
                consumerName);

        this.server = server;
    }

    @Override
    public void init() {
        server.subscribe(getQueueConfiguration().getQueueName(), getConsumerName(), message ->
                getMessageListener().onMessage(new PersistentMessage(message), getContext()));
    }

    @Override
    public void start() {
        server.start(getQueueConfiguration().getQueueName());
    }

    @Override
    public void pause() {
        server.pause(getQueueConfiguration().getQueueName());
    }

    @Override
    public void cleanUp() {
        server.pause(getQueueConfiguration().getQueueName());
    }

    private class PersistentMessage implements Message {
        private final com.emc.ocopea.messaging.Message nazMessage;

        private PersistentMessage(com.emc.ocopea.messaging.Message nazMesage) {
            nazMessage = nazMesage;
        }

        @Override
        public String getMessageHeader(String headerName) {
            return nazMessage.getHeaders().get(headerName);
        }

        @Override
        public Map<String, String> getMessageHeaders() {
            return nazMessage.getHeaders();
        }

        @Override
        public String getContextValue(String key) {
            return getMessageContext().get(key);
        }

        @Override
        public Map<String, String> getMessageContext() {
            return MessagingSerializationHelper.extractContext(nazMessage.getHeaders());
        }

        @Override
        public void readMessage(MessageReader messageReader) {
            nazMessage.readMessage(messageReader::read);
        }

        @Override
        public <T> T readObject(Class<T> format) {
            final DefaultMessageReader<T> messageReader = new DefaultMessageReader<>(
                    PersistentQueueReceiver.this.getContext().getSerializationManager().getReader(format));
            readMessage(messageReader);
            return messageReader.getResult();
        }

        @Override
        public Object getUnderlyingMessageObject() {
            return nazMessage;
        }
    }
}
