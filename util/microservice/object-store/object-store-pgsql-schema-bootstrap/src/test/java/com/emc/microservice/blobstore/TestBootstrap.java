// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2014 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.blobstore;

import com.emc.microservice.bootstrap.SchemaBootstrapRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.postgresql.ds.PGPoolingDataSource;
import org.postgresql.ds.jdbc4.AbstractJdbc4PoolingDataSource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 *
 */
public class TestBootstrap {

    @Test
    @Ignore
    public void testInit() throws Exception {
        String dbSchemaName = "blobi";
        PGBlobstoreSchemaBootstrap schemaBootstrap = new PGBlobstoreSchemaBootstrap("test_store");
        SchemaBootstrapRunner.runBootstrap(getDS(), schemaBootstrap, dbSchemaName, "some_role");
    }

    public DataSource getDS() {
        final AbstractJdbc4PoolingDataSource pgSource = new PGPoolingDataSource();
        pgSource.setDataSourceName("apollo");
        pgSource.setServerName("localhost");
        pgSource.setPortNumber(9003);
        pgSource.setDatabaseName("apollo");
        pgSource.setUser("apollosuperuser");
        pgSource.setPassword("3g1;23#6hFd 05(}c8/F5rB2lL1J79");
        pgSource.setMaxConnections(10);

        // Wrapping inline for enhancing ds connections (e.g. set search path)

        return new DataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                return enhanceConnection(pgSource.getConnection());
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                return enhanceConnection(pgSource.getConnection(username, password));
            }

            @Override
            public PrintWriter getLogWriter() throws SQLException {
                return pgSource.getLogWriter();
            }

            @Override
            public void setLogWriter(PrintWriter out) throws SQLException {
                pgSource.setLogWriter(out);
            }

            @Override
            public void setLoginTimeout(int seconds) throws SQLException {
                pgSource.setLoginTimeout(seconds);
            }

            @Override
            public int getLoginTimeout() throws SQLException {
                return pgSource.getLoginTimeout();
            }

            @Override
            public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
                return pgSource.getParentLogger();
            }

            @Override
            public <T> T unwrap(Class<T> interfaceClass) throws SQLException {
                return pgSource.unwrap(interfaceClass);
            }

            @Override
            public boolean isWrapperFor(Class<?> interfaceClass) throws SQLException {
                return pgSource.isWrapperFor(interfaceClass);
            }

            private Connection enhanceConnection(Connection connection) throws SQLException {
                connection.createStatement().execute("SET search_path=blobi");
                return connection;
            }

        };
    }
}
