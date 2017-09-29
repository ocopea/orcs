// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import java.util.UUID;

/**
 * Created by liebea on 1/19/16.
 * Drink responsibly
 */
public class StopAppOnSiteCommandArgs implements SiteCommandArgs {
    private final UUID appInstanceId;

    private StopAppOnSiteCommandArgs() {
        this(null);
    }

    public StopAppOnSiteCommandArgs(UUID appInstanceId) {
        this.appInstanceId = appInstanceId;
    }

    public UUID getAppInstanceId() {
        return appInstanceId;
    }

    @Override
    public String toString() {
        return "StopAppCommandArgs{" +
                "appInstanceId=" + appInstanceId +
                '}';
    }
}
