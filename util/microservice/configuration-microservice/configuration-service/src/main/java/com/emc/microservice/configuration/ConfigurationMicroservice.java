// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.configuration;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceInitializationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shresa on 31/07/15.
 */
public class ConfigurationMicroservice extends MicroService {

    public static final String SERVICE_ID = "configuration";
    public static final String CONFIG_DB = "config-db";
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationMicroservice.class);

    public ConfigurationMicroservice() {
        super(
                "Configuration Service",
                SERVICE_ID,
                "Service for global settings.",
                1,
                LOGGER,
                new MicroServiceInitializationHelper()
                        //API
                        .withRestResource(ConfigResource.class, "Configuration API")
                        // database
                        .withDatasource(CONFIG_DB, "Configuration Database")
        );
    }
}
