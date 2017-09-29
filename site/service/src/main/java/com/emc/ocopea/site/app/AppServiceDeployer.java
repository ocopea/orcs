// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import com.emc.microservice.schedule.ManagedScheduler;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.psb.DeployAppServiceManifestDTO;
import com.emc.ocopea.psb.DeployAppServiceResponseDTO;
import com.emc.ocopea.psb.PSBAppServiceInstanceDTO;
import com.emc.ocopea.psb.PSBBindPortDTO;
import com.emc.ocopea.psb.PSBServiceBindingInfoDTO;
import com.emc.ocopea.psb.PSBWebAPI;
import com.emc.ocopea.site.SiteRepository;
import com.emc.ocopea.site.artifact.SiteArtifactRegistry;
import com.emc.ocopea.util.MapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Responsible for deploying app service.
 */
public class AppServiceDeployer {
    private static final Logger log = LoggerFactory.getLogger(AppServiceDeployer.class);
    private final DeployedApplicationLoader deployedApplicationLoader;
    private final WebAPIResolver webAPIResolver;
    private final ManagedScheduler scheduler;
    private final SiteRepository siteRepository;
    private DeployedApplicationPersisterService deployedApplicationPersisterService;

    public AppServiceDeployer(
            DeployedApplicationLoader deployedApplicationLoader,
            WebAPIResolver webAPIResolver,
            ManagedScheduler scheduler,
            SiteRepository siteRepository,
            DeployedApplicationPersisterService deployedApplicationPersisterService) {

        this.deployedApplicationLoader = deployedApplicationLoader;
        this.webAPIResolver = webAPIResolver;
        this.scheduler = scheduler;
        this.siteRepository = siteRepository;
        this.deployedApplicationPersisterService = deployedApplicationPersisterService;
    }

    private void deployAppService(UUID appInstanceId, String serviceName) {

        // Loading deployed application
        DeployedApplication deployedApplication = deployedApplicationLoader.load(appInstanceId);

        try {

            // Getting the specific app service
            final DeployedAppService appService = getDeployedAppService(deployedApplication, serviceName);

            // First things first, we have to do an idempotency check, whether this already happened

            // First check is to see if the service is in an unexpected state, we expect it at this stage to be in state
            // queued which means it is ready to deploy, if it is not as expected we'll handle it accordingly
            boolean skipToPolling = false;
            switch (appService.getState()) {
                case queued:
                    // Great, this is what we expected, but we can't still be sure just yet, need to verify
                    // the orchestrator didn't crash in-between the queued and deploying state, lets check if the
                    // PSB knows something about this app service..
                    try {
                        final PSBAppServiceInstanceDTO appInstanceStatus = webAPIResolver
                                .getWebAPI(appService.getPsbUrl(), PSBWebAPI.class)
                                .getAppService(appService.getSpace(), appService.getPsbAppServiceId());

                        // We found app info although site is not up-to-date, need to decide which state it is
                        switch (appInstanceStatus.getStatus()) {

                            // In case state is running/starting we update the state of the app service and
                            // skip to the polling stage
                            case running:
                            case starting:
                                // Marking app as in status deploying
                                deployedApplicationPersisterService.persist(
                                        deployedApplication.markAppServiceAsDeploying(serviceName));
                                skipToPolling = true;
                                break;
                            case error:
                            case stopped:
                            default:
                                throw new IllegalStateException("App service " + appService.getAppServiceName() +
                                        " was expected to be queued, psb app state is " +
                                        appInstanceStatus.getStatus());
                        }

                    } catch (NotFoundException nfe) {
                        // This is great, it means we have no idea what this is about, and we can deploy from scratch
                        log.debug("psb returned 404 for service {}", appService.getPsbAppServiceId());
                    } catch (Exception ex) {
                        // This is not so bad, theoretically the PSB should either return a value or not found,
                        // But if there was a bug, we'll treat it on the safe side as if it was not found
                        log.warn("PSB returned an error for app service " + appService.getPsbAppServiceId() + " " +
                                "assuming it does not exist", ex);
                    }

                    break;
                case deploying:
                case deployed:
                    // In case the service is already deploying we skip to the polling scheduler (yey)
                    skipToPolling = true;
                    break;
                case error:
                case errorstopping:
                case pending:
                case stopped:
                case stopping:
                default:
                    throw new IllegalStateException("App service " + appService.getAppServiceName() + " was expected " +
                            "to be queued, but in state " + appService.getState());

            }

            if (!skipToPolling) {
                Map<String, Collection<PSBServiceBindingInfoDTO>> serviceBindings = new HashMap<>();
                Map<String, Set<String>> dependencies =
                        deployedApplication.getAppServiceToDataServiceMappings().get(appService.getAppServiceName());

                // In case there are dependencies, collect the binding in order to send to PSB
                if (dependencies != null) {
                    for (Map.Entry<String, Set<String>> currDSB : dependencies.entrySet()) {
                        Collection<PSBServiceBindingInfoDTO> dsbBindings =
                                currDSB.getValue().stream()
                                        .map(dataServiceName -> {
                                            final DeployedDataService deployedDataService =
                                                    deployedApplication.getDeployedDataServices().get(dataServiceName);
                                            DeployedDataService.DeployedDataServiceBindings bindingInfo =
                                                    deployedDataService.getBindingInfo();
                                            return new PSBServiceBindingInfoDTO(
                                                    dataServiceName,
                                                    deployedDataService.getServiceId(),
                                                    deployedDataService.getPlan(),
                                                    bindingInfo.getBindInfo(),
                                                    bindingInfo.getPorts().stream()
                                                            .map(dsbBindPortDTO ->
                                                                    new PSBBindPortDTO(
                                                                            dsbBindPortDTO.getProtocol(),
                                                                            dsbBindPortDTO.getDestination(),
                                                                            dsbBindPortDTO.getPort()))
                                                            .collect(Collectors.toList()));
                                        })
                                        .collect(Collectors.toList());

                        // DSB URN to bindings
                        serviceBindings.put(currDSB.getKey(), dsbBindings);
                    }
                }

                // Getting artifact registry configuration
                final SiteArtifactRegistry artifactRegistry = siteRepository.load().getArtifactRegistry(
                        appService.getArtifactRegistryName());
                if (artifactRegistry == null) {
                    throw new IllegalStateException(
                            "Unsupported artifact registry " + appService.getArtifactRegistryName()
                                    + " when deploying " + appService.getAppServiceName());
                }

                // Creating connection to the PSB
                DeployAppServiceResponseDTO deployAppServiceResponseDTO =
                        webAPIResolver.getWebAPI(appService.getPsbUrl(), PSBWebAPI.class).deployApplicationService(
                                new DeployAppServiceManifestDTO(
                                        appService.getPsbAppServiceId(),
                                        appService.getSpace(),
                                        appService.getImageName(),
                                        appService.getImageVersion(),
                                        appService.getEnvironmentVariables(),
                                        artifactRegistry.getType().name(),
                                        artifactRegistry.getParameters(),
                                        appService.getPsbSettings(),
                                        appService.getRoute(),
                                        appService.getExposedPorts(),
                                        appService.getHttpPort(),
                                        serviceBindings)
                        );

                log.info("Response from PSB: {}", deployAppServiceResponseDTO.getMessage());

                if (deployAppServiceResponseDTO.getStatus() != 0) {
                    throw new IllegalStateException("Failed deploying app service " + serviceName + " " +
                            deployAppServiceResponseDTO.getMessage() + " (" + deployAppServiceResponseDTO.getStatus() +
                            ")");
                }

                // Marking app as in status deploying
                deployedApplicationPersisterService.persist(
                        deployedApplication.markAppServiceAsDeploying(serviceName));
            }

            // Creating a schedule in order to poll
            scheduler.create(
                    getScheduleName(deployedApplication.getId(), serviceName),
                    5,
                    DeployChecker.SCHEDULE_LISTENER_IDENTIFIER,
                    MapBuilder.<String, String>newHashMap()
                            .with("appInstanceId", deployedApplication.getId().toString())
                            .with("serviceName", serviceName)
                            .build()
            );

        } catch (Exception ex) {
            log.error("Failed pushing app service " + serviceName, ex);
            deployedApplicationPersisterService.persist(
                    deployedApplication.markAppServiceAsError(serviceName, ex.getMessage()));
        }
    }

