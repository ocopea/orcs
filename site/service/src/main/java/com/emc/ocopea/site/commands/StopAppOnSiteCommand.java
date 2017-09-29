// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.commands;

import com.emc.ocopea.site.StopAppOnSiteCommandArgs;
import com.emc.ocopea.site.app.DeployedApplication;
import com.emc.ocopea.site.app.DeployedApplicationLoader;
import com.emc.ocopea.site.app.DeployedApplicationPersisterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import java.util.UUID;

/**
 * Created by liebea on 1/3/16.
 * Drink responsibly
 */
public class StopAppOnSiteCommand extends SiteCommand<StopAppOnSiteCommandArgs, Void> {
    private final Logger log = LoggerFactory.getLogger(StopAppOnSiteCommand.class);
    private final DeployedApplicationLoader deployedApplicationLoader;
    private final DeployedApplicationPersisterService deployedApplicationPersisterService;

    public StopAppOnSiteCommand(
            DeployedApplicationLoader deployedApplicationLoader,
            DeployedApplicationPersisterService deployedApplicationPersisterService) {
        this.deployedApplicationLoader = deployedApplicationLoader;
        this.deployedApplicationPersisterService = deployedApplicationPersisterService;
    }

    @Override
    protected Void run(StopAppOnSiteCommandArgs args) {
        final UUID appInstanceId = validateEmptyField("appInstanceId", args.getAppInstanceId());

        final DeployedApplication deployedApplication = deployedApplicationLoader.load(appInstanceId);
        if (deployedApplication == null) {
            throw new NotFoundException(
                    "application instance with id " + appInstanceId + " was not found on site");
        }

        log.info("Stopping app {}", deployedApplication.getName());
        deployedApplicationPersisterService.persist(deployedApplication.stop());

        return null;
    }
}
