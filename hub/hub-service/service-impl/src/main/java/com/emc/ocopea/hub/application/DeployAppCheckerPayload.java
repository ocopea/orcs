// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 1/10/17.
 * Drink responsibly
 */
public class DeployAppCheckerPayload {
    private final UUID appInstanceId;
    private final UUID siteId;
    private final Date startTime;
    private final String k8sHost;
    private final String appInstanceName;
    private final String scheduleName;
    private final String entryPointServicePath;

    private DeployAppCheckerPayload() {
        this(null,null,null,null,null,null, null);
    }

    public DeployAppCheckerPayload(
            UUID appInstanceId,
            UUID siteId,
            Date startTime,
            String k8sHost,
            String appInstanceName,
            String scheduleName,
            String entryPointServicePath) {
        this.appInstanceId = appInstanceId;
        this.siteId = siteId;
        this.startTime = startTime;
        this.k8sHost = k8sHost;
        this.appInstanceName = appInstanceName;
        this.scheduleName = scheduleName;
        this.entryPointServicePath = entryPointServicePath;
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

    public String getK8sHost() {
        return k8sHost;
    }

    public String getAppInstanceName() {
        return appInstanceName;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public String getEntryPointServicePath() {
        return entryPointServicePath;
    }

    @Override
    public String toString() {
        return "DeployAppCheckerPayload{" +
                "appInstanceId=" + appInstanceId +
                ", siteId=" + siteId +
                ", startTime=" + startTime +
                ", k8sHost='" + k8sHost + '\'' +
                ", appInstanceName='" + appInstanceName + '\'' +
                ", scheduleName='" + scheduleName + '\'' +
                ", entryPointServicePath='" + entryPointServicePath + '\'' +
                '}';
    }
}
