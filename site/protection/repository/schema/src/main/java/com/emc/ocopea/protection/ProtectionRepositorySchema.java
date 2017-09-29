// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import com.emc.microservice.bootstrap.SchemaBootstrap;
import com.emc.microservice.bootstrap.SchemaBootstrapScript;

import java.util.Collections;
import java.util.List;

public class ProtectionRepositorySchema extends SchemaBootstrap {
    private static final String SCHEMA_NAME = "protection_repository";
    private static final List<SchemaBootstrapScript> SCRIPTS = Collections.singletonList(
            new SchemaBootstrapScript("protectiondb/protection_repository.sql",
                    "Protection Repository Tables", 1)
    );
    private static final int SCHEMA_VERSION = 1;

    public ProtectionRepositorySchema() {
        super(SCHEMA_NAME, SCRIPTS, SCHEMA_VERSION);
    }

    public ProtectionRepositorySchema(String schemaName) {
        super(schemaName, SCRIPTS, SCHEMA_VERSION);
    }
}
