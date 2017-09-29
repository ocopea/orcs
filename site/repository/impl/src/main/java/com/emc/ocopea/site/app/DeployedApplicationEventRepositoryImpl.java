// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import com.emc.microservice.Context;
import com.emc.microservice.datasource.MicroServiceDataSource;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.ocopea.util.PostgresUtil;
import com.emc.ocopea.util.database.BasicNativeQueryService;
import com.emc.ocopea.util.database.NativeQueryService;
import com.emc.ocopea.util.database.UuidNativeQueryConverter;

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
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DeployedApplicationEventRepositoryImpl implements DeployedApplicationEventRepository, ServiceLifecycle {

    private static final String SQL_INSERT_EVENTS =
            "insert into deployedApplicationEvent (appInstanceId,version,data) values(?,?,?)";

    private static final String SQL_SELECT_EVENTS_BY_APP_INSTANCE_ID =
            "select * from deployedApplicationEvent " +
                    "where appInstanceId = ? " +
                    "order by version asc";

    private static final String SQL_SELECT_ALL_EVENTS =
            "select * from deployedApplicationEvent " +
                    "order by version asc";

    private NativeQueryService nqs;
    private MicroServiceDataSource dataSource;
    private final Map<UUID, Consumer<DeployedApplicationEvent>> consumers = new ConcurrentHashMap<>();

    public DeployedApplicationEventRepositoryImpl(MicroServiceDataSource dataSource) {
        initialize(dataSource);
    }

    public void initialize(MicroServiceDataSource dataSource) {
        this.dataSource = dataSource;
        this.nqs = new BasicNativeQueryService(dataSource);
    }

    public DeployedApplicationEventRepositoryImpl() {
    }

    @Override
    public void init(Context context) {
        initialize(context.getDatasourceManager().getManagedResourceByName("site-db").getDataSource());
    }

    @Override
    public void shutDown() {
    }

    @Override
    public UUID subscribe(Consumer<DeployedApplicationEvent> consumer) {
        UUID id = UUID.randomUUID();
        consumers.put(id, consumer);
        return id;
    }

    @Override
    public void unSubscribe(UUID consumerId) {
        consumers.remove(consumerId);
    }

    @Override
    public Collection<DeployedApplicationEvent> listSortedEvents(UUID appInstanceId) {
        return nqs.getList(
                SQL_SELECT_EVENTS_BY_APP_INSTANCE_ID,
                this::convertRow,
                Collections.singletonList(appInstanceId));
    }

    private DeployedApplicationEvent convertRow(ResultSet rset, int pos) throws SQLException {
        String eventJson = rset.getString("data");
        return PostgresUtil.fromJsonB(eventJson, DeployedApplicationEvent.class);
    }

    @Override
    public Map<UUID, List<DeployedApplicationEvent>> listAppInstances() {
        return nqs.getMapOfList(SQL_SELECT_ALL_EVENTS, new UuidNativeQueryConverter("appInstanceId"),
                this::convertRow, Collections.emptyList());
    }

    @Override
    public DeployedApplicationEvent store(DeployedApplicationEvent applicationDeployedEvent) {

        final DeployedApplicationEvent event = runInTx(() -> {
            nqs.executeUpdate(
                    SQL_INSERT_EVENTS,
                    Arrays.asList(
                            applicationDeployedEvent.getAppInstanceId(),
                            applicationDeployedEvent.getVersion(),
                            PostgresUtil.objectToJsonBParameter(applicationDeployedEvent, nqs)));
            return applicationDeployedEvent;
        });
        consumers.values().forEach(consumer -> consumer.accept(applicationDeployedEvent));
        return event;
    }

    @Override
    public Collection<DeployedApplicationEvent> store(Collection<DeployedApplicationEvent> applicationDeployedEvents) {
        final Collection<DeployedApplicationEvent> events = runInTx(() -> {
            nqs.executeUpdates(
                    SQL_INSERT_EVENTS,
                    applicationDeployedEvents
                            .stream()
                            .map(
                                    event ->
                                            Arrays.asList(
                                                    event.getAppInstanceId(),
                                                    event.getVersion(),
                                                    PostgresUtil.objectToJsonBParameter(event, nqs)))
                            .collect(Collectors.toList())
            );

            return applicationDeployedEvents;
        });
        consumers.values().forEach(applicationDeployedEvents::forEach);
        return events;
    }

    private <T> T runInTx(Supplier<T> supplier) {

        dataSource.beginTransaction();
        try {
            T retVal = supplier.get();
            dataSource.commitTransaction();
            return retVal;
        } catch (Exception ex) {
            dataSource.rollbackTransaction();
            throw ex;
        }
    }

}
