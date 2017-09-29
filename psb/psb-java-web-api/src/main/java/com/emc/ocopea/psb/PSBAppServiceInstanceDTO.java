// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.psb;

import java.util.Map;

public class PSBAppServiceInstanceDTO {
    private final String name;
    private final PSBAppServiceStatusEnumDTO status;
    private final String statusMessage;
    private final int instances;
    private final Map<String, String> psbMetrics;
    private final String entryPointURL;

    private PSBAppServiceInstanceDTO() {
        this(null, null, null, 0, null, null);
    }

    public PSBAppServiceInstanceDTO(
            String name,
            PSBAppServiceStatusEnumDTO status,
            String statusMessage,
            int instances,
            Map<String, String> psbMetrics,
            String entryPointURL) {

        this.name = name;
        this.status = status;
        this.statusMessage = statusMessage;
        this.instances = instances;
        this.psbMetrics = psbMetrics;
        this.entryPointURL = entryPointURL;
    }

    public String getName() {
        return name;
    }

    public PSBAppServiceStatusEnumDTO getStatus() {
        return status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public int getInstances() {
        return instances;
    }

    public Map<String, String> getPsbMetrics() {
        return psbMetrics;
    }

    public String getEntryPointURL() {
        return entryPointURL;
    }

    @Override
    public String toString() {
        return "PSBAppServiceInstanceDTO{" +
                "name='" + name + '\'' +
                ", status=" + status +
                ", statusMessage='" + statusMessage + '\'' +
                ", instances=" + instances +
                ", psbMetrics=" + psbMetrics +
                ", entryPointURL='" + entryPointURL + '\'' +
                '}';
    }
}
