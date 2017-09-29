// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2014 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.blobstore;

import com.emc.microservice.bootstrap.AbstractSchemaBootstrap;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Initialize Blob store schema
 */
public class PGBlobstoreFlywayDBSchemaBootstrap extends AbstractSchemaBootstrap {

    public static final Logger log = LoggerFactory.getLogger(PGBlobstoreFlywayDBSchemaBootstrap.class);

    public PGBlobstoreFlywayDBSchemaBootstrap(String schemaName, String databaseSchemaName) {
        super(schemaName, 1);
    }

    @Override
    public void execute(DataSource dataSource, int fromVersion, String databaseSchemaName, String role)
            throws IOException, SQLException {
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setSchemas(databaseSchemaName);
        flyway.migrate();
    }
}
