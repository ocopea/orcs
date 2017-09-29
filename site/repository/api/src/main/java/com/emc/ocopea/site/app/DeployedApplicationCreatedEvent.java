// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by liebea on 10/18/16.
 * Drink responsibly
 */
public class DeployedApplicationCreatedEvent extends DeployedApplicationEvent {
    private final String name;
    private final String appTemplateName;
    private final String appTemplateVersion;
    private final Map<String, DataService> deployedDataServices;
    private final Map<String, AppService> deployedAppServices;
    private final Map<String, Map<String, Set<String>>> appServiceToDataServiceMappings;
    private final Collection<DataServicePolicy> dataServicePolicies;
    private final String entryPointService;

    private DeployedApplicationCreatedEvent() {
        this(null, null, null, null, null, null, null, null, null, null, null);
    }

    public DeployedApplicationCreatedEvent(
            UUID id, Date timestamp,
            String message,
            String name,
            String appTemplateName,
            String appTemplateVersion,
            Map<String, DataService> deployedDataServices,
            Map<String, AppService> deployedAppServices,
            Map<String, Map<String, Set<String>>> appServiceToDataServiceMappings,
            Collection<DataServicePolicy> dataServicePolicies,
            String entryPointService) {
        super(id, 1, timestamp, message);
        this.name = name;
        this.appTemplateName = appTemplateName;
        this.appTemplateVersion = appTemplateVersion;
        this.deployedDataServices = deployedDataServices;
        this.deployedAppServices = deployedAppServices;
        this.appServiceToDataServiceMappings = appServiceToDataServiceMappings;
        this.dataServicePolicies = dataServicePolicies;
        this.entryPointService = entryPointService;
    }

    public String getName() {
        return name;
    }

    public String getAppTemplateName() {
        return appTemplateName;
    }

    public String getAppTemplateVersion() {
        return appTemplateVersion;
    }

    public Map<String, DataService> getDeployedDataServices() {
        return deployedDataServices;
    }

    public Map<String, AppService> getDeployedAppServices() {
        return deployedAppServices;
    }

    public Map<String, Map<String, Set<String>>> getAppServiceToDataServiceMappings() {
        return appServiceToDataServiceMappings;
    }

    public Collection<DataServicePolicy> getDataServicePolicies() {
        return dataServicePolicies;
    }

    public String getEntryPointService() {
        return entryPointService;
    }

    public static class DataService {
        private final String dsbUrn;
        private final String dsbUrl;
        private final String plan;
        private final String bindName;
        private final String serviceId;
        private final Map<String, String> dsbSettings;
        private final RestoreInfo restoreInfo;

        public DataService(
                String dsbUrn,
                String dsbUrl,
                String plan,
                String bindName,
                String serviceId,
                Map<String, String> dsbSettings,
                RestoreInfo restoreInfo) {
            this.dsbUrn = dsbUrn;
            this.dsbUrl = dsbUrl;
            this.plan = plan;
            this.bindName = bindName;
            this.serviceId = serviceId;
            this.dsbSettings = dsbSettings;
            this.restoreInfo = restoreInfo;
        }

        private DataService() {
            this(null, null, null, null, null, null, null);
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

        public Map<String, String> getDsbSettings() {
            return dsbSettings;
        }

        public RestoreInfo getRestoreInfo() {
            return restoreInfo;
        }

        @Override
        public String toString() {
            return "DataService{" +
                    "dsbUrn='" + dsbUrn + '\'' +
                    ", dsbUrl='" + dsbUrl + '\'' +
                    ", plan='" + plan + '\'' +
                    ", bindName='" + bindName + '\'' +
                    ", serviceId='" + serviceId + '\'' +
                    ", dsbSettings=" + dsbSettings +
                    ", restoreInfo=" + restoreInfo +
                    '}';
        }
    }

