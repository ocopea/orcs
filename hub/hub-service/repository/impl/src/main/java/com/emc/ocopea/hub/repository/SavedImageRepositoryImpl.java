// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import com.emc.microservice.Context;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.ocopea.util.database.BasicNativeQueryService;
import com.emc.ocopea.util.database.NativeQueryService;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by liebea on 7/24/16.
 * Drink responsibly
 */
public class SavedImageRepositoryImpl
        extends AbstractHubRepositoryImpl
        implements SavedImageRepository, ServiceLifecycle {

    private static final String SQL_SELECT = "SELECT * FROM savedImage";
    private static final String SQL_SELECT_BY_ID = SQL_SELECT + " WHERE id=?";
    private static final String SQL_SELECT_BY_NAME = SQL_SELECT + " WHERE name=?";
    private static final String SQL_SELECT_BY_APP_TEMPLATE_ID = SQL_SELECT + " WHERE appTemplateId=?";
    private static final String SQL_INSERT_SAVED_IMAGE =
            "INSERT INTO savedImage (" +
                        "id," +
                        "appTemplateId," +
                        "name," +
                        "description," +
                        "tags," +
                        "creatorUserId," +
                        "dateCreated," +
                        "appCopyId," +
                        "siteId," +
                        "baseImageId," +
                        "state)" +
                    " VALUES (?,?,?,?,?,?,?,?,?,?,?)";
    private static final String SQL_UPDATE_IMAGE_STATE = "UPDATE savedImage SET state=? WHERE id=?";

    private NativeQueryService nqs;

    public SavedImageRepositoryImpl() {}

    SavedImageRepositoryImpl(DataSource dataSource) {
        nqs = new BasicNativeQueryService(dataSource);
    }

    @Override
    public void init(Context context) {
        initialize(context.getDatasourceManager().getManagedResourceByName("hub-db").getDataSource());
    }

    private void initialize(DataSource dataSource) {
        nqs = new BasicNativeQueryService(dataSource);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void shutDown() {}

    @Override
    public DBSavedImage get(UUID savedImageId) {
        return nqs.getSingleValue(SQL_SELECT_BY_ID, this::convertRow, Collections.singletonList(savedImageId));
    }

    @Override
    public Collection<DBSavedImage> findByAppTemplateId(UUID appTemplateId) {
        return nqs.getList(SQL_SELECT_BY_APP_TEMPLATE_ID, this::convertRow, Collections.singletonList(appTemplateId));
    }

    @Override
    public void add(DBSavedImage savedImage) throws DuplicateResourceException {
        execWithRefinedDuplicateResourceException(() ->
                nqs.executeUpdate(SQL_INSERT_SAVED_IMAGE,
                        Arrays.asList(
                                savedImage.getId(),
                                savedImage.getAppTemplateId(),
                                savedImage.getName(),
                                savedImage.getDescription(),
                                savedImage.getTags().toString(),
                                savedImage.getCreatorUserId(),
                                System.currentTimeMillis(),
                                savedImage.getAppCopyId(),
                                savedImage.getSiteId(),
                                savedImage.getBaseImageId(),
                                savedImage.getState().name()
                        )
                ));
    }

    @Override
    public Collection<DBSavedImage> list() {
        return nqs.getList(SQL_SELECT, this::convertRow);
    }

    private DBSavedImage convertRow(ResultSet rset, int pos) throws SQLException {
        final String strTags = rset.getString("tags");
        Set<String> tags;
        if (strTags == null || strTags.isEmpty()) {
            tags = Collections.emptySet();
        } else {
            tags = new HashSet<>(Arrays.asList(strTags.substring(1, strTags.length() - 1).split(", ")));
        }
        return new DBSavedImage(
                getUuid(rset, "id"),
                getUuid(rset, "appTemplateId"),
                rset.getString("name"),
                rset.getString("description"),
                getUuid(rset, "creatorUserId"),
                tags,
                new Date(rset.getLong("dateCreated")),
                getUuid(rset, "siteId"),
                getUuid(rset, "appCopyId"),
                getUuid(rset,"baseImageId"),
                DBSavedImage.DBSavedImageState.valueOf(rset.getString("state")));
    }

    @Override
    public void updateImageState(UUID savedImageId, DBSavedImage.DBSavedImageState state) {
        nqs.executeUpdate(SQL_UPDATE_IMAGE_STATE, Arrays.asList(state.name(), savedImageId));
    }
}
