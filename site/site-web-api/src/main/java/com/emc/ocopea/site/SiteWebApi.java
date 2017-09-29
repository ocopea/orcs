// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Created by liebea on 12/13/15.
 * Drink responsibly
 */
@Path("site")
public interface SiteWebApi {

    // Query
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("info")
    SiteInfoDto getSiteInfo();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("app-instance")
    Collection<AppInstanceInfoDTO> listAppInstanceInfo();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("psb")
    Collection<SitePsbInfoDto> listPsbs();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("psb-detailed")
    Collection<SitePsbDetailedInfoDto> listPsbsDetailed();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("artifact-registry")
    Collection<SiteArtifactRegistryInfoDTO> listArtifactRegistries();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("artifact-registry/{artifactRegistryName}/{artifactId}")
    Collection<String> listArtifactVersions(@PathParam("artifactRegistryName") String artifactRegistryName,
                                            @PathParam("artifactId") String artifactId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("copy-repo")
    Collection<SiteCopyRepoInfoDTO> listCopyRepositories();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("app-instance/{appInstanceId}")
    AppInstanceInfoDTO getAppInstanceInfo(@PathParam("appInstanceId") UUID appInstanceId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("app-instance/{appInstanceId}/logs")
    List<ServiceLogsWebSocketDTO> getAppInstanceLogsWebSockets(@PathParam("appInstanceId") UUID appInstanceId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("app-instance/{appInstanceId}/copy-history")
    Collection<AppInstanceCopyStatisticsDTO> getCopyHistory(
            @PathParam("appInstanceId") UUID appInstanceId,
            @QueryParam("intervalStart") Long intervalStart,
            @QueryParam("intervalEnd") Long intervalEnd);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("app-instance/{appInstanceId}/stats")
    AppInstanceStatisticsDTO getAppInstanceStatistics(@PathParam("appInstanceId") UUID appInstanceId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("copy/{copyId}")
    AppInstanceCopyStatisticsDTO getCopyMetadata(@PathParam("copyId") UUID copyId);

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("copy-repository/{copyRepoURN}/{copyId}")
    Response downloadCopy(@PathParam("copyRepoURN") String copyRepoURN, @PathParam("copyId") UUID copyId);

    // Commands
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("commands/add-cr")
    void addCr(AddCrToSiteCommandArgs addCrToSiteCommandArgs);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("commands/register-crb")
    void registerCrb(RegisterCrbToSiteCommandArgs registerCrbToSiteCommandArgs);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("commands/register-psb")
    void registerPsb(RegisterPsbToSiteCommandArgs registerPsbToSiteCommandArgs);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("commands/register-dsb")
    SupportedServiceDto registerDsb(RegisterDsbToSiteCommandArgs args);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("commands/add-custom-artifact-registry")
    void addCustomRestArtifactRegistry(
            AddCustomArtifactRegistryToSiteCommandArgs addCustomArtifactRegistryToSiteCommandArgs);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("commands/add-maven-artifact-registry")
    void addMavenArtifactRegistry(
            AddMavenArtifactRegistryToSiteCommandArgs addMavenArtifactRegistryToSiteCommandArgs);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("commands/add-docker-artifact-registry")
    void addDockerArtifactRegistry(
            AddDockerArtifactRegistryToSiteCommandArgs addDockerArtifactRegistryToSiteCommandArgs);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("commands/remove-artifact-registry")
    void removeArtifactRegistry(
            RemoveArtifactRegistryFromSiteCommandArgs removeArtifactRegistryFromSiteCommandArgs);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("commands/remove-dsb")
    void removeDsb(RemoveDsbFromSiteCommandArgs removeDsbFromSiteCommandArgs);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("commands/remove-crb")
    void removeCrb(RemoveCrbFromSiteCommandArgs removeCrbFromSiteCommandArgs);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("commands/deploy-application")
    void deployApplicationOnSite(DeployApplicationOnSiteCommandArgs deployAppManifest);

    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Path("commands/stage-copy")
    void stageCopy(
            InputStream inputStream,
            @HeaderParam("crbUrn") String crbUrn,
            @HeaderParam("dsb") String dsb,
            @HeaderParam("copyTimestamp") Long copyTimestamp,
            @HeaderParam("facility") String facility,
            @HeaderParam("meta") String meta,
            @HeaderParam("copyId") UUID copyId);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("commands/create-app-copy")
    UUID createAppCopy(CreateAppCopyCommandArgs createAppCopyCommandArgs);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("commands/stop-app")
    void stopApp(StopAppOnSiteCommandArgs stopAppOnSiteCommandArgs);

}
