// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.crb.CopyMetaData;
import com.emc.ocopea.crb.CrbWebApi;
import com.emc.ocopea.dsb.CopyServiceInstance;
import com.emc.ocopea.dsb.CopyServiceInstanceResponse;
import com.emc.ocopea.dsb.DsbWebApi;
import com.emc.ocopea.site.crb.CrbNegotiationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Responsible for creating data service copies.
 */
public class DataServiceCopyCreator {
    private static final Logger log = LoggerFactory.getLogger(DataServiceCopyCreator.class);
    private final WebAPIResolver webAPIResolver;
    private final ApplicationCopyLoader applicationCopyLoader;
    private final AppCopyPersisterService appCopyPersisterService;
    private final CopyRepositoryNegotiator copyRepositoryNegotiator;

    public DataServiceCopyCreator(
            WebAPIResolver webAPIResolver,
            ApplicationCopyLoader applicationCopyLoader,
            AppCopyPersisterService appCopyPersisterService,
            CopyRepositoryNegotiator copyRepositoryNegotiator) {
        this.webAPIResolver = webAPIResolver;
        this.applicationCopyLoader = applicationCopyLoader;
        this.appCopyPersisterService = appCopyPersisterService;
        this.copyRepositoryNegotiator = copyRepositoryNegotiator;
    }

    /***
     * Processing and responding to ApplicationCopyEvents picking events relevant for
     * data service copies (ApplicationCopyDataServiceQueuedEvent).
     * @param event event to process
     */
    public void processEvent(ApplicationCopyEvent event) {
        if (event instanceof ApplicationCopyDataServiceQueuedEvent) {
            final ApplicationCopyDataServiceQueuedEvent e = (ApplicationCopyDataServiceQueuedEvent) event;
            createDataServiceCopy(e.getAppCopyId(), e.getDsbUrn(), e.getDsbUrl(), e.getServiceId());
        }
    }

    private void createDataServiceCopy(UUID applicationCopyId, String dsbUrn, String dsbUrl, String serviceId) {
        final ApplicationCopy applicationCopy = applicationCopyLoader.load(applicationCopyId);
        try {
            final ApplicationDataServiceCopy dataServiceCopy = applicationCopy.getDataServiceCopy(dsbUrn, serviceId);

            if (dataServiceCopy == null) {
                markCopyAsFailed(
                        dsbUrn,
                        serviceId,
                        applicationCopy,
                        "Received an invalid data service queue event dsbURN:");
                return;
            }

            applicationCopy.markDataServiceCopyAsRunning(dsbUrn, serviceId, null);
            appCopyPersisterService.persist(applicationCopy);

            DsbWebApi dsbWebAPI;

            try {
                dsbWebAPI = webAPIResolver.getWebAPI(dsbUrl, DsbWebApi.class);
            } catch (Exception ex) {

                log.error("Failed resolving DSB URN " + dsbUrn, ex);
                markCopyAsFailed(
                        dsbUrn,
                        serviceId,
                        applicationCopy,
                        "Failed resolving DSB URN " + dsbUrn + " - " + ex.getMessage());
                return;
            }

            // Negotiate copy
            CrbNegotiationResult crb;
            try {
                crb = copyRepositoryNegotiator.findCrb();
            } catch (Exception ex) {
                log.error("Failed negotiating dsb protocol for dsb" + dsbUrn, ex);
                markCopyAsFailed(
                        dsbUrn,
                        serviceId,
                        applicationCopy,
                        "Failed negotiating CRB protocol for  DSB URN " + dsbUrn + " - " + ex.getMessage());
                return;
            }

            final UUID dataServiceCopyId = UUID.randomUUID();

            webAPIResolver.getWebAPI(crb.getCrbUrl(), CrbWebApi.class).storeCopyMetaData(
                    dataServiceCopyId.toString(),
                    new CopyMetaData(
                            dataServiceCopyId.toString(),
                            crb.getRepoId(),
                            dataServiceCopy.getStateTimestamp(),
                            crb.getProtocolName(),
                            crb.getProtocolVersion(),
                            dataServiceCopy.getFacility(),
                            crb.getCrbCredentials().get("url"),
                            dataServiceCopy.getDsbSettings()

                    ));

            final CopyServiceInstanceResponse copyResult = dsbWebAPI.copyServiceInstance(
                    serviceId,
                    new CopyServiceInstance(
                            dataServiceCopyId.toString(),
                            crb.getProtocolName(),
                            crb.getProtocolVersion(),
                            crb.getCrbCredentials(),
                            dataServiceCopy.getFacility(),
                            applicationCopy.getTimeStamp().getTime()));

            if (copyResult.getStatus() != null && copyResult.getStatus() != 0) {
                markCopyAsFailed(
                        dsbUrn,
                        serviceId,
                        applicationCopy,
                        "Failed creating copy for dsbURN " + dsbUrn + " - " + copyResult.getStatus() + " - " +
                                copyResult.getStatusMessage());
            } else {
                appCopyPersisterService.persist(
                        applicationCopy.markDataServiceCopyAsCreatedSuccessfully(
                                dsbUrn,
                                serviceId,
                                null,
                                dataServiceCopyId,
                                crb.getCrbUrn()));
            }
        } catch (Exception ex) {
            log.error("Failed creating copy for dsbURN " + dsbUrn, ex);
            markCopyAsFailed(
                    dsbUrn,
                    serviceId,
                    applicationCopy,
                    "Failed creating copy for dsbURN " + dsbUrn + " - " + ex.getMessage());
        }
    }

    private void markCopyAsFailed(String dsbURN, String serviceId, ApplicationCopy applicationCopy, String message) {
        appCopyPersisterService.persist(
                applicationCopy.markAppCopyAsInvalid(message +
                        dsbURN + " serviceId: " + serviceId));
    }
}
