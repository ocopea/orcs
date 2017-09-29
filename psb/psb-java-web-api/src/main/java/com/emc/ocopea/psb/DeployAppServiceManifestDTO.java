// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.psb;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by liebea on 1/11/16.
 * Drink responsibly
 */
public class DeployAppServiceManifestDTO {
    private final String appServiceId;
    private final String space;
    private final String imageName;
    private final String imageVersion;
    private final Map<String, String> environmentVariables;
    private final String artifactRegistryType;
    private final Map<String, String> artifactRegistryParameters;
    private final Map<String, String> psbSettings;
    private final String route;
    private final Set<Integer> exposedPorts;
    private final Integer httpPort;

    // dsbType/[]BindInfo
    private final Map<String, Collection<PSBServiceBindingInfoDTO>> serviceBindings;

    private DeployAppServiceManifestDTO() {
        this(null, null, null, null, null, null, null, null, null, null, null,
                null);
    }

    public DeployAppServiceManifestDTO(
            String appServiceId,
            String space,
            String imageName,
            String imageVersion,
            Map<String, String> environmentVariables,
            String artifactRegistryType,
            Map<String, String> artifactRegistryParameters,
            Map<String, String> psbSettings,
            String route,
            Set<Integer> exposedPorts,
            Integer httpPort,
            Map<String, Collection<PSBServiceBindingInfoDTO>> serviceBindings) {

        this.appServiceId = appServiceId;
        this.space = space;
        this.imageName = imageName;
        this.imageVersion = imageVersion;
        this.environmentVariables = environmentVariables;
        this.artifactRegistryType = artifactRegistryType;
        this.artifactRegistryParameters = artifactRegistryParameters;
        this.psbSettings = psbSettings;
        this.route = route;
        this.exposedPorts = exposedPorts;
        this.httpPort = httpPort;
        this.serviceBindings = serviceBindings;
    }

    public String getAppServiceId() {
        return appServiceId;
    }

    public String getSpace() {
        return space;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageVersion() {
        return imageVersion;
    }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public String getArtifactRegistryType() {
        return artifactRegistryType;
    }

    public Map<String, String> getArtifactRegistryParameters() {
        return artifactRegistryParameters;
    }

    public Map<String, String> getPsbSettings() {
        return psbSettings;
    }

    public String getRoute() {
        return route;
    }

    public Set<Integer> getExposedPorts() {
        return exposedPorts;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public Map<String, Collection<PSBServiceBindingInfoDTO>> getServiceBindings() {
        return serviceBindings;
    }

    @Override
    public String toString() {
        return "DeployAppServiceManifestDTO{" +
                "appServiceId='" + appServiceId + '\'' +
                ", space='" + space + '\'' +
                ", imageName='" + imageName + '\'' +
                ", imageVersion='" + imageVersion + '\'' +
                ", environmentVariables=" + environmentVariables +
                ", artifactRegistryType='" + artifactRegistryType + '\'' +
                ", artifactRegistryParameters=" + artifactRegistryParameters +
                ", psbSettings=" + psbSettings +
                ", route='" + route + '\'' +
                ", exposedPorts=" + exposedPorts +
                ", httpPort=" + httpPort +
                ", serviceBindings=" + serviceBindings +
                '}';
    }
}
