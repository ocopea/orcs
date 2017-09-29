// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.messaging;

/**
 * Created with love by liebea on 5/28/2014.
 */
public interface ManagedMessageListener extends MessageListener {

    QueueExecutionState getQueueExecutionState();

}
