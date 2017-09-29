// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.restapi;

import com.emc.microservice.resource.ResourceDescriptor;

/**
 * Created by liebea on 7/6/2014. Enjoy it
 */
public class ManagedResourceDescriptor implements ResourceDescriptor {
    private final Class resourceClass;
    private final String description;

    public ManagedResourceDescriptor(Class resourceClass, String description) {
        this.resourceClass = resourceClass;
        this.description = description;
    }

    public Class getResourceClass() {
        return resourceClass;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String getName() {
        return getResourceClass().getSimpleName();
    }
}
