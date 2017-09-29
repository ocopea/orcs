// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2015-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.schedule;

import com.emc.microservice.resource.ManagedResource;

import java.util.Map;

/**
 * In all the "create" methods:
 * @param name a unique name for this schedule
 * @param listenerIdentifier an identifier which was previously registered using MicroServiceInitializationHelper.
 */
public interface ManagedScheduler extends ManagedResource<SchedulerDescriptor, SchedulerConfiguration> {

    void create(String name, int intervalInSeconds, String listenerIdentifier);

    void create(
            String name,
            int intervalInSeconds,
            String listenerIdentifier,
            Map<String, String> headers);

    <T> void create(
            String name,
            int intervalInSeconds,
            String listenerIdentifier,
            Map<String, String> headers,
            Class<T> payloadClass,
            T payload);
}
