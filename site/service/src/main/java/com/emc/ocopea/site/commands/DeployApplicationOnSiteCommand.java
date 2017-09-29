// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.commands;

import com.emc.ocopea.dsb.DsbPlan;
import com.emc.ocopea.site.DeployApplicationOnSiteCommandArgs;
import com.emc.ocopea.site.Site;
import com.emc.ocopea.site.SiteRepository;
import com.emc.ocopea.site.app.DeployedApplicationBuilder;
import com.emc.ocopea.site.app.DeployedApplicationCreatorService;
import com.emc.ocopea.site.artifact.SiteArtifactRegistry;
import com.emc.ocopea.site.dsb.Dsb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liebea on 1/3/16.
 * Drink responsibly.
 */
public class DeployApplicationOnSiteCommand extends SiteCommand<DeployApplicationOnSiteCommandArgs, Void> {
    public static final int MAX_SERVICE_ID_LENGTH = 50;
    private final Logger log = LoggerFactory.getLogger(DeployApplicationOnSiteCommand.class);
    private final DeployedApplicationCreatorService deployedApplicationCreatorService;
    private final SiteRepository siteRepository;

    public DeployApplicationOnSiteCommand(
            DeployedApplicationCreatorService deployedApplicationCreatorService,
            SiteRepository siteRepository) {
        this.deployedApplicationCreatorService = deployedApplicationCreatorService;
        this.siteRepository = siteRepository;
    }

    @Override
    public Void run(DeployApplicationOnSiteCommandArgs args) {

        // Input validation
        validateArgs(args);

        log.info(
                "Deploying application {} with template {}",
                validateEmptyField("appInstanceName", args.getAppInstanceName()),
                validateEmptyField("appTemplateName", args.getAppTemplateName()));

        // Generate psbAppServiceIds for all app services
        Map<String, String> psbAppServiceIdByAppServiceName =
                PsbAppServiceIdGenerator.generatePsbAppServiceIdsByAppSvcName(
                        args.getAppInstanceName(),
                        args.getAppServiceTemplates().keySet(),
                        50
                );

        // Loading site configuration
        final Site site = siteRepository.load();

        // building deployedApplication
        DeployedApplicationBuilder builder =
                new DeployedApplicationBuilder(
                        args.getAppInstanceId(),
                        args.getAppInstanceName(),
                        args.getAppTemplateName(),
                        args.getAppTemplateVersion(),
                        args.getEntryPointService());

        // populating deployed app with app services and data services
        for (DeployApplicationOnSiteCommandArgs.DeployAppServiceOnSiteManifestDTO currAppServiceTemplate :
                args.getAppServiceTemplates().values()) {

            // Matching requested psb type with actual PSB URN
            final String psbUrn = site.getPsbUrnByType(currAppServiceTemplate.getPsbType());
            if (psbUrn == null) {
                throw new BadRequestException("Site does not support deploying apps with psb type " +
                        currAppServiceTemplate.getPsbType());
            }

            // Validating artifactRegistryName is supported
            final String artifactRegistryName = currAppServiceTemplate.getArtifactRegistryName();
            final SiteArtifactRegistry artifactRegistry = site.getArtifactRegistry(artifactRegistryName);
            if (artifactRegistry == null) {
                throw new BadRequestException(
                        "Unsupported artifact registry " + artifactRegistryName + " for service " +
                                currAppServiceTemplate.getAppServiceName() + " image: " +
                                currAppServiceTemplate.getImageName());
            }

            // Get generated psb app service id
            final String psbAppServiceId =
                    psbAppServiceIdByAppServiceName.get(currAppServiceTemplate.getAppServiceName());

            // Adding entry for the each app service
            builder.withAppService(
                    currAppServiceTemplate.getAppServiceName(),
                    psbUrn,
                    site.getPsb(psbUrn).getUrl(),
                    psbAppServiceId,
                    currAppServiceTemplate.getSpace(),
                    artifactRegistryName,
                    currAppServiceTemplate.getImageName(),
                    currAppServiceTemplate.getImageType(),
                    currAppServiceTemplate.getImageVersion(),
                    currAppServiceTemplate.getPsbSettings(),
                    currAppServiceTemplate.getEnvironmentVariables(),
                    currAppServiceTemplate.getRoute(),
                    currAppServiceTemplate.getExposedPorts(),
                    currAppServiceTemplate.getHttpPort());

            // Creating entries for each DSB
            for (String currDep : currAppServiceTemplate.getDependencies()) {
                final DeployApplicationOnSiteCommandArgs.DeployDataServiceOnSiteManifestDTO ds =
                        args.getDataServices().get(currDep);
                if (ds == null) {
                    throw new BadRequestException("Service " + currAppServiceTemplate.getAppServiceName() +
                            " dependent on data service " + currDep +
                            " but dependency was not supplied in the deployment manifest");
                }

                // Validate data service URN is valid
                final Dsb dsb = site.getDsb(ds.getDsbUrn());
                if (dsb == null) {
                    throw new BadRequestException("invalid DSB URN " + ds.getDsbUrn() + " when deploying app" +
                            " " + args.getAppInstanceName());
                }

                // Validate plan
                final DsbPlan plan = dsb.getPlans()
                        .stream()
                        .filter(dsbPlan -> dsbPlan.getId().equals(ds.getPlan()))
                        .findFirst()
                        .orElseThrow(() -> new BadRequestException("invalid DSB plan " + ds.getPlan() + " when " +
                                "deploying app " + args.getAppInstanceName()));

                // Building dsbService, if two services rely on the same data service no harm is done, builder
                // consolidates
                Map<String, String> dsbSettings = ds.getDsbSettings();
                if (dsbSettings == null) {
                    dsbSettings = new HashMap<>();
                } else {
                    dsbSettings = new HashMap<>(dsbSettings);
                }
                if (plan.getDsbSettings() != null) {
                    dsbSettings.putAll(plan.getDsbSettings());
                }

                final String dsServiceIdToCreate = createServiceId(args.getAppInstanceId(), ds.getDataServiceName());
                builder
                        .withDataService(
                                ds.getDsbUrn(),
                                dsb.getUrl(),
                                ds.getPlan(),
                                ds.getDataServiceName(),
                                dsServiceIdToCreate,
                                dsbSettings,
                                ds.getRestoreInfo())
                        .withDataServiceMappings(
                                currAppServiceTemplate.getAppServiceName(),
                                ds.getDsbUrn(),
                                ds.getDataServiceName());
            }
        }

        // Add policies
        args.getAppPolicies()
                .stream()
                .filter(deployAppPolicyInfoDTO -> "protection".equals(deployAppPolicyInfoDTO.getPolicyType()))
                .forEach(
                        policy -> builder.withDataServicesPolicy(
                                policy.getPolicyType(),
                                policy.getPolicyName(),
                                policy.getPolicySettings()));

        // Creating a deployed application object and storing the events
        deployedApplicationCreatorService.create(builder);

        return null;
    }

