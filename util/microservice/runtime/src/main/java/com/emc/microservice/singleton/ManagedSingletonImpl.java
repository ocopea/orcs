// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.singleton;

import com.emc.microservice.resource.AbstractManagedResource;

/**
 * Created by liebea on 3/22/15.
 * Drink responsibly
 */
public class ManagedSingletonImpl extends AbstractManagedResource<SingletonDescriptor, SingletonConfiguration>
        implements ManagedSingleton {

    private final Object instance;

    protected ManagedSingletonImpl(
            SingletonDescriptor descriptor,
            SingletonConfiguration configuration,
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
