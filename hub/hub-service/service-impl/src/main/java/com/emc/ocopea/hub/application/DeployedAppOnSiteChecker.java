// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import com.emc.microservice.Context;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.schedule.ScheduleListener;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.ocopea.hub.site.Site;
import com.emc.ocopea.hub.site.SiteManagerService;
import com.emc.ocopea.site.AppInstanceInfoDTO;
import com.emc.ocopea.site.SiteWebApi;
import com.emc.ocopea.site.app.DeployedApplicationState;
import com.emc.ocopea.hub.repository.AppInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.UUID;

public class DeployedAppOnSiteChecker implements ScheduleListener, ServiceLifecycle {

    private SiteManagerService siteManagerService;
    private AppInstanceRepository appInstanceRepository;
    private static final Logger log = LoggerFactory.getLogger(DeployedAppOnSiteChecker.class);

    // Do not change this identifier as it may be persisted in deployed systems
    public static final String SCHEDULE_LISTENER_IDENTIFIER = "deployed-app-on-site-checker";

    @Override
    public void init(Context context) {
        siteManagerService = context.getSingletonManager().getManagedResourceByName("site-manager").getInstance();
        appInstanceRepository = context.getDynamicJavaServicesManager()
                .getManagedResourceByName(AppInstanceRepository.class.getSimpleName()).getInstance();
    }

    @Override
    public void shutDown() {}

    @Override
    public boolean onTick(Message message) {
        final DeployAppCheckerPayload payload = message.readObject(DeployAppCheckerPayload.class);
        final Site site = siteManagerService.getSiteById(payload.getSiteId());

        String appInstanceName = payload.getAppInstanceName();
        log.info("Checking app {} on site {}", appInstanceName, site.getName());

        boolean running = true;
        SiteWebApi siteWebApi = site.getWebAPIConnection().resolve(SiteWebApi.class);
        UUID appInstanceId = payload.getAppInstanceId();
        AppInstanceInfoDTO appInstanceInfo = siteWebApi.getAppInstanceInfo(appInstanceId);

        String siteUrn = site.getUrn();

        DeployedApplicationState state = appInstanceInfo.getState();
        switch (state) {
            case running:
                log.info("app {} on site {} is running yey", appInstanceName, siteUrn);

                //final String k8sHost = payload.getK8sHost();
                String appEntryPointURL = appInstanceInfo.getEntryPointURL().replace("{siteURL}", site.getPublicDns());
                /*
                if (k8sHost != null) {
                    appEntryPointURL = appEntryPointURL.replace("{k8sHost}", k8sHost);
                }
                */
                appEntryPointURL += "/" + payload.getEntryPointServicePath();
                if (appEntryPointURL.endsWith("//")) {
                    appEntryPointURL = appEntryPointURL.substring(0, appEntryPointURL.length() - 1);
                }

                appInstanceRepository.updateStateAndUrl(appInstanceId, "running", URI.create(appEntryPointURL));
                return false;

            case deploying:
                long elapsedTime = System.currentTimeMillis() - payload.getStartTime().getTime();
                int maxWaitingTime = 1000 * 60 * 30; // half an hour
                if (elapsedTime > maxWaitingTime) {
                    log.info("app {} on site {} is not running, enough testing this  ", appInstanceName, siteUrn);
                    appInstanceRepository.updateState(appInstanceId, "unknown");
                    return false;
                } else {
                    log.info("app {} on site {} is still deploying", appInstanceName, siteUrn, state);
                    return true;
                }

            default:
                log.info("app {} on site {} not running, but {}", appInstanceName, siteUrn, state);
                appInstanceRepository.updateState(appInstanceId, "error");
                return false;
        }
    }
}
