// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub;

import com.emc.ocopea.site.AddCrToSiteCommandArgs;
import com.emc.ocopea.site.AddCustomArtifactRegistryToSiteCommandArgs;
import com.emc.ocopea.site.AddDockerArtifactRegistryToSiteCommandArgs;
import com.emc.ocopea.site.AddMavenArtifactRegistryToSiteCommandArgs;
import com.emc.ocopea.site.AppInstanceCopyStatisticsDTO;
import com.emc.ocopea.site.AppInstanceInfoDTO;
import com.emc.ocopea.site.AppInstanceStatisticsDTO;
import com.emc.ocopea.site.CreateAppCopyCommandArgs;
import com.emc.ocopea.site.DeployApplicationOnSiteCommandArgs;
import com.emc.ocopea.site.RegisterCrbToSiteCommandArgs;
import com.emc.ocopea.site.RegisterDsbToSiteCommandArgs;
import com.emc.ocopea.site.RegisterPsbToSiteCommandArgs;
import com.emc.ocopea.site.RemoveArtifactRegistryFromSiteCommandArgs;
import com.emc.ocopea.site.RemoveCrbFromSiteCommandArgs;
import com.emc.ocopea.site.RemoveDsbFromSiteCommandArgs;
import com.emc.ocopea.site.ServiceLogsWebSocketDTO;
import com.emc.ocopea.site.SiteArtifactRegistryInfoDTO;
import com.emc.ocopea.site.SiteCopyRepoInfoDTO;
import com.emc.ocopea.site.SiteInfoDto;
import com.emc.ocopea.site.SiteLocationDTO;
import com.emc.ocopea.site.SitePsbDetailedInfoDto;
import com.emc.ocopea.site.SitePsbInfoDto;
import com.emc.ocopea.site.SiteWebApi;
import com.emc.ocopea.site.StopAppOnSiteCommandArgs;
import com.emc.ocopea.site.SupportedServiceDto;
import com.emc.ocopea.site.app.DeployedAppServiceState;
import com.emc.ocopea.site.app.DeployedApplicationState;
import com.emc.ocopea.site.app.DeployedDataServiceState;
import com.emc.ocopea.util.MapBuilder;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.specimpl.BuiltResponse;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by liebea on 4/18/17.
 * Drink responsibly
 */
public class SiteApiMock implements SiteWebApi {

    private final String siteName;
    private Map<UUID, AppInstanceInfoDTO> appInstancesInfo = new HashMap<>();
    private Map<String, SitePsbInfoDto> psbs = new HashMap<>();
    private Map<String, SiteCopyRepoInfoDTO> copyRepos = new HashMap<>();
    private Map<String, SiteArtifactRegistryInfoDTO> artifactRegistries = new HashMap<>();
    private Map<String, SupportedServiceDto> supportedDSBs = new HashMap<>();
    private Map<UUID, AppInstanceCopyStatisticsDTO> appCopies = new HashMap<>();

    public SiteApiMock(String siteName) {
        this.siteName = siteName;
    }

    @Override
    public Collection<SitePsbInfoDto> listPsbs() {
        return new ArrayList<>(psbs.values());
    }

    @Override
    public Collection<SitePsbDetailedInfoDto> listPsbsDetailed() {
        return psbs.values().stream().map(
                psb -> new SitePsbDetailedInfoDto(psb.getUrn(), psb.getName(), psb.getType(), psb.getVersion(),
                        Collections.singletonList(
                                new SitePsbDetailedInfoDto.SitePSBSpaceInfo("ShpanSpace", Collections.emptyMap()))
                )).collect(Collectors.toList());
    }

    @Override
    public Collection<SiteArtifactRegistryInfoDTO> listArtifactRegistries() {
        return new ArrayList<>(artifactRegistries.values());
    }

    @Override
    public Collection<String> listArtifactVersions(
            @PathParam("artifactRegistryName") String artifactRegistryName,
            @PathParam("artifactId") String artifactId) {
        if (artifactRegistries.containsKey(artifactRegistryName)) {
            return Arrays.asList("1", "2", "3");
        } else {
            throw new NotFoundException();
        }
    }

    @Override
    public void addCustomRestArtifactRegistry(
            AddCustomArtifactRegistryToSiteCommandArgs
                    addCustomArtifactRegistryToSiteCommandArgs) {
        artifactRegistries.put(
                addCustomArtifactRegistryToSiteCommandArgs.getName(),
                new SiteArtifactRegistryInfoDTO(
                        addCustomArtifactRegistryToSiteCommandArgs.getName(),
                        "customRest",
                        MapBuilder.<String, String>newHashMap().with("url", "http://test.my.app.com").build()));
    }

    @Override
    public void addMavenArtifactRegistry(
            AddMavenArtifactRegistryToSiteCommandArgs addMavenArtifactRegistryToSiteCommandArgs) {
    }

    @Override
    public void addDockerArtifactRegistry(AddDockerArtifactRegistryToSiteCommandArgs args) {
    }

    @Override
    public void removeArtifactRegistry(RemoveArtifactRegistryFromSiteCommandArgs args) {
    }

