// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2014 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.blobstore.impl;

import com.emc.microservice.blobstore.BlobWriter;
import com.emc.microservice.blobstore.StoreException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.postgresql.ds.PGPoolingDataSource;
import org.postgresql.ds.jdbc4.AbstractJdbc4PoolingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
@Ignore
public class TestBlobStoreService {

    private static final Logger log = LoggerFactory.getLogger(TestBlobStoreService.class);

    private static DataSource dataSource;
    private static HashMap<String, String> ps = new HashMap<>();

    @BeforeClass
    public static void init() {

        ps.put("file.name", "avatar.png");
        final AbstractJdbc4PoolingDataSource pgSource = new PGPoolingDataSource();
        pgSource.setDataSourceName("apollo");
        pgSource.setServerName("localhost");
        pgSource.setPortNumber(9003);
        pgSource.setDatabaseName("apollo");
        pgSource.setUser("apollosuperuser");
        pgSource.setPassword("3g1;23#6hFd 05(}c8/F5rB2lL1J79");
        pgSource.setMaxConnections(1);

        // Wrapping inline for enhancing ds connections (e.g. set search path)

        dataSource = new DataSource() {
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
                connection.createStatement().execute("SET search_path=blob_store");
                return connection;
            }

        };
    }

    @Before
    public void tearUp() throws StoreException {
        PostgresBlobStoreService bs = new PostgresBlobStoreService(dataSource);
        bs.delete("Z", "Z");
    }

    @After
    public void tearDown() throws StoreException {
        PostgresBlobStoreService bs = new PostgresBlobStoreService(dataSource);
        bs.delete("Z", "Z");
    }

    @Test
    public void testDelete() {

    }

    @Test
    public void testCreateALOT() throws StoreException {
        PostgresBlobStoreService bs = new PostgresBlobStoreService(dataSource);
        bs.delete("Z", "Z");

        for (int i = 0; i < 9; i++) {
            testCreate();
            bs.delete("Z", "Z");
        }
    }

    public void testCreate() throws StoreException {
        PostgresBlobStoreService bs = new PostgresBlobStoreService(dataSource);

        InputStream fis;
        FileOutputStream fos;
        try {
            Profiler profiler = new Profiler("BlobStore small file performance check");
            profiler.setLogger(log);
            profiler.start("Create temporary initial file to compare");
            //create file
            File f = File.createTempFile("noone", "care");
            f.deleteOnExit();
            fos = new FileOutputStream(f);
            fos.write(ps.toString().getBytes());
            fos.flush();
            fos.close();

            fis = new FileInputStream(f);
            profiler.start("Store object");
            bs.create("Z", "Z", ps, fis);
            profiler.start("Create temporary output file");
            File tmpF = File.createTempFile("noonecare", "evenmore");
            tmpF.deleteOnExit();
            fos = new FileOutputStream(tmpF);
            profiler.start("get object to a file ");
            bs.readBlob("Z", "Z", fos);
            fos.flush();
            profiler.start("compare length");
            assertEquals(tmpF.length(), f.length());
            profiler.stop().log();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Ignore
    public void testCreateEmptyBlob() throws Exception {
        PostgresBlobStoreService bs = new PostgresBlobStoreService(dataSource);

        FileOutputStream fos;
        try {
            bs.create("Z", "Z", ps, (BlobWriter) null);
            File tmpF = File.createTempFile("noonecare", "evenmore");
            tmpF.deleteOnExit();
            fos = new FileOutputStream(tmpF);
            bs.readBlob("Z", "Z", fos);
            fos.flush();
            assertEquals(tmpF.length(), 0); //file should be empty

            Map<String, String> hdr = bs.readHeaders("Z", "Z");
            assertTrue(ps.get("file.name").equals(hdr.get("file.name")));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testEmptyHeaders() throws Exception {
        PostgresBlobStoreService bs = new PostgresBlobStoreService(dataSource);
        bs.create("Z", "Z", null, (BlobWriter) null);
        Map<String, String> hdr = bs.readHeaders("Z", "Z");
        assertNull(hdr);
    }

    @Test
    @Ignore
    public void testAutoCommitValue() throws Exception {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(true);
        connection.close();//return to the pool

        connection = dataSource.getConnection();
        assertEquals(true, connection.getAutoCommit());
        connection.setAutoCommit(false);
        connection.commit();
        connection.close();//return to the pool
        connection = dataSource.getConnection();
        boolean falseValue = connection.getAutoCommit();
        connection.close();
        assertEquals(false, falseValue);
    }

}