    public static class AppService {
        private final String serviceName;
        private final String psbUrn;
        private final String psbUrl;
        private final String psbAppServiceId;
        private final String space;
        private final String artifactRegistryName;
        private final String imageName;
        private final String imageType;
        private final String imageVersion;
        private final Map<String, String> psbSettings;
        private final Map<String, String> environmentVariables;
        private final Set<Integer> exposedPorts;
        private final Integer httpPort;
        private final String route;

        public AppService(
                String serviceName,
                String psbUrn,
                String psbUrl,
                String psbAppServiceId,
                String space,
                String artifactRegistryName,
                String imageName,
                String imageType,
                String imageVersion,
                Map<String, String> psbSettings,
                Map<String, String> environmentVariables,
                Set<Integer> exposedPorts,
                Integer httpPort,
                String route) {
            this.serviceName = serviceName;
            this.psbUrn = psbUrn;
            this.psbUrl = psbUrl;
            this.psbAppServiceId = psbAppServiceId;
            this.space = space;
            this.artifactRegistryName = artifactRegistryName;
            this.imageName = imageName;
            this.imageType = imageType;
            this.imageVersion = imageVersion;
            this.psbSettings = psbSettings;
            this.environmentVariables = environmentVariables;
            this.exposedPorts = exposedPorts;
            this.httpPort = httpPort;
            this.route = route;
        }

        private AppService() {
            this(null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        }

        public String getServiceName() {
            return serviceName;
        }

        public String getPsbUrn() {
            return psbUrn;
        }

        public String getPsbUrl() {
            return psbUrl;
        }

        public String getPsbAppServiceId() {
            return psbAppServiceId;
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

        public Map<String, String> getPsbSettings() {
            return psbSettings;
        }

        public Map<String, String> getEnvironmentVariables() {
            return environmentVariables;
        }

        public String getImageVersion() {
            return imageVersion;
        }

        public Set<Integer> getExposedPorts() {
            return exposedPorts;
        }

        public Integer getHttpPort() {
            return httpPort;
        }

        public String getRoute() {
            return route;
        }

        @Override
        public String toString() {
            return "AppService{" +
                    "serviceName='" + serviceName + '\'' +
                    ", psbUrn='" + psbUrn + '\'' +
                    ", psbUrl='" + psbUrl + '\'' +
                    ", psbAppServiceId='" + psbAppServiceId + '\'' +
                    ", space='" + space + '\'' +
                    ", artifactRegistryName='" + artifactRegistryName + '\'' +
                    ", imageName='" + imageName + '\'' +
                    ", imageType='" + imageType + '\'' +
                    ", imageVersion='" + imageVersion + '\'' +
                    ", psbSettings=" + psbSettings +
                    ", environmentVariables=" + environmentVariables +
                    ", exposedPorts=" + exposedPorts +
                    ", httpPort=" + httpPort +
                    ", route='" + route + '\'' +
                    '}';
        }
    }

    public static class RestoreInfo {
        private final String copyRepoUrn;
        private final String copyRepoProtocol;
        private final String copyRepoProtocolVersion;
        private final UUID copyId;
        private final String restoreFacility;

        private RestoreInfo() {
            this(null, null, null, null, null);
        }

        public RestoreInfo(
                String copyRepoUrn,
                String copyRepoProtocol,
                String copyRepoProtocolVersion,
                UUID copyId,
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

    public static class DataServicePolicy {
        private final String policyType;
        private final String policyName;
        private final Map<String, String> policySettings;

        private DataServicePolicy() {
            this(null, null, null);
        }

        public DataServicePolicy(String policyType, String policyName, Map<String, String> policySettings) {
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
        return "DeployedApplicationCreatedEvent{" +
                "name='" + name + '\'' +
                ", appTemplateName='" + appTemplateName + '\'' +
                ", appTemplateVersion='" + appTemplateVersion + '\'' +
                ", deployedDataServices=" + deployedDataServices +
                ", deployedAppServices=" + deployedAppServices +
                ", appServiceToDataServiceMappings=" + appServiceToDataServiceMappings +
                ", dataServicePolicies=" + dataServicePolicies +
                ", entryPointService='" + entryPointService + '\'' +
                '}';
    }
}

