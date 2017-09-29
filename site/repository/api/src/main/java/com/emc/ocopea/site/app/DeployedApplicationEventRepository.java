// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Created by liebea on 10/13/16.
 * Drink responsibly
 */
public interface DeployedApplicationEventRepository {

    UUID subscribe(Consumer<DeployedApplicationEvent> consumer);

    void unSubscribe(UUID consumerId);

    Collection<DeployedApplicationEvent> listSortedEvents(UUID appInstanceId);

    //todo: filter? something?
    Map<UUID, List<DeployedApplicationEvent>> listAppInstances();

    DeployedApplicationEvent store(DeployedApplicationEvent applicationDeployedEvent);

    Collection<DeployedApplicationEvent> store(Collection<DeployedApplicationEvent> applicationDeployedEvents);

}
