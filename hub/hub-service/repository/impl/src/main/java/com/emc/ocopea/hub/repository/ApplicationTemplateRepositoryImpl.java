// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import com.emc.microservice.Context;
import com.emc.microservice.blobstore.IllegalStoreStateException;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.ocopea.util.PostgresUtil;
import com.emc.ocopea.util.database.BasicNativeQueryService;
import com.emc.ocopea.util.database.NativeQueryService;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 7/24/16.
 * Drink responsibly
 */
public class ApplicationTemplateRepositoryImpl
        extends AbstractHubRepositoryImpl
        implements ApplicationTemplateRepository, ServiceLifecycle {

    private static final String SQL_SELECT = "SELECT * FROM applicationTemplate";
    private static final String SQL_SELECT_BY_ID = SQL_SELECT + " WHERE id=?";
    private static final String SQL_SELECT_BY_NAME = SQL_SELECT + " WHERE name=?";

    private NativeQueryService nqs;
    private final String updateSql = "UPDATE applicationTemplate " +
            "SET data=" + getJsonBParam() + ", dateModified=? " +
            "WHERE id=?";
    private final String insertSql = "INSERT INTO applicationTemplate " +
            "(id, name, dateModified, data) " +
            "VALUES (?,?,?," + getJsonBParam() + ")";

    public ApplicationTemplateRepositoryImpl() {}

    ApplicationTemplateRepositoryImpl(DataSource dataSource) {
        initialize(dataSource);
    }

    @Override
    public void init(Context context) {
        initialize(context.getDatasourceManager().getManagedResourceByName("hub-db").getDataSource());
    }

    private void initialize(DataSource dataSource) {
        nqs = new BasicNativeQueryService(dataSource);
    }

    @Override
    public void shutDown() {
    }

    @Override
    public void createApplicationTemplate(DBApplicationTemplate newAppTemplate) throws DuplicateResourceException {
        execWithRefinedDuplicateResourceException(() -> {
            return nqs.executeUpdate(insertSql, Arrays.asList(
                    newAppTemplate.getId(),
                    newAppTemplate.getName(),
                    System.currentTimeMillis(),
                    writeJsonBParameter(newAppTemplate, nqs))
            );
        });
    }

    @Override
    public DBApplicationTemplate getById(UUID appTemplateId) {
        return nqs.getSingleValue(SQL_SELECT_BY_ID, this::convertRow, Collections.singletonList(appTemplateId));
    }

    @Override
    public DBApplicationTemplate findByName(String appTemplateName) {
        return nqs.getSingleValue(SQL_SELECT_BY_NAME, this::convertRow, Collections.singletonList(appTemplateName));
    }

    @Override
    public Collection<DBApplicationTemplate> list() {
        return nqs.getList(SQL_SELECT, this::convertRow);
    }

    private DBApplicationTemplate convertRow(ResultSet rset, int pos) throws SQLException {
        try (Reader data = rset.getCharacterStream("data")) {
            DBApplicationTemplate template = PostgresUtil.fromJsonB(data, DBApplicationTemplate.class);
            return template.withNonSerializedFields(
                    UUID.fromString(rset.getString("id")),
                    rset.getString("name"),
                    new Date(rset.getLong("dateModified")));
        } catch (IOException e) {
            throw new IllegalStoreStateException("Failed reading data for " +
                    DBApplicationTemplate.class.getSimpleName(), e);
        }
    }

    @Override
    public void markAppTemplateAsDeleted(UUID appTemplateId) {
        DBApplicationTemplate template = getById(appTemplateId);
        if (template == null) {
            throw new IllegalArgumentException("found no application template with id " + appTemplateId.toString());
        }
        template = template.asDeleted();
        nqs.executeUpdate(updateSql, Arrays.asList(
                writeJsonBParameter(template, nqs),
                System.currentTimeMillis(),
                template.getId())
        );
    }
}
