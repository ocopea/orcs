// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.dependency;

import com.emc.microservice.resource.ResourceConfiguration;
import com.emc.microservice.resource.ResourceConfigurationProperty;
import com.emc.microservice.resource.ResourceConfigurationPropertyType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by liebea on 9/21/2014. Enjoy it
 */
public class ServiceDependencyConfiguration extends ResourceConfiguration {
    private static final String CONFIGURATION_NAME = "Service Dependency";
    private static final ResourceConfigurationProperty PROPERTY_RESULT_RECEIVER_CONCURRENCY =
            new ResourceConfigurationProperty(
                    "resultReceiverConcurrency",
                    ResourceConfigurationPropertyType.STRING,
                    "Concurrency of callback receiver",
                    true,
                    false);

    private static final List<ResourceConfigurationProperty> PROPERTIES =
            Collections.singletonList(PROPERTY_RESULT_RECEIVER_CONCURRENCY);

    public ServiceDependencyConfiguration() {
        super(CONFIGURATION_NAME, PROPERTIES);
    }

    public ServiceDependencyConfiguration(int resultReceiverConcurrency) {
        this();
        setPropertyValues(propArrayToMap(new String[]{
                PROPERTY_RESULT_RECEIVER_CONCURRENCY.getName(),
                String.valueOf(resultReceiverConcurrency)
        }));
    }

    public int getResultReceiverConcurrency() {
        return Integer.parseInt(getProperty(PROPERTY_RESULT_RECEIVER_CONCURRENCY.getName()));
    }
}
