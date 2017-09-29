// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.database;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Consumer;

/**
 * Created with IntelliJ IDEA. User: liebea Date: 8/30/12 Time: 11:10 AM
 */
public abstract class AbstractNativeQueryService implements NativeQueryService {
    public static final String DUMMY_NULL = "@@@@@DUMMY_NULL";
    private static final Logger logger = LoggerFactory.getLogger(AbstractNativeQueryService.class);

    protected abstract DataSource getDataSource();

    protected <T> T executeNativeQuery(String sql, NativeQueryHandler<T> nativeQueryHandler, List<Object> params) {
        return executeNativeQuery(sql, nativeQueryHandler, params, null);
    }

    protected <T> T executeNativeQuery(
            String sql,
            NativeQueryHandler<T> nativeQueryHandler,
            List<Object> params,
            FetchSpecification fetchSpecification) {
        logger.debug("executing query: {}", sql);
        long tickBefore = -1;
        long tickAfter = -1;

        try (Connection conn = getDataSource().getConnection();
             PreparedStatement statement = setPreparedStatement(conn, sql, params)
        ) {

            Boolean previousAutoCommiValueT = null;
            try {
                if (fetchSpecification != null && fetchSpecification.getFetchBehaviour() ==
                        FetchSpecification.FetchBehaviour.SUGGEST_SPECIFIC_SIZE) {
                    // Well, the postgres jdbc driver setFetchSize impl is ignored unless autoCommit is turned off
                    // I don't want to get into different behaviours per runtime driver here and for now, we only use
                    // postgres, so...
                    previousAutoCommiValueT = conn.getAutoCommit();
                    conn.setAutoCommit(false);
                    statement.setFetchSize(fetchSpecification.getSpecificFetchSize());
                }

                tickBefore = System.currentTimeMillis();
                try (ResultSet resultSet = statement.executeQuery()) {
                    tickAfter = System.currentTimeMillis();
                    return nativeQueryHandler.handleNativeQueryResult(resultSet);
                }
            } finally {
                if (previousAutoCommiValueT != null) {
                    conn.setAutoCommit(previousAutoCommiValueT);
                }
            }

        } catch (Exception e) {
            throw new NativeQueryException("Failed executing query", sql, params, e);
        } finally {
            if (logger.isDebugEnabled()) {
                logger.debug((sql + "; Params: " + params + " " +
                        (tickBefore != -1 && tickAfter != -1 ? " (Total time=" + (tickAfter - tickBefore) + ")" : "")));
            }
        }
    }

    private void setStatement(PreparedStatement stmt, List<Object> params) throws SQLException {
        stmt.clearParameters();
        if (params != null) {
            int paramOrdinal = 1;
            for (Object param : params) {
                // Convert parameter of type util Date to TimeStamp.
                if (param instanceof Date && !(param instanceof Timestamp)) {
                    param = ((Date) param).getTime();
                }

                if (param == null) {
                    stmt.setObject(paramOrdinal, null);
                    paramOrdinal++;
                } else if (!DUMMY_NULL.equals(param)) {
                    stmt.setObject(paramOrdinal, param);
                    paramOrdinal++;
                }
            }
        }
    }

