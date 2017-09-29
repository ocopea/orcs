// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import com.emc.microservice.discovery.WebAPIConnection;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.crb.CopyMetaData;
import com.emc.ocopea.crb.CrbWebApi;
import com.emc.ocopea.protection.ApplicationCopyState;
import com.emc.ocopea.protection.ProtectionAppCopyDTO;
import com.emc.ocopea.protection.ProtectionWebAPI;
import com.emc.ocopea.site.app.DeployedApplication;
import com.emc.ocopea.site.app.DeployedDataService;
import com.emc.ocopea.site.copy.CopyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by liebea on 11/3/16.
 * Drink responsibly
 */
public class AppInstanceStatisticsCalculator {
    private static final Logger log = LoggerFactory.getLogger(AppInstanceStatisticsCalculator.class);

    private final WebAPIConnection protectionPolicyServiceConnection;
    private final WebAPIResolver webAPIResolver;
    private final SiteRepository siteRepository;

    public AppInstanceStatisticsCalculator(
            WebAPIConnection protectionPolicyServiceConnection,
            WebAPIResolver webAPIResolver,
            SiteRepository siteRepository) {
        this.protectionPolicyServiceConnection = protectionPolicyServiceConnection;
        this.webAPIResolver = webAPIResolver;
        this.siteRepository = siteRepository;
    }

    public AppInstanceStatisticsDTO getAppInstanceStatistics(DeployedApplication deployedApplication) {
        return getAppInstanceStatisticsDTO(
                siteRepository.load(),
                deployedApplication,
                getProtectionPolicyAPI().listAppInstanceCopies(deployedApplication.getId()));
    }

    private AppInstanceStatisticsDTO getAppInstanceStatisticsDTO(
            Site site, DeployedApplication deployedApplication,
            Collection<ProtectionAppCopyDTO> protectionAppCopyDTOs) {
        List<AppInstanceCopyStatisticsDTO> appCopies = getAppInstanceCopyStatisticsDTOs(site, protectionAppCopyDTOs);

        List<AppInstanceStatisticsDTO.DataServiceProductionCopyStatisticsDTO> productionCopyStatistics =
                new ArrayList<>(deployedApplication.getDeployedDataServices().size());
        for (DeployedDataService currDeployedDS : deployedApplication.getDeployedDataServices().values()) {

            // Getting statistics from DSB about instance
            //todo:amit:re-enable with cache - this is slowing down ui!!
            //DsbWebApi dsbWebAPI = webAPIResolver.getWebAPI(currDeployedDS.getDsbUrl(), DsbWebApi.class);
            //ServiceInstanceDetails dsbInstanceStats = dsbWebAPI.getServiceInstance(currDeployedDS.getServiceId());
            //final String storageType = dsbInstanceStats.getStorageType();
            //final long poductionStorageSize = dsbInstanceStats.getSize();
            final String productionStorageType = "docker volume";
            final long productionStorageSize = 10240;

            productionCopyStatistics.add(
                    new AppInstanceStatisticsDTO
                            .DataServiceProductionCopyStatisticsDTO(
                            currDeployedDS.getDsbUrn(),
                            currDeployedDS.getBindName(),
                            productionStorageType,
                            productionStorageSize));
        }
        return new AppInstanceStatisticsDTO(deployedApplication.getId(), appCopies, productionCopyStatistics);
    }

    private List<AppInstanceCopyStatisticsDTO> getAppInstanceCopyStatisticsDTOs(
            Site site,
            Collection<ProtectionAppCopyDTO> protectionAppCopyDTOs) {
        return protectionAppCopyDTOs
                .stream()
                .map(protectionAppCopyDTO -> getAppInstanceCopyStatisticsDTO(protectionAppCopyDTO, site))
                .collect(Collectors.toList());
    }

