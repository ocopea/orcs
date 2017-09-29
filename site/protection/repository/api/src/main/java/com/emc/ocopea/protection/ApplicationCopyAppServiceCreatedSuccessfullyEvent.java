// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liebea on 10/26/16.
 * Drink responsibly
 */
public class ApplicationCopyAppServiceCreatedSuccessfullyEvent extends ApplicationCopyEvent {
    private final String appServiceName;
    private final Map<String, String> appConfiguration;

    private ApplicationCopyAppServiceCreatedSuccessfullyEvent() {
        this(null, null, 0, null, null, null, null);
    }

    public ApplicationCopyAppServiceCreatedSuccessfullyEvent(
            UUID id,
            UUID appInstanceId,
            long version,
            Date timeStamp,
            String message,
            String appServiceName,
            Map<String, String> appConfiguration) {
        super(id, appInstanceId, version, timeStamp, message);
        this.appServiceName = appServiceName;
        this.appConfiguration = appConfiguration;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public Map<String, String> getAppConfiguration() {
        return appConfiguration;
    }

    @Override
    public String toString() {
        return "ApplicationCopyAppServiceCreatedSuccessfullyEvent{" +
                "appServiceName='" + appServiceName + '\'' +
                ", appConfiguration=" + appConfiguration +
                '}';
    }
}