    private PreparedStatement setPreparedStatement(Connection conn, String sql, List<Object> params)
            throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);
        setStatement(stmt, params);
        return stmt;
    }

    @Override
    public <T> T getSingleValue(
            final String sql,
            final NativeQueryRowBatchConverter<T> converter,
            final List<Object> params) {
        return executeNativeQuery(sql, rset -> {
            if (!rset.next()) {
                return null;
            }
            T singleValue = converter.convertRow(rset, 1);
            if (rset.next()) {
                throw new NativeQueryException("Single row expected but fetched multiple rows", sql, params, null);
            }
            converter.finalizeBatch(Collections.singletonList(singleValue));
            return singleValue;
        }, params);
    }

    @Override
    public <T> List<T> getList(String sql, NativeQueryRowBatchConverter<T> converter, List<Object> params) {
        return executeNativeQuery(sql, new ListNativeQueryHandler<>(converter), params);
    }

    @Override
    public <T> List<T> getList(String sql, NativeQueryHandler<List<T>> nativeQueryHandler, List<Object> params) {
        return executeNativeQuery(sql, nativeQueryHandler, params);
    }

    @Override
    public <T> List<T> getList(String sql, NativeQueryRowBatchConverter<T> converter) {
        return getList(sql, converter, null);
    }

    @Override
    public void handleResultSet(String sql, NativeQueryRowBatchConverter<Void> converter) {
        handleResultSet(sql, converter, null);
    }

    @Override
    public void handleResultSet(String sql, NativeQueryRowBatchConverter<Void> converter, List<Object> params) {
        handleResultSet(sql, converter, params, FetchSpecification.getUseDriverDefault());
    }

    @Override
    public void handleResultSet(
            String sql,
            NativeQueryRowBatchConverter<Void> converter,
            List<Object> params,
            FetchSpecification fetchSpecification) {
        executeNativeQuery(sql, new VoidNativeQueryHandler(converter), params, fetchSpecification);
    }

    @Override
    public <T> T executeNativeQueryAndInteractWithRawResultSet(
            String sql,
            NativeQueryHandler<T> nativeQueryHandler,
            List<Object> params) {
        return executeNativeQueryAndInteractWithRawResultSet(
                sql,
                nativeQueryHandler,
                params,
                FetchSpecification.getUseDriverDefault());
    }

    @Override
    public <T> T executeNativeQueryAndInteractWithRawResultSet(
            String sql,
            NativeQueryHandler<T> nativeQueryHandler,
            List<Object> params,
            FetchSpecification fetchSpecification) {
        return executeNativeQuery(sql, nativeQueryHandler, params, fetchSpecification);
    }

    @Override
    public <T> Set<T> getSet(String sql, NativeQueryRowBatchConverter<T> converter, List<Object> params) {
        return executeNativeQuery(sql, new SetNativeQueryHandler<>(converter), params);
    }

    @Override
    public <T> Set<T> getSet(String sql, NativeQueryRowBatchConverter<T> converter) {
        return getSet(sql, converter, null);
    }

    @Override
    public <KeyT, ValueT> Map<KeyT, ValueT> getMap(
            String sql,
            NativeQueryRowBatchConverter<KeyT> keyConverter,
            NativeQueryRowBatchConverter<ValueT> valueConverter,
            List<Object> params,
            Integer keyPos,
            Integer valuePos) {
        return executeNativeQuery(
                sql,
                new MapNativeQueryHandler<>(keyConverter, valueConverter, keyPos, valuePos),
                params);
    }

    @Override
    public <KeyT, ValueT> Map<KeyT, ValueT> getMap(
            String sql,
            NativeQueryRowBatchConverter<KeyT> keyConverter,
            NativeQueryRowBatchConverter<ValueT> valueConverter,
            List<Object> params) {
        return executeNativeQuery(sql, new MapNativeQueryHandler<>(keyConverter, valueConverter), params);
    }

    @Override
    public <KeyT, ValueT> Map<KeyT, List<ValueT>> getMapOfList(
            String sql, NativeQueryRowBatchConverter<KeyT> keyConverter,
            NativeQueryRowBatchConverter<ValueT> valueConverter, List<Object> params) {
        return executeNativeQuery(sql, new MapOfListNativeQueryHandler<>(keyConverter, valueConverter), params);
    }

    @Override
    public <KeyT, ValueT> Map<KeyT, Set<ValueT>> getMapOfSet(
            String sql, NativeQueryRowBatchConverter<KeyT> keyConverter,
            NativeQueryRowBatchConverter<ValueT> valueConverter, List<Object> params) {
        return executeNativeQuery(sql, new MapOfSetNativeQueryHandler<>(keyConverter, valueConverter), params);
    }

    @Override
    public <OuterKeyT, ValueT, InnerKeyT> Map<OuterKeyT, Map<InnerKeyT, ValueT>> getMapOfMap(
            String sql,
            NativeQueryRowBatchConverter<OuterKeyT> outerKeyConverter,
            NativeQueryRowBatchConverter<InnerKeyT> innerKeyConverter,
            NativeQueryRowBatchConverter<ValueT> valueConverter,
            List<Object> params) {
        return executeNativeQuery(
                sql,
                new MapOfMapNativeQueryHandler<>(outerKeyConverter, innerKeyConverter, valueConverter),
                params);

    }

    @Override
    public <OuterKeyT, ValueT, InnerKeyT> Map<OuterKeyT, SortedMap<InnerKeyT, ValueT>> getMapOfSortedMap(
            String sql,
            NativeQueryRowBatchConverter<OuterKeyT> outerKeyConverter,
            NativeQueryRowBatchConverter<InnerKeyT> innerKeyConverter,
            NativeQueryRowBatchConverter<ValueT> valueConverter,
            List<Object> params) {
        return executeNativeQuery(
                sql,
                new MapOfSortedMapNativeQueryHandler<>(outerKeyConverter, innerKeyConverter, valueConverter),
                params);

    }

    @Override
    public void getStream(String sql, List<Object> params, NativeQueryReader reader) {
        logger.debug("executing query: {}", sql);
        InputStreamNativeQueryHandler nativeQueryHandler = new InputStreamNativeQueryHandler(reader);
        executeNativeQuery(sql, nativeQueryHandler, params);
    }

    public int executeUpdate(String sql) {
        return executeUpdate(sql, (List<Object>) null);
    }

    @NoJavadoc
    // TODO add Javadoc
    public int executeUpdate(String sql, List<Object> params) {
        int retVal = 0;

        try (Connection conn = getDataSource().getConnection();
             PreparedStatement stmt = setPreparedStatement(conn, sql, params)

        ) {
            retVal = stmt.executeUpdate();
        } catch (SQLException e) {
            throw new NativeQueryException("Failed executing update", sql, params, e);
        } finally {
            if (logger.isDebugEnabled()) {
                logger.debug("Sql statement {}, with params {}", sql, params);
            }
        }
        return retVal;
    }

    protected int executeUpdate(String sql, Object[] params) {
        return executeUpdate(sql, (params == null ? null : Arrays.asList(params)));
    }

    @NoJavadoc
    // TODO add Javadoc
    public int[] executeUpdates(String sql, List<List<Object>> params) {
        if (params.isEmpty()) {
            return null;
        }

        int[] retVal = null;

        // first prepare the statement
        List<Object> currParams = null;
        try (Connection conn = getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            for (List<Object> entityParams : params) {
                currParams = entityParams;
                setStatement(stmt, entityParams);
                stmt.addBatch();
            }
            retVal = stmt.executeBatch();
        } catch (SQLException e) {
            throw new NativeQueryException("Failed executing update", sql, currParams, e);
        } finally {
            if (logger.isDebugEnabled()) {
                logger.debug("Sql statement {}, params {}", sql, params);
            }
        }
        return retVal;
    }

    @Override
    public void readDatabaseMetadata(Consumer<DatabaseMetaData> metaDataConsumer) {
        try (final Connection connection = getDataSource().getConnection()) {
            metaDataConsumer.accept(connection.getMetaData());
        } catch (SQLException e) {
            throw new NativeQueryException("Could not obtain db metadata URL", e);
        }
    }

    public interface NativeQueryReader {
        public void read(InputStream in) throws IOException;

    }
}


