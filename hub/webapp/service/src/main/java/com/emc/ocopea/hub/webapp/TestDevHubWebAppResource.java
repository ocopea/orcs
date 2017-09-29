// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.webapp;

import com.emc.microservice.Context;
import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.dependency.ManagedDependency;
import com.emc.microservice.discovery.ServiceDiscoveryManager;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.microservice.webclient.WebApiResolverBuilder;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.hub.AppServiceExternalDependencyProtocol;
import com.emc.ocopea.hub.DsbResolver;
import com.emc.ocopea.hub.HubWebAppUtil;
import com.emc.ocopea.hub.application.AppServiceExternalDependencyDTO;
import com.emc.ocopea.hub.application.ApplicationTemplateDTO;
import com.emc.ocopea.hub.application.HubAppInstanceConfigurationDTO;
import com.emc.ocopea.hub.application.HubAppInstanceWithStateDTO;
import com.emc.ocopea.hub.application.HubWebApi;
import com.emc.ocopea.hub.site.SiteDto;
import com.emc.ocopea.hub.testdev.SavedImageDTO;
import com.emc.ocopea.site.AppInstanceCopyStatisticsDTO;
import com.emc.ocopea.site.AppInstanceStatisticsDTO;
import com.emc.ocopea.site.DsbCatalogWebApi;
import com.emc.ocopea.site.ServiceInstanceInfo;
import com.emc.ocopea.site.SiteArtifactRegistryInfoDTO;
import com.emc.ocopea.site.SiteWebApi;
import com.emc.ocopea.site.SupportedServiceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by liebea on 5/10/16.
 * Drink responsibly
 */
public class TestDevHubWebAppResource implements HubWebAppTestDevWebApi {
    private static final Logger log = LoggerFactory.getLogger(TestDevHubWebAppResource.class);
    private ManagedDependency hubServiceDependency;
    private AppTemplateCache appTemplateCache;
    private ServiceDiscoveryManager serviceDiscoveryManager;
    private WebAPIResolver webAPIResolver;
    private JiraQueryUtil jiraQueryUtil;
    private PivotalTrackerUtil pivotalTrackerUtil;

    private static Random random = new Random();

    @javax.ws.rs.core.Context
    private UriInfo uriInfo;

    @NoJavadoc
    @javax.ws.rs.core.Context
    public void setApplication(Application application) {
        Context context = ((MicroServiceApplication) application).getMicroServiceContext();
        jiraQueryUtil = new JiraQueryUtil(context.getWebAPIResolver()
                .buildResolver(new WebApiResolverBuilder().withVerifySsl(false)));
        pivotalTrackerUtil = new PivotalTrackerUtil(context.getWebAPIResolver()
                .buildResolver(new WebApiResolverBuilder().withVerifySsl(false)));

        hubServiceDependency = context.getDependencyManager().getManagedResourceByName("hub");
        appTemplateCache = context.getSingletonManager()
                .getManagedResourceByName(AppTemplateCache.class.getSimpleName()).getInstance();
        serviceDiscoveryManager = context.getServiceDiscoveryManager();
        this.webAPIResolver = context.getWebAPIResolver();

    }

    @Override
    public List<UISavedAppImage> getSavedIAppImages(@QueryParam("appTemplateId") UUID appTemplateId) {
        return hubServiceDependency.getWebAPI(HubWebApi.class).listSavedImages(appTemplateId)
                .stream()
                .map(this::convertSavedImages)
                .collect(Collectors.toList());
    }

    @Override
    public UISavedAppImage getSavedIAppImage(@PathParam("savedImageId") UUID savedImageId) {
        final SavedImageDTO savedImage = hubServiceDependency.getWebAPI(HubWebApi.class).getSavedImage(savedImageId);
        if (savedImage == null) {
            throw new NotFoundException("Saved image with id " + savedImageId + " not found");
        }
        return convertSavedImages(savedImage);
    }

