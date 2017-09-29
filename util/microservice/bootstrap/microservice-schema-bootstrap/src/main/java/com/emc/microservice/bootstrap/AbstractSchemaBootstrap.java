// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.bootstrap;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created with true love by liebea on 10/27/2014.
 */
public abstract class AbstractSchemaBootstrap {
    private final String schemaName;
    private final int schemaVersion;

    public AbstractSchemaBootstrap(String schemaName, int schemaVersion) {
        this.schemaName = schemaName;
        this.schemaVersion = schemaVersion;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public abstract void execute(DataSource dataSource, int fromVersion, String databaseSchemaName, String role)
            throws IOException, SQLException;
}