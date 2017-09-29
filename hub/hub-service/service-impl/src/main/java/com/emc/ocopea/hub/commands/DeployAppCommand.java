// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.commands;

import com.emc.ocopea.hub.application.AppInstanceManagerService;
import com.emc.ocopea.hub.application.ApplicationTemplate;
import com.emc.ocopea.hub.application.ApplicationTemplateManagerService;
import com.emc.ocopea.hub.application.DeployAppCommandArgs;
import com.emc.ocopea.hub.policy.AppPolicy;
import com.emc.ocopea.hub.site.Site;
import com.emc.ocopea.hub.site.SiteManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;

/**
 * Created by liebea on 5/17/16.
 * Drink responsibly
 */
public class DeployAppCommand extends HubCommand<DeployAppCommandArgs, UUID> {
    private final Logger logger = LoggerFactory.getLogger(DeployAppCommand.class);
    private final SiteManagerService siteManagerService;
    private final ApplicationTemplateManagerService applicationTemplateManagerService;
    private final AppInstanceManagerService appInstanceManagerService;

    public DeployAppCommand(
            SiteManagerService siteManagerService,
            ApplicationTemplateManagerService applicationTemplateManagerService,
            AppInstanceManagerService appInstanceManagerService) {
        this.siteManagerService = siteManagerService;
        this.applicationTemplateManagerService = applicationTemplateManagerService;
        this.appInstanceManagerService = appInstanceManagerService;
    }

    @Override
    protected UUID run(DeployAppCommandArgs deployAppCommandArgs) {

        validateEmptyField("appInstanceName", deployAppCommandArgs.getAppInstanceName());
        validateEmptyField("creatorUserId", deployAppCommandArgs.getCreatorUserId());
        validateEmptyField("deploymentPlan", deployAppCommandArgs.getDeploymentPlan());
        validateEmptyField("appTemplateId", deployAppCommandArgs.getAppTemplateId());
        validateEmptyField("siteId", deployAppCommandArgs.getSiteId());

        Site site = siteManagerService.getSiteById(deployAppCommandArgs.getSiteId());
        if (site == null) {
            throw new BadRequestException(
                    "Unable to find site with id " + deployAppCommandArgs.getSiteId() + " to run " +
                    deployAppCommandArgs.getAppTemplateId() + "/" + deployAppCommandArgs.getAppInstanceName());
        }

        logger.info("Selected site " + site.getName() + " - urn: " + site.getUrn());

        // Verifying template is supported
        ApplicationTemplate appTemplate =
                applicationTemplateManagerService.getAppTemplateById(deployAppCommandArgs.getAppTemplateId(), true);
        if (appTemplate == null) {
            throw new BadRequestException("Unsupported app template " + deployAppCommandArgs.getAppInstanceName());
        }

        // Converting app policies
        List<AppPolicy> applyPolicies = CreateAppDeploymentPlanCommand.buildPolicies(deployAppCommandArgs);

        try {
            return appInstanceManagerService.runApp(
                    appTemplate,
                    deployAppCommandArgs.getDeploymentPlan(),
                    null,
                    deployAppCommandArgs.getAppInstanceName(),
                    null,
                    null,
                    deployAppCommandArgs.getCreatorUserId(),
                    site,
                    URLEncoder.encode(deployAppCommandArgs.getAppInstanceName(), "UTF-8"),
                    deployAppCommandArgs.getPurpose(),
                    applyPolicies);
        } catch (UnsupportedEncodingException e) {
            throw new InternalServerErrorException(e);
        }
    }
}
