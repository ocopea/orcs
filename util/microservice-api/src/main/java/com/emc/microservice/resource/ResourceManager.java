// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2015-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.resource;

import com.emc.microservice.Context;

import java.util.List;

/**
 * Created by liebea on 4/5/15.
 * Drink responsibly
 */
public interface ResourceManager<D extends ResourceDescriptor,
        C extends ResourceConfiguration,
        R extends ManagedResource<D, C>> {

    List<D> getDescriptors();

    List<R> getManagedResources();

    R getManagedResourceByName(String name);

    void addResourceDynamically(D desc, C conf, Context context);

    boolean hasResource(String resourceName);

    Class<D> getDescriptorClass();
}