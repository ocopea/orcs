// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceInitializationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtectionMicroService extends MicroService {
    private static final Logger logger = LoggerFactory.getLogger(ProtectionMicroService.class);
    private static final String SERVICE_NAME = "Protection Service";
    private static final int SERVICE_VERSION = 1;
    private static final String SERVICE_IDENTIFIER = "protection";
    private static final String SERVICE_DESCRIPTION = "Protection Service";

    public ProtectionMicroService() {
        super(
                SERVICE_NAME,
                SERVICE_IDENTIFIER,
                SERVICE_DESCRIPTION,
                SERVICE_VERSION,
                logger,
                new MicroServiceInitializationHelper()
                        .withRestResource(ProtectionWebResource.class, "Protection Policy API")
                        .withSingleton("app-copy-manager", "App Copy Manager", AppCopyManager.class)
                        .withDestination("application-copy-events", "well, duh")
                        .withInputQueue(
                                "application-copy-events",
                                "well, duh",
                                ApplicationCopyEventsMessageBroker.class,
                                null,
                                "appInstanceId",
                                "appCopyId",
                                "eventType")

                        .withInputQueue(
                                "pending-application-copy-events",
                                "pending application copy events work queue",
                                PendingAppCopyEventsProcessor.class,
                                null,
                                "appInstanceId",
                                "appCopyId",
                                "eventType")
                        .withDestination(
                                "pending-application-copy-events",
                                "pending application copy events work queue")

                        .withScheduler("default")
                        .withSchedulerListenerMapping(
                                AppCopyManager.ProtectScheduleListener.SCHEDULE_LISTENER_IDENTIFIER,
                                AppCopyManager.ProtectScheduleListener.class)
                        .withDatasource("protection-db", "Stores application copies")
                        .withDynamicJavaService(ApplicationCopyEventRepository.class)
                        .withJacksonSerialization(ApplicationCopyEvent.class)
                        .withJacksonSerialization(ProtectApplicationInstanceInfoDTO.class)

        );
    }

}
