// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.database;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Consumer;

/**
 * Created with IntelliJ IDEA.
 * User: liebea
 * Date: 8/30/12
 * Time: 11:07 AM
 */
public interface NativeQueryService {

    /**
     * Returns a single result from an sql query
     *
     * @param sql select statement that expect a single row to be returned
     * @param converter converter to parse the row returned by the sql
     * @param params optional sql bind variables
     * @param <T> expected object type
     *
     * @return the selected object or null if not exist
     *
     * @throws NativeQueryException if more than one row resulted or sql exception
     */
    <T> T getSingleValue(String sql, NativeQueryRowBatchConverter<T> converter, List<Object> params);

    /**
     * Get a list of objects by an sql statement
     *
     * @param sql sql statement
     * @param converter converter to parse fetched rows
     * @param params sql bind variables
     * @param <T> the object type we expect as return value
     *
     * @return list of objects returned by the sql statement and converted using the converter supplied
     *
     * @throws NativeQueryException kaboom
     */
    <T> List<T> getList(String sql, NativeQueryRowBatchConverter<T> converter, List<Object> params);

    /**
     * Get a list of objects by an sql statement
     *
     * @param sql query to execute
     * @param nativeQueryHandler Native query handler
     * @param params sql parameters
     *
     * @return list of converted classes
     */
    <T> List<T> getList(String sql, NativeQueryHandler<List<T>> nativeQueryHandler, List<Object> params);

    /**
     * Get a list of objects by an sql statement
     *
     * @param sql sql statement
     * @param converter converter to parse fetched rows
     * @param <T> the object type we expect as return value
     *
     * @return list of objects returned by the sql statement and converted using the converter supplied
     *
     * @throws NativeQueryException kaboom
     */
    <T> List<T> getList(String sql, NativeQueryRowBatchConverter<T> converter);

    /***
     * Iterate over a result set, invoke a handler, however not return anything
     * @param sql sql to execute
     * @param converter query converter that does not return anything
     */
    void handleResultSet(String sql, NativeQueryRowBatchConverter<Void> converter);

    /***
     * Iterate over a result set, invoke a handler, however not return anything
     * @param sql sql to execute
     * @param converter query converter that does not return anything
     * @param params optional - query parameters
     */
    void handleResultSet(String sql, NativeQueryRowBatchConverter<Void> converter, List<Object> params);

    /***
     * Iterate over a result set, invoke a handler, however not return anything
     * @param sql sql to execute
     * @param converter query converter that does not return anything
     * @param params optional - query parameters
     * @param fetchSpecification the desired fetch behaviour
     */
    void handleResultSet(
            String sql,
            NativeQueryRowBatchConverter<Void> converter,
            List<Object> params,
            FetchSpecification fetchSpecification);

    /**
     * In rare cases code needs to interact with resultSet object itself (e.g. to interact with resultSet metadata or
     * have custom logic for how to iterate the rows, this method exposes the nativeQueryHandler interface allowing
     * to interact with the raw resultSet object and return a value (or Void if no ret val is needed)
     *
     * @param sql sql to execute
     * @param nativeQueryHandler handler to handle resultSet
     * @param params optional query parameters
     * @param <T> generics for return value
     *
     * @return return value delegated from the handler
     */
    <T> T executeNativeQueryAndInteractWithRawResultSet(
            String sql,
            NativeQueryHandler<T> nativeQueryHandler,
            List<Object> params);

    /**
     * In rare cases code needs to interact with resultSet object itself (e.g. to interact with resultSet metadata or
     * have custom logic for how to iterate the rows, this method exposes the nativeQueryHandler interface allowing
     * to interact with the raw resultSet object and return a value (or Void if no ret val is needed)
     *
     * @param sql sql to execute
     * @param nativeQueryHandler handler to handle resultSet
     * @param params optional query parameters
     * @param <T> generics for return value
     * @param fetchSpecification the desired fetch behaviour
     *
     * @return return value delegated from the handler
     */
    <T> T executeNativeQueryAndInteractWithRawResultSet(
            String sql,
            NativeQueryHandler<T> nativeQueryHandler,
            List<Object> params,
            FetchSpecification fetchSpecification);

    /**
     * Get a set of objects by an sql statement
     *
     * @param sql sql statement
     * @param converter converter to parse fetched rows
     * @param params sql bind variables
     * @param <T> the object type we expect as return value
     *
     * @return set of objects returned by the sql statement and converted using the converter supplied
     *
     * @throws NativeQueryException kaboom
     */
    <T> Set<T> getSet(String sql, NativeQueryRowBatchConverter<T> converter, List<Object> params);

    /**
     * Get a Set of objects by an sql statement
     *
     * @param sql sql statement
     * @param converter converter to parse fetched rows
     * @param <T> the object type we expect as return value
     *
     * @return set of objects returned by the sql statement and converted using the converter supplied
     *
     * @throws NativeQueryException kaboom
     */
    <T> Set<T> getSet(String sql, NativeQueryRowBatchConverter<T> converter);

    /**
     * Return a map of entities by executing an sql statement
     *
     * @param sql sql select statement to run
     * @param keyConverter converter to parse key of the map by the fetched rows
     * @param valueConverter converter to parse value of the map by the fetched rows
     * @param params sql bind variables
     * @param <KeyT> type to use for the map's key
     * @param <ValueT> type to use for the map's value
     * @param keyPos position of keyIndexField - null means moving responsibility to converter
     * @param valuePos position of valueIndexField - null means moving responsibility to converter
     *
     * @return map of objects returned by the sql statement converted using the supplied converters
     */
    <KeyT, ValueT> Map<KeyT, ValueT> getMap(
            String sql,
            NativeQueryRowBatchConverter<KeyT> keyConverter,
            NativeQueryRowBatchConverter<ValueT> valueConverter,
            List<Object> params,
            Integer keyPos,
            Integer valuePos);

