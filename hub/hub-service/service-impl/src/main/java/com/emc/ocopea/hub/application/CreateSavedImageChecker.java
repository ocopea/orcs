// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import com.emc.microservice.Context;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.schedule.ScheduleListener;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.ocopea.hub.commands.SaveImageCheckerTaskPayload;
import com.emc.ocopea.hub.repository.DBSavedImage;
import com.emc.ocopea.hub.site.Site;
import com.emc.ocopea.hub.site.SiteManagerService;
import com.emc.ocopea.site.AppInstanceCopyStatisticsDTO;
import com.emc.ocopea.site.SiteWebApi;
import com.emc.ocopea.hub.repository.SavedImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class CreateSavedImageChecker implements ScheduleListener, ServiceLifecycle {
    private static final Logger log = LoggerFactory.getLogger(CreateSavedImageChecker.class);

    private SavedImageRepository savedImageRepository;
    private SiteManagerService siteManagerService;

    public static final String SCHEDULE_LISTENER_IDENTIFIER = "create-saved-image-checker";

    @Override
    public void init(Context context) {
        siteManagerService = context.getSingletonManager().getManagedResourceByName("site-manager").getInstance();
        savedImageRepository = context.getDynamicJavaServicesManager()
                .getManagedResourceByName(SavedImageRepository.class.getSimpleName()).getInstance();
    }

    @Override
    public void shutDown() {}

    @Override
    public boolean onTick(Message message) {
        final SaveImageCheckerTaskPayload payload = message.readObject(SaveImageCheckerTaskPayload.class);
        final Site site = siteManagerService.getSiteById(payload.getSiteId());
        UUID imageId = payload.getSavedImageId();
        final DBSavedImage dbSavedImage = savedImageRepository.get(imageId);

        log.info("Checking whether saved image {} has been created on site {}", imageId, site.getName());

        SiteWebApi siteWebApi = site.getWebAPIConnection().resolve(SiteWebApi.class);
        AppInstanceCopyStatisticsDTO copyMetadata = siteWebApi.getCopyMetadata(dbSavedImage.getAppCopyId());

        String siteUrn = site.getUrn();
        AppInstanceCopyStatisticsDTO.SiteAppCopyState copyState = copyMetadata.getState();

        switch (copyState) {
            case creating:
                long elapsedTime = System.currentTimeMillis() - payload.getStartTime().getTime();
                int maxWaitingTime = 1000 * 60 * 30; // half an hour
                if (elapsedTime > maxWaitingTime) {
                    log.info("creating saved image {} on site {} took too long. aborting polling", imageId, siteUrn);
                    savedImageRepository.updateImageState(imageId, DBSavedImage.DBSavedImageState.failed);
                    return false;
                } else {
                    log.info("still creating saved image {} on site {}", imageId, siteUrn);
                    return true;
                }

            case created:
                log.info("successfully created saved image {} on site {}", imageId, siteUrn);
                savedImageRepository.updateImageState(imageId, DBSavedImage.DBSavedImageState.created);
                return false;

            default:
                log.info("failed creating a saved image {} on site {}. copyState={}", imageId, siteUrn, copyState);
                savedImageRepository.updateImageState(imageId, DBSavedImage.DBSavedImageState.failed);
                return false;
        }
    }
}
