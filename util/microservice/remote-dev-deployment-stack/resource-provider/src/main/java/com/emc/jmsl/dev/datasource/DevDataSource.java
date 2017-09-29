// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.jmsl.dev.datasource;

import com.emc.microservice.datasource.MicroServiceDataSource;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.ConnectionPoolDataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Handmade code created by ohanaa Date: 3/1/15 Time: 6:22 PM
 */
public class DevDataSource extends JdbcConnectionPool implements MicroServiceDataSource {

    private AtomicBoolean isRunning;

    public static DevDataSource create(String url, String user, String password) {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(url);
        ds.setUser(user);
        ds.setPassword(password);
        return new DevDataSource(ds);
    }

    protected DevDataSource(ConnectionPoolDataSource dataSource) {
        super(dataSource);
        isRunning = new AtomicBoolean(true);
    }

    public Connection getConnection() throws SQLException {
        if (!isRunning.get()) {
            throw new IllegalStateException("DataSource is paused");
        }
        return super.getConnection();
    }

    public void pauseDataSource() {
        isRunning.set(false);
    }

    public void resumeDataSource() {
        isRunning.set(true);
    }

    @Override
    public void beginTransaction() {

    }

    @Override
    public void commitTransaction() {

    }

    @Override
    public void rollbackTransaction() {

    }

    @Override
    public boolean isInTransaction() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isAssignableFrom(this.getClass())) {
            return iface.cast(this);
        } else {
            throw new SQLException("Cannot unwrap to " + iface.getName());
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(this.getClass());
    }

    @Override
    public void close() throws IOException {
        pauseDataSource();
        super.dispose();
    }
}
