// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liebea on 10/26/16.
 * Drink responsibly
 */
public class ApplicationCopyScheduledEvent extends ApplicationCopyEvent {
    private final UUID appInstanceId;
    private final Date copyTimestamp;

    private final Collection<DataServiceInfo> dataServiceCopies;
    private final Collection<AppServiceInfo> appServiceCopies;

    private ApplicationCopyScheduledEvent() {
        this(null, null, 0, null, null, null, null, null);
    }

    public ApplicationCopyScheduledEvent(
            UUID id,
            UUID appInstanceId,
            long version,
            Date timeStamp,
            String message,
            Date copyTimestamp,
            Collection<DataServiceInfo> dataServiceCopies,
            Collection<AppServiceInfo> appServiceCopies) {
        super(id, appInstanceId, version, timeStamp, message);
        this.appInstanceId = appInstanceId;
        this.copyTimestamp = copyTimestamp;
        this.dataServiceCopies = dataServiceCopies;
        this.appServiceCopies = appServiceCopies;
    }

    public static class DataServiceInfo {
        private final String dsbUrn;
        private final String dsbUrl;
        private final String serviceId;
        private final String bindName;
        private final String facility;
        private final Map<String, String> dsbSettings;

        public DataServiceInfo(
                String dsbUrn,
                String dsbUrl,
                String serviceId,
                String bindName,
                String facility,
                Map<String, String> dsbSettings) {
            this.dsbUrn = dsbUrn;
            this.dsbUrl = dsbUrl;
            this.serviceId = serviceId;
            this.bindName = bindName;
            this.facility = facility;
            this.dsbSettings = dsbSettings;
        }

        private DataServiceInfo() {
            this(null, null, null, null, null, null);
        }

        public String getDsbUrn() {
            return dsbUrn;
        }

        public String getDsbUrl() {
            return dsbUrl;
        }

        public String getServiceId() {
            return serviceId;
        }

        public String getBindName() {
            return bindName;
        }

        public String getFacility() {
            return facility;
        }

        public Map<String, String> getDsbSettings() {
            return dsbSettings;
        }

        @Override
        public String toString() {
            return "DataServiceInfo{" +
                    "dsbUrn='" + dsbUrn + '\'' +
                    ", dsbUrl='" + dsbUrl + '\'' +
                    ", serviceId='" + serviceId + '\'' +
                    ", bindName='" + bindName + '\'' +
                    ", facility='" + facility + '\'' +
                    ", dsbSettings=" + dsbSettings +
                    '}';
        }
    }

    public static class AppServiceInfo {
        private final String psbURN;
        private final String appServiceName;
        private final String appImageName;
        private final String appImageType;
        private final String appImageVersion;

        private AppServiceInfo() {
            this(null, null, null, null, null);
        }

        public AppServiceInfo(
                String psbURN, String appServiceName, String appImageName, String appImageType,
                String appImageVersion) {
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
            return "AppServiceInfo{" +
                    "psbURN='" + psbURN + '\'' +
                    ", appServiceName='" + appServiceName + '\'' +
                    ", appImageName='" + appImageName + '\'' +
                    ", appImageType='" + appImageType + '\'' +
                    ", appImageVersion='" + appImageVersion + '\'' +
                    '}';
        }
    }

    public UUID getAppInstanceId() {
        return appInstanceId;
    }

    public Date getCopyTimestamp() {
        return copyTimestamp;
    }

    public Collection<DataServiceInfo> getDataServiceCopies() {
        return dataServiceCopies;
    }

    public Collection<AppServiceInfo> getAppServiceCopies() {
        return appServiceCopies;
    }

    @Override
    public String toString() {
        return "ApplicationCopyScheduledEvent{" +
                "appInstanceId=" + appInstanceId +
                ", copyTimestamp=" + copyTimestamp +
                ", dataServiceCopies=" + dataServiceCopies +
                ", appServiceCopies=" + appServiceCopies +
                '}';
    }
}
