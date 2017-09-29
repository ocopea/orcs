// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import com.emc.microservice.messaging.MessageSender;
import com.emc.ocopea.util.MapBuilder;

/**
 * Created by liebea on 4/25/17.
 * Drink responsibly
 */
public class DeployedApplicationPersisterService {
    private final MessageSender pendingEventQueue;

    public DeployedApplicationPersisterService(MessageSender pendingEventQueue) {
        this.pendingEventQueue = pendingEventQueue;
    }

    /**
     * Persist a deployed application
     */
    public void persist(DeployedApplication deployedApplication) {
        if (deployedApplication.getEventsSinceLoad() != null) {
            pendingEventQueue.sendMessage(
                    DeployedApplicationEvent.class,
                    deployedApplication.getExecutionEvent(),
                    MapBuilder.<String, String>newHashMap()
                            .with("appInstanceId", deployedApplication.getId().toString())
                            .with("appName", deployedApplication.getName())
                            .with("appTemplate", deployedApplication.getAppTemplateName())
                            .with("eventType", deployedApplication.getExecutionEvent().getClass().getSimpleName())
                            .build());
        }

        deployedApplication.markAsClean();
    }
}
