// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import com.emc.microservice.Context;
import com.emc.microservice.schedule.ManagedScheduler;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.ocopea.hub.AppServiceExternalDependencyProtocol;
import com.emc.ocopea.hub.policy.AppPolicy;
import com.emc.ocopea.hub.repository.DBAppInstanceConfig;
import com.emc.ocopea.hub.repository.DBAppInstanceState;
import com.emc.ocopea.hub.repository.DuplicateResourceException;
import com.emc.ocopea.hub.site.Site;
import com.emc.ocopea.hub.site.SiteManagerService;
import com.emc.ocopea.site.AppInstanceCopyStatisticsDTO;
import com.emc.ocopea.site.AppInstanceStatisticsDTO;
import com.emc.ocopea.site.DeployApplicationOnSiteCommandArgs;
import com.emc.ocopea.site.SiteWebApi;
import com.emc.ocopea.util.MapBuilder;
import com.emc.ocopea.hub.repository.AppInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by liebea on 11/29/15.
 * Drink responsibly
 */
public class AppInstanceManagerService implements ServiceLifecycle {
    private static final Logger log = LoggerFactory.getLogger(AppInstanceManagerService.class);
    private ManagedScheduler scheduler;
    private SiteManagerService siteManagerService;
    private AppInstanceRepository appInstanceRepository;

    @Override
    public void init(Context context) {
        scheduler = context.getSchedulerManager().getManagedResourceByName("default");
        siteManagerService = context.getSingletonManager().getManagedResourceByName("site-manager").getInstance();
        appInstanceRepository = context.getDynamicJavaServicesManager()
                .getManagedResourceByName(AppInstanceRepository.class.getSimpleName()).getInstance();
    }

    @Override
    public void shutDown() {
        // Nothing to do here
    }

    public static class RestoreAppInfo {
        private final UUID siteId;
        private final UUID copyId;

        public RestoreAppInfo(UUID siteId, UUID copyId) {
            this.siteId = siteId;
            this.copyId = copyId;
        }
    }

    private static DeployApplicationOnSiteCommandArgs
            .DeployDataServiceOnSiteManifestDTO generateDataServiceDeploymentDTO(
            RestoreAppInfo restoreAppInfo,
            String dataServiceName,
            DeployAppCommandArgs.DataServiceDeploymentPlanDTO dataServiceDeploymentPlanDTO,
            DeployApplicationOnSiteCommandArgs
                    .DeployAppServiceOnSiteManifestDTO
                    .DeployDataServiceRestoreInfoDTO restoreDataInfo,
            Map<String, String> additionalDsbSettings) {
        if (restoreAppInfo != null && restoreDataInfo == null) {
            throw new IllegalStateException("Failed loading staging info for data service " + dataServiceName);
        }

        Map<String, String> dsbSettings = computeDsbSettings(
                additionalDsbSettings,
                dataServiceDeploymentPlanDTO.getDsbSettings());

        return new DeployApplicationOnSiteCommandArgs.DeployDataServiceOnSiteManifestDTO(
                dataServiceName,
                dataServiceDeploymentPlanDTO.getDsbUrn(),
                dataServiceDeploymentPlanDTO.getPlanId(),
                dsbSettings,
                restoreDataInfo);
    }

    private static Map<String, String> computeDsbSettings(
            Map<String, String> additionalDsbSettings,
            Map<String, String> settingsFromPlan) {
        if (settingsFromPlan == null || settingsFromPlan.isEmpty()) {
            return additionalDsbSettings;
        } else if (additionalDsbSettings == null || additionalDsbSettings.isEmpty()) {
            return additionalDsbSettings;
        } else {
            final Map<String, String> dsbSettings = new HashMap<>(settingsFromPlan);
            dsbSettings.putAll(additionalDsbSettings);
            return dsbSettings;
        }
    }