    private void validateArgs(DeployApplicationOnSiteCommandArgs args) {
        validateEmptyField("appInstanceId", args.getAppInstanceId());
        validateEmptyField("appServiceName", args.getAppInstanceName());

        if (args.getAppServiceTemplates() != null) {
            args.getAppServiceTemplates().values().forEach(appServiceTemplate -> {
                        validateEmptyField("appInstanceName", appServiceTemplate.getAppServiceName());
                        validateEmptyField("imageName", appServiceTemplate.getImageName());
                        validateEmptyField("artifactRegistryName", appServiceTemplate.getArtifactRegistryName());
                        validateEmptyField("imageVersion", appServiceTemplate.getImageVersion());
                        validateEmptyField("space", appServiceTemplate.getSpace());
                        if (appServiceTemplate.getDependencies() != null) {
                            appServiceTemplate.getDependencies().forEach(dsName -> {
                                if (args.getDataServices() == null ||
                                        !args.getDataServices().containsKey(dsName)) {
                                    throw new BadRequestException("dependency " + dsName + " declared for app " +
                                            "service " + appServiceTemplate.getAppServiceName() + " but not part " +
                                            "of deployment");
                                }
                            });
                        }
                    }
            );
        }
        if (args.getDataServices() != null) {
            args.getDataServices().keySet().forEach(dsbName -> validateEmptyField("dataServiceNameKey", dsbName));
            args.getDataServices().values().forEach(dsb -> {
                        validateEmptyField("dataServiceName", dsb.getDataServiceName());
                        validateEmptyField("dsbUrn", dsb.getDsbUrn());
                    }
            );
        }
    }

    private String createServiceId(UUID appInstanceId, String dataServiceName) {
        String name = dataServiceName + "-" + appInstanceId.toString();
        if (name.length() > MAX_SERVICE_ID_LENGTH) {
            name = name.substring(name.length() - MAX_SERVICE_ID_LENGTH);
        }
        return name;

    }
}
