// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * Created by liebea on 12/13/15.
 * Drink responsibly
 */
public class DBSavedImage {
    private final UUID id;
    private final UUID appTemplateId;
    private final String name;
    private final String description;
    private final UUID creatorUserId;
    private final Set<String> tags;
    private final Date dateCreated;
    private final UUID siteId;
    private final UUID appCopyId;
    private final UUID baseImageId;
    private final DBSavedImageState state;

    public enum DBSavedImageState {
        creating,
        created,
        failed
    }

    private DBSavedImage() {
        this(null, null, null, null, null, null, null, null, null, null, null);
    }

    public DBSavedImage(
            UUID id,
            UUID appTemplateId,
            String name,
            String description,
            UUID creatorUserId,
            Set<String> tags,
            Date dateCreated,
            UUID siteId,
            UUID appCopyId,
            UUID baseImageId,
            DBSavedImageState state) {
        this.id = id;
        this.appTemplateId = appTemplateId;
        this.name = name;
        this.description = description;
        this.creatorUserId = creatorUserId;
        this.tags = tags;
        this.dateCreated = dateCreated;
        this.siteId = siteId;
        this.appCopyId = appCopyId;
        this.baseImageId = baseImageId;
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

    public String getDescription() {
        return description;
    }

    public UUID getCreatorUserId() {
        return creatorUserId;
    }

    public Set<String> getTags() {
        return tags;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public UUID getAppCopyId() {
        return appCopyId;
    }

    public UUID getSiteId() {
        return siteId;
    }

    public UUID getBaseImageId() {
        return baseImageId;
    }

    public DBSavedImageState getState() {
        return state;
    }

    @Override
    public String toString() {
        return "DBSavedImage{" +
                "id=" + id +
                ", appTemplateId=" + appTemplateId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", creatorUserId=" + creatorUserId +
                ", tags=" + tags +
                ", dateCreated=" + dateCreated +
                ", siteId=" + siteId +
                ", appCopyId=" + appCopyId +
                ", baseImageId=" + baseImageId +
                ", state=" + state +
                '}';
    }
}
