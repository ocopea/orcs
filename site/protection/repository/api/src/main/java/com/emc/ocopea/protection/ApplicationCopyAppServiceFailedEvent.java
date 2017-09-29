// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 10/26/16.
 * Drink responsibly
 */
public class ApplicationCopyAppServiceFailedEvent extends ApplicationCopyEvent {
    private final String appServiceName;

    private ApplicationCopyAppServiceFailedEvent() {
        this(null, null, 0, null, null, null);
    }

    public ApplicationCopyAppServiceFailedEvent(UUID id, UUID appInstanceId, long version, Date timeStamp,
                                                String message, String appServiceName) {
        super(id, appInstanceId, version, timeStamp, message);
        this.appServiceName = appServiceName;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    @Override
    public String toString() {
        return "ApplicationCopyAppServiceQueuedEvent{" +
                "appServiceName='" + appServiceName + '\'' +
                '}';
    }
}
