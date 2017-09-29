// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.commands;

import com.emc.microservice.schedule.ManagedScheduler;
import com.emc.ocopea.hub.application.AppInstanceManagerService;
import com.emc.ocopea.hub.application.AppStoppedOnSiteChecker;
import com.emc.ocopea.hub.application.StopAppCommandArgs;
import com.emc.ocopea.hub.application.StoppingAppCheckerPayload;
import com.emc.ocopea.hub.repository.DBAppInstanceConfig;
import com.emc.ocopea.hub.site.Site;
import com.emc.ocopea.hub.site.SiteManagerService;
import com.emc.ocopea.site.SiteWebApi;
import com.emc.ocopea.site.StopAppOnSiteCommandArgs;
import com.emc.ocopea.util.MapBuilder;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import java.util.Date;

public class StopAppCommand extends HubCommand<StopAppCommandArgs, Void> {
    private final SiteManagerService siteManagerService;
    private final AppInstanceManagerService appInstanceManagerService;
    private final ManagedScheduler scheduler;

    public StopAppCommand(
            SiteManagerService siteManagerService,
            AppInstanceManagerService appInstanceManagerService,
            ManagedScheduler scheduler) {
        this.siteManagerService = siteManagerService;
        this.appInstanceManagerService = appInstanceManagerService;
        this.scheduler = scheduler;
    }

    @Override
    protected Void run(StopAppCommandArgs stopAppCommandArgs) {
        validateEmptyField("appInstanceId", stopAppCommandArgs.getAppInstanceId());
        validateEmptyField("userId", stopAppCommandArgs.getUserId());

        // Getting origin instance Id
        DBAppInstanceConfig appInstance =
                appInstanceManagerService.getInstanceById(stopAppCommandArgs.getAppInstanceId());
        if (appInstance == null) {
            throw new NotFoundException("appInstanceId " + stopAppCommandArgs.getAppInstanceId() + " does not exist");
        }

        // Getting the site the instance is on
        Site site = siteManagerService.getSiteById(appInstance.getSiteId());

        try {
            site.getWebAPIConnection().resolve(SiteWebApi.class)
                    .stopApp(new StopAppOnSiteCommandArgs(stopAppCommandArgs.getAppInstanceId()));
        } catch (Exception ex) {
            throw new InternalServerErrorException("Failed stopping app on site " + site.getName() + ". "
                    + ex.getMessage(), ex);
        }

        String timerName = "stop-app-check-" + site.getId() + "|" + appInstance.getId();
        scheduler.create(
                timerName,
                5,
                AppStoppedOnSiteChecker.SCHEDULE_LISTENER_IDENTIFIER,
                MapBuilder
                        .<String,String>newHashMap()
                        .with("appInstanceId", appInstance.getId().toString())
                        .build(),
                StoppingAppCheckerPayload.class,
                new StoppingAppCheckerPayload(
                        appInstance.getId(),
                        site.getId(),
                        new Date(),
                        appInstance.getName(),
                        timerName));
        return null;
    }
}
