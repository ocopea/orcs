// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.resource;

/**
 * Created by liebea on 9/21/2014. Enjoy it
 * Describes a static "design time" resource descriptor for a micro-service to declare
 * on runtime, every descriptor will be "resolved" by a configuration object with physical connectivity details
 */
public interface ResourceDescriptor {

    // Name of the resource, should be unique within a single micro-service
    String getName();
}
