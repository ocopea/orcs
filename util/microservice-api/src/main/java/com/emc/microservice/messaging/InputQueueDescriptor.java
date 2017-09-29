// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.messaging;

import com.emc.microservice.resource.ResourceDescriptor;

/**
 * Created with love by liebea on 5/26/2014.
 */
public class InputQueueDescriptor implements ResourceDescriptor {
    private final String queueName;
    private final String queueDescription;
    private final Class<? extends MessageListener> messageListener;
    private final String messagesSelector;
    private final String[] messageHeadersForLogging;

    public InputQueueDescriptor(String queueName,
                                String queueDescription,
                                Class<? extends MessageListener> messageListener,
                                String messagesSelector,
                                String[] messageHeadersForLogging) {
        this.queueName = queueName;
        this.queueDescription = queueDescription;
        this.messageListener = messageListener;
        this.messagesSelector = messagesSelector;
        this.messageHeadersForLogging = messageHeadersForLogging;
    }

    public InputQueueDescriptor(String queueName,
                                String queueDescription,
                                Class<? extends MessageListener> messageListener,
                                String messagesSelector) {
        this(queueName, queueDescription, messageListener, messagesSelector, null);
    }

    public String getQueueName() {
        return queueName;
    }

    public String getQueueDescription() {
        return queueDescription;
    }

    public Class<? extends MessageListener> getMessageListener() {
        return messageListener;
    }

    public String getMessagesSelector() {
        return messagesSelector;
    }

    public String[] getMessageHeadersForLogging() {
        return messageHeadersForLogging;
    }

    @Override
    public String getName() {
        String name = getQueueName();
        if (messagesSelector != null) {
            name += "/" + messagesSelector;
        }
        return name;
    }

}
