// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import com.emc.microservice.MicroService;

import java.util.Collection;

/**
 * Created by liebea on 7/22/15.
 * Drink responsibly
 */
public class ShpanPaaSAppTemplate {
    private final String name;
    private final MicroService microServiceDescriptor;
    private final Collection<AppServiceDependency> dependencies;

    public ShpanPaaSAppTemplate(
            String name,
            MicroService microServiceDescriptor,
            Collection<AppServiceDependency> dependencies) {
        this.name = name;
        this.microServiceDescriptor = microServiceDescriptor;
        this.dependencies = dependencies;
    }

    public String getName() {
        return name;
    }

    public MicroService getMicroServiceDescriptor() {
        return microServiceDescriptor;
    }

    public Collection<AppServiceDependency> getDependencies() {
        return dependencies;
    }
}
