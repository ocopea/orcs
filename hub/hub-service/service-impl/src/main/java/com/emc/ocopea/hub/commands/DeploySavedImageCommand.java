// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.commands;

import com.emc.ocopea.hub.application.AppInstanceManagerService;
import com.emc.ocopea.hub.application.ApplicationTemplate;
import com.emc.ocopea.hub.application.ApplicationTemplateManagerService;
import com.emc.ocopea.hub.application.DeploySavedImageCommandArgs;
import com.emc.ocopea.hub.policy.AppPolicy;
import com.emc.ocopea.hub.repository.DBSavedImage;
import com.emc.ocopea.hub.site.Site;
import com.emc.ocopea.hub.site.SiteManagerService;
import com.emc.ocopea.hub.repository.SavedImageRepository;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by liebea on 5/17/16.
 * Drink responsibly
 */
public class DeploySavedImageCommand extends HubCommand<DeploySavedImageCommandArgs, UUID> {
    private final SiteManagerService siteManagerService;
    private final AppInstanceManagerService appInstanceManagerService;
    private final ApplicationTemplateManagerService applicationTemplateManagerService;
    private final SavedImageRepository savedImageRepository;

    public DeploySavedImageCommand(
            SiteManagerService siteManagerService,
            AppInstanceManagerService appInstanceManagerService,
            ApplicationTemplateManagerService applicationTemplateManagerService,
            SavedImageRepository savedImageRepository) {
        this.siteManagerService = siteManagerService;
        this.appInstanceManagerService = appInstanceManagerService;
        this.applicationTemplateManagerService = applicationTemplateManagerService;
        this.savedImageRepository = savedImageRepository;
    }

    @Override
    protected UUID run(DeploySavedImageCommandArgs deploySavedImageCommandArgs) {

        validateEmptyField("siteId", deploySavedImageCommandArgs.getSiteId());
        validateEmptyField("userId", deploySavedImageCommandArgs.getUserId());
        validateEmptyField("appInstanceName", deploySavedImageCommandArgs.getAppInstanceName());

        final DBSavedImage savedImage = savedImageRepository.get(validateEmptyField(
                "savedImageId",
                deploySavedImageCommandArgs
                .getSavedImageId()));

        Site targetSite = siteManagerService.getSiteById(deploySavedImageCommandArgs.getSiteId());
        if (targetSite == null) {
            throw new BadRequestException("Could not find target site with site id "
                    + deploySavedImageCommandArgs.getSiteId());
        }

        final ApplicationTemplate appTemplate =
                applicationTemplateManagerService.getAppTemplateById(savedImage.getAppTemplateId(), true);

        // When running saved image we don't apply policies
        List<AppPolicy> applyPolicies = Collections.emptyList();

        try {
            return appInstanceManagerService.runApp(
                    appTemplate,
                    deploySavedImageCommandArgs.getDeploymentPlan(),
                    new AppInstanceManagerService.RestoreAppInfo(savedImage.getSiteId(), savedImage.getAppCopyId()),
                    deploySavedImageCommandArgs.getAppInstanceName(),
                    null,
                    deploySavedImageCommandArgs.getSavedImageId(),
                    deploySavedImageCommandArgs.getUserId(),
                    targetSite,
                    URLEncoder.encode(deploySavedImageCommandArgs.getAppInstanceName(), "UTF-8"),
                    "test-dev",
                    applyPolicies);
        } catch (UnsupportedEncodingException e) {
            throw new InternalServerErrorException(e);
        }
    }
}