    /***
     * Run an application.
     * @return UUID of the app
     */
    public UUID runApp(
            ApplicationTemplate appTemplate,
            DeployAppCommandArgs.AppTemplateDeploymentPlanDTO deploymentPlan,
            RestoreAppInfo restoreAppInfo,
            String appInstanceName,
            final UUID baseAppId,
            final UUID baseSavedImageId,
            final UUID creatorUserId,
            final Site targetSite,
            String route,
            String purpose,
            Collection<AppPolicy> applyPolicies) {

        // Generate uuid for app instance
        UUID appInstanceId = UUID.randomUUID();
        log.info("Running app {}/{} on site {}", appTemplate.getName(), appInstanceName, targetSite.getUrn());

        // Building the args we send to site
        final DeployApplicationOnSiteCommandArgs deployApplicationOnSiteCommandArgs =
                buildDeployAppOnSiteArgs(
                        appTemplate,
                        deploymentPlan,
                        restoreAppInfo,
                        appInstanceName,
                        targetSite,
                        route,
                        applyPolicies,
                        appInstanceId);

        final Date now = new Date();

        try {
            // Persisting managed site info
            storeAppInstanceConfig(
                    appTemplate,
                    appInstanceName,
                    baseAppId,
                    baseSavedImageId,
                    creatorUserId,
                    targetSite,
                    purpose,
                    appInstanceId,
                    now);

            // Invoking the command on the site
            invokeDeployAppOnSiteCommand(
                    appTemplate,
                    appInstanceName,
                    targetSite,
                    appInstanceId,
                    deployApplicationOnSiteCommandArgs);

            // Scheduling the deployment check


            createDeployCheckSchedule(appTemplate, appInstanceName, targetSite, appInstanceId);

        } catch (DuplicateResourceException e) {
            throw new AppAlreadyDeployedException(
                    appTemplate.getName(),
                    targetSite.getUrn(),
                    appInstanceName, e);
        }

        log.info(
                "Successfully deployed app {} {} on site {} - generated appInstanceId {}",
                appTemplate.getName(),
                appInstanceName,
                targetSite.getUrn(),
                appInstanceId);
        return appInstanceId;
    }

    private void createDeployCheckSchedule(
            ApplicationTemplate appTemplate,
            String appInstanceName,
            Site targetSite,
            UUID appInstanceId) {
        // Creating schedule
        String timerName = "deploycheck-" + targetSite.getUrn()
                + "|" + appTemplate.getName()
                + "|" + appInstanceName;

        scheduler.create(
                timerName,
                5,
                DeployedAppOnSiteChecker.SCHEDULE_LISTENER_IDENTIFIER,
                MapBuilder
                        .<String, String>newHashMap()
                        .with("appInstanceId", appInstanceId.toString())
                        .build(),
                DeployAppCheckerPayload.class,
                new DeployAppCheckerPayload(
                        appInstanceId,
                        targetSite.getId(),
                        new Date(),
                        null,
                        appInstanceName,
                        timerName,
                        appTemplate.getEntryPointUrl()));
    }

