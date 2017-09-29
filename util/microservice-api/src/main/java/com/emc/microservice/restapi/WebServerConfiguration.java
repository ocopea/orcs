// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.restapi;

import com.emc.microservice.resource.ResourceConfiguration;
import com.emc.microservice.resource.ResourceConfigurationProperty;

import java.util.List;

/**
 * Created by liebea on 10/5/2014. Enjoy it
 */
public abstract class WebServerConfiguration extends ResourceConfiguration {
    protected WebServerConfiguration(String configurationName, List<ResourceConfigurationProperty> properties) {
        super(configurationName, properties);
    }
}
