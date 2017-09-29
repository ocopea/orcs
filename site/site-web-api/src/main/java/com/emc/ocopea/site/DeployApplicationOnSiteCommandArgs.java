// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by liebea on 1/12/16.
 * Drink responsibly
 */
public class DeployApplicationOnSiteCommandArgs implements SiteCommandArgs {
    private final UUID appInstanceId;
    private final String appInstanceName;
    private final String appTemplateName;
    private final String appTemplateVersion;
    private final String entryPointService;
    private final Map<String, DeployAppServiceOnSiteManifestDTO> appServiceTemplates;
    private final Map<String, DeployDataServiceOnSiteManifestDTO> dataServices;
    private final Collection<DeployAppPolicyInfoDTO> appPolicies;

    private DeployApplicationOnSiteCommandArgs() {
        this(null, null, null, null, null, null, null, null);
    }

    public DeployApplicationOnSiteCommandArgs(
            UUID appInstanceId,
            String appInstanceName,
            String appTemplateName,
            String appTemplateVersion,
            String entryPointService,
            Map<String, DeployAppServiceOnSiteManifestDTO> appServiceTemplates,
            Map<String, DeployDataServiceOnSiteManifestDTO> dataServices,
            Collection<DeployAppPolicyInfoDTO> appPolicies) {

        this.appInstanceId = appInstanceId;
        this.appInstanceName = appInstanceName;
        this.appTemplateName = appTemplateName;
        this.appTemplateVersion = appTemplateVersion;
        this.entryPointService = entryPointService;
        this.appServiceTemplates = appServiceTemplates;
        this.dataServices = dataServices;
        this.appPolicies = appPolicies;
    }

    public UUID getAppInstanceId() {
        return appInstanceId;
    }

    public String getAppInstanceName() {
        return appInstanceName;
    }

    public String getAppTemplateName() {
        return appTemplateName;
    }

    public String getAppTemplateVersion() {
        return appTemplateVersion;
    }

    public String getEntryPointService() {
        return entryPointService;
    }

    public Map<String, DeployAppServiceOnSiteManifestDTO> getAppServiceTemplates() {
        return appServiceTemplates;
    }

    public Map<String, DeployDataServiceOnSiteManifestDTO> getDataServices() {
        return dataServices;
    }

    public Collection<DeployAppPolicyInfoDTO> getAppPolicies() {
        return appPolicies;
    }

    public static class DeployDataServiceOnSiteManifestDTO {
        private final String dataServiceName;
        private final String dsbUrn;
        private final String plan;
        private final Map<String, String> dsbSettings;
        private final DeployAppServiceOnSiteManifestDTO.DeployDataServiceRestoreInfoDTO restoreInfo;

        private DeployDataServiceOnSiteManifestDTO() {
            this(null, null, null, null, null);
        }

        public DeployDataServiceOnSiteManifestDTO(
                String dataServiceName,
                String dsbUrn,
                String plan,
                Map<String, String> dsbSettings,
                DeployAppServiceOnSiteManifestDTO.DeployDataServiceRestoreInfoDTO restoreInfo) {
            this.dataServiceName = dataServiceName;
            this.dsbUrn = dsbUrn;
            this.plan = plan;
            this.dsbSettings = dsbSettings;
            this.restoreInfo = restoreInfo;
        }

        public String getDataServiceName() {
            return dataServiceName;
        }

        public String getDsbUrn() {
            return dsbUrn;
        }

        public String getPlan() {
            return plan;
        }

        public Map<String, String> getDsbSettings() {
            return dsbSettings;
        }

        public DeployAppServiceOnSiteManifestDTO.DeployDataServiceRestoreInfoDTO getRestoreInfo() {
            return restoreInfo;
        }

        @Override
        public String toString() {
            return "DeployDataServiceOnSiteManifestDTO{" +
                    "dataServiceName='" + dataServiceName + '\'' +
                    ", dsbUrn='" + dsbUrn + '\'' +
                    ", plan='" + plan + '\'' +
                    ", dsbSettings=" + dsbSettings +
                    ", restoreInfo=" + restoreInfo +
                    '}';
        }
    }

    public static class DeployAppServiceOnSiteManifestDTO {
        private final String appServiceName;
        private final String psbType;
        private final String space;
        private final String artifactRegistryName;
        private final String imageName;
        private final String imageType;
        private final String imageVersion;
        private final Map<String, String> psbSettings;
        private final Map<String, String> environmentVariables;
        private final String route;
        private final Set<Integer> exposedPorts;
        private final Integer httpPort;
        private final Set<String> dependencies;

        private DeployAppServiceOnSiteManifestDTO() {
            this(null, null, null, null, null, null, null, null, null, null, null, null, null);
        }

