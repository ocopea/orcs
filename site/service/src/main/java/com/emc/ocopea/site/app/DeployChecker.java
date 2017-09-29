// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import com.emc.microservice.Context;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.schedule.ScheduleListener;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.psb.PSBAppServiceInstanceDTO;
import com.emc.ocopea.psb.PSBAppServiceStatusEnumDTO;
import com.emc.ocopea.psb.PSBWebAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Created by liebea on 6/19/17.
 * Drink responsibly
 */
public class DeployChecker implements ScheduleListener, ServiceLifecycle {
    private static final Logger log = LoggerFactory.getLogger(DeployChecker.class);
    private int count = 0;
    private DeployedApplicationLoader deployedApplicationLoader;
    private DeployedApplicationPersisterService deployedApplicationPersisterService;
    private WebAPIResolver webAPIResolver;
    private Integer deployTimeoutInSeconds;

    // Do not change this identifier as it may be persisted in deployed systems
    public static final String SCHEDULE_LISTENER_IDENTIFIER = "deploy-checker-schedule-listener";

    @Override
    public boolean onTick(Message message) {
        final UUID deployedApplicationId = UUID.fromString(message.getMessageHeader("appInstanceId"));
        final String serviceName = message.getMessageHeader("serviceName");

        boolean runCheckerAgain = true;
        count++;
        final DeployedApplication deployedApplication = deployedApplicationLoader.load(deployedApplicationId);

        String templateName = deployedApplication.getAppTemplateName();
        String appName = deployedApplication.getName();

        log.info("Checking app deployment appTemplateName:{}, appName:{} #{}", templateName, appName, count);

        // getting specific app service from the deployedAppService
        final DeployedAppService deployedAppService = AppServiceDeployer
                .getDeployedAppService(deployedApplication, serviceName);

        String imageName = deployedAppService.getImageName();
        String psbUrn = deployedAppService.getPsbUrn();
        String psbUrl = deployedAppService.getPsbUrl();
        String route = deployedAppService.getRoute();
        log.info("psb URN={}; psb URL={}; imageName={}; route={}", psbUrn, psbUrl, imageName, route);

        PSBAppServiceInstanceDTO appServiceInfo = null;
        try {
            appServiceInfo = webAPIResolver
                    .getWebAPI(psbUrl, PSBWebAPI.class)
                    .getAppService(deployedAppService.getSpace(), deployedAppService.getPsbAppServiceId());
        } catch (Exception ex) {
            log.debug("error polling psb for stats", ex);
            log.info("error polling psb for stats {}", ex.getMessage());
        }

        if (appServiceInfo == null) {
            log.info("Service {}/{} on app {}/{} not yet running, but unknown",
                    imageName, route, templateName, appName);
            // service is not yet running, continue polling

        } else {

            PSBAppServiceStatusEnumDTO appServiceStatus = appServiceInfo.getStatus();
            String entryPointURL = appServiceInfo.getEntryPointURL();
            switch (appServiceStatus) {
                case running:
                    log.info("Service {}/{} on app {}/{} is running yey", imageName, route, templateName, appName);
                    deployedApplicationPersisterService.persist(
                            deployedApplication.markAppServiceAsDeployed(serviceName, entryPointURL));
                    runCheckerAgain = false; // app is running, we can stop polling
                    break;

                case starting:
                    log.info("Service {}/{} on app {}/{} not yet running, but {}",
                            imageName, route, templateName, appName, appServiceStatus);
                    break; // service is not yet running, continue polling

                default:
                    deployedApplicationPersisterService.persist(
                            deployedApplication.markAppServiceAsError(
                                    serviceName,
                                    "failed running with status " + appServiceStatus));

                    log.warn("Service {}/{} on app {}/{} failed running with status {}",
                            imageName, route, templateName, appName, appServiceStatus);

                    runCheckerAgain = false;
                    break;
            }

            // Checking if timeout passed for app to be deployed
            if (runCheckerAgain && deployTimeoutInSeconds != null && deployedApplication.getDeployedOn() != null &&
                    (((System.currentTimeMillis() - deployedApplication.getDeployedOn().getTime()) / 1000) >
                            deployTimeoutInSeconds)) {

                deployedApplicationPersisterService.persist(
                        deployedApplication.markAppServiceAsError(serviceName, "app deployment exceeded timeout of " +
                                deployTimeoutInSeconds + " seconds"));
                log.warn(
                        "app deployment exceeded timeout of {} seconds: templateName{}, appName:{} #{}",
                        deployTimeoutInSeconds,
                        templateName,
                        appName,
                        count);

                runCheckerAgain = false;
            }
        }

        return runCheckerAgain;

    }

    @Override
    public void init(Context context) {
        this.deployedApplicationLoader = new DeployedApplicationLoader(context
                .getDynamicJavaServicesManager().getManagedResourceByName(
                        DeployedApplicationEventRepository.class.getSimpleName()).getInstance());
        this.deployedApplicationPersisterService =
                new DeployedApplicationPersisterService(
                        context.getDestinationManager()
                                .getManagedResourceByName(
                                        "pending-deployed-application-events").getMessageSender());

        this.webAPIResolver = context.getWebAPIResolver();
        deployTimeoutInSeconds = context.getParametersBag().getInt("deploy-app-timeout-in-seconds");
    }

    @Override
    public void shutDown() {
    }
}
