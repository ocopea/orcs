// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.testing;

import com.emc.microservice.Context;
import com.emc.microservice.messaging.DefaultMessageReader;
import com.emc.microservice.messaging.InputQueueConfiguration;
import com.emc.microservice.messaging.ManagedMessageListener;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.messaging.MessageReader;
import com.emc.microservice.messaging.QueueReceiverImpl;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 * Created with love by liebea on 6/23/2014.
 */
class MessageReceiverMock extends QueueReceiverImpl<MockTestingResourceProvider.MockQueueConfiguration> {
    MessageReceiverMock(
            InputQueueConfiguration inputQueueConfiguration,
            MockTestingResourceProvider.MockQueueConfiguration mockQueueConfiguration,
            ManagedMessageListener messageListener,
            Context context,
            String consumerName) {
        super(
                inputQueueConfiguration,
                mockQueueConfiguration,
                Collections.emptyMap(),
                messageListener,
                context,
                consumerName);
    }

    public void sendMessage(
            final Map<String, String> messageHeaders,
            final Map<String, String> messageContext,
            final InputStream messageAsInputStream) {
        getMessageListener().onMessage(new Message() {
            @Override
            public String getMessageHeader(String headerName) {
                return messageHeaders.get(headerName);
            }

            @Override
            public Map<String, String> getMessageHeaders() {
                return messageHeaders;
            }

            @Override
            public String getContextValue(String key) {
                return messageContext.get(key);
            }

            @Override
            public Map<String, String> getMessageContext() {
                return messageContext;
            }

            @Override
            public void readMessage(MessageReader messageReader) {
                messageReader.read(messageAsInputStream);
            }

            @Override
            public <T> T readObject(Class<T> format) {
                DefaultMessageReader<T> messageReader =
                        new DefaultMessageReader<>(getContext().getSerializationManager().getReader(format), format);
                    readMessage(messageReader);
                return messageReader.getResult();
            }

            @Override
            public Object getUnderlyingMessageObject() {
                return null;
            }
        }, getContext());
    }

    @Override
    public void init() {
        System.out.println("Receiver initialized");
    }

    @Override
    public void start() {
        System.out.println("Receiver started");
    }

    @Override
    public void pause() {
        System.out.println("Receiver paused");
    }

    @Override
    public void cleanUp() {
        System.out.println("Receiver cleaned up");
    }

}
