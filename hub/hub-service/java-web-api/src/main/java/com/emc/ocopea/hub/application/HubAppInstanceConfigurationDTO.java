// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 1/24/16.
 * Drink responsibly
 */
public class HubAppInstanceConfigurationDTO {
    private final UUID id;
    private final String name;
    private final UUID appTemplateId;
    private final UUID siteId;
    private final UUID baseSavedImageId;
    private final UUID creatorUserId;
    private final String deploymentType;
    private final Date created;

    private HubAppInstanceConfigurationDTO() {
        this(null, null, null, null, null, null, null, null);
    }

    public HubAppInstanceConfigurationDTO(
            UUID id,
            String name,
            UUID appTemplateId,
            UUID siteId,
            UUID baseSavedImageId,
            UUID creatorUserId,
            String deploymentType,
            Date created) {
        this.id = id;
        this.name = name;
        this.appTemplateId = appTemplateId;
        this.siteId = siteId;
        this.baseSavedImageId = baseSavedImageId;
        this.creatorUserId = creatorUserId;
        this.deploymentType = deploymentType;
        this.created = created;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getAppTemplateId() {
        return appTemplateId;
    }

    public UUID getSiteId() {
        return siteId;
    }

    public UUID getBaseSavedImageId() {
        return baseSavedImageId;
    }

    public UUID getCreatorUserId() {
        return creatorUserId;
    }

    public String getDeploymentType() {
        return deploymentType;
    }

    public Date getCreated() {
        return created;
    }

    @Override
    public String toString() {
        return "HubAppInstanceConfigurationDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", appTemplateId=" + appTemplateId +
                ", siteId=" + siteId +
                ", baseSavedImageId=" + baseSavedImageId +
                ", creatorUserId=" + creatorUserId +
                ", deploymentType='" + deploymentType + '\'' +
                ", created=" + created +
                '}';
    }
}
