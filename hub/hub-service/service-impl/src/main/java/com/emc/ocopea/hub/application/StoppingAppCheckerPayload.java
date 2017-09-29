// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 1/10/17.
 * Drink responsibly
 */
public class StoppingAppCheckerPayload {
    private final UUID appInstanceId;
    private final UUID siteId;
    private final Date startTime;
    private final String appInstanceName;
    private final String scheduleName;

    private StoppingAppCheckerPayload() {
        this(null,null,null,null,null);
    }

    public StoppingAppCheckerPayload(
            UUID appInstanceId,
            UUID siteId,
            Date startTime,
            String appInstanceName,
            String scheduleName) {
        this.appInstanceId = appInstanceId;
        this.siteId = siteId;
        this.startTime = startTime;
        this.appInstanceName = appInstanceName;
        this.scheduleName = scheduleName;
    }

    public UUID getAppInstanceId() {
        return appInstanceId;
    }

    public UUID getSiteId() {
        return siteId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public String getAppInstanceName() {
        return appInstanceName;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    @Override
    public String toString() {
        return "StoppingAppCheckerPayload{" +
                "appInstanceId=" + appInstanceId +
                ", siteId=" + siteId +
                ", startTime=" + startTime +
                ", appInstanceName='" + appInstanceName + '\'' +
                ", scheduleName='" + scheduleName + '\'' +
                '}';
    }
}
