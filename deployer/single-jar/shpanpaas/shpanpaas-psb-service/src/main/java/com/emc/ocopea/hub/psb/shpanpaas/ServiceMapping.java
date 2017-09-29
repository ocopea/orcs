// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.psb.shpanpaas;

/**
 * Created by liebea on 7/23/15.
 * Drink responsibly
 */
public class ServiceMapping {
    private final String serviceType;
    private final String serviceLogicalName;
    private final String servicePhysicalName;

    public ServiceMapping(String serviceType, String serviceLogicalName, String servicePhysicalName) {
        this.serviceType = serviceType;
        this.serviceLogicalName = serviceLogicalName;
        this.servicePhysicalName = servicePhysicalName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getServiceLogicalName() {
        return serviceLogicalName;
    }

    public String getServicePhysicalName() {
        return servicePhysicalName;
    }
}
