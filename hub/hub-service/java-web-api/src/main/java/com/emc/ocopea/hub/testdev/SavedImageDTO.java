// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.testdev;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by liebea on 10/10/16.
 * Drink responsibly
 */
public class SavedImageDTO {
    private final UUID id;
    private final UUID appTemplateId;
    private final String name;
    private final UUID baseImageId;
    private final UUID userId;
    private final Date createdDate;
    private final List<String> tags;
    private final String comment;
    private final UUID siteId;
    private final UUID appCopyId;
    private final String state;

    private SavedImageDTO() {
        this(null, null, null, null, null, null, null, null, null, null, null);
    }

    public SavedImageDTO(
            UUID id,
            UUID appTemplateId,
            String name,
            UUID baseImageId,
            UUID userId,
            Date createdDate,
            List<String> tags,
            String comment,
            UUID siteId,
            UUID appCopyId,
            String state) {
        this.id = id;
        this.appTemplateId = appTemplateId;
        this.name = name;
        this.baseImageId = baseImageId;
        this.userId = userId;
        this.createdDate = createdDate;
        this.tags = tags;
        this.comment = comment;
        this.siteId = siteId;
        this.appCopyId = appCopyId;
        this.state = state;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAppTemplateId() {
        return appTemplateId;
    }

    public String getName() {
        return name;
    }

    public UUID getBaseImageId() {
        return baseImageId;
    }

    public UUID getUserId() {
        return userId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getComment() {
        return comment;
    }

    public UUID getSiteId() {
        return siteId;
    }

    public UUID getAppCopyId() {
        return appCopyId;
    }

    public String getState() {
        return state;
    }

    @Override
    public String toString() {
        return "SavedImageDTO{" +
                "id=" + id +
                ", appTemplateId=" + appTemplateId +
                ", name='" + name + '\'' +
                ", baseImageId=" + baseImageId +
                ", userId=" + userId +
                ", createdDate=" + createdDate +
                ", tags=" + tags +
                ", comment='" + comment + '\'' +
                ", siteId=" + siteId +
                ", appCopyId=" + appCopyId +
                ", state='" + state + '\'' +
                '}';
    }
}
