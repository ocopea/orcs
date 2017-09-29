// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import java.util.Collection;
import java.util.UUID;

/**
 * Created by liebea on 12/13/15.
 * Drink responsibly
 */
public class CreateDeploymentPlanCommandArgs {
    private final UUID appTemplateId;
    private final Collection<DeployAppCommandArgs.ApplicationPolicyInfoDTO> appPolicies;

    private CreateDeploymentPlanCommandArgs() {
        this(null, null);
    }

    public CreateDeploymentPlanCommandArgs(
            UUID appTemplateId,
            Collection<DeployAppCommandArgs.ApplicationPolicyInfoDTO> appPolicies) {
        this.appTemplateId = appTemplateId;
        this.appPolicies = appPolicies;
    }

    public UUID getAppTemplateId() {
        return appTemplateId;
    }

    public Collection<DeployAppCommandArgs.ApplicationPolicyInfoDTO> getAppPolicies() {
        return appPolicies;
    }

    @Override
    public String toString() {
        return "CreateDeploymentPlanCommandArgs{" +
                "appTemplateId=" + appTemplateId +
                ", appPolicies=" + appPolicies +
                '}';
    }
}
