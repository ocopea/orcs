// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.configuration.bootstrap;

import com.emc.microservice.bootstrap.SchemaBootstrap;
import com.emc.microservice.bootstrap.SchemaBootstrapScript;

import java.util.Arrays;
import java.util.List;

/**
 * Created by shresa on 03/08/15.
 */
public class ConfigurationSchemaBootstrap extends SchemaBootstrap {

    private static final String SCHEMA_NAME = "Configuration";
    private static final List<SchemaBootstrapScript> SCRIPTS = Arrays.asList(
            new SchemaBootstrapScript("configuration/create_configuration_db.sql", "Create configuration database", 1)
    );
    private static final int SCHEMA_VERSION = 1;

    public ConfigurationSchemaBootstrap() {
        super(SCHEMA_NAME, SCRIPTS, SCHEMA_VERSION);
    }
}
