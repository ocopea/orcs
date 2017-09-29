// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.discovery;

import java.util.Map;

/**
 * Created by liebea on 5/2/16.
 * Drink responsibly
 */
public class DiscoveredService {
    private final String serviceName;
    private final String serviceURL;
    private final Map<String, String> params;

    public DiscoveredService(String serviceName, String serviceURL, Map<String, String> params) {
        this.serviceName = serviceName;
        this.serviceURL = serviceURL;
        this.params = params;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceURL() {
        return serviceURL;
    }

    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "DiscoveredService{" +
                "serviceName='" + serviceName + '\'' +
                ", serviceURL='" + serviceURL + '\'' +
                ", params=" + params +
                '}';
    }
}