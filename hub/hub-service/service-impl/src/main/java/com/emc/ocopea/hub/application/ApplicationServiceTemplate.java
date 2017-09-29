// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by liebea on 7/22/15.
 * Drink responsibly
 */
public class ApplicationServiceTemplate {
    private final String appServiceName;
    private final String psbType;
    private final String imageName;
    private final String imageType;
    private final String imageVersion;
    private final Map<String, String> psbSettings;
    private final Map<String, String> environmentVariables;
    private final Collection<AppServiceExternalDependency> dependencies;
    private final List<Integer> exposedPorts;
    private final Integer httpPort;
    private final String entryPointUrl;

    public ApplicationServiceTemplate(
            String appServiceName,
            String psbType,
            String imageName,
            String imageType,
            String imageVersion,
            Map<String, String> psbSettings,
            Map<String, String> environmentVariables,
            Collection<AppServiceExternalDependency> dependencies,
            List<Integer> exposedPorts,
            Integer httpPort,
            String entryPointUrl) {
        this.appServiceName = appServiceName;
        this.psbType = psbType;
        this.imageName = imageName;
        this.imageType = imageType;
        this.imageVersion = imageVersion;
        this.psbSettings = psbSettings;
        this.environmentVariables = environmentVariables;
        this.dependencies = dependencies;
        this.exposedPorts = exposedPorts;
        this.httpPort = httpPort;
        this.entryPointUrl = entryPointUrl;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public String getPsbType() {
        return psbType;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageType() {
        return imageType;
    }

    public String getImageVersion() {
        return imageVersion;
    }

    public Map<String, String> getPsbSettings() {
        return psbSettings;
    }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public Collection<AppServiceExternalDependency> getDependencies() {
        return dependencies;
    }

    public List<Integer> getExposedPorts() {
        return exposedPorts;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public String getEntryPointUrl() {
        return entryPointUrl;
    }
}
