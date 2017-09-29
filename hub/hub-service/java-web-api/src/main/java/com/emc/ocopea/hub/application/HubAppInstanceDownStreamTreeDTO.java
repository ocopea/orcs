// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import java.util.Collection;
import java.util.UUID;

/**
 * Created by liebea on 1/24/16.
 * Drink responsibly
 */
public class HubAppInstanceDownStreamTreeDTO {
    private final UUID siteId;
    private final UUID appInstanceId;
    private final String deploymentType;
    private final Collection<HubAppInstanceDownStreamTreeDTO> downStreamInstances;

    private HubAppInstanceDownStreamTreeDTO() {
        this(null, null, null, null);
    }

    public HubAppInstanceDownStreamTreeDTO(
            UUID siteId,
            UUID appInstanceId,
            String deploymentType,
            Collection<HubAppInstanceDownStreamTreeDTO> downStreamInstances) {
        this.siteId = siteId;
        this.appInstanceId = appInstanceId;
        this.deploymentType = deploymentType;
        this.downStreamInstances = downStreamInstances;
    }

    public UUID getSiteId() {
        return siteId;
    }

    public UUID getAppInstanceId() {
        return appInstanceId;
    }

    public String getDeploymentType() {
        return deploymentType;
    }

    public Collection<HubAppInstanceDownStreamTreeDTO> getDownStreamInstances() {
        return downStreamInstances;
    }
}
