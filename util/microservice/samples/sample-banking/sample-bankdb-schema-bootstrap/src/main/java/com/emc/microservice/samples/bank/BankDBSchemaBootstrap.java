// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.samples.bank;

import com.emc.microservice.bootstrap.SchemaBootstrap;
import com.emc.microservice.bootstrap.SchemaBootstrapScript;

import java.util.Arrays;
import java.util.List;

/**
 * Created with true love by liebea on 10/27/2014.
 */
public class BankDBSchemaBootstrap extends SchemaBootstrap {
    private static final String SCHEMA_NAME = "BankDB";
    private static final List<SchemaBootstrapScript> SCRIPTS = Arrays.asList(
            new SchemaBootstrapScript("bankdb/account_schema.sql", "Account tables creation", 1)
    );
    private static final int SCHEMA_VERSION = 1;

    public BankDBSchemaBootstrap() {
        super(SCHEMA_NAME, SCRIPTS, SCHEMA_VERSION);
    }

    public BankDBSchemaBootstrap(String schemaName) {
        super(schemaName, SCRIPTS, SCHEMA_VERSION);
    }
}
