// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.dservice;

import com.emc.microservice.resource.AbstractManagedResource;

/**
 * Created by liebea on 3/22/15.
 * Drink responsibly
 */
public class ManagedDynamicJavaServiceImpl
        extends AbstractManagedResource<DynamicJavaServiceDescriptor, DynamicJavaServiceConfiguration>
        implements ManagedDynamicJavaService {

    private final Object instance;

    protected ManagedDynamicJavaServiceImpl(
            DynamicJavaServiceDescriptor descriptor,
            DynamicJavaServiceConfiguration configuration,
            Object instance) {
        super(descriptor, configuration);
        this.instance = instance;
    }

    @Override
    public <T> T getInstance() {
        //noinspection unchecked
        return (T) instance;
    }
}