    /**
     * Return a map of entities by executing an sql statement
     *
     * @param sql sql select statement to run
     * @param keyConverter converter to parse key of the map by the fetched rows
     * @param valueConverter converter to parse value of the map by the fetched rows
     * @param params sql bind variables
     * @param <KeyT> type to use for the map's key
     * @param <ValueT> type to use for the map's value
     *
     * @return map of objects returned by the sql statement converted using the supplied converters
     */
    <KeyT, ValueT> Map<KeyT, ValueT> getMap(
            String sql,
            NativeQueryRowBatchConverter<KeyT> keyConverter,
            NativeQueryRowBatchConverter<ValueT> valueConverter,
            List<Object> params);

    /**
     * Return a map of entities list by executing an sql statement
     *
     * @param sql sql select statement to run
     * @param keyConverter converter to parse key of the map by the fetched rows
     * @param valueConverter converter to parse each list value of the map by the fetched rows
     * @param params sql bind variables
     * @param <KeyT> type to use for the map's key
     * @param <ValueT> type to use for the map's value
     *
     * @return map of objects returned by the sql statement converted using the supplied converters
     */
    <KeyT, ValueT> Map<KeyT, List<ValueT>> getMapOfList(
            String sql,
            NativeQueryRowBatchConverter<KeyT> keyConverter,
            NativeQueryRowBatchConverter<ValueT> valueConverter,
            List<Object> params);

    /**
     * Return a map of entities set by executing an sql statement
     *
     * @param sql sql select statement to run
     * @param keyConverter converter to parse key of the map by the fetched rows
     * @param valueConverter converter to parse each set value of the map by the fetched rows
     * @param params sql bind variables
     * @param <KeyT> type to use for the map's key
     * @param <ValueT> type to use for the map's value
     *
     * @return map of objects returned by the sql statement converted using the supplied converters
     */
    <KeyT, ValueT> Map<KeyT, Set<ValueT>> getMapOfSet(
            String sql,
            NativeQueryRowBatchConverter<KeyT> keyConverter,
            NativeQueryRowBatchConverter<ValueT> valueConverter,
            List<Object> params);

    /**
     * Return a map of entities map by executing an sql statement
     *
     * @param sql sql select statement to run
     * @param outerKeyConverter converter to parse key of the outer map by the fetched rows
     * @param innerKeyConverter converter to parse key of the inner map by the fetched rows
     * @param valueConverter converter to parse each set value of the inner map by the fetched rows
     * @param params sql bind variables
     * @param <OuterKeyT> type to use for the map's key ??
     * @param <ValueT> type to use for the map's value
     *
     * @return map of objects returned by the sql statement converted using the supplied converters
     */
    <OuterKeyT, ValueT, InnerKeyT> Map<OuterKeyT, Map<InnerKeyT, ValueT>> getMapOfMap(
            String sql,
            NativeQueryRowBatchConverter<OuterKeyT> outerKeyConverter,
            NativeQueryRowBatchConverter<InnerKeyT> innerKeyConverter,
            NativeQueryRowBatchConverter<ValueT> valueConverter,
            List<Object> params);

    /**
     * Return a map of entities map by executing an sql statement
     *
     * @param sql sql select statement to run
     * @param outerKeyConverter converter to parse key of the outer map by the fetched rows
     * @param innerKeyConverter converter to parse key of the inner map by the fetched rows
     * @param valueConverter converter to parse each set value of the inner map by the fetched rows
     * @param params sql bind variables
     * @param <ValueT> type to use for the map's value
     *
     * @return map of objects returned by the sql statement converted using the supplied converters
     */
    <OuterKeyT, ValueT, InnerKeyT> Map<OuterKeyT, SortedMap<InnerKeyT, ValueT>> getMapOfSortedMap(
            String sql,
            NativeQueryRowBatchConverter<OuterKeyT> outerKeyConverter,
            NativeQueryRowBatchConverter<InnerKeyT> innerKeyConverter,
            NativeQueryRowBatchConverter<ValueT> valueConverter,
            List<Object> params);

    /**
     * @param sql sql statement
     * @param params query params
     * @param reader Native Query reader
     */
    void getStream(String sql, List<Object> params, AbstractNativeQueryService.NativeQueryReader reader);

    /**
     * Execute a DML command against the database
     *
     * @param sql dml command
     *
     * @return number of rows affected by the execution of the sql statement
     *
     * @throws NativeQueryException naughty exception
     */
    int executeUpdate(String sql);

    /**
     * Execute a DML command against the database
     *
     * @param sql dml command
     * @param params sql bind parameters
     *
     * @return number of rows affected by the execution of the sql statement
     *
     * @throws NativeQueryException naughty exception
     */
    int executeUpdate(String sql, List<Object> params);

    /**
     * Execute a batch DML command against the database
     *
     * @param sql dml command
     * @param params list of list of sql bind parameters
     *
     * @return Array of number of rows updated by each execution of the bind variables in the same order supplied
     *
     * @throws NativeQueryException kaboom!
     */
    int[] executeUpdates(String sql, List<List<Object>> params);

    /**
     * Obtain the URL of the datasource that is associated with this service
     *
     * @return connection url
     */
    default String getURL() {
        final String[] r = {null};
        readDatabaseMetadata(databaseMetaData -> {
            try {
                r[0] = databaseMetaData.getURL();
            } catch (SQLException e) {
                throw new IllegalStateException("Failed getting db url");
            }
        });
        return r[0];
    }

    /**
     * Read db metadata
     *
     * @param metaDataConsumer metadata consumer
     */
    void readDatabaseMetadata(Consumer<DatabaseMetaData> metaDataConsumer);
}