    private AppInstanceCopyStatisticsDTO getAppInstanceCopyStatisticsDTO(ProtectionAppCopyDTO currAppCopy, Site site) {
        long appCopySize = 0L;
        List<AppInstanceStatisticsDTO.DataServiceCopyStatisticsDTO> dataServiceCopies = new ArrayList<>();
        for (Map.Entry<String, Map<String, ProtectionAppCopyDTO.DataProtectionDataServiceCopyInfoDTO>> currDSBEntry :
                currAppCopy.getDataServiceCopies().entrySet()) {
            String dsbName = currDSBEntry.getKey();
            for (Map.Entry<String, ProtectionAppCopyDTO.DataProtectionDataServiceCopyInfoDTO> currBindSCopy :
                    currDSBEntry.getValue().entrySet()) {

                long dataServiceCopySize = 0;
                String bindName = currBindSCopy.getKey();
                final String copyRepoUrn = currBindSCopy.getValue().getCopyRepoURN();
                Date copyTimeStamp = null;
                String copyRepoId = null;
                String copyRepoProtocol = "N/A";
                String copyRepositoryProtocolVersion = "N/A";
                String copyRepoName = "N/A";
                String facility = "N/A";

                // Getting additional copy metadata from CRB
                if (copyRepoUrn != null) {
                    final CopyRepository crb = site.getCopyRepositoryByUrn(copyRepoUrn);
                    if (crb != null) {
                        copyRepoName = crb.getName();
                        CopyMetaData copyMetadata = fetchCopyMetadata(crb.getUrl(), currBindSCopy.getValue()
                                .getCopyId());
                        if (copyMetadata != null) {

                            try {
                                // TODO: do we expect to have copy size? if so, where? requires definition.
                                // TODO: do we expect to have copy size? if so, where? requires definition.
                                dataServiceCopySize = Long.parseLong(
                                        copyMetadata.getCopyAdditionalInfo().getOrDefault("size", "0"));
                            } catch (Exception ex) {
                                log.warn("Failed getting copy size from crb", ex);
                                dataServiceCopySize = 0;
                            }

                            copyTimeStamp = copyMetadata.getCopyTimeStamp();
                            copyRepoId = copyMetadata.getRepoId();
                            copyRepoProtocol = copyMetadata.getProtocol();
                            copyRepositoryProtocolVersion = copyMetadata.getProtocolVersion();
                            facility = copyMetadata.getFacility();
                        }
                    }
                }

                appCopySize += dataServiceCopySize;

                dataServiceCopies.add(
                        new AppInstanceStatisticsDTO
                                .DataServiceCopyStatisticsDTO(
                                currBindSCopy.getValue().getCopyId(),
                                dsbName,
                                bindName,
                                copyTimeStamp,
                                copyRepoId,
                                facility,
                                copyRepoUrn,
                                copyRepoName,
                                copyRepoProtocol,
                                copyRepositoryProtocolVersion,
                                dataServiceCopySize,
                                currBindSCopy.getValue().getState().toString()
                        )
                );
            }
        }
        return new AppInstanceCopyStatisticsDTO(
                currAppCopy.getCopyId(),
                currAppCopy.getTimeStamp(),
                appCopySize,
                mapCopyState(currAppCopy.getState()),
                dataServiceCopies,
                currAppCopy.getAppServiceCopies()
                        .entrySet()
                        .stream().map(e ->
                        new AppInstanceStatisticsDTO.AppServiceCopyStatisticsDTO(
                                e.getKey(),
                                e.getValue().getAppImageName(),
                                e.getValue().getAppImageType(),
                                e.getValue().getAppImageVersion(),
                                e.getValue().getCopyTimestamp(),
                                e.getValue().getAppServiceConfiguration(),
                                e.getValue().getState().toString()))
                        .collect(Collectors.toList())

        );
    }

    private CopyMetaData fetchCopyMetadata(String copyRepoUrl, UUID copyId) {
        CrbWebApi crbWebApi = webAPIResolver.getWebAPI(copyRepoUrl, CrbWebApi.class);
        if (crbWebApi == null) {
            log.warn("Failed locating copy repo with URL {}", copyRepoUrl);
            return null;
        }
        try {
            return crbWebApi.getCopyMetaData(copyId.toString());
        } catch (WebApplicationException ex) {
            log.warn("Failed fetching copy metadata from crb " + copyRepoUrl + " - " + ex.getMessage());
            log.debug("Failed fetching copy metadata from crb " + copyRepoUrl, ex);
            return null;
        }
    }

    private AppInstanceCopyStatisticsDTO.SiteAppCopyState mapCopyState(ApplicationCopyState state) {
        switch (state) {
            case created:
                return AppInstanceCopyStatisticsDTO.SiteAppCopyState.created;
            case failed:
                return AppInstanceCopyStatisticsDTO.SiteAppCopyState.failed;
            case inprogress:
            case scheduled:
            default:
                return AppInstanceCopyStatisticsDTO.SiteAppCopyState.creating;
        }
    }

    public List<AppInstanceCopyStatisticsDTO> getCopyHistory(UUID appInstanceId, Long intervalStart, Long intervalEnd) {
        return getAppInstanceCopyStatisticsDTOs(
                siteRepository.load(),
                getProtectionPolicyAPI()
                        .listAppInstanceCopies(appInstanceId, intervalStart, intervalEnd));
    }

    public AppInstanceCopyStatisticsDTO getAppCopyMetadata(UUID copyId) {
        ProtectionAppCopyDTO copy = getProtectionPolicyAPI().getCopy(copyId);
        return getAppInstanceCopyStatisticsDTO(copy, siteRepository.load());
    }

    private ProtectionWebAPI getProtectionPolicyAPI() {
        return protectionPolicyServiceConnection.resolve(ProtectionWebAPI.class);
    }
}
