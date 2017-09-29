// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import com.emc.microservice.bootstrap.SchemaBootstrap;
import com.emc.microservice.bootstrap.SchemaBootstrapScript;

import java.util.Arrays;
import java.util.List;

/**
 * Created by liebea on 7/24/16.
 * Drink responsibly
 */
public class HubRepositorySchema extends SchemaBootstrap {
    private static final String SCHEMA_NAME = "hub_repository";
    private static final List<SchemaBootstrapScript> SCRIPTS = Arrays.asList(
            new SchemaBootstrapScript("hubdb/hub_repository.sql", "Hub Repository Tables", 1)
    );
    private static final int SCHEMA_VERSION = 1;

    public HubRepositorySchema() {
        super(SCHEMA_NAME, SCRIPTS, SCHEMA_VERSION);
    }

    public HubRepositorySchema(String schemaName) {
        super(schemaName, SCRIPTS, SCHEMA_VERSION);
    }

}
