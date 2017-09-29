// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.bootstrap;

import com.emc.ocopea.util.database.BasicNativeQueryService;
import com.emc.ocopea.util.database.IntegerNativeQueryConverter;
import com.emc.ocopea.util.database.NativeQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

/**
 * Created with true love by liebea on 10/27/2014.
 */
public abstract class SchemaBootstrapRunner {
    private static final Logger logger = LoggerFactory.getLogger(SchemaBootstrapRunner.class);

    public static void dropSchemaIfExist(DataSource dataSource, String databaseSchemaName) throws SQLException {
        if (schemaExists(dataSource, databaseSchemaName)) {
            NativeQueryService nqs = new BasicNativeQueryService(dataSource);
            nqs.executeUpdate("drop schema " + databaseSchemaName + " cascade");
        }

    }

    /***
     * Run the bootstrap
     * @param dataSource datasource connection to the database
     * @param schemaBootstrap schema bootstrap descriptor
     * @param databaseSchemaName db schema to use
     * @param currentRole role to use
     */
    public static void runBootstrap(
            DataSource dataSource,
            AbstractSchemaBootstrap schemaBootstrap,
            String databaseSchemaName,
            String currentRole) throws SQLException, IOException {
        String schemaBootstrapSchemaName = schemaBootstrap.getSchemaName();
        logger.info(
                "Bootstrapping schema {} version {}",
                schemaBootstrapSchemaName,
                schemaBootstrap.getSchemaVersion());
        int fromVersion = 0;
        NativeQueryService nqs = new BasicNativeQueryService(dataSource);

        // First step is verifying whether the schema exists
        if (!schemaExists(dataSource, databaseSchemaName)) {

            // Creating the schema
            nqs.executeUpdate("create schema " + databaseSchemaName);

            // Creating the schema version table
            nqs.executeUpdate("create table " + databaseSchemaName + ".schema_version (version integer not null);");

            // Inserting the first and only row into schema version
            nqs.executeUpdate("insert into " + databaseSchemaName + ".schema_version values(0);");
        } else {
            fromVersion = nqs.getSingleValue("select version from " + databaseSchemaName + ".schema_version",
                    new IntegerNativeQueryConverter(),
                    null);
            logger.info("Found existing schema version {} {}", databaseSchemaName, fromVersion);
        }

        // Executing the upgrade itself
        schemaBootstrap.execute(dataSource, fromVersion, databaseSchemaName, currentRole);

        // Updating schema version to the current version
        nqs.executeUpdate(
                "update " + databaseSchemaName + ".schema_version set version=?",
                Collections.singletonList(schemaBootstrap.getSchemaVersion()));

    }

    private static boolean schemaExists(DataSource dataSource, String databaseSchemaName) throws SQLException {

        try (Connection connection = dataSource.getConnection();
             ResultSet schemas = connection.getMetaData().getSchemas()) {
            while (schemas.next()) {
                if (schemas.getString(1).equalsIgnoreCase(databaseSchemaName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
