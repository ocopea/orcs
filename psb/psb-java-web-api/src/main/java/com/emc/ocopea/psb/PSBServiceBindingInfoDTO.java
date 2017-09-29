// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.psb;

import java.util.List;
import java.util.Map;

public class PSBServiceBindingInfoDTO {

    private final String serviceName;
    private final String serviceId;
    private final String plan;
    private final Map<String, String> bindInfo;
    private final List<PSBBindPortDTO> ports;

    private PSBServiceBindingInfoDTO() {
        this(null, null, null, null, null);
    }

    public PSBServiceBindingInfoDTO(
            String serviceName,
            String serviceId,
            String plan,
            Map<String, String> bindInfo,
            List<PSBBindPortDTO> ports) {
        this.serviceName = serviceName;
        this.serviceId = serviceId;
        this.plan = plan;
        this.bindInfo = bindInfo;
        this.ports = ports;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getPlan() {
        return plan;
    }

    public Map<String, String> getBindInfo() {
        return bindInfo;
    }

    public List<PSBBindPortDTO> getPorts() {
        return ports;
    }

    @Override
    public String toString() {
        return "PSBServiceBindingInfoDTO{" +
                "serviceName='" + serviceName + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", plan='" + plan + '\'' +
                ", bindInfo=" + bindInfo +
                ", ports=" + ports +
                '}';
    }
}
