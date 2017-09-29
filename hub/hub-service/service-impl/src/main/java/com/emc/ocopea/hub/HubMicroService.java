// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceInitializationHelper;
import com.emc.ocopea.hub.application.AppInstanceManagerService;
import com.emc.ocopea.hub.application.AppStoppedOnSiteChecker;
import com.emc.ocopea.hub.application.ApplicationTemplateManagerService;
import com.emc.ocopea.hub.application.CreateSavedImageChecker;
import com.emc.ocopea.hub.application.DeployAppCheckerPayload;
import com.emc.ocopea.hub.application.DeployedAppOnSiteChecker;
import com.emc.ocopea.hub.application.HubResource;
import com.emc.ocopea.hub.application.StoppingAppCheckerPayload;
import com.emc.ocopea.hub.commands.SaveImageCheckerTaskPayload;
import com.emc.ocopea.hub.repository.DbConnectedSite;
import com.emc.ocopea.hub.site.SiteManagerService;
import com.emc.ocopea.hub.repository.AppInstanceRepository;
import com.emc.ocopea.hub.repository.ApplicationTemplateRepository;
import com.emc.ocopea.hub.repository.ConnectedSiteRepository;
import com.emc.ocopea.hub.repository.SavedImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HubMicroService extends MicroService {
    private static final Logger logger = LoggerFactory.getLogger(HubMicroService.class);
    private static final String SERVICE_NAME = "Hub";
    private static final int SERVICE_VERSION = 1;
    private static final String SERVICE_IDENTIFIER = "hub";
    private static final String SERVICE_DESCRIPTION = "Cross Site App Manager";

    public HubMicroService() {
        super(
                SERVICE_NAME,
                SERVICE_IDENTIFIER,
                SERVICE_DESCRIPTION,
                SERVICE_VERSION,
                logger,
                new MicroServiceInitializationHelper()
                        .withJacksonSerialization(DbConnectedSite.class)
                        .withJacksonSerialization(DeployAppCheckerPayload.class)
                        .withJacksonSerialization(StoppingAppCheckerPayload.class)
                        .withJacksonSerialization(SaveImageCheckerTaskPayload.class)
                        .withDatasource("hub-db", "Hub Database")
                        .withDynamicJavaService(ApplicationTemplateRepository.class)
                        .withDynamicJavaService(ConnectedSiteRepository.class)
                        .withDynamicJavaService(AppInstanceRepository.class)
                        .withDynamicJavaService(SavedImageRepository.class)
                        .withSingleton(
                                "app-template-manager",
                                "App template manager",
                                ApplicationTemplateManagerService.class)
                        .withSingleton(
                                "app-instance-manager",
                                "App instance manager",
                                AppInstanceManagerService.class)
                        .withSingleton(
                                "site-manager",
                                "Site manager",
                                SiteManagerService.class)
                        .withRestResource(HubResource.class, "Manage Applications")
                        .withScheduler("default")
                        .withSchedulerListenerMapping(
                                DeployedAppOnSiteChecker.SCHEDULE_LISTENER_IDENTIFIER,
                                DeployedAppOnSiteChecker.class)
                        .withSchedulerListenerMapping(
                                CreateSavedImageChecker.SCHEDULE_LISTENER_IDENTIFIER,
                                CreateSavedImageChecker.class)
                        .withSchedulerListenerMapping(
                                AppStoppedOnSiteChecker.SCHEDULE_LISTENER_IDENTIFIER,
                                AppStoppedOnSiteChecker.class)

        );

    }

}
