// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class ApplicationCopyLoader {
    private final ApplicationCopyEventRepository applicationCopyEventRepository;

    public ApplicationCopyLoader(ApplicationCopyEventRepository applicationCopyEventRepository) {
        this.applicationCopyEventRepository = applicationCopyEventRepository;
    }

    /**
     * Loads an ApplicationCopy from persistence
     * @return ApplicationCopy, or null if no copy exists with the specified copy id
     */
    public ApplicationCopy load(UUID id) {
        ApplicationCopy copy = null;
        Collection<ApplicationCopyEvent> events = applicationCopyEventRepository.listOrderedEvents(id);
        if (!events.isEmpty()) {
            copy = new ApplicationCopy(events);
        }
        return copy;
    }

    public Collection<ApplicationCopy> listByAppInstanceId(UUID appInstanceId) {
        return applicationCopyEventRepository.listByAppInstanceId(appInstanceId)
                .entrySet()
                .stream()
                .map(entry -> new ApplicationCopy(entry.getValue())).collect(Collectors.toList());
    }

}
