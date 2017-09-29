// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

/**
 * Created by liebea on 4/25/17.
 * Drink responsibly
 */
public class AppCopyCreatorService {
    private final ApplicationCopyEventRepository applicationCopyEventRepository;
    private final AppCopyPersisterService appCopyPersisterService;

    public AppCopyCreatorService(
            ApplicationCopyEventRepository applicationCopyEventRepository,
            AppCopyPersisterService appCopyPersisterService) {
        this.applicationCopyEventRepository = applicationCopyEventRepository;
        this.appCopyPersisterService = appCopyPersisterService;
    }

    public ApplicationCopy create(ApplicationCopyBuilder builder) {
        ApplicationCopy applicationCopy = new ApplicationCopy().create(builder);
        applicationCopyEventRepository.store(applicationCopy.getExecutionEvent());
        appCopyPersisterService.persist(applicationCopy);
        applicationCopy.markAsClean();
        return applicationCopy;
    }
}
