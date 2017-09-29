// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging;

import com.emc.microservice.resource.AbstractManagedResource;

import java.util.List;
import java.util.Objects;

/**
 * Created with love by liebea on 5/26/2014.
 */
public class ManagedInputQueueImpl extends AbstractManagedResource<InputQueueDescriptor, InputQueueConfiguration>
        implements ManagedInputQueue {
    protected final List<? extends QueueReceiver> receivers;

    public ManagedInputQueueImpl(
            InputQueueDescriptor descriptor,
            InputQueueConfiguration configuration,
            List<? extends QueueReceiver> receivers) {
        super(descriptor, configuration);
        this.receivers = Objects.requireNonNull(receivers);
    }

    @Override
    public String getQueueName() {
        return getDescriptor().getQueueName();
    }

    @Override
    public List<QueueReceiver> getReceivers() {
        return (List<QueueReceiver>) receivers;
    }
}
