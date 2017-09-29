// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2015-2016 EMC Corporation All Rights Reserved
 */

package com.emc.microservice.singleton;

import com.emc.microservice.resource.ResourceDescriptor;

/**
 * Created by liebea on 3/22/15.
 * Drink responsibly
 */
public class SingletonDescriptor<T extends ServiceLifecycle> implements ResourceDescriptor {

    private final String name;
    private final String description;
    private final Class<T> clazz;

    public SingletonDescriptor(String name, String description, Class<T> clazz) {
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

    public Class<T> getClazz() {
        return clazz;
    }
}