    private DeployApplicationOnSiteCommandArgs buildDeployAppOnSiteArgs(
            ApplicationTemplate appTemplate,
            DeployAppCommandArgs.AppTemplateDeploymentPlanDTO deploymentPlan,
            RestoreAppInfo restoreAppInfo,
            String appInstanceName,
            Site targetSite,
            String route, Collection<AppPolicy> applyPolicies,
            UUID appInstanceId) {

        // In case we need to restore, matching target site copies with data service copies
        final Map<String, DeployApplicationOnSiteCommandArgs
                .DeployAppServiceOnSiteManifestDTO
                .DeployDataServiceRestoreInfoDTO> restoreDataInfoByDSName =
                getTargetCopyRestoreInfo(deploymentPlan, restoreAppInfo, targetSite);

        // Building app service deployment plan according to app template, filtering app services that are not enabled
        Map<String, DeployApplicationOnSiteCommandArgs.DeployAppServiceOnSiteManifestDTO> appServiceTemplates =
                new HashMap<>();

        Map<String, Map<String, String>> templateEnforcedDsbSettings = new HashMap<>();

        for (ApplicationServiceTemplate currAST : appTemplate.getAppServiceTemplates()) {
            final DeployAppCommandArgs.AppServiceDeploymentPlanDTO appServiceDeploymentPlan =
                    deploymentPlan.getAppServices().get(currAST.getAppServiceName());

            // Validating that each application service in the template have a corresponding application service plan
            if (appServiceDeploymentPlan == null) {
                throw new IllegalStateException("no deployment plan found for app service "
                        + currAST.getAppServiceName() + " as part of application template deployment "
                        + appTemplate.getName());
            }

            // Including only services which were chosen to be enabled in this deployment
            if (appServiceDeploymentPlan.isEnabled()) {
                appServiceTemplates.put(
                        currAST.getAppServiceName(),
                        new DeployApplicationOnSiteCommandArgs.DeployAppServiceOnSiteManifestDTO(
                                currAST.getAppServiceName(),
                                currAST.getPsbType(),
                                appServiceDeploymentPlan.getSpace(),
                                appServiceDeploymentPlan.getArtifactRegistryName(),
                                currAST.getImageName(),
                                currAST.getImageType(),
                                appServiceDeploymentPlan.getImageVersion(),
                                currAST.getPsbSettings(),
                                currAST.getEnvironmentVariables(),
                                route,
                                new HashSet<>(currAST.getExposedPorts()),
                                currAST.getHttpPort(),
                                currAST.getDependencies()
                                        .stream()
                                        .map(AppServiceExternalDependency::getName)
                                        .collect(Collectors.toSet())));
            }

            // Validating app dependencies
            for (AppServiceExternalDependency currDep : currAST.getDependencies()) {

                final DeployAppCommandArgs.DataServiceDeploymentPlanDTO dataServicePlan =
                        deploymentPlan.getDataServices().get(currDep.getName());

                // Validating that each dependency has a deployment plan for its data service
                if (dataServicePlan == null) {
                    throw new IllegalStateException(
                            "deployment plan didn't include data service plan for dependency " + currDep.getName() +
                                    " of service " + currAST.getAppServiceName() + " in template " +
                                    appTemplate.getName() + " when trying to deploy app " + appInstanceName);
                }

                // Validating that there is a match between the supported dependency protocol and the dsb plan
                // provisioned
                final AppServiceExternalDependencyProtocol appServiceExternalDependencyProtocol = currDep.getProtocols()
                        .stream()
                        .filter(protocol -> protocol.getProtocol().equals(dataServicePlan.getProtocol()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(
                                "deployment for dependency " + currDep.getName() + " of service " +
                                        currAST.getAppServiceName() + " in template " + appTemplate.getName() +
                                        " has been configured with protocol " + dataServicePlan.getProtocol() +
                                        " although not supported by the dependency definition - when trying to " +
                                        "deploy app " + appInstanceName)
                        );

                // If all is validating, checking whether there are dependency level settings needs to be set for the
                // dsb defined by the app template
                if (appServiceExternalDependencyProtocol.getSettings() != null &&
                        !appServiceExternalDependencyProtocol.getSettings().isEmpty()) {
                    templateEnforcedDsbSettings.computeIfAbsent(currDep.getName(), s -> new HashMap<>())
                            .putAll(appServiceExternalDependencyProtocol.getSettings());
                }
            }
        }

        // Converting app policies
        List<DeployApplicationOnSiteCommandArgs.DeployAppPolicyInfoDTO> appPolicies = applyPolicies
                .stream()
                .map(appPolicy ->
                        new DeployApplicationOnSiteCommandArgs.DeployAppPolicyInfoDTO(
                                appPolicy.getPolicyType(),
                                appPolicy.getPolicyName(),
                                appPolicy.getPolicySettings()))
                .collect(Collectors.toList());

        // Building data services deployment plan
        final Map<String, DeployApplicationOnSiteCommandArgs.DeployDataServiceOnSiteManifestDTO> dataServices =
                deploymentPlan.getDataServices()
                        .entrySet()
                        .stream()

                        // Deploying only data services that were selected to be enabled
                        .filter(e -> e.getValue().isEnabled())

                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                o -> AppInstanceManagerService.generateDataServiceDeploymentDTO(
                                        restoreAppInfo,
                                        o.getKey(),
                                        o.getValue(),
                                        restoreAppInfo == null ?
                                                null :
                                                restoreDataInfoByDSName.get(o.getKey()),
                                        templateEnforcedDsbSettings.get(o.getKey()))
                        ));

