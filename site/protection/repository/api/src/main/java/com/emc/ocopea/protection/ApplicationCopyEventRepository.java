// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public interface ApplicationCopyEventRepository {

    /**
     * Subscribe to application copy events
     * @return a subscriber UUID, to be used to unsubscribe
     */
    UUID subscribe(Consumer<ApplicationCopyEvent> consumer);

    void unSubscribe(UUID subscriberId);

    /**
     * List event for the specified copy, ordered by ascending event version
     */
    List<ApplicationCopyEvent> listOrderedEvents(UUID copyId);

    Map<UUID, List<ApplicationCopyEvent>> listByAppInstanceId(UUID appInstanceId);

    ApplicationCopyEvent store(ApplicationCopyEvent event);

    Collection<ApplicationCopyEvent> store(Collection<ApplicationCopyEvent> events);
}
