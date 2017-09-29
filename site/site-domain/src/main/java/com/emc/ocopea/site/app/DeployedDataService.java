// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import com.emc.ocopea.site.DeployApplicationOnSiteCommandArgs;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by liebea on 1/12/16.
 * Drink responsibly
 */
public class DeployedDataService {
    private final String dsbUrn;
    private final String dsbUrl;
    private final String plan;
    private final String bindName;
    private final String serviceId;
    private final DeployApplicationOnSiteCommandArgs
            .DeployAppServiceOnSiteManifestDTO
            .DeployDataServiceRestoreInfoDTO restoreInfo;
    private final Map<String, String> dsbSettings;
    private DeployedDataServiceState state = DeployedDataServiceState.pending;
    private Date stateTimeStamp;
    private String stateMessage = null;
    private DeployedDataServiceBindings bindingInfo;

    public DeployedDataService(
            String dsbUrn,
            String dsbUrl,
            String plan,
            String bindName,
            String serviceId,
            Map<String, String> dsbSettings,
            DeployApplicationOnSiteCommandArgs.DeployAppServiceOnSiteManifestDTO
                    .DeployDataServiceRestoreInfoDTO restoreInfo) {
        this.dsbUrn = dsbUrn;
        this.dsbUrl = dsbUrl;
        this.plan = plan;
        this.bindName = bindName;
        this.serviceId = serviceId;
        this.dsbSettings = dsbSettings;
        this.restoreInfo = restoreInfo;
        this.stateTimeStamp = new Date();
        this.bindingInfo = null;

    }

    public void bind(DeployedDataServiceBindings bindInfo, Date stateTimeStamp) {
        this.bindingInfo = bindInfo;
        this.state = DeployedDataServiceState.bound;
        this.stateTimeStamp = stateTimeStamp;
    }

    public String getDsbUrn() {
        return dsbUrn;
    }

    public String getDsbUrl() {
        return dsbUrl;
    }

    public String getPlan() {
        return plan;
    }

    public String getBindName() {
        return bindName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public DeployApplicationOnSiteCommandArgs
            .DeployAppServiceOnSiteManifestDTO.DeployDataServiceRestoreInfoDTO getRestoreInfo() {
        return restoreInfo;
    }

    public Map<String, String> getDsbSettings() {
        return dsbSettings;
    }

    public DeployedDataServiceState getState() {
        return state;
    }

    public Date getStateTimeStamp() {
        return stateTimeStamp;
    }

    public String getStateMessage() {
        return stateMessage;
    }

    public DeployedDataServiceBindings getBindingInfo() {
        return bindingInfo;
    }

    public void setState(DeployedDataServiceState state, String message, Date stateTimeStamp) {
        // todo: test that it makes sense according to current state
        this.state = state;
        this.stateMessage = message;
        this.stateTimeStamp = stateTimeStamp;
    }

    public void setBindingInfo(DeployedDataServiceBindings bindingInfo) {
        this.bindingInfo = bindingInfo;
    }

    public static class DeployedDataServiceBindings {
        private final Map<String, String> bindInfo;
        private final List<DeployedDataServicePort> ports;

        private DeployedDataServiceBindings() {
            this(null, null);
        }

        public DeployedDataServiceBindings(Map<String, String> bindInfo, List<DeployedDataServicePort> ports) {
            this.bindInfo = bindInfo;
            this.ports = ports;
        }

        public Map<String, String> getBindInfo() {
            return bindInfo;
        }

        public List<DeployedDataServicePort> getPorts() {
            return ports;
        }
    }

    public static class DeployedDataServicePort {
        private final String protocol;
        private final String destination;
        private final int port;

        private DeployedDataServicePort() {
            this(null, null, 0);
        }

        public DeployedDataServicePort(String protocol, String destination, int port) {
            this.protocol = protocol;
            this.destination = destination;
            this.port = port;
        }

        public String getProtocol() {
            return protocol;
        }

        public String getDestination() {
            return destination;
        }

        public int getPort() {
            return port;
        }
    }
}
