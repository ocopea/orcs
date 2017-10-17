// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.webapp;

import com.emc.microservice.Context;
import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.dependency.ManagedDependency;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.hub.HubWebAppUtil;
import com.emc.ocopea.hub.UserContextService;
import com.emc.ocopea.hub.application.AppServiceExternalDependencyDTO;
import com.emc.ocopea.hub.application.ApplicationServiceTemplateDTO;
import com.emc.ocopea.hub.application.ApplicationTemplateDTO;
import com.emc.ocopea.hub.application.CreateAppCopyCommandArgs;
import com.emc.ocopea.hub.application.CreateSavedImageCommandArgs;
import com.emc.ocopea.hub.application.DataServiceTypeEnumDTO;
import com.emc.ocopea.hub.application.DeployAppCommandArgs;
import com.emc.ocopea.hub.application.DeploySavedImageCommandArgs;
import com.emc.ocopea.hub.application.HubAppInstanceConfigurationDTO;
import com.emc.ocopea.hub.application.HubAppInstanceDownStreamTreeDTO;
import com.emc.ocopea.hub.application.HubAppInstanceWithStateDTO;
import com.emc.ocopea.hub.application.HubWebApi;
import com.emc.ocopea.hub.application.RepurposeAppCommandArgs;
import com.emc.ocopea.hub.application.StopAppCommandArgs;
import com.emc.ocopea.hub.site.AddSiteToHubCommandArgs;
import com.emc.ocopea.hub.site.SiteDto;
import com.emc.ocopea.hub.webapp.sankey.DistributionSankeyHelper;
import com.emc.ocopea.site.AddCrToSiteCommandArgs;
import com.emc.ocopea.site.AddCustomArtifactRegistryToSiteCommandArgs;
import com.emc.ocopea.site.AddDockerArtifactRegistryToSiteCommandArgs;
import com.emc.ocopea.site.AddMavenArtifactRegistryToSiteCommandArgs;
import com.emc.ocopea.site.AppInstanceCopyStatisticsDTO;
import com.emc.ocopea.site.AppInstanceInfoDTO;
import com.emc.ocopea.site.AppInstanceStatisticsDTO;
import com.emc.ocopea.site.DsbCatalogWebApi;
import com.emc.ocopea.site.RegisterCrbToSiteCommandArgs;
import com.emc.ocopea.site.RegisterDsbToSiteCommandArgs;
import com.emc.ocopea.site.RegisterPsbToSiteCommandArgs;
import com.emc.ocopea.site.RemoveArtifactRegistryFromSiteCommandArgs;
import com.emc.ocopea.site.RemoveCrbFromSiteCommandArgs;
import com.emc.ocopea.site.RemoveDsbFromSiteCommandArgs;
import com.emc.ocopea.site.ServiceLogsWebSocketDTO;
import com.emc.ocopea.site.SiteArtifactRegistryInfoDTO;
import com.emc.ocopea.site.SiteCopyRepoInfoDTO;
import com.emc.ocopea.site.SiteLocationDTO;
import com.emc.ocopea.site.SitePsbDetailedInfoDto;
import com.emc.ocopea.site.SitePsbInfoDto;
import com.emc.ocopea.site.SiteWebApi;
import com.emc.ocopea.site.SupportedServiceDto;
import com.emc.ocopea.site.app.DeployedApplicationState;
import com.emc.ocopea.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class HubWebAppResource implements HubWebAppWebApi {
    private static final Random random = new Random(System.currentTimeMillis());
    private static final Logger log = LoggerFactory.getLogger(HubWebAppResource.class);
    private static final long DAY = 24L * 60 * 60 * 1000;
    private static final long WEEK = 7 * DAY;

    private static final long MONTH = 4 * WEEK;
    private static final String NAZGUL_LOCATION_HEADER_FILTER = "NAZGUL-LOCATION-HEADER-FILTER";
    private ManagedDependency hubServiceDependency;
    private WebAPIResolver webAPIResolver;
    //TODO: should we remove this cache?
    private AppTemplateCache appTemplateCache;
    private UserContextService userContextService;

    @javax.ws.rs.core.Context
    private SecurityContext securityContext;
    @javax.ws.rs.core.Context
    private UriInfo uriInfo;
    @javax.ws.rs.core.Context
    private HttpServletRequest servletRequest;

    @NoJavadoc
    @javax.ws.rs.core.Context
    public void setApplication(Application application) {
        Context context = ((MicroServiceApplication) application).getMicroServiceContext();
        hubServiceDependency = context.getDependencyManager().getManagedResourceByName("hub");
        appTemplateCache = context.getSingletonManager().getManagedResourceByName(
                AppTemplateCache.class.getSimpleName()).getInstance();

        userContextService = new UserContextService(securityContext);
        webAPIResolver = context.getWebAPIResolver();
    }

    @Override
    public UIAppInstanceState getAppInstanceState(@PathParam("appInstanceId") UUID appInstanceId) {
        return HubWebAppUtil.wrap(
                "getting app instance state for appInstanceId " + appInstanceId,
                () -> {
                    // Loading app instance from hub
                    final HubAppInstanceConfigurationDTO appInstance = getAppInstanceFromHub(appInstanceId);

                    // Loading site urn by id
                    final String siteUrl = getSite(appInstance.getSiteId()).getUrl();

                    // Communicating directly with site to get app instance info
                    AppInstanceInfoDTO appInstanceInfoFromSite = getAppInstanceInfoFromSite(appInstanceId, siteUrl);

                    // Fetching for each service - which protocol it requires from dsb
                    final Map<String, String> serviceNameToProtocol =
                            getProtocolByServiceName(appTemplateCache, appInstance.getAppTemplateId());

                    // Getting entry point urls by app service names
                    final Map<String, String> appServiceRelativePathSuffixByAppServiceName =
                            getAppServiceNameSuffixPath(appTemplateCache, appInstance.getAppTemplateId());

                    // Path is used for building absolute path for images and static resources
                    final String rawPath = getRawHubWebAppUriPath();

                    // Getting root entry point url for the app
                    String entryPointURL = getRootEntryPointUrl(
                            appInstance,
                            appInstanceInfoFromSite,
                            appServiceRelativePathSuffixByAppServiceName);

                    return new UIAppInstanceState(
                            appInstanceId,
                            appInstanceInfoFromSite.getName(),
                            convertSiteState(appInstanceInfoFromSite.getState()),
                            appInstanceInfoFromSite.getStateMessage(),
                            entryPointURL,
                            appInstanceInfoFromSite.getAppServices()
                                    .stream()
                                    .map(as -> new UIAppServiceState(
                                            as.getServiceName(),
                                            as.getImageName(),
                                            as.getImageType(),
                                            as.getImageVersion(),
                                            gutServicePublicUrl(appServiceRelativePathSuffixByAppServiceName, as),
                                            rawPath + "images/image-type/" + as.getImageType(),
                                            UIAppServiceState.StateEnum.fromValue(as.getState().name()),
                                            as.getStateMessage(),
                                            as.getStateDate(),
                                            new ArrayList<String>(as.getServiceBindings())))
                                    .collect(Collectors.toList()),
                            appInstanceInfoFromSite.getDataServices()
                                    .stream()
                                    .map(ds -> {
                                        String iconUrl = null;
                                        final String protocolName = serviceNameToProtocol.get(ds.getBindName());
                                        if (protocolName != null) {
                                            iconUrl = rawPath + "images/dsb-protocol/" + protocolName;
                                        }
                                        return new UIDataServiceState(
                                                ds.getBindName(),
                                                ds.getDsbURN(),
                                                ds.getServiceId(),
                                                UIDataServiceState.StateEnum.fromValue(ds.getState().name()),
                                                ds.getStateMessage(),
                                                iconUrl,
                                                ds.getStateDate());
                                    })
                                    .collect(Collectors.toList())
                    );
                });
    }

    private String getRawHubWebAppUriPath() {
        return uriInfo.getBaseUri().getRawPath();
    }

    private AppInstanceInfoDTO getAppInstanceInfoFromSite(UUID appInstanceId, String siteUrl) {
        return HubWebAppUtil.wrapMandatory(
                "Fetching app instance info from site",
                () -> getSiteApi(siteUrl).getAppInstanceInfo(appInstanceId));
    }

    private String getRootEntryPointUrl(
            HubAppInstanceConfigurationDTO appInstance,
            AppInstanceInfoDTO appInstanceInfoFromSite,
            Map<String, String> appServiceRelativePathSuffixByAppServiceName) {

        String entryPointURL = appInstanceInfoFromSite.getEntryPointURL();
        final ApplicationTemplateDTO appTemplateById =
                appTemplateCache.getAppTemplateById(appInstance.getAppTemplateId(), true);

        if (appTemplateById != null && appTemplateById.getEntryPointServiceName() != null) {

            // In case site did not provide a root url, getting from the entry point service url
            if (entryPointURL == null || entryPointURL.isEmpty()) {
                entryPointURL = appInstanceInfoFromSite.getAppServices().stream()
                        .filter(appServiceInfoDTO ->
                                appServiceInfoDTO.getServiceName().equals(appTemplateById.getEntryPointServiceName()))
                        .map((appServiceInfoDTO1) -> appServiceInfoDTO1.getPublicUrl() == null ?
                                "" :
                                appServiceInfoDTO1.getPublicUrl())
                        .findAny().orElseGet(() -> "");
            }

            final String entryPointServiceUrlSuffix =
                    appServiceRelativePathSuffixByAppServiceName.get(appTemplateById.getEntryPointServiceName());
            if (entryPointServiceUrlSuffix != null) {
                if (!entryPointURL.endsWith("/")) {
                    entryPointURL += "/";
                }
                entryPointURL += entryPointServiceUrlSuffix;
            }
        }
        return entryPointURL;
    }

    private String gutServicePublicUrl(
            Map<String, String> appServiceRelativePathSuffixByAppServiceName,
            AppInstanceInfoDTO.AppServiceInfoDTO as) {

        String publicUrl = as.getPublicUrl();
        if (publicUrl != null) {
            if (!publicUrl.endsWith("/")) {
                publicUrl += "/";
            }
            final String suffix = appServiceRelativePathSuffixByAppServiceName.get(as.getServiceName());
            if (suffix != null) {
                publicUrl += suffix;
            }
        }
        return publicUrl;
    }

    /**
     * Get entry point url by app service name for services that have entry points
     */
    private Map<String, String> getAppServiceNameSuffixPath(AppTemplateCache appTemplateCache, UUID appTemplateId) {
        Map<String, String> pathByAppService = Collections.emptyMap();
        final ApplicationTemplateDTO appTemplateById = appTemplateCache.getAppTemplateById(appTemplateId, true);
        if (appTemplateById != null) {
            pathByAppService = appTemplateById.getAppServiceTemplates()
                    .stream()
                    .filter(ast -> ast.getEntryPointURL() != null && !ast.getEntryPointURL().isEmpty())
                    .collect(Collectors.toMap(
                            ApplicationServiceTemplateDTO::getAppServiceName,
                            ApplicationServiceTemplateDTO::getEntryPointURL
                    ));
        }
        return pathByAppService;
    }

    @Override
    public List<UILogsWebSocketInfo> getLogsWebSockets(@PathParam("appInstanceId") UUID appInstanceId) {

        // This address matches the address in HubLogsWebSocket class
        String hubWebSocketAddress = uriInfo.getAbsolutePathBuilder().scheme("ws").build().toString();

        return getWebSocketsFromSite(appInstanceId, getSite(getAppInstanceConf(appInstanceId).getSiteId()).getUrl())
                .stream()
                .map(serviceLogsWebSocketDTO -> new UILogsWebSocketInfo(
                        hubWebSocketAddress,
                        UILogsWebSocketInfo.SerializationEnum.fromValue(serviceLogsWebSocketDTO.getSerialization()),
                        serviceLogsWebSocketDTO.getTags()))
                .collect(Collectors.toList());

    }

    private List<ServiceLogsWebSocketDTO> getWebSocketsFromSite(UUID appInstanceId, String siteUrl) {
        return HubWebAppUtil.wrap(
                "fetching logging web sockets from site",
                () -> getSiteApi(siteUrl).getAppInstanceLogsWebSockets(appInstanceId));
    }

    private HubAppInstanceConfigurationDTO getAppInstanceConf(UUID appInstanceId) {
        return HubWebAppUtil.wrap(
                "fetching app instance",
                () -> getHubApi().getAppInstance(appInstanceId));
    }

    private SiteWebApi getSiteApi(String siteUrl) {
        return webAPIResolver.getWebAPI(siteUrl, SiteWebApi.class);
    }

    static Map<String, String> getProtocolByServiceName(AppTemplateCache appTemplateCache, UUID appTemplateId) {
        Map<String, String> serviceNameToProtocol = Collections.emptyMap();

        // Getting app template from cache
        final ApplicationTemplateDTO appTemplateById = appTemplateCache.getAppTemplateById(appTemplateId, true);
        if (appTemplateById != null) {
            serviceNameToProtocol = appTemplateById.getAppServiceTemplates()
                    .stream()
                    .flatMap(a -> a.getDependencies()
                            .stream()
                    ).collect(Collectors.toMap(
                            AppServiceExternalDependencyDTO::getName,
                            d -> d.getProtocols().iterator().next().getProtocolName(),
                            (s, s2) -> s
                    ));
        }
        return serviceNameToProtocol;
    }

    private HubAppInstanceConfigurationDTO getAppInstanceFromHub(@PathParam("appInstanceId") UUID appInstanceId) {
        return HubWebAppUtil.wrapMandatory(
                "loading app with id " + appInstanceId + " from hub",
                () -> getHubApi().getAppInstance(appInstanceId));
    }

    private UIAppInstanceState.StateEnum convertSiteState(DeployedApplicationState state) {
        switch (state) {
            case deploying:
                return UIAppInstanceState.StateEnum.DEPLOYING;
            case pending:
                return UIAppInstanceState.StateEnum.PENDING;
            case running:
                return UIAppInstanceState.StateEnum.RUNNING;
            case stopped:
                return UIAppInstanceState.StateEnum.STOPPED;
            case stopping:
                return UIAppInstanceState.StateEnum.STOPPING;
            case errorstopping:
                return UIAppInstanceState.StateEnum.ERRORSTOPPING;
            default:
                return UIAppInstanceState.StateEnum.ERROR;
        }

    }

    // todo:add site cache?
    private SiteDto getSite(UUID siteId) {
        return HubWebAppUtil.wrapMandatory(
                "loading site info with id " + siteId + " from the hub",
                () -> getHubApi().getSite(siteId));
    }

    @Override
    // "period" is generated by Swagger as String instead of Enum.
    // See https://github.com/swagger-api/swagger-codegen/issues/1347
    public UICopyHistoryData getCopyHistory(
            @PathParam("appInstanceId") UUID appInstanceId,
            @QueryParam("period") @DefaultValue("week") String period,
            @QueryParam("interval") @DefaultValue("0") Integer interval) {

        HubAppInstanceConfigurationDTO appInstance = getAppInstanceFromHub(appInstanceId);

        SiteDto siteDto = getSite(appInstance.getSiteId());

        SiteWebApi siteWebApi = getSiteApi(siteDto.getUrl());

        long periodLength;
        switch (period) {
            case "day":
                periodLength = DAY;
                break;
            case "week":
                periodLength = WEEK;
                break;
            case "month":
                periodLength = MONTH;
                break;
            case "year":
                periodLength = DAY * 365;
                break;
            default:
                throw new BadRequestException("Unsupported period type " + period);
        }
        Date now = new Date();

        // If interval is now - giving extra 5 minutes for possible future copies when we have clock skew
        long graceFoCurrent = 0;
        if (interval == 0) {
            graceFoCurrent = 5 * 60 * 1000L;
        }
        Date fromDate = new Date(now.getTime() - (interval + 1) * periodLength);
        Date toDate = new Date(fromDate.getTime() + periodLength + graceFoCurrent);

        Collection<AppInstanceCopyStatisticsDTO> copies =
                HubWebAppUtil.wrap(
                        "loading copy history from site " + siteDto.getUrn(),
                        () -> siteWebApi.getCopyHistory(
                                appInstanceId,
                                fromDate.getTime(),
                                toDate.getTime()));

        return new UICopyHistoryData(fromDate, toDate,
                copies.stream().map(
                        this::convertCopy)
                        .collect(Collectors.toList()));
    }

    private UIAppInstanceCopy convertCopy(AppInstanceCopyStatisticsDTO copy) {
        return new UIAppInstanceCopy(
                copy.getCopyId(),
                copy.getTimeStamp(),
                copy.getState().toString(),
                "offlineBackup");
    }

    @Override
    public UIDashboardStats getDashboardStatistics(@PathParam("appInstanceId") UUID appInstanceId) {

        HubAppInstanceDownStreamTreeDTO downStreamInfo =
                HubWebAppUtil.wrap(
                        "listing downstream instances for appInstanceId " + appInstanceId,
                        () -> getHubApi().listDownStreamInstances(appInstanceId));

        Map<String, Integer> copySummary = new HashMap<>();
        count(downStreamInfo, copySummary);

        // Todo:amit:multi-site
        copySummary.put(downStreamInfo.getDeploymentType(), 1);

        HubAppInstanceWithStateDTO appInstance = getHubAppInstanceWithState(appInstanceId);
        SiteDto site = getSite(appInstance.getSiteId());
        SiteWebApi siteWebApi = getSiteApi(site.getUrl());

        AppInstanceStatisticsDTO siteAppInstanceStats =
                getAppInstanceStatisticsFromSite(appInstanceId, siteWebApi);

        Collection<AppInstanceCopyStatisticsDTO> appCopies = siteAppInstanceStats.getAppCopies();
        if (!appCopies.isEmpty()) {
            copySummary.put("offlineBackup", appCopies.size());
        }

        // Todo: we can probably optimize, the UIHubAppInstanceStatistics is huge and we need only the summary here
        UIHubAppInstanceStatistics uiHubAppInstanceStatistics = buildAppInstanceStats(appInstanceId, downStreamInfo);
        UISankeyInfo uiSankeyInfo = DistributionSankeyHelper.buildCopyDistributionSankey(uiHubAppInstanceStatistics);

        final ApplicationTemplateDTO appTemplate =
                appTemplateCache.getAppTemplateById(appInstance.getAppTemplateId(), true);

        final UIAppInstanceQuota appQuotaSummary = new UIAppInstanceQuota(
                appTemplate.getAppServiceTemplates()
                        .stream()
                        .map(ast -> new UIAppInstanceQuotaAppServiceQuotas(
                                ast.getAppServiceName(),
                                random.nextDouble() * 22,
                                null,
                                null,
                                "GB"))
                        .collect(Collectors.toList()),
                appTemplate.getAppServiceTemplates()
                        .stream()
                        .flatMap(ast -> ast.getDependencies().stream())
                        .distinct()
                        .flatMap(dep -> dep.getProtocols().stream())
                        .distinct()
                        .map(p -> new UIAppInstanceQuotaAppServiceQuotas(
                                p.getProtocolName(),
                                random.nextDouble() * 100,
                                null,
                                null,
                                null))
                        .collect(Collectors.toList()));

        return new UIDashboardStats(
                copySummary,
                uiSankeyInfo,
                convert(appInstance),
                convertTemplate(appTemplate),
                convertAppLocation(site),
                appQuotaSummary);
    }

    private AppInstanceStatisticsDTO getAppInstanceStatisticsFromSite(
            @PathParam("appInstanceId") UUID appInstanceId,
            SiteWebApi siteWebApi) {

        return HubWebAppUtil.wrapMandatory(
                "loading appInstance Statistics for appInstanceId " + appInstanceId,
                () -> siteWebApi.getAppInstanceStatistics(appInstanceId));
    }

    private HubAppInstanceWithStateDTO getHubAppInstanceWithState(@PathParam("appInstanceId") UUID appInstanceId) {
        return HubWebAppUtil.wrapMandatory(
                "loading state with id " + appInstanceId + " from hub",
                () -> getHubApi().getAppInstanceState(appInstanceId));
    }

    private UIAppGeography convertAppLocation(SiteDto siteDto) {
        final UISiteLocation uiSiteLocation = convertLocation(siteDto);
        return new UIAppGeography(Collections.singletonList(uiSiteLocation));
    }

    private UISiteLocation convertLocation(SiteDto siteDto) {
        SiteLocationDTO siteLocation = siteDto.getLocation();
        Map<String, String> properties = new HashMap<>();
        properties.put("name", siteLocation.getName());
        properties.put("siteId", siteDto.getId().toString());
        return new UISiteLocation(
                "Feature",
                new UIPoint("Point", Arrays.asList(siteLocation.getLatitude(), siteLocation.getLongitude())),
                properties
        );
    }

    private void count(HubAppInstanceDownStreamTreeDTO downStreamInfo, Map<String, Integer> copySummary) {
        downStreamInfo.getDownStreamInstances().forEach(curr -> {
                    Integer count = copySummary.get(curr.getDeploymentType());
                    if (count == null) {
                        copySummary.put(curr.getDeploymentType(), 1);
                    } else {
                        copySummary.put(curr.getDeploymentType(), count + 1);
                    }
                    count(curr, copySummary);
                }
        );
    }

    private UIHubAppInstanceStatistics buildAppInstanceStats(
            UUID appInstanceId,
            HubAppInstanceDownStreamTreeDTO downStreamInfo) {

        HubAppInstanceConfigurationDTO appInstance = getAppInstanceFromHub(appInstanceId);
        SiteDto siteDto = getSite(appInstance.getSiteId());

        AppInstanceInfoDTO siteInfo = getAppInstanceInfoFromSite(appInstanceId, siteDto.getUrl());
        AppInstanceStatisticsDTO siteAppInstanceStats =
                getAppInstanceStatisticsFromSite(appInstanceId, getSiteApi(siteDto.getUrl()));
        Map<String, Collection<UIHubAppInstanceStatistics>> downStreamAppInstanceStats = new HashMap<>();

        if (!downStreamInfo.getDownStreamInstances().isEmpty()) {
            for (HubAppInstanceDownStreamTreeDTO currDownStreamInstance : downStreamInfo.getDownStreamInstances()) {
                UIHubAppInstanceStatistics uiHubAppInstanceStatistics =
                        buildAppInstanceStats(currDownStreamInstance.getAppInstanceId(), currDownStreamInstance);
                Collection<UIHubAppInstanceStatistics> byDeploymentType =
                        downStreamAppInstanceStats.computeIfAbsent(
                                uiHubAppInstanceStatistics.getDeploymentType(),
                                k -> new ArrayList<>());

                byDeploymentType.add(uiHubAppInstanceStatistics);
            }
        }

        return new UIHubAppInstanceStatistics(
                appInstance.getSiteId(),
                appInstance.getName(),
                appInstanceId,
                downStreamInfo.getDeploymentType(),
                siteInfo.getState().name(),
                siteAppInstanceStats.getAppCopies(),
                siteAppInstanceStats.getProductionCopyStatistics(),
                downStreamAppInstanceStats);
    }

    @Override
    public UIAppInstance getAppInstance(@PathParam("appInstanceId") UUID appInstanceId) {
        return convert(getHubApi().getAppInstanceState(appInstanceId));
    }

    @Override
    public List<UIAppInstance> listAppInstances() {
        return getHubApi().listAppInstanceStates()
                .stream()
                .map(
                        this::convert
                ).collect(Collectors.toList());
    }

    private UIAppInstance convert(HubAppInstanceWithStateDTO currInstance) {
        return new UIAppInstance(
                currInstance.getId(),
                currInstance.getName(),
                currInstance.getAppTemplateId(),
                appTemplateCache.getAppTemplateById(currInstance.getAppTemplateId(), true).getName(),
                currInstance.getCreatorUserId(),
                currInstance.getWebEntryPointURL(),
                currInstance.getState(),
                currInstance.getStateMessage(),
                currInstance.getDeploymentType(),
                currInstance.getCreated());
    }

    @Override
    public UIApplicationTemplate getAppTemplate(@PathParam("appTemplateId") UUID appTemplateId) {
        return convertTemplate(appTemplateCache.getAppTemplateById(appTemplateId, false));
    }

    @Override
    public void deleteAppTemplate(@PathParam("appTemplateId") UUID appTemplateId) {
        invokeUICommand("Delete Application Template", () -> {
            getHubApi().deleteAppTemplate(appTemplateId);
            appTemplateCache.clear();
        });
    }

    @Override
    public UIAppInstanceCopy getAppInstanceCopy(
            @PathParam("appInstanceId") UUID appInstanceId,
            @PathParam("appCopyId") UUID appCopyId) {

        HubAppInstanceConfigurationDTO appInstance = getAppInstanceFromHub(appInstanceId);
        SiteDto siteDto = getSite(appInstance.getSiteId());
        return convertCopy(getSiteApi(siteDto.getUrl()).getCopyMetadata(appCopyId));
    }

    @Override
    public List<UIApplicationTemplate> listAppTemplates() {
        return appTemplateCache.listAppTemplates()
                .stream()
                .map(this::convertTemplate)
                .collect(Collectors.toList());
    }

    @Override
    public List<UISite> listSites() {
        return fetchSitesFromHub()
                .stream()
                .map(this::convertUISite)
                .collect(Collectors.toList());
    }

    private Collection<SiteDto> fetchSitesFromHub() {
        return HubWebAppUtil.wrap(
                "fetching sites from hub",
                getHubApi()::listSites);
    }

    private UISite convertUISite(SiteDto siteDto) {
        return new UISite(
                siteDto.getId(),
                siteDto.getName(),
                siteDto.getUrn(),
                convertLocation(siteDto));
    }

    private UISiteConfig convertUISiteConfig(SiteDto siteDto) {

        return new UISiteConfig(
                siteDto.getId(),
                siteDto.getName(),
                siteDto.getUrn(),
                convertLocation(siteDto),
                getDSBCatalog(siteDto.getUrl())
                        .stream()
                        .map(this::convertDsb)
                        .collect(Collectors.toList()),
                getSitePsbs(siteDto.getUrl())
                        .stream()
                        .map(this::convertPsb)
                        .collect(Collectors.toList()),
                getSiteCopyRepositories(siteDto.getUrl())
                        .stream()
                        .map(this::convertCopyRepo)
                        .collect(Collectors.toList()),
                getSiteArtifactRegistries(siteDto.getUrl())
                        .stream()
                        .map(this::convertArtifactRegistry)
                        .collect(Collectors.toList())
        );
    }

    private Collection<SupportedServiceDto> getDSBCatalog(String siteUrl) {
        return HubWebAppUtil.wrap(
                "loading dsb catalog from site " + siteUrl,
                webAPIResolver.getWebAPI(siteUrl, DsbCatalogWebApi.class)::getCatalog);
    }

    private Collection<SitePsbInfoDto> getSitePsbs(String siteUrl) {
        return HubWebAppUtil.wrap(
                "loading PSBs from site " + siteUrl,
                getSiteApi(siteUrl)::listPsbs);
    }

    private Collection<SiteCopyRepoInfoDTO> getSiteCopyRepositories(String siteUrl) {
        return HubWebAppUtil.wrap(
                "loading copy repositories from site " + siteUrl,
                getSiteApi(siteUrl)::listCopyRepositories);
    }

    private Collection<SiteArtifactRegistryInfoDTO> getSiteArtifactRegistries(String siteUrl) {
        return HubWebAppUtil.wrap(
                "loading artifact registries from site " + siteUrl,
                getSiteApi(siteUrl)::listArtifactRegistries);
    }

    private UISiteDsb convertDsb(SupportedServiceDto dsb) {
        return new UISiteDsb(
                dsb.getUrn(),
                dsb.getName(),
                dsb.getDescription(),
                getRawHubWebAppUriPath() + "images/dsb-protocol/" +
                        dsb.getPlans().stream().filter(planDTO -> !planDTO.getSupportedProtocols().isEmpty())
                                .map(SupportedServiceDto.SupportedServicePlanDto::getSupportedProtocols)
                                .findFirst().orElse(Collections.emptyList())
                                .stream()
                                .findFirst().orElse(
                                new SupportedServiceDto.SupportedServiceProtocolDto(
                                        "undefined",
                                        "1",
                                        Collections.emptyMap()))
                                .getProtocolName(),
                dsb.getPlans()
                        .stream()
                        .map(p ->
                                new UISiteDsbPlan(
                                        p.getName(),
                                        p.getDescription(),
                                        //todo: cost
                                        "Free",
                                        p.getSupportedProtocols()
                                                .stream()
                                                .map(protocol -> new UISiteDsbProtocol(
                                                        protocol.getProtocolName(),
                                                        protocol.getProtocolVersion()))
                                                .collect(Collectors.toList())
                                ))
                        .collect(Collectors.toList())
        );
    }

    private UISitePsb convertPsb(SitePsbInfoDto psb) {
        return new UISitePsb(
                psb.getUrn(),
                psb.getName(),
                psb.getType(),
                psb.getVersion()
        );
    }

    private UISiteCopyRepoBroker convertCopyRepo(SiteCopyRepoInfoDTO cr) {
        return new UISiteCopyRepoBroker(
                cr.getUrn(),
                cr.getName(),
                cr.getType(),
                cr.getVersion(),
                //todo: copy protocols - read from crb
                Collections.singletonList(new UISiteCopyRepo(

                        "default",
                        Collections.singletonList(new UISiteCopyRepoCopyProtocols(
                                "shpanRest",
                                "1.0"
                        ))
                )),

                //todo: property types - read from crb
                Arrays.asList(
                        new UISiteCopyRepoBrokerCrPropertyTypes(
                                "addr",
                                "Address",
                                "Host Address/IP",
                                UISiteCopyRepoBrokerCrPropertyTypes.TypeEnum.STRING,
                                Boolean.TRUE),
                        new UISiteCopyRepoBrokerCrPropertyTypes(
                                "user",
                                "User Name",
                                "User name to access the host",
                                UISiteCopyRepoBrokerCrPropertyTypes.TypeEnum.STRING,
                                Boolean.TRUE),
                        new UISiteCopyRepoBrokerCrPropertyTypes(
                                "password",
                                "password",
                                "user password for the authenticating with the host",
                                UISiteCopyRepoBrokerCrPropertyTypes.TypeEnum.PASSWORD,
                                Boolean.FALSE)
                )

        );
    }

    private UISiteArtifactRegistry convertArtifactRegistry(SiteArtifactRegistryInfoDTO ar) {
        return new UISiteArtifactRegistry(
                ar.getName(),
                ar.getType(),

                // Remove passwords from the parameters
                ar.getParameters().entrySet()
                        .stream()
                        .filter(entry ->
                                !"password".equals(entry.getKey()) &&
                                        entry.getValue() != null)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue))
        );
    }

    @Override
    public List<UISiteConfig> listSiteConfigurations() {
        return fetchSitesFromHub()
                .stream()
                .map(this::convertUISiteConfig)
                .collect(Collectors.toList());
    }

    @Override
    public UISiteConfig getSiteConfiguration(@PathParam("siteId") UUID siteId) {
        return convertUISiteConfig(getSite(siteId));
    }

    @Override
    public List<UISiteTopology> listSiteTopologies() {
        return fetchSitesFromHub()
                .stream()
                .map(this::convertUISiteTopology)
                .collect(Collectors.toList());
    }

    @Override
    public UISiteTopology getSiteTopology(@PathParam("siteId") UUID siteId) {
        return convertUISiteTopology(getSite(siteId));
    }

    private UISiteTopology convertUISiteTopology(SiteDto siteDto) {

        Collection<SitePsbDetailedInfoDto> psbsDetailed = listPsbDetailedFromSite(siteDto.getUrl());

        return new UISiteTopology(
                siteDto.getId(),
                siteDto.getName(),
                convertLocation(siteDto),
                psbsDetailed
                        .stream()
                        .flatMap(psbInfo ->
                                psbInfo.getSpaces()
                                        .stream()
                                        .map(SitePsbDetailedInfoDto.SitePSBSpaceInfo::getName)
                        ).collect(Collectors.toList()));
    }

    private Collection<SitePsbDetailedInfoDto> listPsbDetailedFromSite(String siteUrl) {
        return HubWebAppUtil.wrap("list psb detailed from site", getSiteApi(siteUrl)::listPsbsDetailed);
    }

    private UIApplicationTemplate convertTemplate(ApplicationTemplateDTO currTemplate) {
        final String rawPath = getRawHubWebAppUriPath();
        return new UIApplicationTemplate(
                currTemplate.getId(),
                currTemplate.getName(),
                currTemplate.getVersion(),
                currTemplate.getDescription(),
                currTemplate.getAppServiceTemplates().stream().map(
                        currAppServiceTemplate ->
                                new UIApplicationServiceTemplate(
                                        currAppServiceTemplate.getAppServiceName(),
                                        currAppServiceTemplate.getPsbType(),
                                        currAppServiceTemplate.getImageName(),
                                        currAppServiceTemplate.getImageType(),
                                        currAppServiceTemplate.getImageVersion(),
                                        currAppServiceTemplate.getEnvironmentVariables(),
                                        currAppServiceTemplate.getDependencies()
                                                .stream()
                                                .map(
                                                        currDependency -> new UIAppServiceExternalDependency(
                                                                convertDSType(currDependency.getType()),
                                                                currDependency.getName(),
                                                                currDependency.getDescription(),
                                                                rawPath + "images/dsb-protocol/" +
                                                                        currDependency.getProtocols()
                                                                                .iterator().next().getProtocolName()))
                                                .collect(Collectors.toList()),
                                        currAppServiceTemplate.getExposedPorts(),
                                        currAppServiceTemplate.getHttpPort(),
                                        currAppServiceTemplate.getEntryPointURL(),
                                        rawPath + "images/image-type/" + currAppServiceTemplate.getImageType()))
                        .collect(Collectors.toList()),
                currTemplate.getEntryPointServiceName(),
                rawPath + "images/app-template/" + currTemplate.getId().toString());
    }

    private UIAppServiceExternalDependency.TypeEnum convertDSType(DataServiceTypeEnumDTO type) {
        final UIAppServiceExternalDependency.TypeEnum ret =
                UIAppServiceExternalDependency.TypeEnum.fromValue(type.name());
        return ret == null ? UIAppServiceExternalDependency.TypeEnum.OTHER : ret;
    }

    private DataServiceTypeEnumDTO convertDSTypeFromUI(UICreateAppServiceExternalDependency.TypeEnum type) {
        try {
            return DataServiceTypeEnumDTO.valueOf(type.value());
        } catch (Exception ex) {
            return DataServiceTypeEnumDTO.other;
        }
    }

    private <T> T checkMissingField(T o, String name) {
        if (o == null) {
            throw new BadRequestException("missing " + name);
        }
        return o;
    }

    @Override
    public UUID rePurposeApp(UICommandRePurposeApp rePurposeAppParams) {
        return invokeUICommand("Repurpose application", () -> {

            final String appInstanceName =
                    checkMissingField(rePurposeAppParams.getAppInstanceName(), "appInstanceName")
                    .toLowerCase();

            checkMissingField(rePurposeAppParams.getOriginAppInstanceId(), "originAppInstanceId");
            checkMissingField(rePurposeAppParams.getPurpose(), "purpose");
            checkMissingField(rePurposeAppParams.getCopyId(), "copyId");

            UUID appId = getHubApi().repurposeApp(
                    new RepurposeAppCommandArgs(
                            appInstanceName,
                            rePurposeAppParams.getOriginAppInstanceId(),
                            rePurposeAppParams.getCopyId(),
                            userContextService.getUser().getId(),
                            rePurposeAppParams.getPurpose(), Collections.emptyList()));
            servletRequest.setAttribute(NAZGUL_LOCATION_HEADER_FILTER, "app-instance/" + appId);
            return appId;
        });
    }

    @Override
    public UUID deploySavedImage(UIDeploySavedImageCommandArgs deploySavedImageCommandArgs) {
        return invokeUICommand("Deploy saved image", () -> {

            //Input validation
            final String appInstanceName =
                    checkMissingField(deploySavedImageCommandArgs.getAppInstanceName(), "appInstanceName")
                    .toLowerCase();
            checkMissingField(deploySavedImageCommandArgs.getSavedImageId(), "savedImageId");
            checkMissingField(deploySavedImageCommandArgs.getDeploymentPlan(), "deploymentPlan");
            checkMissingField(deploySavedImageCommandArgs.getSiteId(), "siteId");

            UUID appId = getHubApi().deploySavedImage(
                    new DeploySavedImageCommandArgs(
                            appInstanceName,
                            deploySavedImageCommandArgs.getSavedImageId(),
                            userContextService.getUser().getId(),
                            "test-dev",
                            deploySavedImageCommandArgs.getSiteId(),
                            convertDeploymentPlanDTO(deploySavedImageCommandArgs.getDeploymentPlan())));
            servletRequest.setAttribute(NAZGUL_LOCATION_HEADER_FILTER, "app-instance/" + appId);
            return appId;
        });
    }

    @Override
    public UUID deployTestDevAppCommand(UICommandDeployTestDevAppArgs deployAppTestDevParams) {
        return invokeUICommand("Deploy TestDev application", () -> {
            final String appInstanceName =
                    checkMissingField(deployAppTestDevParams.getAppInstanceName(), "appInstanceName")
                            .toLowerCase();

            checkMissingField(deployAppTestDevParams.getAppTemplateId(), "appTemplateId");
            checkMissingField(deployAppTestDevParams.getSiteId(), "siteId");
            checkMissingField(deployAppTestDevParams.getDeploymentPlan(), "deploymentPlan");

            DeployAppCommandArgs.AppTemplateDeploymentPlanDTO deploymentPlan =
                    convertDeploymentPlanDTO(deployAppTestDevParams.getDeploymentPlan());

            UUID appId = getHubApi().deployApp(
                    new DeployAppCommandArgs(
                            appInstanceName,
                            deployAppTestDevParams.getAppTemplateId(),
                            deployAppTestDevParams.getSiteId(),
                            userContextService.getUser().getId(),
                            "test-dev",
                            Collections.emptyList(),
                            deploymentPlan));
            servletRequest.setAttribute(NAZGUL_LOCATION_HEADER_FILTER, "app-instance/" + appId);
            return appId;
        });
    }

    private DeployAppCommandArgs.AppTemplateDeploymentPlanDTO convertDeploymentPlanDTO(
            UIAppTemplateDeploymentPlan deploymentPlan) {
        return new DeployAppCommandArgs.AppTemplateDeploymentPlanDTO(
                deploymentPlan.getAppServices()
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                o -> new DeployAppCommandArgs.AppServiceDeploymentPlanDTO(
                                        o.getValue().isEnabled(),
                                        checkMissingField(
                                                o.getValue().getSpace(),
                                                "space"),
                                        checkMissingField(
                                                o.getValue().getArtifactRegistryName(),
                                                "artifactRegistryName"),
                                        checkMissingField(
                                                o.getValue().getImageVersion(),
                                                "imageVersion")))),
                deploymentPlan.getDataServices()
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                o -> new DeployAppCommandArgs.DataServiceDeploymentPlanDTO(
                                        checkMissingField(o.getValue().getDsbURN(), "dsbURN"),
                                        checkMissingField(o.getValue().getDsbPlan(), "dsbPlan"),
                                        checkMissingField(o.getValue().getDsbProtocol(), "dsbProtocol"),
                                        checkMissingField(o.getValue().isEnabled(), "enabled"),
                                        Collections.emptyMap())
                        ))
        );
    }

    @Override
    public void createAppCopy(UICommandCreateAppCopy createAppCopyParams) {
        invokeUICommand("Create Copy", () ->
                getHubApi().createAppCopy(
                        new CreateAppCopyCommandArgs(
                                checkMissingField(createAppCopyParams.getAppInstanceId(), "appInstanceId"))));
    }

    @Override
    public UUID createAppTemplate(UICommandCreateAppTemplate body) {
        return invokeUICommand("Create App Template", () -> {

            UUID appTemplateId = getHubApi().createApplicationTemplate(
                    new ApplicationTemplateDTO(
                            null,
                            checkMissingField(body.getName(), "name"),
                            checkMissingField(body.getVersion(), "version"),
                            checkMissingField(body.getDescription(), "description"),
                            checkMissingField(body.getAppServiceTemplates(), "AppServiceTemplates")
                                    .stream()
                                    .map(ast -> new ApplicationServiceTemplateDTO(
                                            ast.getAppServiceName(),
                                            ast.getPsbType(),
                                            ast.getImageName(),
                                            ast.getImageType(),
                                            ast.getImageVersion(),
                                            ast.getPsbSettings(),
                                            ast.getEnvironmentVariables(),
                                            ast.getDependencies()
                                                    .stream()
                                                    .map(d -> new AppServiceExternalDependencyDTO(
                                                            convertDSTypeFromUI(d.getType()),
                                                            d.getName(),
                                                            d.getProtocols()
                                                                    .stream()
                                                                    .map(p -> new AppServiceExternalDependencyDTO
                                                                            .AppServiceExternalDependencyProtocolDTO(
                                                                            p.getProtocolName(),
                                                                            p.getVersion(),
                                                                            p.getConditions(),
                                                                            p.getSettings()))
                                                                    .collect(Collectors.toList()),
                                                            d.getDescription()
                                                    ))
                                                    .collect(Collectors.toList()),
                                            ast.getExposedPorts(),
                                            ast.getHttpPort(),
                                            ast.getEntryPointUrl()

                                    ))
                                    .collect(Collectors.toList()),
                            body.getEntryPointServiceName(),
                            userContextService.getUser().getId()))
                    .getId();

            servletRequest.setAttribute(
                    NAZGUL_LOCATION_HEADER_FILTER,
                    "test-dev/saved-app-images/" + appTemplateId);
            return appTemplateId;
        });

    }

    @Override
    public UUID createSavedImage(UICommandCreateSavedImage createSavedImageParams) {

        return invokeUICommand("Create Saved Image", () -> {

            UUID imageId = getHubApi().createSavedImage(
                    new CreateSavedImageCommandArgs(
                            checkMissingField(createSavedImageParams.getName(), "name"),
                            checkMissingField(createSavedImageParams.getAppInstanceId(), "appInstanceId"),
                            userContextService.getUser().getId(),
                            createSavedImageParams.getTags(),
                            createSavedImageParams.getComment()));
            servletRequest.setAttribute(
                    NAZGUL_LOCATION_HEADER_FILTER,
                    "test-dev/saved-app-images/" + imageId);
            return imageId;
        });
    }

    private void invokeUICommand(String commandName, Runnable r) {
        invokeUICommand(commandName, () -> {
            r.run();
            return null;
        });
    }

    private <T> T invokeUICommand(String commandName, Supplier<T> r) {
        log.info("Executing command {}", commandName);
        return HubWebAppUtil.wrap(
                commandName,
                r);
    }

    @Override
    public void stopApp(UIStopAppCommandArgs stopAppCommandArgs) {
        invokeUICommand("Stop Application", () -> {
            checkMissingField(stopAppCommandArgs.getAppInstanceId(), "appInstanceId");
            getHubApi().stopApp(new StopAppCommandArgs(
                    stopAppCommandArgs.getAppInstanceId(),
                    userContextService.getUser().getId()));
        });
    }

    @Override
    public void addMavenArtifactRegistry(UICommandAddMavenArtifactRegistry args) {
        invokeUICommand("Add Maven Artifact Registry", () ->
                getSiteApi(getSite(checkMissingField(args.getSiteId(), "siteId")).getUrl())
                        .addMavenArtifactRegistry(
                                new AddMavenArtifactRegistryToSiteCommandArgs(
                                        checkMissingField(args.getName(), "name"),
                                        checkMissingField(args.getUrl(), "url"),
                                        args.getUsername(),
                                        args.getPassword()
                                )));
    }

    @Override
    public void addDockerArtifactRegistry(final UICommandAddDockerArtifactRegistry args) {
        invokeUICommand("Add Docker Artifact Registry", () ->
                getSiteApi(getSite(checkMissingField(args.getSiteId(), "siteId")).getUrl())
                        .addDockerArtifactRegistry(
                                new AddDockerArtifactRegistryToSiteCommandArgs(
                                        checkMissingField(args.getName(), "name"),
                                        checkMissingField(args.getUrl(), "url"),
                                        args.getUsername(),
                                        args.getPassword()
                                )));
    }

    @Override
    public void addCrb(UICommandAddCrb body) {
        invokeUICommand("Add CRB to Site", () ->
                getSiteApi(getSite(checkMissingField(body.getSiteId(), "siteId")).getUrl()).registerCrb(
                        new RegisterCrbToSiteCommandArgs(
                                checkMissingField(body.getCrbUrn(), "crbUrn"),
                                checkMissingField(body.getCrbUrl(), "crbUrl")
                        )
                ));
    }

    @Override
    public void addCopyRepository(UICommandAddCopyRepository body) {
        invokeUICommand("Add copy repository to CRB", () ->
                getSiteApi(getSite(checkMissingField(body.getSiteId(), "siteId")).getUrl()).addCr(
                        new AddCrToSiteCommandArgs(
                                checkMissingField(body.getCrbUrn(), "crbUrn"),
                                checkMissingField(body.getCrName(), "crName"),
                                checkMissingField(body.getCrProperties(), "crProperties")
                        )
                ));
    }

    @Override
    public void addCustomArtifactRegistry(UICommandAddCustomArtifactRegistry commandArgs) {
        invokeUICommand("Add Custom Artifact Registry", () -> {
            checkMissingField(commandArgs.getName(), "name");
            checkMissingField(commandArgs.getUrl(), "url");
            final UUID siteId = checkMissingField(commandArgs.getSiteId(), "siteId");
            SiteDto siteDto = getSite(siteId);
            getSiteApi(siteDto.getUrl())
                    .addCustomRestArtifactRegistry(
                            new AddCustomArtifactRegistryToSiteCommandArgs(
                                    commandArgs.getName(),
                                    commandArgs.getUrl()
                            ));
        });
    }

    @Override
    public void removeArtifactRegistry(UICommandRemoveArtifactRegistry args) {
        invokeUICommand("Remove Artifact Registry", () -> {
            checkMissingField(args.getName(), "name");
            final UUID siteId = checkMissingField(args.getSiteId(), "siteId");
            final SiteDto site = getSite(siteId);

            getSiteApi(site.getUrl()).removeArtifactRegistry(
                    new RemoveArtifactRegistryFromSiteCommandArgs(args.getName()));
        });
    }

    @Override
    public void removeCrb(UICommandRemoveCrb body) {
        invokeUICommand("Remove CRB", () ->
                getSiteApi(getSite(checkMissingField(body.getSiteId(), "siteId")).getUrl()).removeCrb(
                        new RemoveCrbFromSiteCommandArgs(
                                checkMissingField(body.getCrbUrn(), "crbUrn")
                        )
                ));
    }

    @Override
    public void addJiraIntegration(UICommandAddJiraIntegration addJiraIntegration) {

        invokeUICommand("Add Jira Integration", () -> {

            checkMissingField(addJiraIntegration.getUrl(), "url");
            checkMissingField(addJiraIntegration.getProjectId(), "projectId");
            checkMissingField(addJiraIntegration.getIssueTypeId(), "issueTypeId");

            IntegrationsUtil.addJiraIntegration(this::getHubApi, addJiraIntegration);
        });
    }

    @Override
    public void addPivotalTrackerIntegration(UICommandAddPivotalTrackerIntegration addPivotalTrackerIntegration) {

        invokeUICommand("Add Pivotal Tracker Integration", () -> {

            checkMissingField(addPivotalTrackerIntegration.getUrl(), "url");
            checkMissingField(addPivotalTrackerIntegration.getProjectId(), "projectId");
            checkMissingField(addPivotalTrackerIntegration.getIssueTypeId(), "issueTypeId");
            checkMissingField(addPivotalTrackerIntegration.getToken(), "token");

            IntegrationsUtil.addPivotalTrackerIntegration(this::getHubApi, addPivotalTrackerIntegration);
        });
    }

    @Override
    public UISiteDsb addDsb(UICommandAddDsb body) {
        return invokeUICommand(
                "Add DSB to Site", () -> convertDsb(
                        getSiteApi(getSite(checkMissingField(body.getSiteId(), "siteId")).getUrl())
                                .registerDsb(
                                        new RegisterDsbToSiteCommandArgs(
                                                checkMissingField(body.getDsbUrn(), "dsbUrn"),
                                                checkMissingField(body.getDsbUrl(), "dsbUrl")
                                        )
                                )));
    }

    @Override
    public void addPsb(UICommandAddPsb body) {
        invokeUICommand(
                "Add PSB to Site", () ->
                        getSiteApi(getSite(checkMissingField(body.getSiteId(), "siteId")).getUrl())
                                .registerPsb(
                                        new RegisterPsbToSiteCommandArgs(
                                                checkMissingField(body.getPsbUrn(), "psbUrn"),
                                                checkMissingField(body.getPsbUrl(), "psbUrl")
                                        )
                                ));
    }

    @Override
    public UISite addSite(UICommandAddSite body) {
        return invokeUICommand("Add Site to Hub", () -> {
                    final String siteId = getHubApi().addSite(new AddSiteToHubCommandArgs(
                            checkMissingField(body.getUrn(), "urn"),
                            checkMissingField(body.getUrl(), "url")
                    ));

                    return convertUISite(getSite(UUID.fromString(JsonUtil.fromJson(String.class, siteId))));
                }
        );
    }

    @Override
    public void removeDsb(UICommandRemoveDsb body) {
        invokeUICommand("Remove DSB", () ->
                getSiteApi(getSite(checkMissingField(body.getSiteId(), "siteId")).getUrl()).removeDsb(
                        new RemoveDsbFromSiteCommandArgs(
                                checkMissingField(body.getDsbUrn(), "dsbUrn")
                        )
                ));
    }

    private HubWebApi getHubApi() {
        return hubServiceDependency.getWebAPI(HubWebApi.class);
    }

    @Override
    public List<UIIntegrationDetails> listIntegrations() {
        return HubWebAppUtil.wrapMandatory(
                "fetching integrations list",
                () -> IntegrationsUtil.listIntegrations(this::getHubApi)
        );
    }
}
