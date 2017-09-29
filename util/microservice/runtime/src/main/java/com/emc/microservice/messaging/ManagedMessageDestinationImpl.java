// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging;

import com.emc.microservice.resource.AbstractManagedResource;

import java.util.Objects;

/**
 * Created with love by liebea on 6/1/2014.
 */
public class ManagedMessageDestinationImpl
        extends AbstractManagedResource<DestinationDescriptor, DestinationConfiguration>
        implements ManagedMessageDestination {
    private final MessageSender messageSender;

    public ManagedMessageDestinationImpl(
            DestinationDescriptor descriptor,
            DestinationConfiguration configuration,
            MessageSender messageSender) {
        super(descriptor, configuration);
        this.messageSender = Objects.requireNonNull(messageSender, "Message sender must not be null");
    }

    @Override
    public MessageSender getMessageSender() {
        return messageSender;
    }
}
