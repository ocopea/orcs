// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */
package com.emc.ocopea.util.database;

import com.emc.ocopea.util.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author nivenb
 */
public class BasicNativeQueryServiceTest {

    private static final String CONNECTION_STRING =
            "jdbc:h2:mem:databaseForBasicNativeQueryServiceTest;DB_CLOSE_DELAY=-1;MODE=PostgreSQL";

    private DataSource h2InMemoryDataSource;

    private BasicNativeQueryService nativeQueryService;

    @BeforeClass
    public static void initClass() {
        loadH2DriverClass();
    }

    private static void loadH2DriverClass() {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot load H2 driver", e);
        }
    }

    @Before
    public void initTest() throws Exception {
        setupDataSource();
        nativeQueryService = new BasicNativeQueryService(h2InMemoryDataSource);
    }

    /**
     * This doesn't test that the fetch size is adhered to. It merely tests that the execution of this code passes
     * without exception
     */
    @Test
    public void testHandleResultSet() {
        nativeQueryService.executeUpdate("CREATE TABLE testHandleResultSet (col1 varchar, col2 int)");
        nativeQueryService.executeUpdate("INSERT INTO testHandleResultSet VALUES ('Henry', 8)");
        nativeQueryService.executeUpdate("INSERT INTO testHandleResultSet VALUES ('Brett', 1)");

        final List<Pair<String, Integer>> returnedValues = new ArrayList<>();
        NativeQueryRowBatchConverter<Void> converter = (resultSet, i) -> {
            returnedValues.add(new Pair<>(resultSet.getString(1), resultSet.getInt(2)));
            return null;
        };

        nativeQueryService.handleResultSet(
                "SELECT * FROM testHandleResultSet ORDER BY col2 DESC",
                converter,
                null,
                FetchSpecification.getSuggestSpecificFetchSize(5));
        Assert.assertEquals(2, returnedValues.size());
        Assert.assertEquals("Henry", returnedValues.get(0).getKey());
        Assert.assertEquals(Integer.valueOf(8), returnedValues.get(0).getValue());
        Assert.assertEquals("Brett", returnedValues.get(1).getKey());
        Assert.assertEquals(Integer.valueOf(1), returnedValues.get(1).getValue());

        // try again with a fetch size of 1
        returnedValues.clear();
        nativeQueryService.handleResultSet(
                "SELECT * FROM testHandleResultSet ORDER BY col2 DESC",
                converter,
                null,
                FetchSpecification.getSuggestSpecificFetchSize(1));
        Assert.assertEquals(2, returnedValues.size());
        Assert.assertEquals("Henry", returnedValues.get(0).getKey());
        Assert.assertEquals(Integer.valueOf(8), returnedValues.get(0).getValue());
        Assert.assertEquals("Brett", returnedValues.get(1).getKey());
        Assert.assertEquals(Integer.valueOf(1), returnedValues.get(1).getValue());
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_STRING, "sa", "");
    }

    private void setupDataSource() throws Exception {

        /*
         * We only care about implementing the getConnection methods. Seeing as this is only used in unit testing, the native query service will simply obtain,
         * use, then close a connection every simple time (pooling/re-use would be a slight overkill :))
         */
        h2InMemoryDataSource = new DataSource() {

            @Override
            public Connection getConnection() throws SQLException {
                return BasicNativeQueryServiceTest.this.getConnection();
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                return BasicNativeQueryServiceTest.this.getConnection();
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
            }            @Override
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
}
