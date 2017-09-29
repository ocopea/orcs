// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.commands;

import com.emc.microservice.schedule.ManagedScheduler;
import com.emc.ocopea.hub.application.AppInstanceManagerService;
import com.emc.ocopea.hub.application.CreateSavedImageChecker;
import com.emc.ocopea.hub.application.CreateSavedImageCommandArgs;
import com.emc.ocopea.hub.repository.DBAppInstanceConfig;
import com.emc.ocopea.hub.repository.DBSavedImage;
import com.emc.ocopea.hub.repository.DuplicateResourceException;
import com.emc.ocopea.hub.site.Site;
import com.emc.ocopea.hub.site.SiteManagerService;
import com.emc.ocopea.site.CreateAppCopyCommandArgs;
import com.emc.ocopea.site.SiteWebApi;
import com.emc.ocopea.util.MapBuilder;
import com.emc.ocopea.hub.repository.SavedImageRepository;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

public class CreateSavedImageCommand extends HubCommand<CreateSavedImageCommandArgs, UUID> {
    private final SiteManagerService siteManagerService;
    private final AppInstanceManagerService appInstanceManagerService;
    private final SavedImageRepository savedImageRepository;
    private final ManagedScheduler scheduler;

    public CreateSavedImageCommand(
            SiteManagerService siteManagerService,
            AppInstanceManagerService appInstanceManagerService,
            SavedImageRepository savedImageRepository,
            ManagedScheduler scheduler) {
        this.siteManagerService = siteManagerService;
        this.appInstanceManagerService = appInstanceManagerService;
        this.savedImageRepository = savedImageRepository;
        this.scheduler = scheduler;
    }

    @Override
    protected UUID run(CreateSavedImageCommandArgs createSavedImageCommandArgs) {

        validateEmptyField("name", createSavedImageCommandArgs.getName());
        validateEmptyField("userId", createSavedImageCommandArgs.getUserId());

        UUID appInstanceId = validateEmptyField("appInstanceId", createSavedImageCommandArgs.getAppInstanceId());
        // Getting app instance
        DBAppInstanceConfig appInstance = appInstanceManagerService.getInstanceById(appInstanceId);

        if (appInstance == null) {
            throw new WebApplicationException("appInstanceId " + appInstanceId + " does not exist", 409);
        }

        UUID siteId = appInstance.getSiteId();
        Site site = siteManagerService.getSiteById(siteId);
        if (site == null) {
            throw new BadRequestException("Unable to find site to run " + appInstanceId +
                    " app template id: " + appInstance.getAppTemplateId().toString() +
                    " siteId used: " + siteId);
        }
        UUID appCopyId;
        try {
            appCopyId = site.getWebAPIConnection().resolve(SiteWebApi.class)
                    .createAppCopy(new CreateAppCopyCommandArgs(appInstanceId));
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed creating app copy on site - " + e.getMessage(), e);
        }

        //todo-2: Create a "Saved image" entity pointing at this copy and persist it
        final UUID savedImageId = UUID.randomUUID();
        try {
            savedImageRepository.add(
                    new DBSavedImage(
                            savedImageId,
                            appInstance.getAppTemplateId(),
                            createSavedImageCommandArgs.getName(),
                            createSavedImageCommandArgs.getComment(),
                            createSavedImageCommandArgs.getUserId(),
                            new HashSet<>(createSavedImageCommandArgs.getTags()),
                            new Date(),
                            site.getId(),
                            appCopyId,
                            appInstance.getBaseSavedImageId(),
                            DBSavedImage.DBSavedImageState.creating));
        } catch (DuplicateResourceException e) {
            throw new WebApplicationException("Duplicate saved image", e, 409);
        }

        String scheduleName = "create-saved-image-" + savedImageId;
        scheduler.create(
                scheduleName,
                5,
                CreateSavedImageChecker.SCHEDULE_LISTENER_IDENTIFIER,
                MapBuilder.<String, String>newHashMap()
                        .with("savedImageId", savedImageId.toString())
                        .build(),
                SaveImageCheckerTaskPayload.class,
                new SaveImageCheckerTaskPayload(new Date(), savedImageId, site.getId(), scheduleName)
        );

        return savedImageId;
    }
}
