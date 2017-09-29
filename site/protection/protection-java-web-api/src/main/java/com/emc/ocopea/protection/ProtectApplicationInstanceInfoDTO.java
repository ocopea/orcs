// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import java.util.Collection;
import java.util.Map;

public class ProtectApplicationInstanceInfoDTO {
    private final String appInstanceId;
    private final int intervalSeconds;
    private final Collection<ProtectDataServiceBinding> bindings;
    private final Collection<ProtectAppConfiguration> appConfigurations;

    private ProtectApplicationInstanceInfoDTO() {
        this(null, 0, null, null);
    }

    public ProtectApplicationInstanceInfoDTO(
            Collection<ProtectDataServiceBinding> bindings,
            int intervalSeconds,
            String appInstanceId,
            Collection<ProtectAppConfiguration> appConfigurations) {
        this.bindings = bindings;
        this.intervalSeconds = intervalSeconds;
        this.appInstanceId = appInstanceId;
        this.appConfigurations = appConfigurations;
    }

    public String getAppInstanceId() {
        return appInstanceId;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public Collection<ProtectDataServiceBinding> getBindings() {
        return bindings;
    }

    public Collection<ProtectAppConfiguration> getAppConfigurations() {
        return appConfigurations;
    }

    @Override
    public String toString() {
        return "ProtectApplicationInstanceInfoDTO{" +
                "appInstanceId='" + appInstanceId + '\'' +
                ", intervalSeconds=" + intervalSeconds +
                ", bindings=" + bindings +
                ", appConfigurations=" + appConfigurations +
                '}';
    }

    public static class ProtectDataServiceBinding {
        private final String dsbName;
        private final String dsbUrn;
        private final String dsbUrl;
        private final String serviceBindName;
        private final String dataServiceId;
        private final String facility;
        private final Map<String, String> dsbSettings;

        private ProtectDataServiceBinding() {
            this(null, null, null, null, null, null, null);
        }

        public ProtectDataServiceBinding(
                String dsbName,
                String dsbUrn,
                String dsbUrl,
                String serviceBindName,
                String dataServiceId,
                String facility,
                Map<String, String> dsbSettings) {

            this.dsbName = dsbName;
            this.dsbUrn = dsbUrn;
            this.dsbUrl = dsbUrl;
            this.serviceBindName = serviceBindName;
            this.dataServiceId = dataServiceId;
            this.facility = facility;
            this.dsbSettings = dsbSettings;
        }

        public String getDsbName() {
            return dsbName;
        }

        public String getDsbUrn() {
            return dsbUrn;
        }

        public String getDsbUrl() {
            return dsbUrl;
        }

        public String getServiceBindName() {
            return serviceBindName;
        }

        public String getDataServiceId() {
            return dataServiceId;
        }

        public String getFacility() {
            return facility;
        }

        public Map<String, String> getDsbSettings() {
            return dsbSettings;
        }

        @Override
        public String toString() {
            return "ProtectDataServiceBinding{" +
                    "dsbName='" + dsbName + '\'' +
                    ", dsbUrn='" + dsbUrn + '\'' +
                    ", dsbUrl='" + dsbUrl + '\'' +
                    ", serviceBindName='" + serviceBindName + '\'' +
                    ", dataServiceId='" + dataServiceId + '\'' +
                    ", facility='" + facility + '\'' +
                    ", dsbSettings=" + dsbSettings +
                    '}';
        }
    }

    public static class ProtectAppConfiguration {
        private final String psbURN;
        private final String appServiceName;
        private final String appImageName;
        private final String appImageType;
        private final String appImageVersion;

        private ProtectAppConfiguration() {
            this(null, null, null, null, null);
        }

        public ProtectAppConfiguration(String psbURN, String appServiceName, String appImageName,
                                       String appImageType, String appImageVersion) {
            this.psbURN = psbURN;
            this.appServiceName = appServiceName;
            this.appImageName = appImageName;
            this.appImageType = appImageType;
            this.appImageVersion = appImageVersion;
        }

        public String getPsbURN() {
            return psbURN;
        }

        public String getAppServiceName() {
            return appServiceName;
        }

        public String getAppImageName() {
            return appImageName;
        }

        public String getAppImageType() {
            return appImageType;
        }

        public String getAppImageVersion() {
            return appImageVersion;
        }

        @Override
        public String toString() {
            return "ProtectAppConfiguration{" +
                    "psbURN='" + psbURN + '\'' +
                    ", appServiceName='" + appServiceName + '\'' +
                    ", appImageName='" + appImageName + '\'' +
                    ", appImageType='" + appImageType + '\'' +
                    ", appImageVersion='" + appImageVersion + '\'' +
                    '}';
        }
    }
}
