// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Created by liebea on 1/12/16.
 * Drink responsibly
 */
public class DeployedAppService {
    private final String appServiceName;
    private final String artifactRegistryName;
    private final String psbUrn;
    private final String psbUrl;

    /***
     * appServiceId is an internal identifier for Site-PSB communication.
     */
    private final String psbAppServiceId;
    private final String space;
    private final String imageName;
    private final String imageType;
    private final String imageVersion;
    private final Map<String, String> psbSettings;
    private final Map<String, String> environmentVariables;
    private final Set<Integer> exposedPorts;
    private final Integer httpPort;
    private final String route;
    private DeployedAppServiceState state = DeployedAppServiceState.pending;
    private Date stateTimeStamp;
    private String stateMessage = null;
    private String publicURL = null;

    public DeployedAppService(
            String appServiceName,
            String psbUrn,
            String psbAppServiceId,
            String artifactRegistryName,
            String psbUrl,
            String space,
            String imageName,
            String imageType,
            String imageVersion,
            Map<String, String> psbSettings,
            Map<String, String> environmentVariables,
            Set<Integer> exposedPorts,
            Integer httpPort,
            String route) {
        this.appServiceName = appServiceName;
        this.psbUrn = psbUrn;
        this.psbAppServiceId = psbAppServiceId;
        this.artifactRegistryName = artifactRegistryName;
        this.psbUrl = psbUrl;
        this.space = space;
        this.imageName = imageName;
        this.imageType = imageType;
        this.imageVersion = imageVersion;
        this.psbSettings = psbSettings;
        this.environmentVariables = environmentVariables;
        this.exposedPorts = exposedPorts;
        this.httpPort = httpPort;
        this.route = route;
        this.stateTimeStamp = new Date();
        this.publicURL = null;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public String getPsbAppServiceId() {
        return psbAppServiceId;
    }

    public String getPsbUrn() {
        return psbUrn;
    }

    public String getPsbUrl() {
        return psbUrl;
    }

    public String getSpace() {
        return space;
    }

    public String getArtifactRegistryName() {
        return artifactRegistryName;
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

    public Set<Integer> getExposedPorts() {
        return exposedPorts;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public String getRoute() {
        return route;
    }

    public DeployedAppServiceState getState() {
        return state;
    }

    public Date getStateTimeStamp() {
        return stateTimeStamp;
    }

    public String getStateMessage() {
        return stateMessage;
    }

    public String getPublicURL() {
        return publicURL;
    }

    public void setState(DeployedAppServiceState state, String message, Date stateTimeStamp) {
        // todo: test that it makes sense according to current state
        this.state = state;
        this.stateMessage = message;
        this.stateTimeStamp = stateTimeStamp;
    }

    public void setPublicURL(String publicURL) {
        this.publicURL = publicURL;
    }

    @Override
    public String toString() {
        return "DeployedAppService{" +
                "appServiceName='" + appServiceName + '\'' +
                ", artifactRegistryName='" + artifactRegistryName + '\'' +
                ", psbUrn='" + psbUrn + '\'' +
                ", psbUrl='" + psbUrl + '\'' +
                ", psbAppServiceId='" + psbAppServiceId + '\'' +
                ", space='" + space + '\'' +
                ", imageName='" + imageName + '\'' +
                ", imageType='" + imageType + '\'' +
                ", imageVersion='" + imageVersion + '\'' +
                ", psbSettings=" + psbSettings +
                ", environmentVariables=" + environmentVariables +
                ", exposedPorts=" + exposedPorts +
                ", httpPort=" + httpPort +
                ", route='" + route + '\'' +
                ", state=" + state +
                ", stateTimeStamp=" + stateTimeStamp +
                ", stateMessage='" + stateMessage + '\'' +
                ", publicURL='" + publicURL + '\'' +
                '}';
    }
}

