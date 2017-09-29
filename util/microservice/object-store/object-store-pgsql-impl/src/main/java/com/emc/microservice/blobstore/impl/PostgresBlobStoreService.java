// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * This computer code is copyright 2014 - 2015 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.blobstore.impl;

import com.emc.microservice.blobstore.BlobReader;
import com.emc.microservice.blobstore.BlobStore;
import com.emc.microservice.blobstore.BlobStoreLink;
import com.emc.microservice.blobstore.BlobWriter;
import com.emc.microservice.blobstore.DuplicateObjectKeyException;
import com.emc.microservice.blobstore.IllegalStoreStateException;
import com.emc.microservice.blobstore.ObjectKeyFormatException;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.util.JsonUtil;
import com.emc.ocopea.util.io.StreamUtil;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Blob Store Service implementation using PGSQL data base as primary storage
 */
public class PostgresBlobStoreService implements BlobStore {

    private final Logger log = LoggerFactory.getLogger(PostgresBlobStoreService.class);
    private final DataSource dataSource;

    /* accept accept string with dash or underscore in the middle once */
    private static final String KEY_ACCEPTED_CHARS = "([\\p{Alnum}_-])*";
    private static final Pattern ACCEPTED_CHARS_PATTERN = Pattern.compile(KEY_ACCEPTED_CHARS);
    private static final String CREATE_OBJECT =
            "INSERT INTO objects " +
                    "(f_namespace, f_key, f_headers, f_lastmodified, f_retention, f_value) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String MOVE_NAMESPACE =
            "UPDATE objects set f_namespace = ? where  f_key = ? and f_namespace = ?";
    private static final String CHECK_IF_EXISTS = "select f_key from objects where f_namespace = ? AND f_key = ?";
    private static final String GET_OID_BY_KEY = "SELECT f_value FROM objects WHERE f_namespace = ? AND f_key= ?";
    private static final String LIST_KEYS_AND_HEADERS = "SELECT f_namespace, f_key, f_headers FROM objects";
    private static final String GET_HEADERS_BY_KEY = "SELECT f_headers FROM objects WHERE f_namespace = ? AND f_key= ?";
    private static final String DELETE_BY_KEY = "DELETE FROM objects WHERE f_namespace = ? AND f_key= ?";
    private static final String UPDATE_HEADERS_BY_KEY =
            "UPDATE objects SET f_headers = ?, f_lastmodified = ? WHERE f_namespace = ? AND f_key= ?";
    private static final String UPDATE_OID_BY_KEY =
            "UPDATE objects SET f_value = ?, f_lastmodified = ? WHERE f_namespace = ? AND f_key= ?";
    private static final String UPDATE_OID_AND_HEADERS_BY_KEY =
            "UPDATE objects SET f_headers = ?, f_value = ?, f_lastmodified = ? WHERE f_namespace = ? AND f_key= ?";
    private static final String GET_ID_AND_OID_BY_EXPIRY =
            "SELECT f_id, f_value FROM objects WHERE f_lastmodified < ? ";
    private static final String DELETE_BY_IDS = "DELETE FROM objects WHERE f_id in ";

    public PostgresBlobStoreService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    static void isKeyAcceptable(String key) throws ObjectKeyFormatException {
        if (!ACCEPTED_CHARS_PATTERN.matcher(key).matches()) {
            throw new ObjectKeyFormatException("Key '" + key + "' doesn't match pattern.");
        }
    }

    @NoJavadoc
    public void create(String namespace, String key, Map<String, String> headers, final InputStream blob)
            throws ObjectKeyFormatException, DuplicateObjectKeyException, IllegalStoreStateException {
        if (blob != null) {
            create(namespace, key, headers, out -> StreamUtil.copy(blob, out));
        } else {
            create(namespace, key, headers, (BlobWriter) null);
        }
    }