    @Override
    public void removeDsb(RemoveDsbFromSiteCommandArgs args) {
        supportedDSBs.remove(args.getDsbUrn());
    }

    @Override
    public void removeCrb(RemoveCrbFromSiteCommandArgs args) {
        copyRepos.remove(args.getCrbUrn());
    }

    @Override
    public Collection<SiteCopyRepoInfoDTO> listCopyRepositories() {
        return new ArrayList<>(copyRepos.values());
    }

    @Override
    public SiteInfoDto getSiteInfo() {
        return new SiteInfoDto(
                siteName,
                "1.0",
                null,
                new SiteLocationDTO(32.1792126, 34.9005128, "Israel", Collections.emptyMap()));
    }

    @Override
    public SupportedServiceDto registerDsb(RegisterDsbToSiteCommandArgs args) {
        final SupportedServiceDto supportedServiceDto = new SupportedServiceDto(
                args.getDsbUrn(),
                args.getDsbUrn(),
                "dsb",
                args.getDsbUrn(),
                Collections.singletonList(new SupportedServiceDto.SupportedServicePlanDto(
                        "default",
                        "default",
                        "default",
                        "1$",
                        Collections.singletonList(
                                new SupportedServiceDto.SupportedServiceProtocolDto(
                                        "mysql",
                                        "8",
                                        Collections.emptyMap()
                                )
                        ),
                        Collections.emptyMap()
                )));
        supportedDSBs.put(
                args.getDsbUrn(),
                supportedServiceDto);
        return supportedServiceDto;
    }

    @Override
    public void addCr(AddCrToSiteCommandArgs addCrToSiteCommandArgs) {
    }

    @Override
    public void registerCrb(RegisterCrbToSiteCommandArgs args) {
        copyRepos.put(args.getCrbUrn(),
                new SiteCopyRepoInfoDTO(args.getCrbUrn(), args.getCrbUrl(), "crb1", "mock", "1"));
    }

    @Override
    public void registerPsb(RegisterPsbToSiteCommandArgs registerPsbToSiteCommandArgs) {
        psbs.put(
                registerPsbToSiteCommandArgs.getPsbUrn(),
                new SitePsbInfoDto(
                        registerPsbToSiteCommandArgs.getPsbUrn(),
                        registerPsbToSiteCommandArgs.getPsbUrn(),
                        "mockPSB",
                        "1"));
    }

    @Override
    public void deployApplicationOnSite(DeployApplicationOnSiteCommandArgs deployApplicationOnSiteCommandArgs) {
        final Date stateDate = new Date();
        appInstancesInfo.put(deployApplicationOnSiteCommandArgs.getAppInstanceId(), new AppInstanceInfoDTO(
                deployApplicationOnSiteCommandArgs.getAppInstanceId(),
                deployApplicationOnSiteCommandArgs.getAppInstanceName(),
                deployApplicationOnSiteCommandArgs.getAppTemplateName(),
                deployApplicationOnSiteCommandArgs.getAppTemplateVersion(),
                DeployedApplicationState.deploying,
                "yey",
                stateDate,
                stateDate,
                "http://go.to." + deployApplicationOnSiteCommandArgs.getAppInstanceName() + ".test",
                deployApplicationOnSiteCommandArgs.getAppServiceTemplates().values()
                        .stream()
                        .map(a -> new AppInstanceInfoDTO.AppServiceInfoDTO(
                                a.getImageName(),
                                a.getImageName(),
                                a.getImageType(),
                                a.getImageVersion(),
                                "http://go.to." + a.getAppServiceName() + ".test",
                                DeployedAppServiceState.deployed,
                                null,
                                stateDate,
                                a.getDependencies()
                        ))
                        .collect(Collectors.toList()),
                deployApplicationOnSiteCommandArgs.getAppServiceTemplates().values()
                        .stream()
                        .flatMap(a ->
                                a.getDependencies()
                                        .stream()
                                        .map(dsName -> {
                                            final DeployApplicationOnSiteCommandArgs
                                                    .DeployDataServiceOnSiteManifestDTO d =
                                                    deployApplicationOnSiteCommandArgs.getDataServices()
                                                            .get(dsName);

                                            return new AppInstanceInfoDTO.DataServiceInfoDTO(
                                                    d.getDataServiceName(),
                                                    d.getDsbUrn(),
                                                    UUID.randomUUID().toString(),
                                                    DeployedDataServiceState.bound,
                                                    null,
                                                    stateDate);
                                        })
                        ).collect(Collectors.toList())
        ));
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
    }

