// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.messaging;

import com.emc.microservice.resource.ResourceDescriptor;

/**
 * Created with love by liebea on 6/1/2014.
 */
public class DestinationDescriptor implements ResourceDescriptor {
    private final String name;
    private final String description;

    public DestinationDescriptor(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
