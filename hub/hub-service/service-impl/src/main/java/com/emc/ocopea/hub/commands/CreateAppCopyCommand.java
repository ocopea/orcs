// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.commands;

import com.emc.ocopea.hub.application.AppInstanceManagerService;
import com.emc.ocopea.hub.application.CreateAppCopyCommandArgs;
import com.emc.ocopea.hub.repository.DBAppInstanceConfig;
import com.emc.ocopea.hub.site.Site;
import com.emc.ocopea.hub.site.SiteManagerService;
import com.emc.ocopea.site.SiteWebApi;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import java.util.UUID;

/**
 * Created by liebea on 5/17/16.
 * Drink responsibly
 */
public class CreateAppCopyCommand extends HubCommand<CreateAppCopyCommandArgs, UUID> {
    private final SiteManagerService siteManagerService;
    private final AppInstanceManagerService appInstanceManagerService;

    public CreateAppCopyCommand(
            SiteManagerService siteManagerService,
            AppInstanceManagerService appInstanceManagerService) {
        this.siteManagerService = siteManagerService;
        this.appInstanceManagerService = appInstanceManagerService;
    }

    @Override
    protected UUID run(CreateAppCopyCommandArgs createAppCopyArgs) {

        UUID appInstanceId = validateEmptyField("appInstanceId", createAppCopyArgs.getAppInstanceId());
        // Getting app instance
        DBAppInstanceConfig appInstance =
                appInstanceManagerService.getInstanceById(createAppCopyArgs.getAppInstanceId());

        if (appInstance == null) {
            throw new NotFoundException("appInstanceId " + createAppCopyArgs.getAppInstanceId() + " does not exist");
        }

        //todo:multi-site app
        Site site = siteManagerService.getSiteById(appInstance.getSiteId());
        if (site == null) {
            throw new InternalServerErrorException("Unable to find site to run " + createAppCopyArgs.getAppInstanceId()
                    + " app template id: " + appInstance.getAppTemplateId().toString()
                    + " siteId used: " + appInstance.getSiteId());
        }
        try {
            return site.getWebAPIConnection().resolve(SiteWebApi.class)
                    .createAppCopy(new com.emc.ocopea.site.CreateAppCopyCommandArgs(appInstanceId));
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed invoking create copy on site", e);
        }
    }
}
