// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import java.util.UUID;

/**
 * Created by liebea on 12/13/15.
 * Drink responsibly
 */
public class CreateAppCopyCommandArgs {
    private final UUID appInstanceId;

    private CreateAppCopyCommandArgs() {
        this(null);
    }

    public CreateAppCopyCommandArgs(UUID appInstanceId) {
        this.appInstanceId = appInstanceId;
    }

    public UUID getAppInstanceId() {
        return appInstanceId;
    }

    @Override
    public String toString() {
        return "CreateAppCopyCommandArgs{" +
                "appInstanceId='" + appInstanceId + '\'' +
                '}';
    }
}
