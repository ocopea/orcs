// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import java.util.UUID;

/**
 * Created by liebea on 12/13/15.
 * Drink responsibly
 */
public class DeploySavedImageCommandArgs {
    private final String appInstanceName;
    private final UUID savedImageId;
    private final UUID userId;
    private final String purpose;
    private final UUID siteId;
    private final DeployAppCommandArgs.AppTemplateDeploymentPlanDTO deploymentPlan;

    private DeploySavedImageCommandArgs() {
        this(null,null,null,null, null, null);
    }

    public DeploySavedImageCommandArgs(
            String appInstanceName,
            UUID savedImageId,
            UUID userId,
            String purpose,
            UUID siteId,
            DeployAppCommandArgs.AppTemplateDeploymentPlanDTO deploymentPlan) {
        this.appInstanceName = appInstanceName;
        this.savedImageId = savedImageId;
        this.userId = userId;
        this.purpose = purpose;
        this.siteId = siteId;
        this.deploymentPlan = deploymentPlan;
    }

    public String getAppInstanceName() {
        return appInstanceName;
    }

    public UUID getSavedImageId() {
        return savedImageId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getPurpose() {
        return purpose;
    }

    public UUID getSiteId() {
        return siteId;
    }

    public DeployAppCommandArgs.AppTemplateDeploymentPlanDTO getDeploymentPlan() {
        return deploymentPlan;
    }

    @Override
    public String toString() {
        return "DeploySavedImageCommandArgs{" +
                "appInstanceName='" + appInstanceName + '\'' +
                ", savedImageId=" + savedImageId +
                ", userId=" + userId +
                ", purpose='" + purpose + '\'' +
                ", siteId=" + siteId +
                ", deploymentPlan=" + deploymentPlan +
                '}';
    }
}
