// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

/**
 * Application template persistent format
 */
public class DBApplicationTemplate {
    @JsonIgnore
    private final UUID id;
    @JsonIgnore
    private final String name;
    @JsonIgnore
    private final Date dateModified;

    private final Date dateCreated;
    private final String version;
    private final String description;
    private final Collection<DBApplicationServiceTemplate> appServiceTemplates;
    private final String entryPointServiceName;
    private final UUID createdByUserId;
    private final boolean deleted;

    @SuppressWarnings("unused")
    private DBApplicationTemplate() {
        this(null,null,null,null,null, null, null,null, null);
    }

    DBApplicationTemplate withNonSerializedFields(UUID id, String name, Date dateUpdated) {
        return new DBApplicationTemplate(
                id,
                name,
                dateUpdated,
                this.dateCreated,
                this.version,
                this.description,
                this.appServiceTemplates,
                this.entryPointServiceName,
                this.createdByUserId,
                this.deleted);
    }

    private DBApplicationTemplate(
            UUID id,
            String name,
            Date dateModified,
            Date dateCreated,
            String version,
            String description,
            Collection<DBApplicationServiceTemplate> appServiceTemplates,
            String entryPointServiceName,
            UUID createdByUserId,
            boolean deleted) {
        this.id = id;
        this.name = name;
        this.dateModified = dateModified;
        this.dateCreated = dateCreated;
        this.version = version;
        this.description = description;
        this.appServiceTemplates = appServiceTemplates;
        this.entryPointServiceName = entryPointServiceName;
        this.createdByUserId = createdByUserId;
        this.deleted = deleted;
    }

    public DBApplicationTemplate(
            UUID id,
            String name,
            Date dateModified,
            Date dateCreated,
            String version,
            String description,
            Collection<DBApplicationServiceTemplate> appServiceTemplates,
            String entryPointServiceName,
            UUID createdByUserId) {
        this(
                id,
                name,
                dateModified,
                dateCreated,
                version,
                description,
                appServiceTemplates,
                entryPointServiceName,
                createdByUserId,
                false);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public Collection<DBApplicationServiceTemplate> getAppServiceTemplates() {
        return appServiceTemplates;
    }

    public String getEntryPointServiceName() {
        return entryPointServiceName;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    @NoJavadoc
    public DBApplicationTemplate asDeleted() {
        return new DBApplicationTemplate(
                this.id,
                this.name,
                this.dateModified,
                this.dateCreated,
                this.version,
                this.description,
                this.appServiceTemplates,
                this.entryPointServiceName,
                this.createdByUserId,
                true);
    }

    @Override
    public String toString() {
        return "DBApplicationTemplate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dateModified=" + dateModified +
                ", dateCreated=" + dateCreated +
                ", version='" + version + '\'' +
                ", description='" + description + '\'' +
                ", appServiceTemplates=" + appServiceTemplates +
                ", entryPointServiceName='" + entryPointServiceName + '\'' +
                ", createdBy='" + createdByUserId + '\'' +
                ", deleted='" + deleted + '\'' +
                '}';
    }
}
