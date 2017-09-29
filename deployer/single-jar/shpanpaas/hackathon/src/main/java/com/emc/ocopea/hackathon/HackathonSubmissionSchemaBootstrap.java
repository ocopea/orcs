// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hackathon;

import com.emc.microservice.bootstrap.SchemaBootstrap;
import com.emc.microservice.bootstrap.SchemaBootstrapScript;

import java.util.Arrays;
import java.util.List;

/**
 * Created by liebea on 5/21/15.
 * Drink responsibly
 */
public class HackathonSubmissionSchemaBootstrap extends SchemaBootstrap {
    private static final String SCHEMA_NAME = "HackDB";
    private static final int SCHEMA_VERSION = 1;
    private static final List<SchemaBootstrapScript> SCRIPTS = Arrays.asList(
            new SchemaBootstrapScript("create_hackathon_schema.sql", "Create hackathon tables", 1)
    );

    public HackathonSubmissionSchemaBootstrap() {
        super(SCHEMA_NAME, SCRIPTS, SCHEMA_VERSION);
    }
}
