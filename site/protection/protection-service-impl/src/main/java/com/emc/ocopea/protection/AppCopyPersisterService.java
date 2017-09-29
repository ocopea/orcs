// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import com.emc.microservice.messaging.MessageSender;
import com.emc.ocopea.util.MapBuilder;

/**
 * Created by liebea on 4/25/17.
 * Drink responsibly
 */
public class AppCopyPersisterService {
    private final MessageSender pendingEventQueue;

    public AppCopyPersisterService(MessageSender pendingEventQueue) {
        this.pendingEventQueue = pendingEventQueue;
    }

    /**
     * Persist a deployed application
     */
    public void persist(ApplicationCopy applicationCopy) {
        final ApplicationCopyEvent event = applicationCopy.getExecutionEvent();
        if (event != null) {
            pendingEventQueue.sendMessage(
                    ApplicationCopyEvent.class,
                    event,
                    MapBuilder.<String, String>newHashMap()
                            .with("appInstanceId", event.getAppInstanceId().toString())
                            .with("appCopyId", event.getAppCopyId().toString())
                            .with("eventType", event.getClass().getSimpleName())
                            .build());
        }

        applicationCopy.markAsClean();
    }
}
