// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import com.emc.microservice.Context;
import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.ParametersBag;
import com.emc.microservice.dependency.ManagedDependency;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.crb.CrbWebDataApi;
import com.emc.ocopea.psb.PSBSpaceDTO;
import com.emc.ocopea.psb.PSBWebAPI;
import com.emc.ocopea.site.app.DeployedAppService;
import com.emc.ocopea.site.app.DeployedApplication;
import com.emc.ocopea.site.app.DeployedApplicationCreatorService;
import com.emc.ocopea.site.app.DeployedApplicationEventRepository;
import com.emc.ocopea.site.app.DeployedApplicationLoader;
import com.emc.ocopea.site.app.DeployedApplicationPersisterService;
import com.emc.ocopea.site.app.DeployedDataService;
import com.emc.ocopea.site.artifact.ArtifactRegistryFactory;
import com.emc.ocopea.site.artifact.ArtifactRegistryFactoryImpl;
import com.emc.ocopea.site.artifact.SiteArtifactRegistry;
import com.emc.ocopea.site.commands.AddCrToSiteCommand;
import com.emc.ocopea.site.commands.AddCustomArtifactRegistryToSiteCommand;
import com.emc.ocopea.site.commands.AddDockerArtifactRegistryToSiteCommand;
import com.emc.ocopea.site.commands.AddMavenArtifactRegistryToSiteCommand;
import com.emc.ocopea.site.commands.CreateAppCopyCommand;
import com.emc.ocopea.site.commands.DeployApplicationOnSiteCommand;
import com.emc.ocopea.site.commands.RegisterCrbToSiteCommand;
import com.emc.ocopea.site.commands.RegisterDsbToSiteCommand;
import com.emc.ocopea.site.commands.RegisterPsbToSiteCommand;
import com.emc.ocopea.site.commands.RemoveArtifactRegistryFromSiteCommand;
import com.emc.ocopea.site.commands.RemoveCrbFromSiteCommand;
import com.emc.ocopea.site.commands.RemoveDsbFromSiteCommand;
import com.emc.ocopea.site.commands.StopAppOnSiteCommand;
import com.emc.ocopea.site.copy.CopyRepository;
import com.emc.ocopea.site.crb.CrbNegotiationResult;
import com.emc.ocopea.site.crb.CrbUtil;
import com.emc.ocopea.util.io.StreamUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SiteResource implements SiteWebApi {
    private static final Logger log = LoggerFactory.getLogger(SiteResource.class);
    private SiteRepository siteRepository;
    private ParametersBag serviceParams;
    private int serviceVersion;
    private DeployedApplicationLoader deployedApplicationLoader;
    private RegisterDsbToSiteCommand registerDsbToSiteCommand;
    private AddCrToSiteCommand addCrToSiteCommand;
    private RegisterCrbToSiteCommand registerCrbToSiteCommand;
    private RegisterPsbToSiteCommand registerPsbToSiteCommand;
    private AddCustomArtifactRegistryToSiteCommand addCustomArtifactRegistryToSiteCommand;
    private AddMavenArtifactRegistryToSiteCommand addMavenArtifactRegistryToSiteCommand;
    private AddDockerArtifactRegistryToSiteCommand addDockerArtifactRegistryToSiteCommand;
    private RemoveArtifactRegistryFromSiteCommand removeArtifactRegistryFromSiteCommand;
    private RemoveDsbFromSiteCommand removeDsbFromSiteCommand;
    private RemoveCrbFromSiteCommand removeCrbFromSiteCommand;
    private CreateAppCopyCommand createAppCopyCommand;
    private DeployApplicationOnSiteCommand deployApplicationOnSiteCommand;
    private StopAppOnSiteCommand stopAppOnSiteCommand;
    private AppInstanceStatisticsCalculator appInstanceStatisticsCalculator;

    private WebAPIResolver webAPIResolver;
    @javax.ws.rs.core.Context
    private UriInfo uriInfo;

    /***
     * Standard setApplication method.
     * @param application application object injected
     */
    @javax.ws.rs.core.Context
    public void setApplication(Application application) {
        Context context = ((MicroServiceApplication) application).getMicroServiceContext();
        webAPIResolver = context.getWebAPIResolver();
        siteRepository = context.getSingletonManager().getManagedResourceByName("site-singleton").getInstance();
        deployedApplicationLoader = new DeployedApplicationLoader(
                context.getDynamicJavaServicesManager().getManagedResourceByName(
                        DeployedApplicationEventRepository.class.getSimpleName()).getInstance());

        serviceParams = context.getParametersBag();
        serviceVersion = context.getServiceDescriptor().getVersion();
        registerDsbToSiteCommand = new RegisterDsbToSiteCommand(siteRepository, context.getWebAPIResolver());
        removeCrbFromSiteCommand = new RemoveCrbFromSiteCommand(siteRepository);
        addCrToSiteCommand = new AddCrToSiteCommand(siteRepository, webAPIResolver);
        registerCrbToSiteCommand = new RegisterCrbToSiteCommand(siteRepository, webAPIResolver);
        registerPsbToSiteCommand = new RegisterPsbToSiteCommand(siteRepository, context.getWebAPIResolver());

        ManagedDependency protectionPolicyDependency =
                context.getDependencyManager().getManagedResourceByName("protection");
        DeployedApplicationEventRepository deployedApplicationEventRepository = context.getDynamicJavaServicesManager()
                .getManagedResourceByName(DeployedApplicationEventRepository.class.getSimpleName()).getInstance();
        final DeployedApplicationPersisterService deployedApplicationPersisterService =
                new DeployedApplicationPersisterService(
                        context.getDestinationManager()
                                .getManagedResourceByName(
                                        "pending-deployed-application-events").getMessageSender());
        deployApplicationOnSiteCommand = new DeployApplicationOnSiteCommand(
                new DeployedApplicationCreatorService(
                        deployedApplicationEventRepository,
                        deployedApplicationPersisterService),
                siteRepository);
        createAppCopyCommand = new CreateAppCopyCommand(protectionPolicyDependency, deployedApplicationEventRepository);
        stopAppOnSiteCommand = new StopAppOnSiteCommand(deployedApplicationLoader, deployedApplicationPersisterService);
        final ArtifactRegistryFactory artifactRegistryFactory = context.getSingletonManager()
                .getManagedResourceByName(ArtifactRegistryFactoryImpl.class.getSimpleName()).getInstance();

        addCustomArtifactRegistryToSiteCommand = new AddCustomArtifactRegistryToSiteCommand(
                siteRepository,
                artifactRegistryFactory,
                context.getWebAPIResolver());

        addMavenArtifactRegistryToSiteCommand = new AddMavenArtifactRegistryToSiteCommand(
                siteRepository,
                artifactRegistryFactory,
                context.getWebAPIResolver());

        addDockerArtifactRegistryToSiteCommand = new AddDockerArtifactRegistryToSiteCommand(
                siteRepository,
                artifactRegistryFactory,
                context.getWebAPIResolver());

        removeArtifactRegistryFromSiteCommand = new RemoveArtifactRegistryFromSiteCommand(siteRepository);
        removeDsbFromSiteCommand = new RemoveDsbFromSiteCommand(siteRepository);

        appInstanceStatisticsCalculator = new AppInstanceStatisticsCalculator(
                protectionPolicyDependency,
                context.getWebAPIResolver(),
                siteRepository);

    }

    @Override
    public SiteInfoDto getSiteInfo() {
        String location = nullIfEmpty(serviceParams.getString("location"));
        SiteLocationDTO siteLocation = null;
        if (location != null) {
            try {
                siteLocation = new ObjectMapper().readValue(location, SiteLocationDTO.class);
            } catch (IOException e) {
                throw new WebApplicationException("Failed parsing site location", e);
            }
        }

        return new SiteInfoDto(
                serviceParams.getString("site-name"),
                Integer.toString(serviceVersion),
                nullIfEmpty(serviceParams.getString("public-load-balancer")),
                siteLocation);
    }

    private String nullIfEmpty(String s) {
        return s == null || s.isEmpty() ? null : s;
    }

    @Override
    public void addCr(AddCrToSiteCommandArgs addCrToSiteCommandArgs) {
        addCrToSiteCommand.execute(addCrToSiteCommandArgs);
    }

    @Override
    public void deployApplicationOnSite(DeployApplicationOnSiteCommandArgs deployAppManifest) {
        deployApplicationOnSiteCommand.execute(deployAppManifest);
    }

    @Override
    public SupportedServiceDto registerDsb(RegisterDsbToSiteCommandArgs args) {
        return DsbCatalogResource.convertSupportedService(registerDsbToSiteCommand.execute(args));
    }

    @Override
    public void registerCrb(RegisterCrbToSiteCommandArgs registerCrbToSiteCommandArgs) {
        registerCrbToSiteCommand.execute(registerCrbToSiteCommandArgs);
    }

    @Override
    public void registerPsb(RegisterPsbToSiteCommandArgs registerPsbToSiteCommandArgs) {
        registerPsbToSiteCommand.execute(registerPsbToSiteCommandArgs);
    }

    @Override
    public void addCustomRestArtifactRegistry(AddCustomArtifactRegistryToSiteCommandArgs args) {
        addCustomArtifactRegistryToSiteCommand.execute(args);
    }

    @Override
    public void addMavenArtifactRegistry(AddMavenArtifactRegistryToSiteCommandArgs args) {
        addMavenArtifactRegistryToSiteCommand.execute(args);
    }

    @Override
    public void addDockerArtifactRegistry(AddDockerArtifactRegistryToSiteCommandArgs args) {
        addDockerArtifactRegistryToSiteCommand.execute(args);
    }

    @Override
    public void removeArtifactRegistry(RemoveArtifactRegistryFromSiteCommandArgs args) {
        removeArtifactRegistryFromSiteCommand.execute(args);
    }

    @Override
    public void removeDsb(RemoveDsbFromSiteCommandArgs removeDsbFromSiteCommandArgs) {
        removeDsbFromSiteCommand.execute(removeDsbFromSiteCommandArgs);
    }

    @Override
    public void removeCrb(RemoveCrbFromSiteCommandArgs removeCrbFromSiteCommandArgs) {
        removeCrbFromSiteCommand.execute(removeCrbFromSiteCommandArgs);
    }

    @Override
    public UUID createAppCopy(CreateAppCopyCommandArgs createAppCopyCommandArgs) {
        return createAppCopyCommand.execute(createAppCopyCommandArgs);
    }

    @Override
    public Collection<AppInstanceInfoDTO> listAppInstanceInfo() {
        return deployedApplicationLoader.list()
                .stream()
                .map(this::convertAppInstanceInfo)
                .collect(Collectors.toList());
    }

    @Override
    public AppInstanceInfoDTO getAppInstanceInfo(@PathParam("appInstanceId") UUID appInstanceId) {
        return convertAppInstanceInfo(loadDeployedApplication(appInstanceId));
    }

    @Override
    public List<ServiceLogsWebSocketDTO> getAppInstanceLogsWebSockets(@PathParam("appInstanceId") UUID appInstanceId) {
        final DeployedApplication deployedApplication = loadDeployedApplication(appInstanceId);

        // Tags are app service names + data service names
        List<String> tags =
                Stream
                        .concat(
                                deployedApplication.getDeployedAppServices().values()
                                        .stream()
                                        .map(DeployedAppService::getAppServiceName),
                                deployedApplication.getDeployedDataServices().values()
                                        .stream()
                                        .map(DeployedDataService::getBindName))
                        .collect(Collectors.toList());

        // This address matches the address in SiteLogsWebSocket class
        String siteLogsWebsocketAddress = uriInfo.getAbsolutePathBuilder().scheme("ws").build().toString();

        return Collections.singletonList(
                new ServiceLogsWebSocketDTO(
                        siteLogsWebsocketAddress,
                        "json",
                        tags
                )
        );
    }

    private DeployedApplication loadDeployedApplication(@PathParam("appInstanceId") UUID appInstanceId) {
        DeployedApplication deployedApplication = deployedApplicationLoader.load(appInstanceId);
        if (deployedApplication == null) {
            throw new NotFoundException("App Instance " + appInstanceId + " not found");
        }
        return deployedApplication;
    }

    private AppInstanceInfoDTO convertAppInstanceInfo(DeployedApplication deployedApplication) {
        return new AppInstanceInfoDTO(
                deployedApplication.getId(),
                deployedApplication.getName(),
                deployedApplication.getAppTemplateName(),
                deployedApplication.getAppTemplateVersion(),
                deployedApplication.getState(),
                deployedApplication.getStateMessage(),
                deployedApplication.getStateDate(),
                deployedApplication.getDeployedOn(),
                deployedApplication.getEntryPointURL(),
                deployedApplication.getDeployedAppServices()
                        .values()
                        .stream()
                        .map(das -> new AppInstanceInfoDTO.AppServiceInfoDTO(
                                das.getAppServiceName(),
                                das.getImageName(),
                                das.getImageType(),
                                das.getImageVersion(),
                                das.getPublicURL(),
                                das.getState(),
                                das.getStateMessage(),
                                das.getStateTimeStamp(),
                                deployedApplication.getBoundServices(das.getAppServiceName())
                                        .stream()
                                        .map(DeployedDataService::getBindName)
                                        .collect(Collectors.toSet())
                        ))
                        .collect(Collectors.toList()),
                deployedApplication.getDeployedDataServices()
                        .values()
                        .stream()
                        .map(dds -> new AppInstanceInfoDTO.DataServiceInfoDTO(
                                dds.getBindName(),
                                dds.getDsbUrn(),
                                dds.getServiceId(),
                                dds.getState(),
                                dds.getStateMessage(),
                                dds.getStateTimeStamp()))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Collection<AppInstanceCopyStatisticsDTO> getCopyHistory(
            @PathParam("appInstanceId") UUID appInstanceId,
            @QueryParam("intervalStart") Long intervalStart,
            @QueryParam("intervalEnd") Long intervalEnd) {
        return appInstanceStatisticsCalculator.getCopyHistory(appInstanceId, intervalStart, intervalEnd);
    }

    @Override
    public AppInstanceStatisticsDTO getAppInstanceStatistics(@PathParam("appInstanceId") UUID appInstanceId) {
        return appInstanceStatisticsCalculator.getAppInstanceStatistics(loadDeployedApplication(appInstanceId));
    }

    @Override
    public AppInstanceCopyStatisticsDTO getCopyMetadata(@PathParam("copyId") UUID copyId) {
        return appInstanceStatisticsCalculator.getAppCopyMetadata(copyId);
    }

    @Override
    public void stageCopy(
            InputStream inputStream,
            @HeaderParam("crbUrn") String crbUrn,
            @HeaderParam("dsb") String dsb,
            @HeaderParam("copyTimestamp") Long copyTimestamp,
            @HeaderParam("facility") String facility,
            @HeaderParam("meta") String meta,
            @HeaderParam("copyId") UUID copyId) {
        // Streaming copy directly to CRB

        final String crbUrl = siteRepository.load().getCopyRepositoryByUrn(crbUrn).getUrl();
        //TODO: write meta-data?
        CrbWebDataApi crbWebDataApi = getCrbDataWebApi(crbUrn);
        CrbNegotiationResult crbNegotiationResult = CrbUtil.getCrbNegotiationResult(webAPIResolver, crbUrn, crbUrl);
        crbWebDataApi.createCopyInRepo(
                crbNegotiationResult.getRepoId(),
                copyId.toString(),
                inputStream);
    }

    @Override
    public Response downloadCopy(@PathParam("copyRepoURN") String copyRepoUrn, @PathParam("copyId") UUID copyId) {
        CrbWebDataApi crbWebDataApi = getCrbDataWebApi(copyRepoUrn);
        return Response.ok((StreamingOutput) outputStream -> {
            Response download = crbWebDataApi.retrieveCopy(copyId.toString());
            try (InputStream inputStream = download.readEntity(InputStream.class)) {
                StreamUtil.copy(inputStream, outputStream);
            } finally {
                download.close();
            }
        }).build();
    }

    private CrbWebDataApi getCrbDataWebApi(String copyRepoUrn) {
        final Site site = siteRepository.load();

        // Checking we have such copyRepo
        final CopyRepository crb = Objects.requireNonNull(
                site.getCopyRepositoryByUrn(copyRepoUrn),
                "copy repository with Id " + copyRepoUrn + " does not exist on site");

        return Objects.requireNonNull(
                webAPIResolver.getWebAPI(crb.getUrl(), CrbWebDataApi.class),
                "failed locating copy repo with URN " + copyRepoUrn);
    }

    @Override
    public void stopApp(StopAppOnSiteCommandArgs stopAppOnSiteCommandArgs) {
        stopAppOnSiteCommand.execute(stopAppOnSiteCommandArgs);
    }

    @Override
    public Collection<SitePsbInfoDto> listPsbs() {
        return siteRepository.load().getPsbs()
                .stream()
                .map(p -> new SitePsbInfoDto(p.getUrn(), p.getName(), p.getType(), p.getVersion()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<SitePsbDetailedInfoDto> listPsbsDetailed() {
        return siteRepository.load().getPsbs()
                .stream()
                .map(this::convertSitePsbDetailedInfo)
                .collect(Collectors.toList());
    }

    private PSBWebAPI getPsbWebApi(String psbUrl) {
        final PSBWebAPI psbConnection = webAPIResolver.getWebAPI(psbUrl, PSBWebAPI.class);
        if (psbConnection == null) {
            throw new InternalServerErrorException("Failed getting psb connection for psbUrl=" + psbUrl);
        }
        return psbConnection;
    }

    private SitePsbDetailedInfoDto convertSitePsbDetailedInfo(Psb p) {
        List<PSBSpaceDTO> spaces = null;

        // Listing the spaces from psb, not failing if can't
        try {
            spaces = getPsbWebApi(p.getUrl()).listSpaces();
        } catch (Exception ex) {
            log.warn("Failed listing spaces from psb " + p.getUrn(), ex);
        }

        // In case we can't find, creating a default namespace entry which always exists
        if (spaces == null || spaces.isEmpty()) {
            spaces = Collections.singletonList(new PSBSpaceDTO("default", Collections.emptyMap()));
        }

        return new SitePsbDetailedInfoDto(
                p.getUrn(),
                p.getName(),
                p.getType(),
                p.getVersion(),
                spaces
                        .stream()
                        .map(s -> new SitePsbDetailedInfoDto.SitePSBSpaceInfo(
                                s.getName(),
                                s.getProperties()))
                        .collect(Collectors.toList()));
    }

    @Override
    public Collection<SiteArtifactRegistryInfoDTO> listArtifactRegistries() {
        return siteRepository.load().getArtifactRegistires()
                .stream()
                .map(ar -> new SiteArtifactRegistryInfoDTO(ar.getName(), ar.getType().name(), ar.getParameters()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<SiteCopyRepoInfoDTO> listCopyRepositories() {
        return siteRepository
                .load()
                .getCopyRepositories()
                .stream()
                .map(c -> new SiteCopyRepoInfoDTO(c.getUrn(), c.getUrl(), c.getName(), c.getType(), c.getVersion()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<String> listArtifactVersions(
            @PathParam("artifactRegistryName") String artifactRegistryName,
            @PathParam("artifactId") String artifactId) {
        final SiteArtifactRegistry artifactRegistry = siteRepository.load().getArtifactRegistry(artifactRegistryName);
        if (artifactRegistry == null) {
            throw new NotFoundException("Failed locating artifact registry " + artifactRegistryName);
        }
        try {
            return artifactRegistry.getApi().listVersions(artifactId);
        } catch (Exception ex) {
            throw new NotFoundException("Failed locating artifact " + artifactId + " from artifact registry " +
                    artifactRegistryName, ex);
        }
    }
}
