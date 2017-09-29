// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by liebea on 11/29/15.
 * Drink responsibly
 */
public class ApplicationTemplate {
    private final UUID id;
    private final String name;
    private final String version;
    private final String description;
    private final Map<String, ApplicationServiceTemplate> appServiceTemplates;
    private final String entryPointServiceName;
    private final UUID createdByUserId;

    public ApplicationTemplate(
            UUID id,
            String name,
            String version,
            String description,
            Collection<ApplicationServiceTemplate> appServiceTemplates,
            String entryPointServiceName,
            UUID createdByUserId) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.description = description;
        this.appServiceTemplates =
                appServiceTemplates
                        .stream()
                        .collect(Collectors.toMap(
                                ApplicationServiceTemplate::getAppServiceName,
                                o -> o));

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

    public Collection<ApplicationServiceTemplate> getAppServiceTemplates() {
        return appServiceTemplates.values();
    }

    public String getEntryPointServiceName() {
        return entryPointServiceName;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    /***
     * Returns the relative path for http url for the application entry point.
     */
    public String getEntryPointUrl() {
        if (entryPointServiceName != null && !entryPointServiceName.isEmpty()) {
            final ApplicationServiceTemplate entryPointAppService = appServiceTemplates.get(entryPointServiceName);
            if (entryPointAppService != null) {
                final String entryPointUrl = entryPointAppService.getEntryPointUrl();
                return entryPointUrl == null ? "" : entryPointUrl;
            }
        }
        return "";
    }
}
