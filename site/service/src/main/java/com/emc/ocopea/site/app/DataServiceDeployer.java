// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import com.emc.microservice.schedule.ManagedScheduler;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.dsb.CreateServiceInstance;
import com.emc.ocopea.dsb.DsbRestoreCopyInfo;
import com.emc.ocopea.dsb.DsbWebApi;
import com.emc.ocopea.dsb.ServiceInstance;
import com.emc.ocopea.dsb.ServiceInstanceDetails;
import com.emc.ocopea.site.DeployApplicationOnSiteCommandArgs;
import com.emc.ocopea.site.Site;
import com.emc.ocopea.site.SiteRepository;
import com.emc.ocopea.site.copy.CopyRepository;
import com.emc.ocopea.site.crb.CrbUtil;
import com.emc.ocopea.util.MapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by liebea on 10/18/16.
 * Drink responsibly
 */
public class DataServiceDeployer {
    private static final Logger log = LoggerFactory.getLogger(DataServiceDeployer.class);
    private final DeployedApplicationLoader deployedApplicationLoader;
    private final WebAPIResolver webAPIResolver;
    private final SiteRepository siteRepository;
    private final DeployedApplicationPersisterService deployedApplicationPersisterService;
    private final ManagedScheduler scheduler;

    public DataServiceDeployer(
            DeployedApplicationLoader deployedApplicationLoader,
            WebAPIResolver webAPIResolver,
            SiteRepository siteRepository,
            DeployedApplicationPersisterService deployedApplicationPersisterService,
            ManagedScheduler scheduler) {

        this.deployedApplicationLoader = deployedApplicationLoader;
        this.webAPIResolver = webAPIResolver;
        this.siteRepository = siteRepository;
        this.deployedApplicationPersisterService = deployedApplicationPersisterService;
        this.scheduler = scheduler;
    }

    /***
     * Process DataServiceStateChangeEvent events responding to queued,created,unbinding,unbound states.
     * @param event events for processing
     */
    void process(DataServiceStateChangeEvent event) {
        switch (event.getState()) {
            case queued:
                createDataService(event.getAppInstanceId(), event.getBindName());
                break;
            case created:
                bindDataService(event.getAppInstanceId(), event.getBindName());
                break;
            case unbinding:
                unBindDataService(event.getAppInstanceId(), event.getBindName());
                break;
            case unbound:
                removeDataService(event.getAppInstanceId(), event.getBindName());
                break;
            default:
                break;
        }
    }

