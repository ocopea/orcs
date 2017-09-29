// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2015-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.schedule;

import com.emc.microservice.resource.ResourceConfiguration;
import com.emc.microservice.resource.ResourceConfigurationProperty;
import com.emc.microservice.resource.ResourceConfigurationPropertyType;

import java.util.ArrayList;
import java.util.List;

public class SchedulerConfiguration extends ResourceConfiguration {

    private static final ResourceConfigurationProperty PROPERTY_NAME =
            new ResourceConfigurationProperty("name", ResourceConfigurationPropertyType.STRING, "name", true, false);

    public SchedulerConfiguration(String name) {
        this();
        setPropertyValues(propArrayToMap(new String[]{
                PROPERTY_NAME.getName(), name
        }));
    }

    protected SchedulerConfiguration(String name, List<ResourceConfigurationProperty> properties) {
        super(name, properties);
    }

    public SchedulerConfiguration() {
        super("Scheduler", new ArrayList<>());
    }

    public String getName() {
        return getProperty(PROPERTY_NAME.getName());
    }
}
