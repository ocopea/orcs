// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liebea on 1/17/16.
 * Drink responsibly
 */
public class AppInstanceStatisticsDTO {

    private final UUID appInstanceId;
    private final Collection<AppInstanceCopyStatisticsDTO> appCopies;
    private final Collection<DataServiceProductionCopyStatisticsDTO> productionCopyStatistics;

    private AppInstanceStatisticsDTO() {
        this(null, null, null);
    }

    public AppInstanceStatisticsDTO(UUID appInstanceId, Collection<AppInstanceCopyStatisticsDTO> appCopies,
                                    Collection<DataServiceProductionCopyStatisticsDTO> productionCopyStatistics) {
        this.appInstanceId = appInstanceId;
        this.appCopies = appCopies;
        this.productionCopyStatistics = productionCopyStatistics;
    }

    public UUID getAppInstanceId() {
        return appInstanceId;
    }

    public Collection<AppInstanceCopyStatisticsDTO> getAppCopies() {
        return appCopies;
    }

    public Collection<DataServiceProductionCopyStatisticsDTO> getProductionCopyStatistics() {
        return productionCopyStatistics;
    }

    public static class DataServiceCopyStatisticsDTO {
        private final UUID copyId;
        private final String dsbUrn;
        private final String bindName;
        private final Date timeStamp;
        private final String repoId;
        private final String facility;
        private final String copyRepositoryURN;
        private final String copyRepositoryName;
        private final String copyRepositoryProtocol;
        private final String copyRepositoryProtocolVersion;
        private final long size;
        private final String status;

        private DataServiceCopyStatisticsDTO() {
            this(null, null, null, null, null, null, null, null, null, null, 0L, null);
        }

        public DataServiceCopyStatisticsDTO(
                UUID copyId,
                String dsbUrn,
                String bindName,
                Date timeStamp,
                String repoId,
                String facility,
                String copyRepositoryURN,
                String copyRepositoryName,
                String copyRepositoryProtocol,
                String copyRepositoryProtocolVersion,
                long size,
                String status) {
            this.copyId = copyId;
            this.dsbUrn = dsbUrn;
            this.bindName = bindName;
            this.timeStamp = timeStamp;
            this.repoId = repoId;
            this.facility = facility;
            this.copyRepositoryURN = copyRepositoryURN;
            this.copyRepositoryName = copyRepositoryName;
            this.copyRepositoryProtocol = copyRepositoryProtocol;
            this.copyRepositoryProtocolVersion = copyRepositoryProtocolVersion;
            this.size = size;
            this.status = status;
        }

        public UUID getCopyId() {
            return copyId;
        }

        public String getDsbUrn() {
            return dsbUrn;
        }

        public String getBindName() {
            return bindName;
        }

        public Date getTimeStamp() {
            return timeStamp;
        }

        public String getRepoId() {
            return repoId;
        }

        public String getFacility() {
            return facility;
        }

        public String getCopyRepositoryURN() {
            return copyRepositoryURN;
        }

        public String getCopyRepositoryName() {
            return copyRepositoryName;
        }

        public String getCopyRepositoryProtocol() {
            return copyRepositoryProtocol;
        }

        public String getCopyRepositoryProtocolVersion() {
            return copyRepositoryProtocolVersion;
        }

        public long getSize() {
            return size;
        }

        public String getStatus() {
            return status;
        }

        @Override
        public String toString() {
            return "DataServiceCopyStatisticsDTO{" +
                    "copyId=" + copyId +
                    ", dsbUrn='" + dsbUrn + '\'' +
                    ", bindName='" + bindName + '\'' +
                    ", timeStamp=" + timeStamp +
                    ", repoId='" + repoId + '\'' +
                    ", facility='" + facility + '\'' +
                    ", copyRepositoryURN='" + copyRepositoryURN + '\'' +
                    ", copyRepositoryName='" + copyRepositoryName + '\'' +
                    ", copyRepositoryProtocol='" + copyRepositoryProtocol + '\'' +
                    ", copyRepositoryProtocolVersion='" + copyRepositoryProtocolVersion + '\'' +
                    ", size=" + size +
                    ", status='" + status + '\'' +
                    '}';
        }
    }

    public static class AppServiceCopyStatisticsDTO {
        private final String appServiceName;
        private final String appImageName;
        private final String appimageType;
        private final String appImageVersion;
        private final Date timeStamp;
        private final Map<String, String> appServiceConfiguration;
        private final String status;

        private AppServiceCopyStatisticsDTO() {
            this(null, null, null, null, null, null, null);
        }

        public AppServiceCopyStatisticsDTO(String appServiceName, String appImageName, String appimageType,
                                           String appImageVersion, Date timeStamp,
                                           Map<String, String> appServiceConfiguration, String status) {
            this.appServiceName = appServiceName;
            this.appImageName = appImageName;
            this.appimageType = appimageType;
            this.appImageVersion = appImageVersion;
            this.timeStamp = timeStamp;
            this.appServiceConfiguration = appServiceConfiguration;
            this.status = status;
        }

        public String getAppServiceName() {
            return appServiceName;
        }

        public String getAppImageName() {
            return appImageName;
        }

        public String getAppimageType() {
            return appimageType;
        }

        public String getAppImageVersion() {
            return appImageVersion;
        }

        public Date getTimeStamp() {
            return timeStamp;
        }

        public Map<String, String> getAppServiceConfiguration() {
            return appServiceConfiguration;
        }

        public String getStatus() {
            return status;
        }

        @Override
        public String toString() {
            return "AppServiceCopyStatisticsDTO{" + "appServiceName='" + appServiceName + '\'' +
                    ", appImageName='" + appImageName + '\'' +
                    ", appImageVersion='" + appImageVersion + '\'' +
                    ", timeStamp=" + timeStamp +
                    ", appServiceConfiguration=" + appServiceConfiguration +
                    ", status='" + status + '\'' + '}';
        }
    }

    public static class DataServiceProductionCopyStatisticsDTO {
        private final String dsbName;
        private final String bindName;
        private final String storageType;
        private final long size;

        private DataServiceProductionCopyStatisticsDTO() {
            this(null, null, null, 0L);
        }

        public DataServiceProductionCopyStatisticsDTO(String dsbName, String bindName, String storageType, long size) {
            this.dsbName = dsbName;
            this.bindName = bindName;
            this.storageType = storageType;
            this.size = size;
        }

        public String getDsbName() {
            return dsbName;
        }

        public String getBindName() {
            return bindName;
        }

        public String getStorageType() {
            return storageType;
        }

        public long getSize() {
            return size;
        }
    }

    @Override
    public String toString() {
        return "AppInstanceStatisticsDTO{" + "appInstanceId=" + appInstanceId +
                ", appCopies=" + appCopies +
                ", productionCopyStatistics=" + productionCopyStatistics + '}';
    }
}
