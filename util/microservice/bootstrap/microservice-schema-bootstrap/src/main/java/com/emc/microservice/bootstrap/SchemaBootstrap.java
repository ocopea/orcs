// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

package com.emc.microservice.bootstrap;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with true love by liebea on 10/27/2014.
 */
public abstract class SchemaBootstrap extends AbstractSchemaBootstrap {
    private static final Logger log = LoggerFactory.getLogger(SchemaBootstrap.class);
    private final List<SchemaBootstrapScript> fullBootstrapScriptList;
    protected final List<SchemaBootstrapScript> upgradeBootstrapScriptList;
    private static final String SQL_POSTGRES_GET_SEARCH_PATH =
            "SELECT setting FROM pg_settings WHERE name= 'search_path'";
    public static final String POSTGRES_TYPE_MAPPING_STR = "PostgreSQL";
    public static final String H2_TYPE_MAPPING_STR = "H2";

    public SchemaBootstrap(String schemaName, List<SchemaBootstrapScript> bootstrapScriptList, int schemaVersion) {
        super(schemaName, schemaVersion);
        this.fullBootstrapScriptList = bootstrapScriptList;
        this.upgradeBootstrapScriptList = new ArrayList<>();
    }

    public SchemaBootstrap(
            String schemaName,
            List<SchemaBootstrapScript> fullBootstrapScriptList,
            List<SchemaBootstrapScript> upgradeBootstrapScriptList,
            int schemaVersion) {
        super(schemaName, schemaVersion);
        this.fullBootstrapScriptList = fullBootstrapScriptList;
        // null prevention - just in case
        this.upgradeBootstrapScriptList = upgradeBootstrapScriptList != null ?
                upgradeBootstrapScriptList :
                new ArrayList<>();
    }

    public List<SchemaBootstrapScript> getFullBootstrapScriptList() {
        return fullBootstrapScriptList;
    }

    public List<SchemaBootstrapScript> getUpgradeBootstrapScriptList(DataSource dataSource, String databaseSchemaName)
            throws IOException {
        return upgradeBootstrapScriptList;
    }

    @Override
    public void execute(DataSource dataSource, int fromVersion, String databaseSchemaName, String role)
            throws IOException, SQLException {
        // Executing the scripts
        try (final Connection connection = dataSource.getConnection()) {
            String searchPath = null;
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            if (databaseProductName.equals(POSTGRES_TYPE_MAPPING_STR)) {
                try (Statement getPathStatement = connection.createStatement()) {
                    try (ResultSet resultSet = getPathStatement.executeQuery(SQL_POSTGRES_GET_SEARCH_PATH)) {
                        if (resultSet.next()) {
                            searchPath = resultSet.getString(1);
                        }
                    }
                }
            }
            try {
                setSearchPath(connection, databaseSchemaName);
                ScriptRunner scriptRunner = new ScriptRunner(connection, false, true);

                if (databaseProductName.equals(H2_TYPE_MAPPING_STR)) {
                    scriptRunner.addSyntaxFilter(statement -> statement.replaceAll("JSONB", "VARCHAR"));
                }
                final boolean isUpgrade = fromVersion > 0;
                for (SchemaBootstrapScript currScript : getBootstrapScripts(
                        dataSource,
                        databaseSchemaName,
                        isUpgrade)) {

                    // Skipping scripts which we don't need to run
                    if (currScript.getSchemaVersion() > fromVersion) {
                        log.info("Running script: " + currScript.getDescription());

                        Reader scriptReader = currScript.getScriptReader();

                        // validate the SQL script first before running.
                        validateScript(scriptReader);

                        scriptRunner.runScript(scriptReader);
                    }
                }
            } finally {
                if (searchPath != null) {
                    setSearchPath(connection, searchPath);
                }
            }

            //Execute 'grant' queries only when running with postgres - this syntax does not supported by h2 used
            // in the tests
            if (databaseProductName.equals(SchemaBootstrap.POSTGRES_TYPE_MAPPING_STR) && role != null) {
                grantPermissionsToRole(connection, databaseSchemaName, role);
            }
        }
    }

    private static void grantPermissionsToRole(Connection connection, String databaseSchemaName, String currentRole)
            throws SQLException {
        log.debug("Granting permissions to " + currentRole + " on schema " + databaseSchemaName);
        try (Statement statement = connection.createStatement()) {
            statement.execute("grant usage on schema " + databaseSchemaName + " TO " + "\"" + currentRole + "\"");
            statement.execute(
                    "grant select,update,insert,delete on all tables in schema " + databaseSchemaName + " TO " + "\"" +
                            currentRole + "\"");
            statement.execute("grant select, update on all sequences in schema " + databaseSchemaName + " TO " + "\"" +
                    currentRole + "\"");
        }
    }

    private void setSearchPath(Connection connection, String schemaName) throws SQLException {
        try (Statement setPathStatement = connection.createStatement()) {
            setPathStatement.execute("SET search_path=" + schemaName + ";");
        }
    }

    private List<SchemaBootstrapScript> getBootstrapScripts(
            DataSource dataSource,
            String databaseSchemaName,
            boolean isUpgrade) throws IOException {
        return isUpgrade ? getUpgradeBootstrapScriptList(dataSource, databaseSchemaName) : getFullBootstrapScriptList();
    }

    /**
     * Validates the bootstrap script.
     *
     * @param script the script to validate
     */
    protected static void validateScript(Reader script) throws IOException {
        // Retrieve the whole script into a single string so it is more manageable to work.
        // Since each microservice will only have a small script file using String should not be a problem.
        String lines = IOUtils.toString(script);

        //reset the reader so it can be read again
        if (script.markSupported()) {
            script.reset();
        }

        // remove the comments so that it does not mess around with the regex.
        if (lines.contains("--") || lines.contains("#")) {
            lines = lines.replaceAll("(--|#).*(\r\n|\n)", "");
        }

        final String[] sqlStatements = lines.split(";");
        boolean invalidStatement = false;
        String errorMessage =
                "The script contains unsupported statements! Dropping or renaming a table, " +
                        "column or constraint is not allowed. Remove following lines.\n";
        for (final String statement : sqlStatements) {
            // The checks are done with two regex since it was much easier to create 2 regex instead of the one.
            if (statement.trim().matches("(?i)(?m)(?s)^DROP\\s*TABLE.*") || // check for dropping of table
                    statement
                            .trim()
                            .matches("(?i)(?m)(?s)^ALTER\\s*TABLE.*(RENAME|DROP)[\\s]*(TO|COLUMN|CONSTRAINT).*")) {
                //check for dropping or renaming of table, column and constraint
                invalidStatement = true;
                errorMessage += statement + ";\n";
            }
        }

        if (invalidStatement) {
            log.error(errorMessage);
            throw new IOException(errorMessage);
        }
    }
}
