// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.input;

import com.emc.microservice.messaging.MessageListener;

/**
 * Created by liebea on 10/4/2014. Enjoy it
 */
public class MessagingInputDescriptor extends InputDescriptor {

    private final Class<? extends MessageListener> messageListener;
    private final String[] messageHeadersForLogging;

    public MessagingInputDescriptor(String description,
                                    Class format,
                                    Class<? extends MessageListener> messageListener,
                                    String[] messageHeadersForLogging) {
        super(MicroServiceInputType.messaging, description, format);
        this.messageListener = messageListener;
        this.messageHeadersForLogging = messageHeadersForLogging;
    }

    public Class<? extends MessageListener> getMessageListener() {
        return messageListener;
    }

    public String[] getMessageHeadersForLogging() {
        return messageHeadersForLogging;
    }
}
