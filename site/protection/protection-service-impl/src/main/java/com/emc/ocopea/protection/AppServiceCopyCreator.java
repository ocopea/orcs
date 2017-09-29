// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Responsible for creating app service copies.
 */
public class AppServiceCopyCreator {
    private static final Logger log = LoggerFactory.getLogger(AppServiceCopyCreator.class);
    private final AppCopyPersisterService appCopyPersisterService;
    private final ApplicationCopyLoader applicationCopyLoader;

    public AppServiceCopyCreator(
            AppCopyPersisterService appCopyPersisterService,
            ApplicationCopyLoader applicationCopyLoader) {
        this.appCopyPersisterService = appCopyPersisterService;
        this.applicationCopyLoader = applicationCopyLoader;
    }

    /***
     * Processing and responding to ApplicationCopyEvents picking events relevant for
     * app copies (ApplicationCopyAppServiceQueuedEvent).
     * @param event event to process
     */
    public void processEvent(ApplicationCopyEvent event) {
        if (event instanceof ApplicationCopyAppServiceQueuedEvent) {
            final ApplicationCopyAppServiceQueuedEvent e = (ApplicationCopyAppServiceQueuedEvent) event;
            createAppServiceCopy(e.getAppCopyId(), e.getAppServiceName());
        }
    }

    private void createAppServiceCopy(UUID applicationCopyId, String appServiceName) {
        final ApplicationCopy applicationCopy = applicationCopyLoader.load(applicationCopyId);
        try {
            final ApplicationAppServiceCopy appServiceCopy = applicationCopy.getAppServiceCopy(appServiceName);

            if (appServiceCopy == null) {
                appCopyPersisterService.persist(
                        applicationCopy.markAppCopyAsInvalid("Received an invalid app service queue event service " +
                                "Name:" + appServiceName));
                return;
            }

            appCopyPersisterService.persist(
                    applicationCopy.markAppServiceCopyAsRunning(appServiceName, null));

            // todo: actually get data from PSB rather than this map
            Map<String, String> fakeAppProperties = new HashMap<>();
            fakeAppProperties.put("scale", "1");
            fakeAppProperties.put("name", appServiceName);

            appCopyPersisterService.persist(
                    applicationCopy.markAppServiceCopyAsCreatedSuccessfully(appServiceName, fakeAppProperties));
        } catch (Exception ex) {
            log.error("Failed creating copy for app service " + appServiceName, ex);
            appCopyPersisterService.persist(
                    applicationCopy.markAppServiceCopyAsFailed(appServiceName, "Failed creating copy for app service"
                            + appServiceName + " - " + ex.getMessage()));
        }
    }
}
