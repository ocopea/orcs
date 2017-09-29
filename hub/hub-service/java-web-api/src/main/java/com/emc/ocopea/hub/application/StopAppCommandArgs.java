// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import java.util.UUID;

/**
 * Created by liebea on 12/13/15.
 * Drink responsibly
 */
public class StopAppCommandArgs {
    private final UUID appInstanceId;
    private final UUID userId;

    private StopAppCommandArgs() {
        this(null, null);
    }

    public StopAppCommandArgs(UUID appInstanceId, UUID userId) {
        this.appInstanceId = appInstanceId;
        this.userId = userId;
    }

    public UUID getAppInstanceId() {
        return appInstanceId;
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "CreateSavedImageCommandArgs{" +
                "appInstanceId=" + appInstanceId +
                ", userId=" + userId +
                '}';
    }
}
