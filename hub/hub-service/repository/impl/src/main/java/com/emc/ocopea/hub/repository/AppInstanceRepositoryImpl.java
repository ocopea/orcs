// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import com.emc.microservice.Context;
import com.emc.microservice.datasource.MicroServiceDataSource;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.ocopea.util.Pair;
import com.emc.ocopea.util.database.BasicNativeQueryService;
import com.emc.ocopea.util.database.NativeQueryException;
import com.emc.ocopea.util.database.NativeQueryService;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Created by liebea on 8/14/16.
 * Drink responsibly
 */
public class AppInstanceRepositoryImpl
        extends AbstractHubRepositoryImpl
        implements AppInstanceRepository, ServiceLifecycle {

    private static final String SQL_INSERT_CONFIG =
            "INSERT INTO appInstanceConfig (" +
                "id," +
                "name," +
                "appTemplateId," +
                "deploymentType," +
                "creatorUserId," +
                "baseAppInstanceId," +
                "baseSavedImageId," +
                "createdOn," +
                "dateModified," +
                "siteId, " +
                "data) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
    private static final String SQL_GET_CONFIG = "SELECT * FROM appInstanceConfig";
    private static final String SQL_GET_CONFIG_BY_BASE_APP_ID =
            "SELECT *" +
            " FROM appInstanceConfig" +
            " WHERE baseAppInstanceId=?";
    private static final String SQL_GET_CONFIG_BY_ID = SQL_GET_CONFIG + " WHERE id=?";
    private static final String SQL_GET_CONFIG_BY_NAME = SQL_GET_CONFIG + " WHERE name=?";

    private static final String SQL_INSERT_STATE =
            "INSERT INTO appInstanceState" +
            " (appInstanceId,state,url,dateModified,data)" +
            " VALUES (?,?,?,?,?)";
    private static final String SQL_GET_STATE = "SELECT * FROM appInstanceState";
    private static final String SQL_GET_STATE_BY_ID = SQL_GET_STATE + " WHERE appInstanceId=?";
    private static final String SQL_UPDATE_STATE_BY_ID =
            "UPDATE appInstanceState" +
            " SET dateModified=?, state=?" +
            " WHERE appInstanceId=?";
    private static final String SQL_UPDATE_STATE_AND_URL_BY_ID =
            "UPDATE appInstanceState" +
            " SET dateModified=?, state=?, url=?" +
            " WHERE appInstanceId=?";


    //Reporting
    private static final String SQL_GET_CONFIG_WITH_STATE =
            "SELECT c.*, " +
                    "s.appInstanceId AS s_appInstanceId, " +
                    "s.state AS s_state, " +
                    "s.url AS s_url, " +
                    "s.dateModified AS s_dateModified, " +
                    "s.data AS s_data " +
            "FROM appInstanceConfig c, appInstanceState s " +
            "WHERE c.id=s.appInstanceId";

    private static final String SQL_GET_CONFIG_WITH_STATE_BY_ID =
            SQL_GET_CONFIG_WITH_STATE + " and " +
            "c.id=?";

    private NativeQueryService nqs;
    private MicroServiceDataSource dataSource;

    public AppInstanceRepositoryImpl() {}

    public AppInstanceRepositoryImpl(MicroServiceDataSource dataSource) {
        initialize(dataSource);
    }

    private DBAppInstanceConfig convertConfigRow(ResultSet rset, int pos) throws SQLException {
        return new DBAppInstanceConfig(
                getUuid(rset, "id"),
                rset.getString("name"),
                getUuid(rset, "appTemplateId"),
                rset.getString("deploymentType"),
                getUuid(rset, "creatorUserId"),
                getUuid(rset, "baseAppInstanceId"),
                getUuid(rset, "baseSavedImageId"),
                getDate(rset, "createdOn"),
                getDate(rset, "dateModified"),
                getUuid(rset, "siteId")
        );
    }

    private DBAppInstanceState convertStateRow(ResultSet rset, int pos) throws SQLException {
        return convertStateRow("", rset, pos);
    }

    private DBAppInstanceState convertStateRow(String prefix, ResultSet rset, int pos) throws SQLException {
        return new DBAppInstanceState(
                getUuid(rset, prefix + "appInstanceId"),
                rset.getString(prefix + "state"),
                getDate(rset, prefix + "dateModified"),
                getUri(rset, prefix + "url"));
    }

    @Override
    public Collection<DBAppInstanceConfig> listConfig() {
        return nqs.getList(SQL_GET_CONFIG, this::convertConfigRow);
    }

    @Override
    public Collection<Pair<DBAppInstanceConfig, DBAppInstanceState>> listConfigWithState() {
        return nqs.getList(
                SQL_GET_CONFIG_WITH_STATE,
                this::convertToPair);
    }

    @Override
    public Pair<DBAppInstanceConfig, DBAppInstanceState> getConfigWithState(UUID appInstanceId) {
        return nqs.getSingleValue(
                SQL_GET_CONFIG_WITH_STATE_BY_ID,
                this::convertToPair,
                Collections.singletonList(appInstanceId));
    }

    private Pair<DBAppInstanceConfig, DBAppInstanceState> convertToPair(ResultSet rset, int pos) throws SQLException {
        return new Pair<>(
                AppInstanceRepositoryImpl.this.convertConfigRow(rset, pos),
                AppInstanceRepositoryImpl.this.convertStateRow("s_", rset, pos)
        );
    }

    @Override
    public Collection<DBAppInstanceConfig> listDownstreamConfig(UUID appInstanceId) {
        return nqs.getList(
                SQL_GET_CONFIG_BY_BASE_APP_ID,
                this::convertConfigRow,
                Collections.singletonList(appInstanceId));
    }

    @Override
    public DBAppInstanceConfig getConfig(UUID appInstanceId) {
        return nqs.getSingleValue(
                SQL_GET_CONFIG_BY_ID,
                this::convertConfigRow,
                Collections.singletonList(appInstanceId));
    }

    @Override
    public DBAppInstanceConfig findConfig(String appInstanceName) {
        return nqs.getSingleValue(
                SQL_GET_CONFIG_BY_NAME,
                this::convertConfigRow,
                Collections.singletonList(appInstanceName));
    }

    @Override
    public DBAppInstanceState getState(UUID appInstanceId) {
        return nqs.getSingleValue(SQL_GET_STATE_BY_ID, this::convertStateRow, Collections.singletonList(appInstanceId));
    }

    @Override
    public void updateStateAndUrl(UUID appInstanceId, String state, URI url) {
        nqs.executeUpdate(SQL_UPDATE_STATE_AND_URL_BY_ID, Arrays.asList(
                System.currentTimeMillis(),
                state,
                url.toString(),
                appInstanceId));
    }

    @Override
    public void updateState(UUID appInstanceId, String state) {
        nqs.executeUpdate(SQL_UPDATE_STATE_BY_ID, Arrays.asList(System.currentTimeMillis(), state, appInstanceId));
    }

    @Override
    public void add(DBAppInstanceConfig config, DBAppInstanceState state) throws DuplicateResourceException {
        if (!state.getAppInstanceId().equals(config.getId())) {
            throw new IllegalArgumentException("AppInstanceId must match for config and sate");
        }
        try {
            dataSource.beginTransaction();
            nqs.executeUpdate(SQL_INSERT_CONFIG, Arrays.asList(
                    config.getId(),
                    config.getName(),
                    config.getAppTemplateId(),
                    config.getDeploymentType(),
                    config.getCreatorUserId(),
                    config.getBaseAppInstanceId(),
                    config.getBaseSavedImageId(),
                    config.getCreatedDate(),
                    config.getDateModified(),
                    config.getSiteId(),
                    null));
            nqs.executeUpdate(SQL_INSERT_STATE, Arrays.asList(
                    config.getId(),
                    state.getState(),
                    state.getUrl(),
                    state.getDateModified(),
                    null));
        } catch (NativeQueryException nqe) {
            final SQLException sqlException = nqe.getSQLException();
            if (sqlException != null && sqlException.getSQLState() != null &&
                    sqlException.getSQLState().startsWith("23505")) {
                throw new DuplicateResourceException(nqe);
            } else {
                throw nqe;
            }
        } catch (Exception e) {
            dataSource.rollbackTransaction();
            throw e;
        } finally {
            dataSource.commitTransaction();
        }
    }

    @Override
    public void init(Context context) {
        initialize(context.getDatasourceManager().getManagedResourceByName("hub-db").getDataSource());
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    private void initialize(MicroServiceDataSource dataSource) {
        this.nqs = new BasicNativeQueryService(dataSource);
        this.dataSource = dataSource;
    }

    @Override
    public void shutDown() {}
}
