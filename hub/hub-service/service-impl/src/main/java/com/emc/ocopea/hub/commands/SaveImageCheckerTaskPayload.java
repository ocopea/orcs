// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.commands;

import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 1/10/17.
 * Drink responsibly
 */
public class SaveImageCheckerTaskPayload {
    private final Date startTime;
    private final UUID savedImageId;
    private final UUID siteId;
    private final String scheduleName;

    private SaveImageCheckerTaskPayload() {
        this(null, null, null, null);
    }

    public SaveImageCheckerTaskPayload(Date startTime, UUID savedImageId, UUID siteId, String scheduleName) {
        this.startTime = startTime;
        this.savedImageId = savedImageId;
        this.siteId = siteId;
        this.scheduleName = scheduleName;
    }

    public Date getStartTime() {
        return startTime;
    }

    public UUID getSavedImageId() {
        return savedImageId;
    }

    public UUID getSiteId() {
        return siteId;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    @Override
    public String toString() {
        return "SaveImageCheckerTaskPayload{" +
                "startTime=" + startTime +
                ", savedImageId=" + savedImageId +
                ", siteId=" + siteId +
                ", scheduleName='" + scheduleName + '\'' +
                '}';
    }
}
