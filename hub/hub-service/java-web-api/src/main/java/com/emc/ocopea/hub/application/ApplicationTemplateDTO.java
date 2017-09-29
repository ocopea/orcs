// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import java.util.Collection;
import java.util.UUID;

/**
 * Created by liebea on 11/29/15.
 * Drink responsibly
 */
public class ApplicationTemplateDTO {
    private final UUID id;
    private final String name;
    private final String version;
    private final String description;
    private final Collection<ApplicationServiceTemplateDTO> appServiceTemplates;
    private final String entryPointServiceName;
    private final UUID createdByUserId;

    @SuppressWarnings("unused")
    private ApplicationTemplateDTO() {
        this(null, null, null, null, null, null, null);
    }

    public ApplicationTemplateDTO(
            UUID id,
            String name,
            String version,
            String description,
            Collection<ApplicationServiceTemplateDTO> appServiceTemplates,
            String entryPointServiceName,
            UUID createdByUserId) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.description = description;
        this.appServiceTemplates = appServiceTemplates;
        this.entryPointServiceName = entryPointServiceName;
        this.createdByUserId = createdByUserId;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public Collection<ApplicationServiceTemplateDTO> getAppServiceTemplates() {
        return appServiceTemplates;
    }

    public String getEntryPointServiceName() {
        return entryPointServiceName;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    @Override
    public String toString() {
        return "ApplicationTemplateDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", description='" + description + '\'' +
                ", appServiceTemplates=" + appServiceTemplates +
                ", entryPointServiceName='" + entryPointServiceName + '\'' +
                ", createdByUserId=" + createdByUserId +
                '}';
    }
}
