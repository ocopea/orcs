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

public class AppStoppedOnSiteChecker implements ScheduleListener, ServiceLifecycle {
    private static final Logger log = LoggerFactory.getLogger(AppStoppedOnSiteChecker.class);

    private SiteManagerService siteManagerService;
    private AppInstanceRepository appInstanceRepository;

    // Do not change this identifier as it may be persisted in deployed systems
    public static final String SCHEDULE_LISTENER_IDENTIFIER = "app-stopped-on-site-checker";

    @Override
    public void init(Context context) {
        siteManagerService = context.getSingletonManager().getManagedResourceByName("site-manager").getInstance();
        appInstanceRepository = context.getDynamicJavaServicesManager()
                .getManagedResourceByName(AppInstanceRepository.class.getSimpleName()).getInstance();
    }

    @Override
    public void shutDown() {
        // Nothing to do here
    }

    @Override
    public boolean onTick(Message message) {
        final StoppingAppCheckerPayload payload = message.readObject(StoppingAppCheckerPayload.class);
        final Site site = siteManagerService.getSiteById(payload.getSiteId());

        String appName = payload.getAppInstanceName();
        log.info("Checking whether app {} on site {} has stopped", appName, site.getName());

        SiteWebApi siteWebApi = site.getWebAPIConnection().resolve(SiteWebApi.class);
        AppInstanceInfoDTO appInstanceInfo = siteWebApi.getAppInstanceInfo(payload.getAppInstanceId());
        String siteUrn = site.getUrn();
        DeployedApplicationState appState = appInstanceInfo.getState();

        switch (appState) {
            case stopped:
                log.info("app {} on site {} is stopped yey", appName, siteUrn);
                appInstanceRepository.updateState(payload.getAppInstanceId(), "stopped");
                return false; // app stopped, we can stop polling

            case stopping:
            case running:
                long elapsedTime = System.currentTimeMillis() - payload.getStartTime().getTime();
                int maxWaitingTime = 1000 * 60 * 30; // half an hour
                if (elapsedTime > maxWaitingTime) {
                    log.info("app {} on site {} is not stopped, enough testing this  ", appName, siteUrn);
                    appInstanceRepository.updateState(payload.getAppInstanceId(), "unknown");
                    return false; // give up polling
                } else {
                    log.info("app {} on site {} is still stopping", appName, siteUrn, appState);
                    return true;
                }

            default:
                log.info("app {} on site {} not stopped, but {}", appName, siteUrn, appState);
                appInstanceRepository.updateState(payload.getAppInstanceId(), "error");
                return false;
        }
    }

}