    private void createDataService(UUID appInstanceId, String bindName) {
        DeployedApplication deployedApplication = load(appInstanceId);
        try {
            log.debug("creating dataService {} for app {}", bindName, deployedApplication.getName());
            final DeployedDataService ds = getDeployedDataService(deployedApplication, bindName);

            final DsbWebApi dsbWebAPI = getDsbWebAPI(ds.getDsbUrl());

            // Idempotent call - checking if data service has already attempted creating

            boolean shouldSkipToPolling = false;
            switch (ds.getState()) {
                case queued:
                    // Great, business as usual creating the service
                    break;
                case creating:
                    // This means this is not the first attempt - service has been created but not acknowledged,
                    // need to proceed to scheduler
                    log.info("Data service {} with id {} already in creating state while trying to create, " +
                            "skipping create", bindName, ds.getServiceId());

                    shouldSkipToPolling = true;
                    break;
                default:
                    throw new IllegalStateException("Unexpected data service state for data service " +
                            ds.getBindName() + " state: " + ds.getState());
            }

            if (!shouldSkipToPolling) {
                // Checking if service is creating somehow
                try {
                    final ServiceInstanceDetails serviceInstance = dsbWebAPI.getServiceInstance(ds.getServiceId());
                    if (serviceInstance != null) {
                        shouldSkipToPolling = true;

                        log.info("Data service {} with id {} already exist while trying to create, skipping create",
                                bindName, ds.getServiceId());

                        // Marking state as creating since it was skipped
                        deployedApplicationPersisterService.persist(
                                deployedApplication.markDataServiceAsCreating(ds.getServiceId()));
                    }
                } catch (NotFoundException ex) {
                    // Not found Exception is expected, yey, not found exception is even better
                    log.debug("service " + bindName + " with serviceId " + ds.getServiceId() +
                            " does not exist as expected");

                } catch (Exception ex) {
                    // We expect either "not found" exception or success. however since DSBs are being developed by
                    // the community, we don't trust them to return nice http codes, so we warn and continue
                    log.warn("get /service_instances/" + ds.getServiceId() + " resulted in error while " +
                            "testing if service is already deployed", ex);
                }
            }

            // Creating if not already created
            if (!shouldSkipToPolling) {
                // Creating DSB
                createDsb(
                        ds,
                        deployedApplication.getDependentAppServices(bindName),
                        dsbWebAPI);

                // Marking state as creating
                deployedApplicationPersisterService.persist(
                        deployedApplication.markDataServiceAsCreating(bindName));
            }

            // Creating a scheduler task to poll for the service to be created
            final String uniqueSchedulerName = getCreatingUniqueSchedulerName(appInstanceId, bindName);
            scheduler.create(
                    uniqueSchedulerName,
                    10,
                    ServiceCreationChecker.SCHEDULE_LISTENER_IDENTIFIER,
                    MapBuilder.<String, String>newHashMap()
                            .with("appInstanceId", appInstanceId.toString())
                            .with("appName", deployedApplication.getName())
                            .with("appTemplateName", deployedApplication.getAppTemplateName())
                            .with("bindName", bindName)
                            .build());

        } catch (Exception ex) {
            log.error("Failed creating DSB " + bindName, ex);
            deployedApplicationPersisterService.persist(
                    deployedApplication.markDataServiceAsErrorCreating(bindName, ex.getMessage()));
        }
    }

    public static String getCreatingUniqueSchedulerName(UUID appInstanceId, String bindName) {
        return appInstanceId.toString() + "-" + bindName + "-creating";
    }

    private void removeDataService(UUID appInstanceId, String bindName) {
        DeployedApplication deployedApplication = load(appInstanceId);
        try {
            final DeployedDataService ds = getDeployedDataService(deployedApplication, bindName);

            final DsbWebApi dsbWebAPI = getDsbWebAPI(ds.getDsbUrl());

            // todo:When implementing async api we'll have separate task for removing and removed
            deployedApplicationPersisterService.persist(
                    deployedApplication.markDataServiceAsRemoving(bindName));

            // Delete the service
            dsbWebAPI.deleteServiceInstance(ds.getServiceId());

            deployedApplicationPersisterService.persist(
                    deployedApplication.markDataServiceAsRemoved(bindName));

        } catch (Exception ex) {
            log.error("Failed removing DSB " + bindName, ex);
            deployedApplicationPersisterService.persist(
                    deployedApplication.markDataServiceAsErrorRemoving(bindName, ex.getMessage()));
        }
    }

    private DsbWebApi getDsbWebAPI(String dsbUrl) {
        return webAPIResolver.getWebAPI(dsbUrl, DsbWebApi.class);
    }

    private DeployedDataService getDeployedDataService(DeployedApplication deployedApplication, String bindName) {
        return Objects.requireNonNull(
                deployedApplication.getDeployedDataServices().get(bindName),
                () -> "app instance " + deployedApplication.getName() + " does not support service with bindName " +
                        bindName);
    }