    private void stopAppService(UUID appInstanceId, String serviceName) {

        // Loading the deployed app
        DeployedApplication deployedApplication = deployedApplicationLoader.load(appInstanceId);
        try {

            // Getting the specific app service
            final DeployedAppService appService = getDeployedAppService(deployedApplication, serviceName);

            log.info("Stopping app service {}", serviceName);
            DeployAppServiceResponseDTO deployAppServiceResponseDTO =
                    webAPIResolver.getWebAPI(appService.getPsbUrl(), PSBWebAPI.class)
                            .stopApp(
                                    appService.getSpace(),
                                    appService.getPsbAppServiceId());
            log.info("Response from PSB: {}", deployAppServiceResponseDTO.getMessage());

            if (deployAppServiceResponseDTO.getStatus() != 0) {
                throw new IllegalStateException("Failed stopping app service " + serviceName + " " +
                        deployAppServiceResponseDTO.getMessage() + " (" + deployAppServiceResponseDTO.getStatus() +
                        ")");
            }
            deployedApplicationPersisterService.persist(
                    deployedApplication.markAppServiceAsStopped(serviceName));
        } catch (Exception ex) {
            log.error("Failed stopping app service " + serviceName, ex);
            deployedApplicationPersisterService.persist(
                    deployedApplication.markAppServiceAsErrorStopping(serviceName, ex.getMessage()));
        }
    }

    static DeployedAppService getDeployedAppService(
            DeployedApplication deployedApplication,
            String serviceName) {
        return Objects.requireNonNull(
                deployedApplication.getDeployedAppServices().get(serviceName),
                () -> "app instance " + deployedApplication.getName() + " does not support service with name " +
                        serviceName);
    }

    /***
     * Process AppServiceStateChangeEvent events and respond to the queued and stopping events.
     * @param event event for processing
     */
    void process(AppServiceStateChangeEvent event) {
        switch (event.getState()) {
            case queued:
                deployAppService(event.getAppInstanceId(), event.getName());
                break;
            case stopping:
                stopAppService(event.getAppInstanceId(), event.getName());
                break;
            default:
                break;
        }
    }

    private static String getScheduleName(UUID deployedApplicationId, String serviceName) {
        return "deploycheck-" + deployedApplicationId.toString() + "-" + serviceName;
    }

}
