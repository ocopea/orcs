// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.dpa.dbjunit;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.util.database.AbstractNativeQueryService;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * An AbstractNativeQueryService implementation suitable for unit tests, using an in-memory H2 database.
 *
 * @author nivenb
 */
public class UnitTestNativeQueryServiceImpl extends AbstractNativeQueryService {

    private static final String CONNECTION_STRING = "jdbc:h2:mem:@databaseName@;DB_CLOSE_DELAY=-1;MODE=PostgreSQL";

    private static final String SCHEMA_STRING = ";SCHEMA=@schema@";

    private final String connectionString;

    private DataSource h2InMemoryDataSource;

    private static int counter = 1;

    private static synchronized int nextCounter() {
        return counter++;
    }

    public UnitTestNativeQueryServiceImpl(String databaseName, boolean enforceUnique) throws SQLException {
        if (enforceUnique) {
            databaseName = databaseName + "_" + nextCounter();
        } else {
            databaseName = databaseName + "_0";
        }
        connectionString = CONNECTION_STRING.replace("@databaseName@", databaseName);
        setupDataSource();
    }

    public UnitTestNativeQueryServiceImpl(String databaseName, String schema, boolean enforceUnique)
            throws SQLException {
        if (enforceUnique) {
            databaseName = databaseName + "_" + nextCounter();
        } else {
            databaseName = databaseName + "_0";
        }
        connectionString =
                CONNECTION_STRING.replace("@databaseName@", databaseName) + SCHEMA_STRING.replace("@schema@", schema);
        setupDataSource();
    }

    private static Connection createConnection(String connectionString) throws SQLException {
        return DriverManager.getConnection(connectionString, "sa", "");
    }

    @NoJavadoc
    // TODO add javadoc
    public static DataSource createH2InMemoryTestDataSource(String databaseName, boolean enforceUnique)
            throws SQLException {
        if (enforceUnique) {
            databaseName = databaseName + "_" + nextCounter();
        } else {
            databaseName = databaseName + "_0";
        }
        return createH2InMemoryDataSource(CONNECTION_STRING.replace("@databaseName@", databaseName));
    }

    @NoJavadoc
    // TODO add javadoc
    public static DataSource createH2InMemoryTestDataSource(String databaseName, String schema, boolean enforceUnique)
            throws SQLException {
        if (enforceUnique) {
            databaseName = databaseName + "_" + nextCounter();
        } else {
            databaseName = databaseName + "_0";
        }
        return createH2InMemoryDataSource(
                CONNECTION_STRING.replace("@databaseName@", databaseName) + SCHEMA_STRING.replace("@schema@", schema));

    }

    private static DataSource createH2InMemoryDataSource(String connectionString) throws SQLException {
        loadH2DriverClass();

        /*
         * We only care about implementing the getConnection methods. Seeing as this is only used in unit testing,
         * the native query service will simply obtain, use, then close a connection every single time
         * (pooling/re-use would be a slight overkill :))
         */
        return new DataSource() {

            @Override
            public Connection getConnection() throws SQLException {
                return createConnection(connectionString);
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                return createConnection(connectionString);
            }

            @Override
            public <T> T unwrap(Class<T> iface) throws SQLException {
                return null;
            }

            @Override
            public boolean isWrapperFor(Class<?> iface) throws SQLException {
                return false;
            }

            @Override
            public PrintWriter getLogWriter() throws SQLException {
                return null;
            }

            @Override
            public void setLoginTimeout(int seconds) throws SQLException {
            }

            @Override
            public void setLogWriter(PrintWriter out) throws SQLException {
            }

            @Override
            public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
                return null;
            }

            @Override
            public int getLoginTimeout() throws SQLException {
                return 0;
            }
        };
    }

    private static void loadH2DriverClass() {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot load H2 driver", e);
        }

    }

    @Override
    public DataSource getDataSource() {
        return h2InMemoryDataSource;
    }

    private Connection getConnection() throws SQLException {
        return createConnection(connectionString);
    }

    private void setupDataSource() throws SQLException {

        h2InMemoryDataSource = createH2InMemoryDataSource(connectionString);
    }
}
