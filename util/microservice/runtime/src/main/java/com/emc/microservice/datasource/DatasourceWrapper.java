// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * Created by liebea on 4/7/15.
 * Drink responsibly
 */
public class DatasourceWrapper implements MicroServiceDataSource {
    private static final Logger logger = LoggerFactory.getLogger(DatasourceWrapper.class);
    private final DataSource dataSource;

    // a transactionState for each service's thread
    private static final ThreadLocal<TransactionState> transactionState = new ThreadLocal<TransactionState>() {
        @Override
        protected TransactionState initialValue() {
            return TransactionState.NONE;
        }
    };

    // a ConnectionWrapper for each service's thread
    private static final ThreadLocal<ConnectionWrapper> txConnection = new ThreadLocal<ConnectionWrapper>() {
        @Override
        protected ConnectionWrapper initialValue() {
            return null;
        }
    };

    @Override
    public void close() throws IOException {
        if (dataSource instanceof Closeable) {
            ((Closeable) dataSource).close();
        }
    }

    private enum TransactionState {
        NONE,
        REQUESTED,
        OPEN
    }

    public DatasourceWrapper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void beginTransaction() {

        logger.debug(
                "Start beginTransaction - thread id : {}.  transaction state is : {}. txConnection is : {}",
                Thread.currentThread().getId(),
                transactionState.get(),
                txConnection.get());

        transactionState.set(TransactionState.REQUESTED);

        logger.debug(
                "Finish beginTransaction - thread id : {}.  transaction state is : {}. txConnection is : {}",
                Thread.currentThread().getId(),
                transactionState.get(),
                txConnection.get());
    }

    @Override
    public void commitTransaction() {

        final TransactionState state = transactionState.get();
        logger.debug(
                "Start commitTransaction - thread id : {}.  transaction state is : {}. txConnection is : {}",
                Thread.currentThread().getId(),
                state,
                txConnection.get());

        try {
            switch (state) {
                case NONE:
                    logger.warn("Transaction committed without being opened");
                    break;
                case OPEN:
                    logger.debug("About to commit tx");
                    txConnection.get().commit();
                    logger.debug("tx committed successfully");
                    break;
                case REQUESTED:
                    logger.debug("Transaction committed without accessing connection");
                    break;
                default:
                    throw new IllegalStateException("Unsupported state " + state);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed committing tx", e);
        } finally {
            try {
                ConnectionWrapper connectionWrapper = txConnection.get();
                if (connectionWrapper != null) {
                    connectionWrapper.closeOriginalConnectionAndSetAutoCommit();
                }
            } finally {
                txConnection.set(null);
                transactionState.set(TransactionState.NONE);
            }
        }

        logger.debug(
                "Finish commitTransaction - thread id : {}.  transaction state is : {}. txConnection is : {}",
                Thread.currentThread().getId(),
                state,
                txConnection.get());
    }

    @Override
    public void rollbackTransaction() {

        final TransactionState state = transactionState.get();
        logger.debug(
                "Start rollbackTransaction - thread id : {}.  transaction state is : {}. txConnection is : {}",
                Thread.currentThread().getId(),
                state,
                txConnection.get());

        try {
            switch (state) {
                case NONE:
                    logger.warn("Transaction rolled back without being opened");
                    break;
                case OPEN:
                    logger.debug("About to commit tx");
                    txConnection.get().rollback();
                    logger.debug("tx committed successfully");
                    break;
                case REQUESTED:
                    logger.debug("Transaction rolled back without accessing connection");
                    break;
                default:
                    throw new IllegalStateException("Unsupported state " + state);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed rolling back tx", e);
        } finally {
            try {
                ConnectionWrapper connectionWrapper = txConnection.get();
                if (connectionWrapper != null) {
                    connectionWrapper.closeOriginalConnectionAndSetAutoCommit();
                }
            } finally {
                txConnection.set(null);
                transactionState.set(TransactionState.NONE);
            }
        }

        logger.debug(
                "Finish rollbackTransaction - thread id : {}.  transaction state is : {}. txConnection is : {}",
                Thread.currentThread().getId(),
                state,
                txConnection.get());
    }

    @Override
    public boolean isInTransaction() {
        return transactionState.get() == TransactionState.REQUESTED ||
                transactionState.get() == TransactionState.OPEN;
    }

    @Override
    public Connection getConnection() throws SQLException {

        final TransactionState state = transactionState.get();
        logger.debug(
                "Start getConnection - thread id : {}.  transaction state is : {}. txConnection is : {}",
                Thread.currentThread().getId(),
                state,
                txConnection.get());

        switch (state) {
            case REQUESTED:
                txConnection.set(new ConnectionWrapper(dataSource.getConnection()));
                transactionState.set(TransactionState.OPEN);
                return txConnection.get();
            case OPEN:
                return txConnection.get();
            default:
                break;
        }

        logger.debug(
                "Finish getConnection - thread id : {}.  transaction state is : {}. txConnection is : {}",
                Thread.currentThread().getId(),
                state,
                txConnection.get());

        logger.debug("dataSourceClass={}", dataSource.getClass());
        return dataSource.getConnection();

    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return this.getConnection();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        dataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return dataSource.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return dataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return dataSource.isWrapperFor(iface);
    }

}
