// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.repository;

import com.emc.microservice.bootstrap.SchemaBootstrap;
import com.emc.microservice.bootstrap.SchemaBootstrapScript;

import java.util.Arrays;
import java.util.List;

/**
 * Created by liebea on 7/24/16.
 * Drink responsibly
 */
public class SiteRepositorySchema extends SchemaBootstrap {
    private static final String SCHEMA_NAME = "site_repository";
    private static final List<SchemaBootstrapScript> SCRIPTS = Arrays.asList(
            new SchemaBootstrapScript("sitedb/site_repository.sql", "Site Repository Tables", 1)
    );
    private static final int SCHEMA_VERSION = 1;

    public SiteRepositorySchema() {
        super(SCHEMA_NAME, SCRIPTS, SCHEMA_VERSION);
    }

    public SiteRepositorySchema(String schemaName) {
        super(schemaName, SCRIPTS, SCHEMA_VERSION);
    }

}
