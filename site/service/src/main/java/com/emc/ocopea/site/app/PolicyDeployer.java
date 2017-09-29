// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import com.emc.ocopea.protection.ProtectApplicationInstanceInfoDTO;
import com.emc.ocopea.protection.ProtectionWebAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by liebea on 10/18/16.
 * Drink responsibly
 */
public class PolicyDeployer {
    private static final Logger log = LoggerFactory.getLogger(PolicyDeployer.class);
    private final DeployedApplicationLoader deployedApplicationLoader;
    private final PolicyEngineConnector policyEngineConnector;
    private final DeployedApplicationPersisterService deployedApplicationPersisterService;

    public PolicyDeployer(
            DeployedApplicationLoader deployedApplicationLoader,
            PolicyEngineConnector policyEngineConnector,
            DeployedApplicationPersisterService deployedApplicationPersisterService) {
        this.deployedApplicationLoader = deployedApplicationLoader;
        this.policyEngineConnector = policyEngineConnector;
        this.deployedApplicationPersisterService = deployedApplicationPersisterService;
    }

    private DeployedDataServicesPolicy getPolicy(
            DeployedApplication deployedApplication, String policyName,
            String policyType) {
        return Objects.requireNonNull(
                deployedApplication.getDataServicesPolicy(policyName, policyType),
                () -> "policy instance " + deployedApplication.getName() + " does not support policy " + policyType +
                        "-" + policyName);
    }

    private void activatePolicy(UUID appInstanceId, String policyName, String policyType) {
        DeployedApplication deployedApplication = load(appInstanceId);

        // Collecting data service bindings for passing to policy engine

        final DeployedDataServicesPolicy policy = getPolicy(deployedApplication, policyName, policyType);
        List<ProtectApplicationInstanceInfoDTO.ProtectDataServiceBinding> bindings =
                buildProtectDataServiceBindings(deployedApplication, policy.getSettings());
        final List<ProtectApplicationInstanceInfoDTO.ProtectAppConfiguration> appConfigurations =
                buildProtectAppConfigurations(deployedApplication);

        // todo: When we'll have async we'll split this too...
        deployedApplicationPersisterService.persist(
                deployedApplication.markPolicyAsActivating(policyName, policyType));

        final ProtectionWebAPI protectionWebAPI = policyEngineConnector.get(policyType);

        try {

            // todo: delegate copy frequency to be a policy property
            final int policyDefaultFrequencyInSeconds = 60 * 60 * 24;

            protectionWebAPI.protectApplication(
                    new ProtectApplicationInstanceInfoDTO(
                            bindings,
                            policyDefaultFrequencyInSeconds,
                            deployedApplication.getId().toString(),
                            appConfigurations));

            deployedApplicationPersisterService.persist(
                    deployedApplication.markPolicyAsActive(policyName, policyType));

        } catch (Exception ex) {
            log.error("Failed activating policy " + policyType + "-" + policyName + " for " +
                    deployedApplication.getName(), ex);
            deployedApplicationPersisterService.persist(
                    deployedApplication.markPolicyAsError(policyName, policyType, ex.getMessage()));
        }
    }

    public static List<ProtectApplicationInstanceInfoDTO.ProtectAppConfiguration> buildProtectAppConfigurations(
            DeployedApplication deployedApplication) {
        return deployedApplication.getDeployedAppServices()
                .values()
                .stream()
                .map(PolicyDeployer::getProtectAppConfig)
                .collect(Collectors.toList());
    }

    private static ProtectApplicationInstanceInfoDTO.ProtectAppConfiguration getProtectAppConfig(
            DeployedAppService deployedAppService) {

        return new ProtectApplicationInstanceInfoDTO.ProtectAppConfiguration(
                deployedAppService.getPsbUrn(),
                deployedAppService.getAppServiceName(),
                deployedAppService.getImageName(),
                deployedAppService.getImageType(),
                deployedAppService.getImageVersion()
        );
    }

    /***
     * Build ProtectDataServiceBinding descriptor required by the protection api.
     * @param deployedApplication deployed application
     * @param settings custom policy settings
     */
    public static List<ProtectApplicationInstanceInfoDTO.ProtectDataServiceBinding> buildProtectDataServiceBindings(
            DeployedApplication deployedApplication,
            Map<String, String> settings) {
        return deployedApplication.getDeployedDataServices()
                .values()
                .stream()
                .map(currDS ->
                        new ProtectApplicationInstanceInfoDTO.ProtectDataServiceBinding(
                                currDS.getDsbUrn(),
                                currDS.getDsbUrn(),
                                currDS.getDsbUrl(),
                                currDS.getBindName(),
                                currDS.getServiceId(),
                                "shpanCopy",
                                settings))
                .collect(Collectors.toList());
    }

    private DeployedApplication load(UUID appInstanceId) {
        return deployedApplicationLoader.load(appInstanceId);
    }

    /***
     * Process the PolicyStateChangeEvent implementing the policy deployment.
     */
    public void process(PolicyStateChangeEvent event) {
        switch (event.getState()) {
            case queued:
                activatePolicy(event.getAppInstanceId(), event.getName(), event.getType());
                break;
            default:
                break;
        }
    }
}
