// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liebea on 12/13/15.
 * Drink responsibly
 */
public class DeployAppCommandArgs {
    private final String appInstanceName;
    private final UUID appTemplateId;
    private final UUID siteId;
    private final UUID creatorUserId;
    private final String purpose;
    private final Collection<ApplicationPolicyInfoDTO> appPolicies;
    private final AppTemplateDeploymentPlanDTO deploymentPlan;

    private DeployAppCommandArgs() {
        this(null, null, null, null, null, null, null);
    }

    public DeployAppCommandArgs(
            String appInstanceName,
            UUID appTemplateId,
            UUID siteId,
            UUID creatorUserId,
            String purpose,
            Collection<ApplicationPolicyInfoDTO> appPolicies,
            AppTemplateDeploymentPlanDTO deploymentPlan) {
        this.appInstanceName = appInstanceName;
        this.appTemplateId = appTemplateId;
        this.siteId = siteId;
        this.creatorUserId = creatorUserId;
        this.purpose = purpose;
        this.appPolicies = appPolicies;
        this.deploymentPlan = deploymentPlan;
    }

    public String getAppInstanceName() {
        return appInstanceName;
    }

    public UUID getAppTemplateId() {
        return appTemplateId;
    }

    public UUID getSiteId() {
        return siteId;
    }

    public UUID getCreatorUserId() {
        return creatorUserId;
    }

    public String getPurpose() {
        return purpose;
    }

    public Collection<ApplicationPolicyInfoDTO> getAppPolicies() {
        return appPolicies;
    }

    public AppTemplateDeploymentPlanDTO getDeploymentPlan() {
        return deploymentPlan;
    }

    @Override
    public String toString() {
        return "DeployAppCommandArgs{" +
                "appInstanceName='" + appInstanceName + '\'' +
                ", appTemplateId=" + appTemplateId +
                ", siteId=" + siteId +
                ", creatorUserId=" + creatorUserId +
                ", purpose='" + purpose + '\'' +
                ", appPolicies=" + appPolicies +
                ", deploymentPlan=" + deploymentPlan +
                '}';
    }

    public static class ApplicationPolicyInfoDTO {
        private final String policyType;
        private final String policyName;
        private final Map<String, String> policySettings;

        private ApplicationPolicyInfoDTO() {
            this(null, null, null);
        }

        public ApplicationPolicyInfoDTO(String policyType, String policyName, Map<String, String> policySettings) {
            this.policyType = policyType;
            this.policyName = policyName;
            this.policySettings = policySettings;
        }

        public String getPolicyType() {
            return policyType;
        }

        public String getPolicyName() {
            return policyName;
        }

        public Map<String, String> getPolicySettings() {
            return policySettings;
        }

        @Override
        public String toString() {
            return "ApplicationPolicyInfoDTO{" +
                    "policyType='" + policyType + '\'' +
                    ", policyName='" + policyName + '\'' +
                    ", policySettings=" + policySettings +
                    '}';
        }
    }

    public static class AppTemplateDeploymentPlanDTO {
        private final Map<String, AppServiceDeploymentPlanDTO> appServices;
        private final Map<String, DataServiceDeploymentPlanDTO> dataServices;

        private AppTemplateDeploymentPlanDTO() {
            this(null, null);
        }

        public AppTemplateDeploymentPlanDTO(
                Map<String, AppServiceDeploymentPlanDTO> appServices,
                Map<String, DataServiceDeploymentPlanDTO> dataServices) {
            this.appServices = appServices;
            this.dataServices = dataServices;
        }

        public Map<String, AppServiceDeploymentPlanDTO> getAppServices() {
            return appServices;
        }

        public Map<String, DataServiceDeploymentPlanDTO> getDataServices() {
            return dataServices;
        }

        @Override
        public String toString() {
            return "AppTemplateDeploymentPlanDTO{" +
                    "appServices=" + appServices +
                    ", dataServices=" + dataServices +
                    '}';
        }
    }

    public static class AppServiceDeploymentPlanDTO {
        private final boolean enabled;
        private final String space;
        private final String artifactRegistryName;
        private final String imageVersion;

        private AppServiceDeploymentPlanDTO() {
            this(false, null, null, null);
        }

        public AppServiceDeploymentPlanDTO(
                boolean enabled,
                String space,
                String artifactRegistryName,
                String imageVersion) {
            this.enabled = enabled;
            this.space = space;
            this.artifactRegistryName = artifactRegistryName;
            this.imageVersion = imageVersion;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getSpace() {
            return space;
        }

        public String getArtifactRegistryName() {
            return artifactRegistryName;
        }

        public String getImageVersion() {
            return imageVersion;
        }

        @Override
        public String toString() {
            return "AppServiceDeploymentPlanDTO{" +
                    "enabled=" + enabled +
                    ", space='" + space + '\'' +
                    ", artifactRegistryName='" + artifactRegistryName + '\'' +
                    ", imageVersion='" + imageVersion + '\'' +
                    '}';
        }
    }

    public static class DataServiceDeploymentPlanDTO {
        private final String dsbUrn;
        private final String planId;
        private final String protocol;
        private final boolean enabled;
        private final Map<String, String> dsbSettings;

        private DataServiceDeploymentPlanDTO() {
            this(null, null, null, false, null);
        }

        public DataServiceDeploymentPlanDTO(
                String dsbUrn,
                String planId,
                String protocol,
                boolean enabled,
                Map<String, String> dsbSettings) {
            this.dsbUrn = dsbUrn;
            this.planId = planId;
            this.protocol = protocol;
            this.enabled = enabled;
            this.dsbSettings = dsbSettings;
        }

        public String getDsbUrn() {
            return dsbUrn;
        }

        public String getPlanId() {
            return planId;
        }

        public String getProtocol() {
            return protocol;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public Map<String, String> getDsbSettings() {
            return dsbSettings;
        }

        @Override
        public String toString() {
            return "DataServiceDeploymentPlanDTO{" +
                    "dsbUrn='" + dsbUrn + '\'' +
                    ", planId='" + planId + '\'' +
                    ", protocol='" + protocol + '\'' +
                    ", enabled=" + enabled +
                    ", dsbSettings=" + dsbSettings +
                    '}';
        }
    }
}
