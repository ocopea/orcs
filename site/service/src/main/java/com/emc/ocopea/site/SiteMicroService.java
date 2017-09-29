// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceInitializationHelper;
import com.emc.microservice.dependency.SyncCallServiceDependencyDescriptor;
import com.emc.ocopea.protection.AppCopyManager;
import com.emc.ocopea.site.app.DeployChecker;
import com.emc.ocopea.site.app.DeployedApplicationEvent;
import com.emc.ocopea.site.app.DeployedApplicationEventRepository;
import com.emc.ocopea.site.app.ServiceCreationChecker;
import com.emc.ocopea.site.artifact.ArtifactRegistryFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liebea on 2/23/15.
 * Drink responsibly
 */
public class SiteMicroService extends MicroService {
    private static final Logger logger = LoggerFactory.getLogger(SiteMicroService.class);
    private static final String SERVICE_NAME = "Site";
    private static final int SERVICE_VERSION = 1;
    private static final String SERVICE_IDENTIFIER = "site";
    private static final String SERVICE_DESCRIPTION = "Manage Site";

    public SiteMicroService() {
        super(
                SERVICE_NAME,
                SERVICE_IDENTIFIER,
                SERVICE_DESCRIPTION,
                SERVICE_VERSION,
                logger,
                new MicroServiceInitializationHelper()

                        .withRestResource(
                                SiteResource.class,
                                "Site management resource")
                        .withRestResource(
                                DsbCatalogResource.class,
                                "Manage dsb catalog")

                        // Working queue for handling deployed application events
                        .withInputQueue(
                                "deployed-application-events",
                                "deployed application events work queue",
                                DeployedApplicationMessageBroker.class,
                                null,
                                "appInstanceId",
                                "eventType")
                        .withDestination("deployed-application-events", "deployed application events work queue")
                        // Working queue for handling deployed application events
                        .withInputQueue(
                                "pending-deployed-application-events",
                                "pending deployed application events work queue",
                                PendingDeployedApplicationEventsProcessor.class,
                                null,
                                "appInstanceId",
                                "eventType")
                        .withDestination(
                                "pending-deployed-application-events",
                                "pending deployed application events work queue")

                        // Site datasource
                        .withDatasource("site-db", "Stores site configuration")

                        // This singleton is responsible for listening to Deployed application events and putting those
                        // in the deployed-application-events work queue
                        .withSingleton(DeployApplicationEventSingleton.class)
                        .withSingleton(ArtifactRegistryFactoryImpl.class)
                        .withSingleton(LoggingWebSocketsManager.class)

                        .withSingleton("site-singleton", "Site Singleton", SiteRepositoryImpl.class)

                        .withParameter("site-name", "Name for the site", "prod")
                        .withParameter("location", "Region where site is located", "")
                        .withParameter("public-load-balancer", "Public DNS", "")
                        .withParameter(
                                "deploy-app-timeout-in-seconds",
                                "Time to wait for an app to start in seconds",
                                Integer.toString(60 * 15))
                        .withDynamicJavaService(DeployedApplicationEventRepository.class)
                        .withServiceDependency(new SyncCallServiceDependencyDescriptor("protection", true))

                        .withJacksonSerialization(DeployedApplicationEvent.class)

                        .withJacksonSerialization(DbSite.class)

                        .withScheduler("default")
                        .withSchedulerListenerMapping(
                                DeployChecker.SCHEDULE_LISTENER_IDENTIFIER,
                                DeployChecker.class)
                        .withSchedulerListenerMapping(
                                AppCopyManager.ProtectScheduleListener.SCHEDULE_LISTENER_IDENTIFIER,
                                AppCopyManager.ProtectScheduleListener.class
                        )
                        .withSchedulerListenerMapping(
                                ServiceCreationChecker.SCHEDULE_LISTENER_IDENTIFIER,
                                ServiceCreationChecker.class
                        )
                        .withWebSocket(SiteLogsWebSocket.class)

        );
    }
}
