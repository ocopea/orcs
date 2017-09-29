// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.messaging;

import com.emc.microservice.resource.ManagedResource;

/**
 * Created with love by liebea on 6/1/2014.
 */
public interface ManagedMessageDestination extends ManagedResource<DestinationDescriptor, DestinationConfiguration> {
    MessageSender getMessageSender();
}