    private void bindDataService(UUID appInstanceId, String bindName) {
        DeployedApplication deployedApplication = load(appInstanceId);
        try {

            final DeployedDataService ds = getDeployedDataService(deployedApplication, bindName);

            final DsbWebApi dsbWebAPI = getDsbWebAPI(ds.getDsbUrl());

            //todo: when doing async we need the binding stage
            deployedApplicationPersisterService.persist(
                    deployedApplication.markDataServiceAsBinding(ds.getBindName()));

            //todo:err
            final ServiceInstanceDetails serviceInstance = dsbWebAPI.getServiceInstance(ds.getServiceId());
            if (serviceInstance.getBinding() == null || serviceInstance.getBinding().isEmpty()) {
                throw new IllegalStateException("Failed binding DSB " + bindName + " no binding returned from DSB");
            }
            if (serviceInstance.getBindingPorts() == null) {
                serviceInstance.setBindingPorts(Collections.emptyList());
            }

            // Marking as bound
            deployedApplicationPersisterService.persist(
                    deployedApplication.markDataServiceAsBound(
                            ds.getBindName(),
                            new DataServiceBoundEvent.BindingInfo(
                                    serviceInstance.getBinding(),
                                    serviceInstance.getBindingPorts()
                                            .stream()
                                            .map(p ->
                                                    new DataServiceBoundEvent.DeployedDataServicePort(
                                                            p.getProtocol(),
                                                            p.getDestination(),
                                                            p.getPort()))
                                            .collect(Collectors.toList()))
                    ));
        } catch (Exception ex) {
            log.error("Failed binding DSB " + bindName, ex);
            deployedApplicationPersisterService.persist(
                    deployedApplication.markDataServiceAsErrorBinding(bindName, ex.getMessage()));
        }
    }

    private void unBindDataService(UUID appInstanceId, String bindName) {
        DeployedApplication deployedApplication = load(appInstanceId);
        try {

            final DeployedDataService ds = getDeployedDataService(deployedApplication, bindName);

            //todo: get dsbAPI by ds.getDsbUrn and call unbind on it

            // Marking as unbound
            deployedApplicationPersisterService.persist(
                    deployedApplication.markDataServiceAsUnbound(ds.getBindName()));
        } catch (Exception ex) {
            log.error("Failed binding DSB " + bindName, ex);
            deployedApplicationPersisterService.persist(
                    deployedApplication.markDataServiceAsErrorUnBinding(bindName, ex.getMessage()));
        }
    }

    private ServiceInstance createDsb(
            DeployedDataService ds,
            Collection<DeployedAppService> dependentAppServices,
            DsbWebApi dsbWebAPI) {

        final DeployApplicationOnSiteCommandArgs.DeployAppServiceOnSiteManifestDTO.DeployDataServiceRestoreInfoDTO
                restoreInfo = ds.getRestoreInfo();
        DsbRestoreCopyInfo dsbRestoreCopyInfo = null;
        if (restoreInfo != null) {
            final Site site = siteRepository.load();
            final CopyRepository crb = site.getCopyRepositoryByUrn(restoreInfo.getCopyRepoUrn());
            if (crb == null) {
                throw new IllegalStateException("Invalid crb urn used for creating copy " +
                        restoreInfo.getCopyRepoUrn());
            }

            dsbRestoreCopyInfo = new DsbRestoreCopyInfo(
                    restoreInfo.getCopyId().toString(),
                    restoreInfo.getCopyRepoProtocol(),
                    restoreInfo.getCopyRepoProtocolVersion(),
                    CrbUtil.getCrbNegotiationResult(
                            webAPIResolver,
                            crb.getUrn(),
                            crb.getUrl()).getCrbCredentials(),
                    restoreInfo.getRestoreFacility()
            );
        }

        // Adding spaces settings that can be used by paas aware dsbs
        Map<String, String> dsbSettings = ds.getDsbSettings();
        if (dsbSettings == null) {
            dsbSettings = new HashMap<>();
        } else {
            dsbSettings = new HashMap<>(dsbSettings);
        }
        dsbSettings.put("plan", ds.getPlan());

        return dsbWebAPI.createServiceInstance(
                new CreateServiceInstance(
                        ds.getServiceId(),
                        getSpacesList(dependentAppServices),
                        dsbSettings,
                        dsbRestoreCopyInfo
                ));
    }

    private List<String> getSpacesList(Collection<DeployedAppService> dependentAppServices) {
        return dependentAppServices
                .stream()
                .map(DeployedAppService::getSpace)
                .distinct()
                .collect(Collectors.toList());
    }

    private DeployedApplication load(UUID appInstanceId) {
        return deployedApplicationLoader.load(appInstanceId);
    }
}
