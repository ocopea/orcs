// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import com.emc.microservice.Context;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.ocopea.util.PostgresUtil;
import com.emc.ocopea.util.database.BasicNativeQueryService;
import com.emc.ocopea.util.database.UuidNativeQueryConverter;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ApplicationCopyEventRepositoryImpl implements ApplicationCopyEventRepository, ServiceLifecycle {

    private final Map<UUID, Consumer<ApplicationCopyEvent>> consumers = new ConcurrentHashMap<>();
    private BasicNativeQueryService nqs;

    private static final String SQL_INSERT_EVENTS = "INSERT INTO applicationCopyEvent " +
            "(appCopyId,appInstanceId,version,data) VALUES(?,?,?,?)";
    private static final String SQL_SELECT_EVENTS_BY_APP_COPY_ID = "SELECT * FROM applicationCopyEvent " +
            "WHERE appCopyId = ? ORDER BY version ASC";
    private static final String SQL_SELECT_EVENTS_BY_APP_INSTANCE_ID = "SELECT * FROM applicationCopyEvent " +
            "WHERE appInstanceId = ? ORDER BY version ASC";

    public ApplicationCopyEventRepositoryImpl(DataSource dataSource) {
        initialize(dataSource);
    }

    public ApplicationCopyEventRepositoryImpl() {
    }

    private void initialize(DataSource dataSource) {
        this.nqs = new BasicNativeQueryService(dataSource);
    }

    private ApplicationCopyEvent convertRow(ResultSet rset, int pos) throws SQLException {
        String eventJson = rset.getString("data");
        return PostgresUtil.fromJsonB(eventJson, ApplicationCopyEvent.class);
    }

    @Override
    public UUID subscribe(Consumer<ApplicationCopyEvent> consumer) {
        UUID id = UUID.randomUUID();
        consumers.put(id, consumer);
        return id;
    }

    @Override
    public void unSubscribe(UUID subscriberId) {
        consumers.remove(subscriberId);
    }

    @Override
    public List<ApplicationCopyEvent> listOrderedEvents(UUID copyId) {
        return nqs.getList(SQL_SELECT_EVENTS_BY_APP_COPY_ID, this::convertRow, Collections.singletonList(copyId));
    }

    @Override
    public Map<UUID, List<ApplicationCopyEvent>> listByAppInstanceId(UUID appInstanceId) {
        return nqs.getMapOfList(
                SQL_SELECT_EVENTS_BY_APP_INSTANCE_ID,
                new UuidNativeQueryConverter("appCopyId"),
                this::convertRow,
                Collections.singletonList(appInstanceId)
        );
    }

    /**
     * Converts an ApplicationCopyEvents to a parameter list.
     * Order of elements derived from SQL_INSERT_EVENTS
     */
    private List<Object> convertToParams(ApplicationCopyEvent appCopyEvent) {
        return Arrays.asList(
                appCopyEvent.getAppCopyId(),
                appCopyEvent.getAppInstanceId(),
                appCopyEvent.getVersion(),
                PostgresUtil.objectToJsonBParameter(appCopyEvent, nqs)
        );
    }

    @Override
    public ApplicationCopyEvent store(ApplicationCopyEvent appCopyEvent) {
        nqs.executeUpdate(SQL_INSERT_EVENTS, convertToParams(appCopyEvent));

        consumers.values().forEach(consumer -> consumer.accept(appCopyEvent));
        return appCopyEvent;
    }

    @Override
    public Collection<ApplicationCopyEvent> store(Collection<ApplicationCopyEvent> appCopyEvents) {
        nqs.executeUpdates(SQL_INSERT_EVENTS,
                appCopyEvents
                        .stream()
                        .map(this::convertToParams)
                        .collect(Collectors.toList())
        );
        consumers.values().forEach(appCopyEvents::forEach);
        return appCopyEvents;
    }

    @Override
    public void init(Context context) {
        initialize(
                context.getDatasourceManager().getManagedResourceByName("protection-db").getDataSource()
        );
    }

    @Override
    public void shutDown() {
    }
}
