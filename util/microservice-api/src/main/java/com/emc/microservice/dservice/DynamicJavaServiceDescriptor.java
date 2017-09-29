// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2015-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.dservice;

import com.emc.microservice.resource.ResourceDescriptor;

/**
 * Created by liebea on 3/22/15.
 * Drink responsibly
 */
public class DynamicJavaServiceDescriptor implements ResourceDescriptor {
    private final String name;
    private final String description;
    private final Class clazz;

    public DynamicJavaServiceDescriptor(String name, String description, Class clazz) {
        this.name = name;
        this.description = description;
        this.clazz = clazz;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Class getClazz() {
        return clazz;
    }
}
