// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import com.emc.ocopea.site.DeployApplicationOnSiteCommandArgs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Created by liebea on 1/12/16.
 * Drink responsibly
 */
public class DeployedApplicationBuilder {

    private final UUID appInstanceId;
    private final String name;
    private final String appTemplateName;
    private final String appTemplateVersion;
    private final String entryPointService;
    private final Map<String, DeployedApplicationCreatedEvent.DataService> deployedDataServices = new HashMap<>();
    private final Map<String, DeployedApplicationCreatedEvent.AppService> deployedAppServices = new HashMap<>();
    private final Collection<DeployedApplicationCreatedEvent.DataServicePolicy> dataServicePolicies = new ArrayList<>();
    private Date timeStamp = new Date();

    // appServiceName/[dsb]/[dataServices]
    private final Map<String, Map<String, Set<String>>> appServiceToDataServiceMappings = new HashMap<>();

    public DeployedApplicationBuilder(
            UUID appInstanceId,
            String name,
            String appTemplateName,
            String appTemplateVersion,
            String entryPointService) {
        this.appInstanceId = appInstanceId;
        this.name = name;
        this.appTemplateName = appTemplateName;
        this.appTemplateVersion = appTemplateVersion;
        this.entryPointService = entryPointService;
    }

    public DeployedApplicationBuilder withDataService(
            String dsbUrn,
            String dsbUrl,
            String plan,
            String bindName,
            String serviceId,
            Map<String, String> dsbSettings) {
        return withDataService(dsbUrn, dsbUrl,  plan, bindName, serviceId, dsbSettings, null);
    }

    /***
     * Adding data service to the builder.
     */
    public DeployedApplicationBuilder withDataService(
            String dsbUrn,
            String dsbUrl,
            String plan,
            String bindName,
            String serviceId,
            Map<String, String> dsbSettings,
            DeployApplicationOnSiteCommandArgs.DeployAppServiceOnSiteManifestDTO.DeployDataServiceRestoreInfoDTO
                    restoreInfo) {

        DeployedApplicationCreatedEvent.RestoreInfo info = null;
        if (restoreInfo != null) {
            info = new DeployedApplicationCreatedEvent.RestoreInfo(
                    restoreInfo.getCopyRepoUrn(),
                    restoreInfo.getCopyRepoProtocol(),
                    restoreInfo.getCopyRepoProtocolVersion(),
                    restoreInfo.getCopyId(),
                    restoreInfo.getRestoreFacility());
        }
        deployedDataServices.put(
                bindName,
                new DeployedApplicationCreatedEvent.DataService(
                        dsbUrn,
                        dsbUrl,
                        plan,
                        bindName,
                        serviceId,
                        dsbSettings,
                        info));
        return this;
    }

    /***
     * Add Data service mapping between the app and data services.
     * @param appServiceName app service name
     * @param dsb dsb URN to be used
     * @param dataServiceBindName data service bind name
     */
    public DeployedApplicationBuilder withDataServiceMappings(
            String appServiceName,
            String dsb,
            String dataServiceBindName) {

        Map<String, Set<String>> byDSB = appServiceToDataServiceMappings.get(appServiceName);
        if (byDSB == null) {
            byDSB = new HashMap<>();
            appServiceToDataServiceMappings.put(appServiceName, byDSB);
        }

        Set<String> dataServiceNames = byDSB.get(dsb);
        if (dataServiceNames == null) {
            dataServiceNames = new HashSet<>();
            byDSB.put(dsb, dataServiceNames);
        }
        dataServiceNames.add(dataServiceBindName);
        return this;
    }

    /***
     * Add app service to the deploy application builder.
     */
    public DeployedApplicationBuilder withAppService(
            String appServiceName,
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
            String route,
            Set<Integer> exposedPorts,
            Integer httpPort) {
        deployedAppServices.put(
                appServiceName,
                new DeployedApplicationCreatedEvent.AppService(
                        appServiceName,
                        psbUrn,
                        psbUrl,
                        psbAppServiceId,
                        space, artifactRegistryName,
                        imageName,
                        imageType,
                        imageVersion,
                        psbSettings, environmentVariables,
                        exposedPorts,
                        httpPort,
                        route));
        return this;
    }

    public DeployedApplicationBuilder withDataServicesPolicy(
            String policyType,
            String policyName,
            Map<String, String> policySettings) {
        dataServicePolicies.add(
                new DeployedApplicationCreatedEvent.DataServicePolicy(policyType, policyName, policySettings));
        return this;
    }

    public DeployedApplicationBuilder withCustomTimestamp(Date timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    /***
     * Build the DeployedApplicationCreatedEvent represented by this builder.
     */
    public DeployedApplicationCreatedEvent build() {
        // Validating dependencies
        appServiceToDataServiceMappings
                .keySet()
                .forEach(s ->
                        Objects.requireNonNull(
                                deployedAppServices.get(s),
                                "Invalid mapping for app service " + s + ""));

        appServiceToDataServiceMappings
                .values()
                .stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .forEach(s ->
                        Objects.requireNonNull(
                                deployedDataServices.get(s),
                                "Invalid mapping for data service " + s + ""));

        return new DeployedApplicationCreatedEvent(
                appInstanceId,
                timeStamp,
                null,
                name,
                appTemplateName,
                appTemplateVersion,
                deployedDataServices,
                deployedAppServices,
                appServiceToDataServiceMappings,
                dataServicePolicies,
                entryPointService);
    }
}
