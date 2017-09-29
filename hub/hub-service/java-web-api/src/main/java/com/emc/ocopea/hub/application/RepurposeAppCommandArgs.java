// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import java.util.Collection;
import java.util.UUID;

/**
 * Created by liebea on 12/13/15.
 * Drink responsibly
 */
public class RepurposeAppCommandArgs {
    private final String appInstanceName;
    private final UUID originAppInstanceId;
    private final UUID copyId;
    private final UUID userId;
    private final String purpose;
    private final Collection<DeployAppCommandArgs.ApplicationPolicyInfoDTO> appPolicies;

    public RepurposeAppCommandArgs() {
        this(null, null, null, null, null, null);
    }

    public RepurposeAppCommandArgs(
            String appInstanceName,
            UUID originAppInstanceId,
            UUID copyId,
            UUID userId,
            String purpose,
            Collection<DeployAppCommandArgs.ApplicationPolicyInfoDTO> appPolicies) {
        this.appInstanceName = appInstanceName;
        this.originAppInstanceId = originAppInstanceId;
        this.copyId = copyId;
        this.userId = userId;
        this.purpose = purpose;
        this.appPolicies = appPolicies;
    }

    public String getAppInstanceName() {
        return appInstanceName;
    }

    public UUID getOriginAppInstanceId() {
        return originAppInstanceId;
    }

    public UUID getCopyId() {
        return copyId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getPurpose() {
        return purpose;
    }

    public Collection<DeployAppCommandArgs.ApplicationPolicyInfoDTO> getAppPolicies() {
        return appPolicies;
    }

    @Override
    public String toString() {
        return "RepurposeAppCommandArgs{" +
                "appInstanceName='" + appInstanceName + '\'' +
                ", originAppInstanceId='" + originAppInstanceId + '\'' +
                ", copyId='" + copyId + '\'' +
                ", userId='" + userId + '\'' +
                ", purpose='" + purpose + '\'' +
                ", appPolicies=" + appPolicies +
                '}';
    }
}
