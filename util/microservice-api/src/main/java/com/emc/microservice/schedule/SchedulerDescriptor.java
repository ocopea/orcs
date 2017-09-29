// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2015-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.schedule;

import com.emc.microservice.resource.ResourceDescriptor;

/**
 * Created by liebea on 3/22/15.
 * Drink responsibly
 */
public class SchedulerDescriptor implements ResourceDescriptor {
    private final String name;

    public SchedulerDescriptor(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
