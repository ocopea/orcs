// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 12/13/15.
 * Drink responsibly
 */
public class DBAppInstanceConfig {
    @JsonIgnore
    private final UUID id;
    @JsonIgnore
    private final String name;
    @JsonIgnore
    private final UUID appTemplateId;
    @JsonIgnore
    private final String deploymentType;
    @JsonIgnore
    private final UUID creatorUserId;
    @JsonIgnore
    private final UUID baseAppInstanceId;
    @JsonIgnore
    private final UUID baseSavedImageId;
    @JsonIgnore
    private final Date createdDate;
    @JsonIgnore
    private final Date dateModified;
    @JsonIgnore
    private final UUID siteId;

    public DBAppInstanceConfig(
            UUID id,
            String name,
            UUID appTemplateId,
            String deploymentType,
            UUID creatorUserId,
            UUID baseAppInstanceId,
            UUID baseSavedImageId,
            Date createdDate,
            Date dateModified,
            UUID siteId) {
        this.id = id;
        this.name = name;
        this.appTemplateId = appTemplateId;
        this.deploymentType = deploymentType;
        this.creatorUserId = creatorUserId;
        this.baseAppInstanceId = baseAppInstanceId;
        this.baseSavedImageId = baseSavedImageId;
        this.createdDate = createdDate;
        this.dateModified = dateModified;
        this.siteId = siteId;
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

    public String getDeploymentType() {
        return deploymentType;
    }

    public UUID getCreatorUserId() {
        return creatorUserId;
    }

    public UUID getBaseAppInstanceId() {
        return baseAppInstanceId;
    }

    public UUID getBaseSavedImageId() {
        return baseSavedImageId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public UUID getSiteId() {
        return siteId;
    }

    @Override
    public String toString() {
        return "DBAppInstanceConfig{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", appTemplateId=" + appTemplateId +
                ", deploymentType='" + deploymentType + '\'' +
                ", creatorUserId=" + creatorUserId +
                ", baseAppInstanceId=" + baseAppInstanceId +
                ", baseSavedImageId=" + baseSavedImageId +
                ", createdDate=" + createdDate +
                ", dateModified=" + dateModified +
                ", siteId=" + siteId +
                '}';
    }
}
