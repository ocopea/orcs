// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.resource;

import java.util.Objects;

/**
 * Created by liebea on 9/21/2014. Enjoy it
 */
public abstract class AbstractManagedResource<
        D extends ResourceDescriptor,
        C extends ResourceConfiguration>
        implements ManagedResource<D, C> {

    private final D descriptor;
    private final C configuration;

    protected AbstractManagedResource(D descriptor, C configuration) {
        this.descriptor = Objects.requireNonNull(descriptor, "Descriptor must not be null");
        this.configuration = Objects.requireNonNull(configuration, "Configuration must not be null");
    }

    @Override
    public D getDescriptor() {
        return descriptor;
    }

    @Override
    public C getConfiguration() {
        return configuration;
    }

    @Override
    public String getName() {
        return getDescriptor().getName();
    }
}
