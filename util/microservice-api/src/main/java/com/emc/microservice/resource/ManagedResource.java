// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.resource;

/**
 * Created by liebea on 9/21/2014. Enjoy it
 */
public interface ManagedResource<D extends ResourceDescriptor, C extends ResourceConfiguration> {

    String getName();

    D getDescriptor();

    C getConfiguration();
}
