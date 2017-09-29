// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2015-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.messaging;

import com.emc.microservice.resource.ManagedResource;

import java.util.List;

/**
 * Created by liebea on 4/12/15.
 * Drink responsibly
 */
public interface ManagedInputQueue extends ManagedResource<InputQueueDescriptor, InputQueueConfiguration> {

    String getQueueName();

    List<QueueReceiver> getReceivers();
}