        return new DeployApplicationOnSiteCommandArgs(
                appInstanceId,
                appInstanceName,
                appTemplate.getName(),
                appTemplate.getVersion(),
                appTemplate.getEntryPointServiceName(),
                appServiceTemplates,
                dataServices,
                appPolicies);
    }

    private void storeAppInstanceConfig(
            ApplicationTemplate appTemplate,
            String appInstanceName,
            UUID baseAppId,
            UUID baseSavedImageId, UUID creatorUserId, Site targetSite, String purpose, UUID appInstanceId, Date now)
            throws DuplicateResourceException {
        final DBAppInstanceConfig config = new DBAppInstanceConfig(
                appInstanceId,
                appInstanceName,
                appTemplate.getId(),
                purpose,
                creatorUserId,
                baseAppId,
                baseSavedImageId,
                now,
                now,
                targetSite.getId());

        appInstanceRepository.add(
                config,
                new DBAppInstanceState(
                        appInstanceId,
                        "deploying",
                        now,
                        null));
    }

    private void invokeDeployAppOnSiteCommand(
            ApplicationTemplate appTemplate,
            String appInstanceName,
            Site targetSite, UUID appInstanceId, DeployApplicationOnSiteCommandArgs siteCommandArgs) {
        try {
            // Invoking the site's deploy app on site command
            targetSite.getWebAPIConnection().resolve(SiteWebApi.class).deployApplicationOnSite(siteCommandArgs);
        } catch (Exception ex) {
            //todo: need a health checker if we for example fail to update status...
            appInstanceRepository.updateState(appInstanceId, "error");
            throw new InternalServerErrorException("Failed deploying app " + appInstanceName
                    + " of template " + appTemplate.getName()
                    + " on site " + targetSite.getName() + " - " + ex.getMessage(), ex);
        }
    }

    private Map<String, DeployApplicationOnSiteCommandArgs
            .DeployAppServiceOnSiteManifestDTO
            .DeployDataServiceRestoreInfoDTO> getTargetCopyRestoreInfo(
            DeployAppCommandArgs.AppTemplateDeploymentPlanDTO deploymentPlan,
            RestoreAppInfo restoreAppInfo,
            Site targetSite) {

        // Getting copy data if available
        if (restoreAppInfo == null) {
            return Collections.emptyMap();
        } else if (restoreAppInfo.siteId == null) {
            throw new BadRequestException("missing siteId in restore info");
        }
        if (restoreAppInfo.copyId == null) {
            throw new BadRequestException("missing copyId in restore info");
        }
        // In case we restore a copy from the same site, only need to point it at the copy
        if (restoreAppInfo.siteId.equals(targetSite.getId())) {
            return createRestoreInfo(
                    deploymentPlan,
                    restoreAppInfo,
                    targetSite,
                    c -> new DeployApplicationOnSiteCommandArgs
                            .DeployAppServiceOnSiteManifestDTO
                            .DeployDataServiceRestoreInfoDTO(
                            c.getCopyRepositoryURN(),
                            c.getCopyRepositoryProtocol(),
                            c.getCopyRepositoryProtocolVersion(),
                            c.getCopyId(),
                            c.getFacility()));
        } else {
            // When source site and target site are different, we'll eventually do CR to CR copy movement.
            // however at the moment until this API is available we're doing this staging  , bye
            final Site copyOriginSite = siteManagerService.getSiteById(restoreAppInfo.siteId);

            // Selecting crb to use in target site for staging the copy
            final String targetCrbUrn = locateTargetSiteCrb(copyOriginSite);

            return createRestoreInfo(
                    deploymentPlan,
                    restoreAppInfo,
                    copyOriginSite,
                    c -> {
                        Response response = copyOriginSite.getWebAPIConnection().resolve(SiteWebApi.class)
                                .downloadCopy(c.getCopyRepositoryURN(), c.getCopyId());
                        try {
                            try (InputStream inputStream = response.readEntity(InputStream.class)) {

                                // Staging copy on remote site using a local crb
                                UUID targetCopyId = UUID.randomUUID();
                                targetSite.getWebAPIConnection().resolve(SiteWebApi.class).stageCopy(
                                        inputStream,
                                        targetCrbUrn,
                                        c.getDsbUrn(),
                                        c.getTimeStamp().getTime(),
                                        c.getFacility(),
                                        null,
                                        targetCopyId);

                                return new DeployApplicationOnSiteCommandArgs
                                        .DeployAppServiceOnSiteManifestDTO
                                        .DeployDataServiceRestoreInfoDTO(
                                        c.getCopyRepositoryURN(),
                                        c.getCopyRepositoryProtocol(),
                                        c.getCopyRepositoryProtocolVersion(),
                                        c.getCopyId(),
                                        c.getFacility());

                            } catch (IOException e) {
                                throw new IllegalStateException(e);
                            }
                        } finally {
                            response.close();
                        }
                    });
        }
    }

    private String locateTargetSiteCrb(Site copyOriginSite) {
        //todo: here we just use the first one, which is bad, need to match protocol etc. however this is not
        // really important since eventually we'll move to implement crb to crb copy replication so this is temporary
        return copyOriginSite.getWebAPIConnection().resolve(SiteWebApi.class)
                .listCopyRepositories().iterator().next().getUrn();
    }

    private Map<String, DeployApplicationOnSiteCommandArgs
            .DeployAppServiceOnSiteManifestDTO
            .DeployDataServiceRestoreInfoDTO> createRestoreInfo(
            DeployAppCommandArgs.AppTemplateDeploymentPlanDTO deploymentPlan,
            RestoreAppInfo restoreAppInfo,
            Site site,
            Function<AppInstanceStatisticsDTO.DataServiceCopyStatisticsDTO,
                    DeployApplicationOnSiteCommandArgs
                            .DeployAppServiceOnSiteManifestDTO
                            .DeployDataServiceRestoreInfoDTO> converter) {

        // Reading copy metadata
        AppInstanceCopyStatisticsDTO appCopyMetadata =
                site.getWebAPIConnection().resolve(SiteWebApi.class).getCopyMetadata(
                        restoreAppInfo.copyId);

        return appCopyMetadata.getDataServiceCopies()
                .stream()

                // Only data services that were enabled for this deployment
                .filter(copy -> {
                    final DeployAppCommandArgs.DataServiceDeploymentPlanDTO dataServiceDeploymentPlanDTO =
                            deploymentPlan.getDataServices().get(copy.getBindName());
                    if (dataServiceDeploymentPlanDTO == null) {
                        throw new BadRequestException("Failed deploying app, missing deployment plan for data service "
                                + copy.getBindName());
                    }
                    return dataServiceDeploymentPlanDTO.isEnabled();
                })
                .collect(Collectors.toMap(
                        AppInstanceStatisticsDTO.DataServiceCopyStatisticsDTO::getBindName,
                        converter
                ));
    }

    public DBAppInstanceConfig getInstanceById(UUID appInstanceId) {
        return appInstanceRepository.getConfig(appInstanceId);
    }

    public Collection<DBAppInstanceConfig> listAppInstances() {
        return appInstanceRepository.listConfig();
    }

    public Collection<DBAppInstanceConfig> getDownStreamAppInstances(UUID appInstanceId) {
        return appInstanceRepository.listDownstreamConfig(appInstanceId);
    }

}
