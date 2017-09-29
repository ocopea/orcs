// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liebea on 1/17/16.
 * Drink responsibly
 */
public class ProtectionAppCopyDTO {
    private final UUID copyId;
    private final String appInstanceId;
    private final Date timeStamp;
    private final ApplicationCopyState state;

    // dsb, bindName, copyInfo
    private final Map<String, Map<String, DataProtectionDataServiceCopyInfoDTO>> dataServiceCopies;

    // appInstanceName, copyInfo
    private final Map<String, DataProtectionAppServiceCopyInfoDTO> appServiceCopies;

    private ProtectionAppCopyDTO() {
        this(null, null, null, null, null, null);
    }

    public ProtectionAppCopyDTO(UUID copyId, String appInstanceId, Date timeStamp, ApplicationCopyState state,
                                Map<String, Map<String, DataProtectionDataServiceCopyInfoDTO>> dataServiceCopies,
                                Map<String, DataProtectionAppServiceCopyInfoDTO> appServiceCopies) {
        this.copyId = copyId;
        this.appInstanceId = appInstanceId;
        this.timeStamp = timeStamp;
        this.state = state;
        this.dataServiceCopies = dataServiceCopies;
        this.appServiceCopies = appServiceCopies;
    }

    public UUID getCopyId() {
        return copyId;
    }

    public String getAppInstanceId() {
        return appInstanceId;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public ApplicationCopyState getState() {
        return state;
    }

    public Map<String, Map<String, DataProtectionDataServiceCopyInfoDTO>> getDataServiceCopies() {
        return dataServiceCopies;
    }

    public Map<String, DataProtectionAppServiceCopyInfoDTO> getAppServiceCopies() {
        return appServiceCopies;
    }

    @Override
    public String toString() {
        return "ProtectionPolicyAppCopyDTO{" +
                "copyId=" + copyId +
                ", appInstanceId='" + appInstanceId + '\'' +
                ", timeStamp=" + timeStamp +
                ", state=" + state +
                ", dataServiceCopies=" + dataServiceCopies +
                ", appServiceCopies=" + appServiceCopies +
                '}';
    }

    public static class DataProtectionDataServiceCopyInfoDTO {
        private final String copyRepoURN;
        private final UUID copyId;
        private final ApplicationDataServiceCopyState state;

        private DataProtectionDataServiceCopyInfoDTO() {
            this(null, null, null);
        }

        public DataProtectionDataServiceCopyInfoDTO(String copyRepoURN, UUID copyId,
                                                    ApplicationDataServiceCopyState state) {
            this.copyRepoURN = copyRepoURN;
            this.copyId = copyId;
            this.state = state;
        }

        public String getCopyRepoURN() {
            return copyRepoURN;
        }

        public UUID getCopyId() {
            return copyId;
        }

        public ApplicationDataServiceCopyState getState() {
            return state;
        }

        @Override
        public String toString() {
            return "DataProtectionDataServiceCopyInfoDTO{" +
                    "copyRepoURN='" + copyRepoURN + '\'' +
                    ", copyId=" + copyId +
                    ", state=" + state +
                    '}';
        }
    }

    public static class DataProtectionAppServiceCopyInfoDTO {
        private final String appImageName;
        private final String appImageType;
        private final String appImageVersion;
        private final Map<String, String> appServiceConfiguration;
        private final Date copyTimestamp;
        private final ApplicationDataServiceCopyState state;

        private DataProtectionAppServiceCopyInfoDTO() {
            this(null, null, null, null, null, null);
        }

        public DataProtectionAppServiceCopyInfoDTO(String appImageName, String appImageType,
                                                   String appImageVersion,
                                                   Map<String, String> appServiceConfiguration,
                                                   Date copyTimestamp,
                                                   ApplicationDataServiceCopyState state) {
            this.appImageName = appImageName;
            this.appImageType = appImageType;
            this.appImageVersion = appImageVersion;
            this.appServiceConfiguration = appServiceConfiguration;
            this.copyTimestamp = copyTimestamp;
            this.state = state;
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

        public Map<String, String> getAppServiceConfiguration() {
            return appServiceConfiguration;
        }

        public Date getCopyTimestamp() {
            return copyTimestamp;
        }

        public ApplicationDataServiceCopyState getState() {
            return state;
        }

        @Override
        public String toString() {
            return "DataProtectionAppServiceCopyInfoDTO{" +
                    "appImageName='" + appImageName + '\'' +
                    ", appImageVersion='" + appImageVersion + '\'' +
                    ", appServiceConfiguration=" + appServiceConfiguration +
                    ", copyTimestamp=" + copyTimestamp +
                    ", state=" + state +
                    '}';
        }
    }

}
