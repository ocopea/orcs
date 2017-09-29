// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by liebea on 7/22/15.
 * Drink responsibly
 */
public class DBApplicationServiceTemplate {
    private final String appServiceName;
    private final String psbType;
    private final String imageName;
    private final String imageType;
    private final String imageVersion;
    private final Map<String, String> psbSettings;
    private final Map<String, String> environmentVariables;
    private final Collection<DBAppServiceExternalDependency> dependencies;
    private final Set<Integer> exposedPorts;
    private final Integer httpPort;
    private final String entryPointUrl;

    private DBApplicationServiceTemplate() {
        this(null, null, null, null, null, null, null, null, null, null, null);
    }

    public DBApplicationServiceTemplate(
            String appServiceName,
            String psbType,
            String imageName,
            String imageType,
            String imageVersion,
            Map<String, String> psbSettings,
            Map<String, String> environmentVariables,
            Collection<DBAppServiceExternalDependency> dependencies,
            Set<Integer> exposedPorts,
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

    public Collection<DBAppServiceExternalDependency> getDependencies() {
        return dependencies;
    }

    public Set<Integer> getExposedPorts() {
        return exposedPorts;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public String getEntryPointUrl() {
        return entryPointUrl;
    }

    @Override
    public String toString() {
        return "DBApplicationServiceTemplate{" +
                "appServiceName='" + appServiceName + '\'' +
                ", psbType='" + psbType + '\'' +
                ", imageName='" + imageName + '\'' +
                ", imageType='" + imageType + '\'' +
                ", imageVersion='" + imageVersion + '\'' +
                ", psbSettings=" + psbSettings +
                ", environmentVariables=" + environmentVariables +
                ", dependencies=" + dependencies +
                ", exposedPorts=" + exposedPorts +
                ", httpPort=" + httpPort +
                ", entryPointUrl='" + entryPointUrl + '\'' +
                '}';
    }
}
