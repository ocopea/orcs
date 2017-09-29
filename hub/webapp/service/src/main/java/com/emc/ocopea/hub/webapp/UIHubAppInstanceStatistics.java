// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.webapp;

import com.emc.ocopea.site.AppInstanceCopyStatisticsDTO;
import com.emc.ocopea.site.AppInstanceStatisticsDTO;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liebea on 1/17/16.
 * Drink responsibly
 */
public class UIHubAppInstanceStatistics {

    private final UUID siteId;
    private final String appInstanceName;
    private final UUID appInstanceId;
    private final String deploymentType;
    private final String state;
    private final Collection<AppInstanceCopyStatisticsDTO> appCopies;
    private final Collection<AppInstanceStatisticsDTO.DataServiceProductionCopyStatisticsDTO> productionCopyStatistics;
    private final Map<String, Collection<UIHubAppInstanceStatistics>> downStreamAppInstanceStats;

    private UIHubAppInstanceStatistics() {
        this(null, null, null, null, null, null, null, null);
    }

    public UIHubAppInstanceStatistics(
            UUID siteId,
            String appInstanceName,
            UUID appInstanceId,
            String deploymentType,
            String state,
            Collection<AppInstanceCopyStatisticsDTO> appCopies,
            Collection<AppInstanceStatisticsDTO.DataServiceProductionCopyStatisticsDTO> productionCopyStatistics,
            Map<String, Collection<UIHubAppInstanceStatistics>> downStreamAppInstanceStats) {
        this.siteId = siteId;
        this.appInstanceName = appInstanceName;
        this.appInstanceId = appInstanceId;
        this.deploymentType = deploymentType;
        this.state = state;
        this.appCopies = appCopies;
        this.productionCopyStatistics = productionCopyStatistics;
        this.downStreamAppInstanceStats = downStreamAppInstanceStats;
    }

    public UUID getSiteId() {
        return siteId;
    }

    public UUID getAppInstanceId() {
        return appInstanceId;
    }

    public String getAppInstanceName() {
        return appInstanceName;
    }

    public String getDeploymentType() {
        return deploymentType;
    }

    public String getState() {
        return state;
    }

    public Collection<AppInstanceCopyStatisticsDTO> getAppCopies() {
        return appCopies;
    }

    public Collection<AppInstanceStatisticsDTO.DataServiceProductionCopyStatisticsDTO> getProductionCopyStatistics() {
        return productionCopyStatistics;
    }

    public Map<String, Collection<UIHubAppInstanceStatistics>> getDownStreamAppInstanceStats() {
        return downStreamAppInstanceStats;
    }
}
