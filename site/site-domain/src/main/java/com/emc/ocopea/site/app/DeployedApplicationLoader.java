// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by liebea on 10/25/16.
 * Drink responsibly
 */
public class DeployedApplicationLoader {
    private final DeployedApplicationEventRepository deployedApplicationEventRepository;

    public DeployedApplicationLoader(
            DeployedApplicationEventRepository deployedApplicationEventRepository) {
        this.deployedApplicationEventRepository = deployedApplicationEventRepository;
    }

    /***
     * Load DeployedApplication instance from the database by id.
     * @param appInstanceId app instance id
     * @return DeployedApplication instance or null if none exist
     */
    public DeployedApplication load(UUID appInstanceId) {
        final Collection<DeployedApplicationEvent> events =
                deployedApplicationEventRepository.listSortedEvents(appInstanceId);
        if (events.isEmpty()) {
            return null;
        }
        return new DeployedApplication(events);
    }

    public Collection<DeployedApplication> list() {
        return deployedApplicationEventRepository.listAppInstances()
                .values()
                .stream()
                .map(DeployedApplication::new)
                .collect(Collectors.toList());
    }

}
