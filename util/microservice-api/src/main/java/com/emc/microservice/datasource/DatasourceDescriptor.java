// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.datasource;

import com.emc.microservice.resource.ResourceDescriptor;

/**
 * Created with love by liebea on 6/3/2014.
 */
public class DatasourceDescriptor implements ResourceDescriptor {
    private final String name;
    private final String description;

    public DatasourceDescriptor(String name, String description) {
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