    @NoJavadoc
    public void create(
            final String namespace,
            final String key,
            final Map<String, String> headers,
            final BlobWriter blobWriter)
            throws ObjectKeyFormatException, DuplicateObjectKeyException, IllegalStoreStateException {
        Executable executable = connection -> {
            // All LargeObject API calls must be within a transaction block
            connection.setAutoCommit(false);
            //check if unique key
            try (PreparedStatement psCheck = connection.prepareStatement(CHECK_IF_EXISTS)) {
                psCheck.setString(1, namespace);
                psCheck.setString(2, key);
                try (ResultSet rsCheck = psCheck.executeQuery()) {
                    if (rsCheck.isBeforeFirst()) {
                        throw new DuplicateObjectKeyException("Key '" + key + "' already exists in the database");
                    }
                    Long oid = null;
                    if (blobWriter != null) { //blob can be null/empty
                        LargeObjectManager lobj =
                                connection.unwrap(org.postgresql.PGConnection.class).getLargeObjectAPI();
                        oid = lobj.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
                        LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);
                        try {
                            blobWriter.write(obj.getOutputStream());
                        } finally {
                            obj.close();
                        }
                    }
                    try (PreparedStatement psCreateObject = connection.prepareStatement(CREATE_OBJECT)) {
                        psCreateObject.setString(1, namespace);//f_namespace
                        psCreateObject.setString(2, key);//f_key
                        psCreateObject.setObject(3, JsonUtil.toJson(headers), Types.OTHER);//f_headers
                        psCreateObject.setLong(4, System.currentTimeMillis()); //f_lastmodified
                        Long retention = getRetention(headers);
                        if (retention != null) {
                            psCreateObject.setLong(5, getRetention(headers));//retention
                        } else {
                            psCreateObject.setNull(5, Types.BIGINT);
                        }
                        if (oid != null) {
                            psCreateObject.setLong(6, oid);//f_value
                        } else {
                            psCreateObject.setNull(6, Types.BIGINT);
                        }
                        psCreateObject.executeUpdate();
                        psCreateObject.close();

                        connection.commit();
                    }
                }
            }
        };
        log.debug("creating for key {}:{}", namespace, key);
        runWithConnection(namespace, key, executable);
    }

    private Long getRetention(Map<String, String> headers) {
        Long result = null;
        try {
            if (headers != null) {
                result = Long.valueOf(headers.get("retention"));
            }
        } catch (NumberFormatException nfe) {
            log.debug("Illegal or null retention value '" + headers.get("retention") +
                    "'. Returning null == no retention.");
        }
        return result;
    }

    @Override
    public void update(
            final String namespace,
            final String key,
            final Map<String, String> headers,
            final BlobWriter blobWriter) throws ObjectKeyFormatException, IllegalStoreStateException {
        Executable executable = connection -> {
            connection.setAutoCommit(false);
            Long oid;
            if (blobWriter != null) { //blob can be null/empty
                LargeObjectManager lobj = connection.unwrap(org.postgresql.PGConnection.class).getLargeObjectAPI();
                oid = lobj.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
                LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);
                try {
                    blobWriter.write(obj.getOutputStream());
                } finally {
                    obj.close();
                }
                //now read old oid
                readOldOid(namespace, key, headers, connection, oid, lobj);
            } else {
                //update headers only if they not null
                updateHeaders(namespace, key, headers, connection);
            }

            connection.commit();
        };
        try {
            log.debug("Updating for {}:{}", namespace, key);
            runWithConnection(namespace, key, executable);
        } catch (DuplicateObjectKeyException ignored) {
            // never happens for update
        }
    }

    @NoJavadoc
    @Override
    public void update(
            final String namespace,
            final String key,
            final Map<String, String> headers,
            final InputStream blob) throws ObjectKeyFormatException, IllegalStoreStateException {
        Executable executable = connection -> {
            connection.setAutoCommit(false);
            Long oid;
            //if blob != null create new large object
            if (blob != null) {
                LargeObjectManager lobj = connection.unwrap(org.postgresql.PGConnection.class).getLargeObjectAPI();
                oid = lobj.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
                LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);

                StreamUtil.copy(blob, obj.getOutputStream());
                // Close the large object
                obj.close();

                //now read old oid
                readOldOid(namespace, key, headers, connection, oid, lobj);
            } else {
                //update headers only if they not null
                updateHeaders(namespace, key, headers, connection);
            }

            connection.commit();
        };
        try {
            log.debug("Updating for {}:{}", namespace, key);
            runWithConnection(namespace, key, executable);
        } catch (DuplicateObjectKeyException ignored) {
            // never happens for update
        }
    }

    private void readOldOid(
            String namespace,
            String key,
            Map<String, String> headers,
            Connection connection,
            Long oid,
            LargeObjectManager lobj) throws SQLException, IOException {
        try (PreparedStatement psGetOID = connection.prepareStatement(GET_OID_BY_KEY)) {
            psGetOID.setString(1, namespace);
            psGetOID.setString(2, key);
            try (ResultSet rs = psGetOID.executeQuery()) {
                if (rs.next()) {
                    //and delete it
                    Long oldOID = rs.getLong(1);
                    lobj.delete(oldOID);
                }
                //update headers(?) and insert new oid
                String sql = headers == null ? UPDATE_OID_BY_KEY : UPDATE_OID_AND_HEADERS_BY_KEY;
                try (PreparedStatement psUpdateBlob = connection.prepareStatement(sql)) {
                    int i = 1;
                    if (headers != null) {
                        psUpdateBlob.setObject(i++, JsonUtil.toJson(headers), Types.OTHER); //f_headers
                    }
                    psUpdateBlob.setLong(i++, oid);//f_value
                    psUpdateBlob.setLong(i++, System.currentTimeMillis()); //f_lastmodified
                    psUpdateBlob.setString(i++, namespace);//f_namespace
                    psUpdateBlob.setString(i, key);//f_key
                    psUpdateBlob.executeUpdate();
                }
            }
        }
    }

    private void updateHeaders(String namespace, String key, Map<String, String> headers, Connection connection)
            throws SQLException, IOException {
        if (headers != null) {
            try (PreparedStatement psUpdateObject = connection.prepareStatement(UPDATE_HEADERS_BY_KEY)) {
                psUpdateObject.setObject(1, JsonUtil.toJson(headers), Types.OTHER); //f_headers
                psUpdateObject.setLong(2, System.currentTimeMillis()); //f_lastmodified
                psUpdateObject.setString(3, namespace);//f_namespace
                psUpdateObject.setString(4, key);//f_key
                psUpdateObject.executeUpdate();
                psUpdateObject.close();
            }
        }
    }

    @Override
    public void moveNameSpace(final String oldNamespace, final String key, final String newNamespace)
            throws ObjectKeyFormatException, DuplicateObjectKeyException, IllegalStoreStateException {
        isKeyAcceptable(oldNamespace);

        Executable executable = connection -> {
            // All LargeObject API calls must be within a transaction block
            connection.setAutoCommit(false);
            //check if unique key
            try (PreparedStatement psCheck = connection.prepareStatement(CHECK_IF_EXISTS)) {
                psCheck.setString(1, newNamespace);
                psCheck.setString(2, key);
                try (ResultSet rsCheck = psCheck.executeQuery()) {
                    if (rsCheck.isBeforeFirst()) {
                        throw new DuplicateObjectKeyException(
                                "Key '" + key + "' already exists in the database in the new namespace");
                    }

                    try (PreparedStatement psMoveNamespace = connection.prepareStatement(MOVE_NAMESPACE)) {
                        psMoveNamespace.setString(1, newNamespace);//f_namespace (set the new namespace)
                        psMoveNamespace.setString(2, key);//f_key
                        psMoveNamespace.setString(3, oldNamespace);//f_namespace (where  old namespace)
                        psMoveNamespace.executeUpdate();
                        connection.commit();
                    }
                }
            }
        };
        log.debug("moving from old namespace {} to new namespace {} for key {}", oldNamespace, newNamespace, key);
        runWithConnection(newNamespace, key, executable);
    }

    @Override
    public void readBlob(final String namespace, final String key, final BlobReader reader)
            throws ObjectKeyFormatException, IllegalStoreStateException {
        Executable executable = connection -> {
            // Get the Large Object Manager to perform operations with
            try (PreparedStatement ps = connection.prepareStatement(GET_OID_BY_KEY)) {
                ps.setString(1, namespace);
                ps.setString(2, key);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        // Open the large object for reading
                        Long oid = rs.getLong(1);
                        if (!rs.wasNull()) { //blob might be null
                            connection.setAutoCommit(false);
                            LargeObjectManager lobj =
                                    connection.unwrap(org.postgresql.PGConnection.class).getLargeObjectAPI();
                            LargeObject obj = lobj.open(oid, LargeObjectManager.READ);
                            try {
                                reader.read(obj.getInputStream());
                            } finally {
                                // Close the object
                                obj.close();
                            }
                            connection.commit();
                        }
                    }
                }
            }
        };

        try {
            log.debug("reading blob for {}:{}", namespace, key);
            runWithConnection(namespace, key, executable);
        } catch (DuplicateObjectKeyException ignored) {
            // not an issue with readBlob
        }
    }

    public void readBlob(String namespace, String key, final OutputStream out)
            throws ObjectKeyFormatException, IllegalStoreStateException {
        readBlob(namespace, key, in -> StreamUtil.copy(in, out));
    }

    @NoJavadoc
    public Map<String, String> readHeaders(String namespace, String key)
            throws ObjectKeyFormatException, IllegalStoreStateException {
        isKeyAcceptable(key);
        isKeyAcceptable(namespace);

        Map<String, String> result = null;
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(GET_HEADERS_BY_KEY)) {
                ps.setString(1, namespace);
                ps.setString(2, key);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) { //we expect only 1 result
                        result = JsonUtil.readMap(rs.getString(1));
                    }
                }
            }
        } catch (SQLException s) {
            log.debug("Could not obtain connection", s);
            throw new IllegalStoreStateException("Could not obtain connection", s);
        }
        return result;
    }

    @Override
    public void delete(int expirySeconds) throws IllegalStoreStateException {
        StringBuilder idsToDelete = new StringBuilder();

        try (Connection connection = dataSource.getConnection()) {
            boolean oldAutoCommitValue = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);
                try (PreparedStatement ps = connection.prepareStatement(GET_ID_AND_OID_BY_EXPIRY)) {
                    ps.setLong(1, System.currentTimeMillis() - expirySeconds * 1000);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {

                            Long id = rs.getLong(1);
                            if (!rs.wasNull()) {
                                idsToDelete.append(id).append(", ");
                            }

                            // Deleting large objects
                            Long oid = rs.getLong(2);
                            if (!rs.wasNull()) {
                                LargeObjectManager lobj =
                                        connection.unwrap(org.postgresql.PGConnection.class).getLargeObjectAPI();
                                lobj.delete(oid);
                            }
                        }

                        if (idsToDelete.length() > 0) {
                            try (PreparedStatement ps2 = connection.prepareStatement(
                                    DELETE_BY_IDS + "(" + idsToDelete.substring(0, idsToDelete.length() - 2) + ")")) {
                                int deletedRecords = ps2.executeUpdate();
                                connection.commit();
                                log.debug("deleted " + deletedRecords +
                                        "report result objects from the report_object schema");
                            }
                        }
                    }
                }
            } catch (SQLException s) {
                connection.rollback();
                log.debug("Could not delete objects older than the given value", s);
                throw new IllegalStoreStateException("Could not delete object older than the given value", s);
            } finally {
                connection.setAutoCommit(oldAutoCommitValue);
            }
        } catch (SQLException s) {
            log.debug("Could obtain or rollback connection", s);
            throw new IllegalStoreStateException("Could obtain or rollback connection", s);
        }
    }

    @NoJavadoc
    public void delete(final String namespace, final String key)
            throws ObjectKeyFormatException, IllegalStoreStateException {
        Executable executable = connection -> {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(GET_OID_BY_KEY)) {
                ps.setString(1, namespace);
                ps.setString(2, key);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // Open the large object for reading
                        Long oid = rs.getLong(1);
                        if (!rs.wasNull()) {
                            LargeObjectManager lobj =
                                    connection.unwrap(org.postgresql.PGConnection.class).getLargeObjectAPI();
                            lobj.delete(oid);
                        }
                    }
                    try (PreparedStatement ps2 = connection.prepareStatement(DELETE_BY_KEY)) {
                        ps2.setString(1, namespace);
                        ps2.setString(2, key);
                        ps2.executeUpdate();
                        connection.commit();
                    }
                }
            }
        };
        try {
            log.debug("Deleting for key {}:{}", namespace, key);
            runWithConnection(namespace, key, executable);
        } catch (DuplicateObjectKeyException ignored) {
            // never happens for delete
        }
    }

    @Override
    public boolean isExists(String namespace, String key) throws IllegalStoreStateException {
        boolean result = false;
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement psCheck = connection.prepareStatement(CHECK_IF_EXISTS)) {
                psCheck.setString(1, namespace);
                psCheck.setString(2, key);
                try (ResultSet rsCheck = psCheck.executeQuery()) {
                    if (rsCheck.isBeforeFirst()) {
                        result = true;
                    }
                }
            }
        } catch (SQLException s) {
            log.debug("Could obtain connection", s);
            throw new IllegalStoreStateException("Could obtain or rollback connection", s);
        }
        return result;
    }

    private void runWithConnection(String namespace, String key, Executable executable)
            throws IllegalStoreStateException, ObjectKeyFormatException, DuplicateObjectKeyException {
        isKeyAcceptable(namespace);
        isKeyAcceptable(key);
        try (Connection connection = dataSource.getConnection()) {
            boolean oldAutoCommitValue = connection.getAutoCommit();
            try {
                executable.execute(connection);
            } catch (SQLException s) {
                connection.rollback();
                throw new IllegalStoreStateException(
                        "Unable to execute database transaction for " + namespace + ':' + key,
                        s);
            } catch (IOException e) {
                connection.rollback();
                throw new IllegalStoreStateException(
                        "IO error while executing database transaction for " + namespace + ':' + key,
                        e);
            } finally {
                connection.setAutoCommit(oldAutoCommitValue);
            }
        } catch (SQLException s) {
            throw new IllegalStoreStateException(
                    "Error obtaining or rolling back connection for " + namespace + ':' + key,
                    s);
        }
    }

    private interface Executable {
        void execute(Connection connection)
                throws SQLException, IOException, ObjectKeyFormatException, DuplicateObjectKeyException;
    }

    @Override
    public Collection<BlobStoreLink> list() {
        List<BlobStoreLink> retVal = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(LIST_KEYS_AND_HEADERS)) {
            while (rs.next()) {
                String namespace = rs.getString("f_namespace");
                String key = rs.getString("f_key");
                Map<String, String> headers = JsonUtil.readMap(rs.getString("f_headers"));
                retVal.add(new BlobStoreLink(namespace, key, headers));
            }
        } catch (SQLException e) {
            log.debug("Could not obtain connection", e);
            throw new IllegalStoreStateException("Could not obtain connection", e);
        }
        return retVal;
    }
}
