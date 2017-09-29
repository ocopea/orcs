// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by liebea on 10/26/16.
 * Drink responsibly
 */
public class ApplicationCopyEventRepositoryTestingImpl implements ApplicationCopyEventRepository {

    private final Map<UUID, Consumer<ApplicationCopyEvent>> consumers = new ConcurrentHashMap<>();
    private final Map<UUID, Collection<ApplicationCopyEvent>> fakeStore = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> appInstanceIdsToCopyIds = new ConcurrentHashMap<>();

    public ApplicationCopyEventRepositoryTestingImpl() {
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
        final Collection<ApplicationCopyEvent> events = fakeStore.get(copyId);
        return events == null ? Collections.emptyList() : new ArrayList<>(events);
    }

    @Override
    public ApplicationCopyEvent store(ApplicationCopyEvent appCopyEvent) {
        Collection<ApplicationCopyEvent> byId = fakeStore.get(appCopyEvent.getAppCopyId());

        //todo:tx:begin
        if (byId == null) {
            byId = Collections.synchronizedList(new ArrayList<>());
            fakeStore.put(appCopyEvent.getAppCopyId(), byId);
        }
        byId.add(appCopyEvent);
        Set<UUID> byAppInstanceId = this.appInstanceIdsToCopyIds.get(appCopyEvent.getAppInstanceId());
        if (byAppInstanceId == null) {
            byAppInstanceId = new HashSet<>();
            appInstanceIdsToCopyIds.put(appCopyEvent.getAppInstanceId(), byAppInstanceId);
        }
        byAppInstanceId.add(appCopyEvent.getAppCopyId());

        consumers.values().forEach(consumer -> consumer.accept(appCopyEvent));
        //todo:tx:end/rollback...
        return appCopyEvent;
    }

    @Override
    public Collection<ApplicationCopyEvent> store(Collection<ApplicationCopyEvent> applicationDeployedEvents) {
        return applicationDeployedEvents.stream().map(this::store).collect(Collectors.toList());
    }

    @Override
    public Map<UUID, List<ApplicationCopyEvent>> listByAppInstanceId(UUID appInstanceId) {
        final Set<UUID> copyIds = appInstanceIdsToCopyIds.get(appInstanceId);
        if (copyIds == null) {
            return Collections.emptyMap();
        }
        return copyIds.stream().collect(Collectors.toMap(o -> o, this::listOrderedEvents));
    }

}