        public DeployAppServiceOnSiteManifestDTO(
                String appServiceName,
                String psbType,
                String space,
                String artifactRegistryName,
                String imageName,
                String imageType,
                String imageVersion,
                Map<String, String> psbSettings,
                Map<String, String> environmentVariables,
                String route,
                Set<Integer> exposedPorts,
                Integer httpPort,
                Set<String> dependencies) {

            this.appServiceName = appServiceName;
            this.psbType = psbType;
            this.space = space;
            this.artifactRegistryName = artifactRegistryName;
            this.imageName = imageName;
            this.imageType = imageType;
            this.imageVersion = imageVersion;
            this.psbSettings = psbSettings;
            this.environmentVariables = environmentVariables;
            this.route = route;
            this.exposedPorts = exposedPorts;
            this.httpPort = httpPort;
            this.dependencies = dependencies;
        }

        public String getAppServiceName() {
            return appServiceName;
        }

        public String getPsbType() {
            return psbType;
        }

        public String getSpace() {
            return space;
        }

        public String getArtifactRegistryName() {
            return artifactRegistryName;
        }

        public String getImageName() {
            return imageName;
        }

        public String getImageType() {
            return imageType;
        }

        public String getImageVersion() {
            return imageVersion;
        }

        public Map<String, String> getPsbSettings() {
            return psbSettings;
        }

        public Map<String, String> getEnvironmentVariables() {
            return environmentVariables;
        }

        public String getRoute() {
            return route;
        }

        public Set<Integer> getExposedPorts() {
            return exposedPorts;
        }

        public Integer getHttpPort() {
            return httpPort;
        }

        public Set<String> getDependencies() {
            return dependencies;
        }

        @Override
        public String toString() {
            return "DeployAppServiceOnSiteManifestDTO{" +
                    "appServiceName='" + appServiceName + '\'' +
                    ", psbType='" + psbType + '\'' +
                    ", space='" + space + '\'' +
                    ", artifactRegistryName='" + artifactRegistryName + '\'' +
                    ", imageName='" + imageName + '\'' +
                    ", imageType='" + imageType + '\'' +
                    ", imageVersion='" + imageVersion + '\'' +
                    ", psbSettings=" + psbSettings +
                    ", environmentVariables=" + environmentVariables +
                    ", route='" + route + '\'' +
                    ", exposedPorts=" + exposedPorts +
                    ", httpPort=" + httpPort +
                    ", dependencies=" + dependencies +
                    '}';
        }

        public static class DeployDataServiceRestoreInfoDTO {
            private final String copyRepoUrn;
            private final String copyRepoProtocol;
            private final String copyRepoProtocolVersion;
            private final UUID copyId;
            private final String restoreFacility;

            private DeployDataServiceRestoreInfoDTO() {
                this(null, null, null, null, null);
            }

            public DeployDataServiceRestoreInfoDTO(String copyRepoUrn, String copyRepoProtocol,
                                                   String copyRepoProtocolVersion, UUID copyId,
                                                   String restoreFacility) {
                this.copyRepoUrn = copyRepoUrn;
                this.copyRepoProtocol = copyRepoProtocol;
                this.copyRepoProtocolVersion = copyRepoProtocolVersion;
                this.copyId = copyId;
                this.restoreFacility = restoreFacility;
            }

            public String getCopyRepoUrn() {
                return copyRepoUrn;
            }

            public String getCopyRepoProtocol() {
                return copyRepoProtocol;
            }

            public String getCopyRepoProtocolVersion() {
                return copyRepoProtocolVersion;
            }

            public UUID getCopyId() {
                return copyId;
            }

            public String getRestoreFacility() {
                return restoreFacility;
            }

            @Override
            public String toString() {
                return "DeployDataServiceRestoreInfoDTO{" +
                        "copyRepoUrn='" + copyRepoUrn + '\'' +
                        ", copyRepoProtocol='" + copyRepoProtocol + '\'' +
                        ", copyRepoProtocolVersion='" + copyRepoProtocolVersion + '\'' +
                        ", copyId='" + copyId + '\'' +
                        ", restoreFacility='" + restoreFacility + '\'' +
                        '}';
            }
        }
    }

    public static class DeployAppPolicyInfoDTO {
        private final String policyType;
        private final String policyName;
        private final Map<String, String> policySettings;

        private DeployAppPolicyInfoDTO() {
            this(null, null, null);
        }

        public DeployAppPolicyInfoDTO(String policyType, String policyName, Map<String, String> policySettings) {
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
    }

    @Override
    public String toString() {
        return "DeployApplicationOnSiteCommandArgs{" +
                "appInstanceId=" + appInstanceId +
                ", appInstanceName='" + appInstanceName + '\'' +
                ", appTemplateName='" + appTemplateName + '\'' +
                ", appTemplateVersion='" + appTemplateVersion + '\'' +
                ", entryPointService='" + entryPointService + '\'' +
                ", appServiceTemplates=" + appServiceTemplates +
                ", dataServices=" + dataServices +
                ", appPolicies=" + appPolicies +
                '}';
    }
}
