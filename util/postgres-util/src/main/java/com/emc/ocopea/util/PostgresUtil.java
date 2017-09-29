// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util;

import com.emc.ocopea.util.database.NativeQueryService;
import com.emc.ocopea.util.io.StreamUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class PostgresUtil {

    private static final Logger logger = LoggerFactory.getLogger(PostgresUtil.class);
    private static Map<String, HikariDataSource> pooledDataSources = new HashMap<>();
    private static Boolean isTestMode = null;

    // non instantiable
    private PostgresUtil() {
    }

    private static boolean isTestMode(NativeQueryService nqs) {
        if (isTestMode == null) {
            isTestMode = calculateTestMode(nqs);
        }
        return isTestMode;
    }

    private static synchronized boolean calculateTestMode(NativeQueryService nqs) {
        final boolean[] testMode = {false};
        nqs.readDatabaseMetadata(databaseMetaData -> {
            try {
                if (databaseMetaData.getDatabaseProductName().equals("H2")) {
                    testMode[0] = true;
                }
            } catch (SQLException e) {
                //swallow..
                e.printStackTrace();
            }
        });

        return testMode[0];
    }

    /**
     * Checks whether a schema exists in a data source
     */
    public static boolean isSchemaExists(DataSource dataSource, String schemaName) {
        try (java.sql.Connection connection = dataSource.getConnection();
             ResultSet schemas = connection.getMetaData().getSchemas()) {
            while (schemas.next()) {
                if (schemas.getString("TABLE_SCHEM").equalsIgnoreCase(schemaName)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed validating whether schema " + schemaName +
                    " exists in data source");
        }
        return false;
    }

    /**
     * Checks whether a table exists in a data source schema
     */
    public static boolean isTableExists(DataSource dataSource, String schemaName, String tableName) {
        String schema = schemaName.toLowerCase();
        String table = tableName.toLowerCase();

        try (java.sql.Connection connection = dataSource.getConnection();
             ResultSet tables = connection.getMetaData().getTables(null, schema, table, null)) {
            while (tables.next()) {
                if (tables.getString("TABLE_NAME").equalsIgnoreCase(table)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed validating if table " + schemaName + "." + tableName + " exists");
        }

        return false;
    }


    /**
     * Write a JSONB parameter to Postgres.
     *
     * @param object a json serializable object
     *
     * @return JSON representation supported by the underlying database (e.g. String for H2, PGObject for Postgres)
     */
    public static <T> Object objectToJsonBParameter(T object, NativeQueryService nqs) {
        return jsonStringToJsonBParameter(JsonUtil.toJson(object), nqs);
    }

    /**
     * Write a JSONB parameter to Postgres.
     *
     * @param objectJsonString a json serialization of the object to be persisted
     *
     * @return JSON representation supported by the underlying database (e.g. String for H2, PGObject for Postgres)
     */
    public static Object jsonStringToJsonBParameter(String objectJsonString, NativeQueryService nqs) {
        if (isTestMode(nqs)) {
            return objectJsonString;
        }
        try {
            return getJsonB(objectJsonString);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed writing jsonb parameter. objectJsonString=" + objectJsonString, e);
        }
    }

    /**
     * Converts a JsonB read from the database to an object
     *
     * @param objectJson a JsonB that was read from the database
     * @param clazz class of the resulting object
     *
     * @return an object represented by the json
     */
    public static <T> T fromJsonB(String objectJson, Class<T> clazz) {
        return JsonUtil.fromJson(clazz, objectJson);
    }

    /**
     * Converts a JsonB reader from the database to an object
     *
     * @param reader a JsonB reader from the database
     * @param clazz class of the resulting object
     *
     * @return an object represented by the json
     */
    public static <T> T fromJsonB(Reader reader, Class<T> clazz) {
        return JsonUtil.fromJson(clazz, reader);
    }

    /**
     * Retrieve pooled data source based on configuration. If data source with the same databaseName and
     * databaseSchemaName and dbUser already exists method will return existing one.
     */
    public static synchronized DataSource getDataSource(
            String databaseName, String server, int port, String dbUser,
            String dbPassword, int maxConnections,
            String databaseSchemaName) {

        String poolIdentifier = server + "!" + databaseName + "!" + databaseSchemaName + "!" + dbUser;

        HikariDataSource pooledDataSource = pooledDataSources.get(poolIdentifier);

        if (pooledDataSource == null) {
            logger.info("Initializing connection pool {} with maxConnections {}", poolIdentifier, maxConnections);
            HikariConfig config = new HikariConfig();
            config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
            config.setMaximumPoolSize(maxConnections);
            config.setPoolName(poolIdentifier);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);
            config.addDataSourceProperty("serverName", server);
            config.addDataSourceProperty("databaseName", databaseName);
            config.addDataSourceProperty("portNumber", port);
            config.setConnectionInitSql("SET search_path='" + databaseSchemaName + "'");
            config.setMinimumIdle(maxConnections);

            pooledDataSource = new HikariDataSource(config);
            pooledDataSources.put(poolIdentifier, pooledDataSource);
        }
        return pooledDataSource;
    }

    public interface ObjectWriter<T> {
        void writeObject(T var1, OutputStream var2) throws IOException;
    }

    /**
     * Reads a blob from pg using an oid
     */
    public static void readBlob(DataSource dataSource, long oid, Consumer<InputStream> consumer) {
        try (java.sql.Connection connection = dataSource.getConnection()) {
            runWithAutoCommitFalse(connection, (conn) -> {
                LargeObject obj = conn.unwrap(org.postgresql.PGConnection.class).getLargeObjectAPI().open(
                        oid,
                        LargeObjectManager.READ);
                try {
                    consumer.accept(obj.getInputStream());
                } finally {
                    // Close the object
                    obj.close();
                }
                return null;
            });
        } catch (SQLException e) {
            throw new IllegalStateException("Failed reading blob from pg with oid " + oid, e);
        }
    }

    private interface PGTask<T> {
        T run(Connection connection) throws SQLException;
    }

    private static <T> T runWithAutoCommitFalse(Connection connection, PGTask<T> action) throws SQLException {
        final boolean setAutoCommit = connection.getAutoCommit();
        if (setAutoCommit) {
            connection.setAutoCommit(false);
        }
        try {
            return action.run(connection);
        } finally {
            if (setAutoCommit) {
                connection.commit();
                connection.setAutoCommit(true);
            }
        }

    }

    /**
     * Stores a blob in pg and returns its oid
     */
    public static long storeBlob(DataSource dataSource, InputStream inputStream) {
        return storeBlob(dataSource, out -> StreamUtil.copy(inputStream, out));
    }

    /**
     * Stores a blob in pg and returns its oid
     */
    public static long storeBlob(DataSource dataSource, Consumer<OutputStream> writer) {
        try (java.sql.Connection connection = dataSource.getConnection()) {
            return runWithAutoCommitFalse(connection, (conn -> {
                LargeObjectManager largeObjectManager =
                        connection.unwrap(org.postgresql.PGConnection.class).getLargeObjectAPI();
                long oid = largeObjectManager.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
                LargeObject obj = largeObjectManager.open(oid, LargeObjectManager.WRITE);
                try {
                    writer.accept(obj.getOutputStream());
                } finally {
                    obj.close();
                }
                return oid;
            }));
        } catch (SQLException e) {
            throw new IllegalStateException("Failed storing blob on pg", e);
        }
    }

    public static PGobject getJsonB(String value) throws SQLException {
        PGobject pgObject = new PGobject();
        pgObject.setValue(value);
        pgObject.setType("jsonb");
        return pgObject;
    }

    /**
     * Map a string to a legal identifier in Postgres. Not one-to-one.
     *
     * @param unsanitizedIdentifier any string
     *
     * @return a valid Postgres identifier resembling the argument
     */
    public static String sanitizeIdentifier(String unsanitizedIdentifier) {
        String s = unsanitizedIdentifier.replaceAll("-", "_");
        s = s.replaceAll("\\W", "");
        if (Character.isDigit(s.charAt(0))) {
            s = "_" + s;
        }
        return s;
    }
}