    @Override
    public UUID createAppCopy(CreateAppCopyCommandArgs createAppCopyCommandArgs) {
        final UUID id = UUID.randomUUID();

        final AppInstanceInfoDTO appInstanceInfo = getAppInstanceInfo(createAppCopyCommandArgs.getAppInstanceId());
        final Date timeStamp = new Date();
        final SiteCopyRepoInfoDTO copyRepo = copyRepos.values().iterator().next();
        appCopies.put(id, new AppInstanceCopyStatisticsDTO(id, timeStamp, 12L,
                AppInstanceCopyStatisticsDTO.SiteAppCopyState.created,
                appInstanceInfo.getDataServices().stream().map(dto ->
                        new AppInstanceStatisticsDTO
                                .DataServiceCopyStatisticsDTO(
                                UUID.randomUUID(),
                                dto.getDsbURN(),
                                dto.getBindName(),
                                timeStamp,
                                "repoId1",
                                "facility1",
                                copyRepo.getUrn(),
                                copyRepo.getName(),
                                "a",
                                "1",
                                11L,
                                "hi")).collect(Collectors.toList()),
                appInstanceInfo.getAppServices().stream().map(dto ->
                        new AppInstanceStatisticsDTO.AppServiceCopyStatisticsDTO(
                                dto.getServiceName(),
                                dto
                                        .getImageName(),
                                dto.getImageType(),
                                dto.getImageVersion(),
                                timeStamp,
                                Collections.emptyMap(),
                                "bye")
                ).collect(Collectors.toList())
        ));
        return id;
    }

    @Override
    public void stopApp(StopAppOnSiteCommandArgs stopAppOnSiteCommandArgs) {
        //appInstancesInfo.remove(stopAppOnSiteCommandArgs.getAppInstanceId());
        final AppInstanceInfoDTO prev = appInstancesInfo.get(stopAppOnSiteCommandArgs.getAppInstanceId());
        appInstancesInfo.put(stopAppOnSiteCommandArgs.getAppInstanceId(), new AppInstanceInfoDTO(
                prev.getId(),
                prev.getName(),
                prev.getTemplateName(),
                prev.getTemplateVersion(),
                DeployedApplicationState.stopping,
                prev.getStateMessage(),
                new Date(),
                prev.getLaunched(),
                prev.getEntryPointURL(),
                prev.getAppServices(),
                prev.getDataServices())
        );
    }

    @Override
    public Collection<AppInstanceInfoDTO> listAppInstanceInfo() {
        return null;
    }

    @Override
    public List<ServiceLogsWebSocketDTO> getAppInstanceLogsWebSockets(
            @PathParam("appInstanceId") UUID appInstanceId) {
        return Collections.emptyList();
    }

    @Override
    public AppInstanceInfoDTO getAppInstanceInfo(@PathParam("appInstanceId") UUID uuid) {
        final AppInstanceInfoDTO instanceInfoDTO = this.appInstancesInfo.get(uuid);
        if (instanceInfoDTO == null) {
            throw new NotFoundException();
        } else if (instanceInfoDTO.getState() == DeployedApplicationState.deploying) {
            // Second time we see it, mark as running :)
            this.appInstancesInfo.put(uuid, new AppInstanceInfoDTO(
                    instanceInfoDTO.getId(),
                    instanceInfoDTO.getName(),
                    instanceInfoDTO.getTemplateName(),
                    instanceInfoDTO.getTemplateVersion(),
                    DeployedApplicationState.running,
                    instanceInfoDTO.getStateMessage(),
                    new Date(),
                    instanceInfoDTO.getLaunched(),
                    instanceInfoDTO.getEntryPointURL(),
                    instanceInfoDTO.getAppServices(),
                    instanceInfoDTO.getDataServices()
            ));
        } else if (instanceInfoDTO.getState() == DeployedApplicationState.stopping) {
            // Second time we see it, mark as running :)
            this.appInstancesInfo.put(uuid, new AppInstanceInfoDTO(
                    instanceInfoDTO.getId(),
                    instanceInfoDTO.getName(),
                    instanceInfoDTO.getTemplateName(),
                    instanceInfoDTO.getTemplateVersion(),
                    DeployedApplicationState.stopped,
                    instanceInfoDTO.getStateMessage(),
                    new Date(),
                    instanceInfoDTO.getLaunched(),
                    instanceInfoDTO.getEntryPointURL(),
                    instanceInfoDTO.getAppServices(),
                    instanceInfoDTO.getDataServices()
            ));
        }
        return instanceInfoDTO;
    }

    @Override
    public Collection<AppInstanceCopyStatisticsDTO> getCopyHistory(
            @PathParam("appInstanceId") UUID uuid,
            @QueryParam("intervalStart") Long aLong,
            @QueryParam("intervalEnd") Long aLong1) {
        return appCopies.values();
    }

    @Override
    public AppInstanceStatisticsDTO getAppInstanceStatistics(@PathParam("appInstanceId") UUID uuid) {
        return null;
    }

    @Override
    public AppInstanceCopyStatisticsDTO getCopyMetadata(@PathParam("copyId") UUID copyId) {
        return appCopies.get(copyId);
    }

    @Override
    public Response downloadCopy(@PathParam("copyRepositoryId") String s, @PathParam("copyId") UUID uuid) {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream("Hi".getBytes());
        return new ServerResponse((BuiltResponse) Response.ok(inputStream).build()) {
            @Override
            public <T> T readEntity(GenericType<T> entityType, Annotation[] annotations) {
                return getT();

            }

            private <T> T getT() {
                //noinspection unchecked
                return (T) inputStream;
            }

            @Override
            public <T> T readEntity(Class<T> type, Type genericType, Annotation[] annotations) {
                return getT();
            }
        };
    }
}
