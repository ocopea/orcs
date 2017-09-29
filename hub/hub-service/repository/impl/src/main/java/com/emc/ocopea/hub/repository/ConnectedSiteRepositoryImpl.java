// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import com.emc.microservice.Context;
import com.emc.microservice.blobstore.IllegalStoreStateException;
import com.emc.microservice.serialization.SerializationReader;
import com.emc.microservice.serialization.SerializationWriter;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.ocopea.util.database.BasicNativeQueryService;
import com.emc.ocopea.util.database.NativeQueryService;
import org.apache.commons.io.input.ReaderInputStream;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 8/1/16.
 * Drink responsibly
 */
public class ConnectedSiteRepositoryImpl extends AbstractHubRepositoryImpl
        implements ConnectedSiteRepository, ServiceLifecycle {

    private static final String SQL_SELECT = "SELECT * FROM connectedSite";
    private static final String SQL_SELECT_BY_ID = SQL_SELECT + " WHERE id=?";
    private static final String SQL_SELECT_BY_URN = SQL_SELECT + " WHERE urn=?";

    private NativeQueryService nqs;
    private SerializationWriter<DbConnectedSite> writer;
    private SerializationReader<DbConnectedSite> reader;

    ConnectedSiteRepositoryImpl(
            DataSource dataSource,
            SerializationReader<DbConnectedSite> reader,
            SerializationWriter<DbConnectedSite> writer) {
        initialize(dataSource, reader, writer);
    }

    public ConnectedSiteRepositoryImpl() {
        // Default Constructor allowing lifecycle init
    }

    @Override
    public void init(Context context) {
        initialize(
                context.getDatasourceManager().getManagedResourceByName("hub-db").getDataSource(),
                context.getSerializationManager().getReader(DbConnectedSite.class),
                context.getSerializationManager().getWriter(DbConnectedSite.class));
    }

    @Override
    public void shutDown() {
        // Noting on shutdown
    }

    private void initialize(
            DataSource dataSource,
            SerializationReader<DbConnectedSite> reader,
            SerializationWriter<DbConnectedSite> writer) {
        nqs = new BasicNativeQueryService(dataSource);
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public void addConnectedSite(DbConnectedSite connectedSite) throws DuplicateResourceException {
        execWithRefinedDuplicateResourceException(() -> {
            // TODO: shouldn't this be a constant? getJsonBParam prevents it, why is it defined as it is?
            String insertSql = "INSERT INTO connectedSite " +
                    "(id, urn, dateModified, data) " +
                    "VALUES (?,?,?," + getJsonBParam() + ")";
            return nqs.executeUpdate(insertSql, Arrays.asList(
                    connectedSite.getId(),
                    connectedSite.getUrn(),
                    System.currentTimeMillis(),
                    writeJsonBParameter(connectedSite, nqs)));
        });
    }

    @Override
    public Collection<DbConnectedSite> list() {
        return nqs.getList(SQL_SELECT, this::convertRow);
    }

    @Override
    public DbConnectedSite findByURN(String urn) {
        return nqs.getSingleValue(SQL_SELECT_BY_URN, this::convertRow, Collections.singletonList(urn));
    }

    @Override
    public DbConnectedSite getById(UUID id) {
        return nqs.getSingleValue(SQL_SELECT_BY_ID, this::convertRow, Collections.singletonList(id));
    }

    private DbConnectedSite convertRow(ResultSet rset, int pos) throws SQLException {
        try (InputStream data = new ReaderInputStream(rset.getCharacterStream("data"), StandardCharsets.UTF_8)) {
            return reader.readObject(data).withNonSerializedFields(
                    UUID.fromString(rset.getString("id")),
                    rset.getString("urn"),
                    new Date(rset.getLong("dateModified")));
        } catch (IOException e) {
            throw new IllegalStoreStateException("Failed reading data for " +
                    reader.getClass().getGenericInterfaces()[0].getClass().getSimpleName(), e);
        }
    }
}
