// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liebea on 10/18/16.
 * Drink responsibly
 */
public class DataServiceBoundEvent extends DeployedApplicationEvent {
    private final String bindName;
    private final BindingInfo bindingInfo;

    private DataServiceBoundEvent() {
        this(null, 0L, null, null, null, null);
    }

    DataServiceBoundEvent(UUID id, long version, Date timestamp, String message, String bindName,
                          BindingInfo bindingInfo) {
        super(id, version, timestamp, message);
        this.bindName = bindName;
        this.bindingInfo = bindingInfo;
    }

    public static class BindingInfo {
        private final Map<String, String> bindInfo;
        private final List<DeployedDataServicePort> ports;

        private BindingInfo() {
            this(null, null);
        }

        public BindingInfo(Map<String, String> bindInfo, List<DeployedDataServicePort> ports) {
            this.bindInfo = bindInfo;
            this.ports = ports;
        }

        public Map<String, String> getBindInfo() {
            return bindInfo;
        }

        public List<DeployedDataServicePort> getPorts() {
            return ports;
        }

        @Override
        public String toString() {
            return "BindingInfo{" +
                    "bindInfo=" + bindInfo +
                    ", ports=" + ports +
                    '}';
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

    public String getBindName() {
        return bindName;
    }

    public BindingInfo getBindingInfo() {
        return bindingInfo;
    }

    @Override
    public String toString() {
        return "DataServiceBoundEvent{" +
                "bindName='" + bindName + '\'' +
                ", bindingInfo=" + bindingInfo +
                '}';
    }
}
