// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import com.emc.microservice.Context;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.schedule.ScheduleListener;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.dsb.DsbWebApi;
import com.emc.ocopea.dsb.ServiceInstanceDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ServiceCreationChecker implements ScheduleListener, ServiceLifecycle {
    private static final Logger log = LoggerFactory.getLogger(ServiceCreationChecker.class);

    private WebAPIResolver webAPIResolver;
    private DeployedApplicationLoader deployedApplicationLoader;
    private DeployedApplicationPersisterService deployedApplicationPersisterService;

    // Do not change this identifier as it may be persisted in deployed systems
    public static final String SCHEDULE_LISTENER_IDENTIFIER = "service-creation-checker";

    @Override
    public boolean onTick(Message message) {

        final UUID appInstanceId = UUID.fromString(message.getMessageHeader("appInstanceId"));
        final String bindName = message.getMessageHeader("bindName");
        final DeployedApplication deployedApplication = deployedApplicationLoader.load(appInstanceId);
        final DeployedDataService ds = deployedApplication.getDeployedDataServices().get(bindName);

        try {

            final ServiceInstanceDetails serviceInstance = webAPIResolver.getWebAPI(ds.getDsbUrl(), DsbWebApi.class)
                    .getServiceInstance(ds.getServiceId());

            switch (serviceInstance.getState()) {
                case CREATING:
                    log.debug("Data service " + bindName + " for app " + appInstanceId + " is still creating");
                    break;
                case RUNNING:
                    log.debug("Data service " + bindName + " for app " + appInstanceId + " is running yey!");
                    deployedApplicationPersisterService.persist(
                            deployedApplication.markDataServiceAsCreated(bindName));
                    // stop scheduler
                    return false;
                default:
                    deployedApplicationPersisterService.persist(
                            deployedApplication.markDataServiceAsErrorCreating(
                                    bindName,
                                    "data service in " + serviceInstance.getState() + " state"));
                    // stop scheduler
                    return false;

            }

        } catch (Exception ex) {
            log.error("Failed creating DSB " + bindName, ex);
            deployedApplicationPersisterService.persist(
                    deployedApplication.markDataServiceAsErrorCreating(bindName, ex.getMessage()));

        }
        return true;
    }

    @Override
    public void init(Context context) {
        webAPIResolver = context.getWebAPIResolver();
        deployedApplicationLoader = new DeployedApplicationLoader(
                context.getDynamicJavaServicesManager().getManagedResourceByName(
                        DeployedApplicationEventRepository.class.getSimpleName()).getInstance());
        deployedApplicationPersisterService = new DeployedApplicationPersisterService(
                context.getDestinationManager()
                        .getManagedResourceByName(
                                "pending-deployed-application-events").getMessageSender());
    }

    @Override
    public void shutDown() {
    }
}
