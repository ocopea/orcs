// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import java.util.List;
import java.util.UUID;

/**
 * Created by liebea on 12/13/15.
 * Drink responsibly
 */
public class CreateSavedImageCommandArgs {
    private final String name;
    private final UUID appInstanceId;
    private final UUID userId;
    private final List<String> tags;
    private final String comment;

    private CreateSavedImageCommandArgs() {
        this(null, null, null, null, null);
    }

    public CreateSavedImageCommandArgs(
            String name,
            UUID appInstanceId,
            UUID userId,
            List<String> tags,
            String comment) {
        this.name = name;
        this.appInstanceId = appInstanceId;
        this.userId = userId;
        this.tags = tags;
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public UUID getAppInstanceId() {
        return appInstanceId;
    }

    public UUID getUserId() {
        return userId;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return "CreateSavedImageCommandArgs{" +
                "name='" + name + '\'' +
                ", appInstanceId=" + appInstanceId +
                ", userId=" + userId +
                ", tags=" + tags +
                ", comment='" + comment + '\'' +
                '}';
    }
}