    @Override
    public UISavedAppImageDetailed getSavedIAppImageDetailed(@PathParam("savedImageId") UUID savedImageId) {
        final SavedImageDTO savedImage = hubServiceDependency.getWebAPI(HubWebApi.class).getSavedImage(savedImageId);
        if (savedImage == null) {
            throw new NotFoundException("Saved image with id " + savedImageId + " not found");
        }
        return convertSavedImagesDetailed(savedImage);
    }

    private UISavedAppImageDetailed convertSavedImagesDetailed(SavedImageDTO savedImageDTO) {
        final SiteDto site = getSite(savedImageDTO.getSiteId());
        final AppInstanceCopyStatisticsDTO copyMetadata = getCopyMetadata(savedImageDTO, site);
        final Map<String, String> protocolByServiceName =
                HubWebAppResource.getProtocolByServiceName(
                        appTemplateCache,
                        savedImageDTO.getAppTemplateId());

        // todo: how do we calculate this?
        final long restoreTimeInSeconds = 500L;

        final String rawPath = uriInfo.getBaseUri().getRawPath();
        return new UISavedAppImageDetailed(
                savedImageDTO.getId(),
                savedImageDTO.getAppTemplateId(),
                savedImageDTO.getName(),
                savedImageDTO.getBaseImageId(),
                savedImageDTO.getCreatedDate(),
                savedImageDTO.getComment(),
                savedImageDTO.getTags(),
                savedImageDTO.getUserId(),
                savedImageDTO.getState(),
                copyMetadata.getDataServiceCopies()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        AppInstanceStatisticsDTO.DataServiceCopyStatisticsDTO::getBindName,
                                        t -> {
                                            String iconUrl = null;
                                            final String protocolByService =
                                                    protocolByServiceName.get(t.getBindName());
                                            if (protocolByService != null) {
                                                iconUrl = rawPath + "images/dsb-protocol/" + protocolByService;
                                            }
                                            return new UIServiceCopyDetails(
                                                    t.getCopyRepositoryName(),
                                                    t.getSize(),
                                                    restoreTimeInSeconds,
                                                    t.getDsbUrn(),
                                                    t.getFacility(),
                                                    iconUrl);
                                        })),
                getAppServiceCopies(savedImageDTO, copyMetadata));
    }

    private Map<String, UIAppServiceCopyDetails> getAppServiceCopies(
            SavedImageDTO savedImageDTO,
            AppInstanceCopyStatisticsDTO copyMetadata) {
        final ApplicationTemplateDTO appTemplateById =
                appTemplateCache.getAppTemplateById(savedImageDTO.getAppTemplateId(), true);
        final String rawPath = uriInfo.getBaseUri().getRawPath();

        return copyMetadata.getAppServiceCopies()
                .stream()
                .collect(Collectors.toMap(
                        AppInstanceStatisticsDTO.AppServiceCopyStatisticsDTO::getAppServiceName,
                        o -> new UIAppServiceCopyDetails(
                                o.getAppServiceName(),
                                o.getAppImageVersion(),
                                rawPath + "images/image-type/" + o.getAppimageType(),
                                appTemplateById.getAppServiceTemplates()
                                        .stream()
                                        .filter(
                                                applicationServiceTemplateDTO ->
                                                        applicationServiceTemplateDTO.getAppServiceName()
                                                                .equals(o.getAppServiceName()))
                                        .flatMap(applicationServiceTemplateDTO ->
                                                applicationServiceTemplateDTO.getDependencies()
                                                        .stream()
                                                        .map(AppServiceExternalDependencyDTO::getName))
                                        .collect(Collectors.toList())))
                );
    }

    private AppInstanceCopyStatisticsDTO getCopyMetadata(SavedImageDTO savedImageDTO, SiteDto site) {
        return HubWebAppUtil.wrap(
                "locating copy metadata " + savedImageDTO.getAppCopyId(),
                () -> getSiteWebAPI(site.getUrl()).getCopyMetadata(savedImageDTO.getAppCopyId()));
    }

    private SiteWebApi getSiteWebAPI(String siteUrl) {
        return webAPIResolver.getWebAPI(siteUrl, SiteWebApi.class);
    }

    private Collection<SiteDto> listSites() {
        return HubWebAppUtil.wrap(
                "listing sites",
                hubServiceDependency.getWebAPI(HubWebApi.class)::listSites);
    }

    private SiteDto getSite(UUID siteId) {
        return HubWebAppUtil.wrapMandatory(
                "Failed locating site " + siteId,
                () -> hubServiceDependency.getWebAPI(HubWebApi.class).getSite(siteId));
    }

    private UISavedAppImage convertSavedImages(SavedImageDTO savedImageDTO) {
        return new UISavedAppImage(
                savedImageDTO.getId(),
                savedImageDTO.getAppTemplateId(),
                savedImageDTO.getName(),
                savedImageDTO.getBaseImageId(),
                savedImageDTO.getCreatedDate(),
                savedImageDTO.getComment(),
                savedImageDTO.getTags(),
                savedImageDTO.getUserId(),
                savedImageDTO.getState());
    }

    @Override
    public List<UITestDevAppInstance> getTestDevInstances() {
        //todo:make fetch pass type parameter or use a different method so we won't have to filter...
        return
                hubServiceDependency.getWebAPI(HubWebApi.class).listAppInstanceStates()
                        .stream()
                        .filter(i -> !"production".equals(i.getDeploymentType()))
                        .filter(i -> !Objects.equals(i.getState(), "stopped"))
                        .map(this::convert)
                        .collect(Collectors.toList());
    }

    private UITestDevAppInstance convert(HubAppInstanceWithStateDTO currInstance) {
        final ApplicationTemplateDTO appTemplate =
                appTemplateCache.getAppTemplateById(currInstance.getAppTemplateId(), true);

        //todo:use real data :)
        final Map<String, Integer> dsbQuota = new HashMap<>();
        //dsbQuota.put("mongo", random.nextInt(59));
        //dsbQuota.put("postgres", random.nextInt(22));
        //dsbQuota.put("rabbitmq", random.nextInt(13));

        appTemplate.getAppServiceTemplates()
                .stream()
                .flatMap(applicationServiceTemplateDTO -> applicationServiceTemplateDTO.getDependencies().stream())
                .distinct()
                .flatMap(dep -> dep.getProtocols().stream())
                .distinct()
                .forEach(p ->
                        dsbQuota.put(p.getProtocolName(), random.nextInt(22)));

        return new UITestDevAppInstance(
                currInstance.getId(),
                currInstance.getName(),
                currInstance.getAppTemplateId(),
                appTemplate.getName(),
                currInstance.getCreatorUserId(),
                currInstance.getWebEntryPointURL(),
                currInstance.getState(),
                currInstance.getStateMessage(),
                currInstance.getDeploymentType(),
                currInstance.getCreated(),
                new UITestDevQuota("111", random.nextInt(50), dsbQuota),
                appTemplate.getAppServiceTemplates().size(),
                dsbQuota.size());
    }

    @Override
    public UITestDevQuota getOrgQuota(@PathParam("orgId") String orgId) {
        //todo: we choose randomly first site here, need to do per-site
        final Collection<SiteDto> sites = listSites();
        if (sites.isEmpty()) {
            return new UITestDevQuota(orgId, 0, Collections.emptyMap());
        }
        SiteDto site = sites.iterator().next();

        // Getting lists of apps
        // todo: come on get real
        int appPct = getSiteWebAPI(site.getUrl()).listAppInstanceInfo().size();

        //todo: get real data
        final Map<String, Integer> dsbQuota = new HashMap<>();

        for (ServiceInstanceInfo currDSInstance : getDsbInstances(site)) {
            final Integer currQuota = dsbQuota.get(currDSInstance.getDsb());
            if (currQuota == null) {
                dsbQuota.put(currDSInstance.getDsb(), 1);
            } else {
                dsbQuota.put(currDSInstance.getDsb(), currQuota + 1);
            }
        }
        return new UITestDevQuota(orgId, appPct, dsbQuota);
    }

    private Collection<ServiceInstanceInfo> getDsbInstances(SiteDto currSite) {
        return HubWebAppUtil.wrap(
                "fetching data service catalog for site " + currSite.getUrn(),
                () -> webAPIResolver.getWebAPI(currSite.getUrl(), DsbCatalogWebApi.class).getInstancesByDsb());
    }

    @Override
    public UIAppTemplateConfigurationForSite getAppTemplateConfigurationBySite(
            @PathParam("siteId") UUID siteId,
            @PathParam("appTemplateId") UUID appTemplateId) {

        /*
        return new UIAppTemplateConfigurationForSite(
                Collections.singletonList(new UIAppServiceConfiguration
                        ("wordpress", MapBuilder.<String, List<String>>newHashMap().with("shpanRegistry", Collections
                                .singletonList("1.0")).build())),

                Arrays.asList(
                        new UIDataServiceConfiguration(
                                "configuration",
                                MapBuilder.<String, UIDSBConfiguration>newHashMap()
                                        .with(
                                                "rds",
                                                new UIDSBConfiguration(
                                                        "MySQL RDS",
                                                        "MySQL RDS",
                                                        Arrays.asList(
                                                                new UIDSBPlanConfiguration(
                                                                        "commynity",
                                                                        "commynity",
                                                                        "mysql community Edition",
                                                                        Collections.singletonList("mysql")),
                                                                new UIDSBPlanConfiguration(
                                                                        "aurora",
                                                                        "aurora",
                                                                        "Aurora Mysql Plan",
                                                                        Collections.singletonList("mysql"))
                                                        )
                                                )).build()),
                        new UIDataServiceConfiguration(
                                "documents",
                                MapBuilder.<String, UIDSBConfiguration>newHashMap()
                                        .with(
                                                "EBS",
                                                new UIDSBConfiguration(
                                                        "EBS",
                                                        "EBS",
                                                        Arrays.asList(
                                                                new UIDSBPlanConfiguration(
                                                                        "ebs1",
                                                                        "ebs1",
                                                                        "ebs1",
                                                                        Collections.singletonList("docker-volume")),
                                                                new UIDSBPlanConfiguration(
                                                                        "ebs1",
                                                                        "ebs1",
                                                                        "ebs1",
                                                                        Collections
                                                                                .singletonList("docker-volume"))
                                                        )
                                                )).build())
                        ));
        */

        final SiteDto site = getSite(siteId);
        final ApplicationTemplateDTO appTemplateById = appTemplateCache.getAppTemplateById(appTemplateId, true);

        DsbResolver dsbResolver = new DsbResolver(
                () -> webAPIResolver.getWebAPI(site.getUrl(), DsbCatalogWebApi.class).getCatalog(),
                site.getName());

        Map<String, Collection<DsbResolver.ProtocolDsbMatch>> dsbPlansByDataServiceName = new HashMap<>();
        appTemplateById.getAppServiceTemplates().forEach(ast -> ast.getDependencies().forEach(dep -> {
            dsbPlansByDataServiceName.computeIfAbsent(
                    dep.getName(),
                    depName -> dsbResolver.listDSBs(
                            dep.getProtocols().stream().map(p ->
                                    new AppServiceExternalDependencyProtocol(
                                            p.getProtocolName(),
                                            p.getVersion(),
                                            p.getConditions(),
                                            p.getSettings()))
                                    .collect(Collectors.toList())));

        }));

        final Collection<SiteArtifactRegistryInfoDTO> artifactRegistries = listSiteArtifactRegistries(site.getUrl());

        final List<UIAppServiceConfiguration> appServices = appTemplateById.getAppServiceTemplates().stream().map(
                ast -> {
                    final Map<String, List<String>> supportedVersions = new HashMap<>();
                    for (SiteArtifactRegistryInfoDTO currAR : artifactRegistries) {
                        try {
                            final List<String> versions =
                                    new ArrayList<>(getSiteWebAPI(site.getUrl())
                                            .listArtifactVersions(currAR.getName(), ast.getImageName()));
                            if (!versions.isEmpty()) {
                                supportedVersions.put(currAR.getName(), versions);
                            }
                        } catch (Exception ex) {
                            log.debug("failed loading artifact " + ast.getImageName()
                                    + " from registry " + currAR.getName(), ex);
                        }
                    }

                    return new UIAppServiceConfiguration(
                            ast.getAppServiceName(),
                            supportedVersions);
                }).collect(Collectors.toList());
        return new UIAppTemplateConfigurationForSite(
                appServices,
                dsbPlansByDataServiceName.entrySet()
                        .stream()
                        .map(e -> new UIDataServiceConfiguration(
                                e.getKey(),
                                e.getValue()
                                        .stream()
                                        .collect(Collectors.groupingBy(m -> m.getService().getUrn()))
                                        .entrySet()
                                        .stream()
                                        .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                entry -> {
                                                    final SupportedServiceDto service = entry.getValue().iterator()
                                                            .next().getService();
                                                    return new UIDSBConfiguration(
                                                            service.getName(),
                                                            service.getDescription(),
                                                            entry.getValue().stream()
                                                                    .map(match -> new UIDSBPlanConfiguration(
                                                                            match.getPlan().getId(),
                                                                            match.getPlan().getName(),
                                                                            match.getPlan().getDescription(),
                                                                            match.getPlan().getSupportedProtocols()
                                                                                    .stream()
                                                                                    .map(SupportedServiceDto
                                                                                            .SupportedServiceProtocolDto
                                                                                            ::getProtocolName)
                                                                                    .collect(Collectors.toList()))
                                                                    ).collect(Collectors.toList()));
                                                }))
                        )).collect(Collectors.toList()));
    }

    public Collection<SiteArtifactRegistryInfoDTO> listSiteArtifactRegistries(String siteUrl) {
        return getSiteWebAPI(siteUrl).listArtifactRegistries();
    }

    @Override
    public UITestDevDashboardData getAppInstanceDashboardData(@PathParam("appInstanceId") UUID appInstanceId) {

        final HubAppInstanceConfigurationDTO appInstance = getHubAppInstance(appInstanceId);
        UUID baseSavedImageId = appInstance.getBaseSavedImageId();
        final List<UISavedAppImage> imageHistory = new ArrayList<>();

        //todo:perf change this to do a single query rather then iterative!
        while (baseSavedImageId != null) {
            final UISavedAppImage savedIAppImage = getSavedIAppImage(baseSavedImageId);
            imageHistory.add(savedIAppImage);
            baseSavedImageId = savedIAppImage.getBaseImageId();
        }
        return new UITestDevDashboardData(imageHistory);
    }

    @Override
    public UIJiraLoginResponse queryJira(UIJiraLoginArgs body) {
        return jiraQueryUtil.queryJira(body.getJiraUrl(), body.getUsername(), body.getPassword());
    }

    private <T> T checkMissingField(T o, String name) {
        if (o == null) {
            throw new BadRequestException("missing " + name);
        }
        return o;
    }

    @Override
    public String pivotalTrackerAdd(UIPivotalTrackerAdd body) {

        List<UIIntegrationDetails> integrations =
                IntegrationsUtil.listIntegrations(() -> hubServiceDependency.getWebAPI(HubWebApi.class));

        UIIntegrationDetails pivotalTrackerIntegration = integrations
                .stream()
                .filter(integration -> integration.getIntegrationName().equals("pivotal-tracker"))
                .findFirst()
                .get();

        checkMissingField(body.getName(), "name");
        checkMissingField(body.getDescription(), "description");

        return pivotalTrackerUtil.pivotalTrackerAdd(
                pivotalTrackerIntegration.getConnectionDetails().get("url"),
                pivotalTrackerIntegration.getConnectionDetails().get("projectId"),
                pivotalTrackerIntegration.getConnectionDetails().get("token"),
                body.getName(),
                pivotalTrackerIntegration.getConnectionDetails().get("issueTypeId"),
                body.getDescription());
    }

    private HubAppInstanceConfigurationDTO getHubAppInstance(@PathParam("appInstanceId") UUID appInstanceId) {
        return HubWebAppUtil.wrap(
                "loading app instance with id " + appInstanceId,
                () -> hubServiceDependency.getWebAPI(HubWebApi.class).getAppInstance(appInstanceId));
    }
}
