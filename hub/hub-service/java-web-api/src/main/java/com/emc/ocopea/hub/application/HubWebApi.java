// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import com.emc.ocopea.hub.site.AddSiteToHubCommandArgs;
import com.emc.ocopea.hub.site.SiteDto;
import com.emc.ocopea.hub.testdev.SavedImageDTO;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.UUID;

/**
 * Created by liebea on 12/13/15.
 * Drink responsibly
 */
@Path("/")
public interface HubWebApi {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("app/catalog")
    Collection<ApplicationTemplateDTO> listAppCatalog();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("app/catalog/{appTemplateId}")
    ApplicationTemplateDTO getAppTemplate(
            @PathParam("appTemplateId") UUID appTemplateId,
            @HeaderParam("NAZGUL-INCLUDE-DELETED") @DefaultValue("false") Boolean includeDeleted);

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("app/catalog/{appTemplateId}")
    void deleteAppTemplate(@PathParam("appTemplateId") UUID appTemplateId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("app/instance")
    Collection<HubAppInstanceConfigurationDTO> listAppInstances();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("app/instance-state/{appInstanceId}")
    HubAppInstanceWithStateDTO getAppInstanceState(@PathParam("appInstanceId") UUID appInstanceId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("app/instance-state")
    Collection<HubAppInstanceWithStateDTO> listAppInstanceStates();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("app/instance/{appInstanceId}")
    HubAppInstanceConfigurationDTO getAppInstance(@PathParam("appInstanceId") UUID appInstanceId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("app/instance/{appInstanceId}/downstream")
    HubAppInstanceDownStreamTreeDTO listDownStreamInstances(@PathParam("appInstanceId") UUID appInstanceId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("site")
    Collection<SiteDto> listSites();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("site/{siteId}")
    SiteDto getSite(@PathParam("siteId") UUID siteId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("test-dev/saved-images")
    Collection<SavedImageDTO> listSavedImages(@QueryParam("appTemplateId") UUID appTemplateId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("test-dev/saved-images/{savedImageId}")
    SavedImageDTO getSavedImage(@PathParam("savedImageId") UUID savedImageId);

    @GET
    @Path("config/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    String readHubConfig(@PathParam("key") String key);

    @POST
    @Path("config/{key}")
    @Consumes(MediaType.APPLICATION_JSON)
    void writeHubConfig(@PathParam("key") String key, String value);

    // Commands
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("commands/createApplicationTemplate")
    ApplicationTemplateDTO createApplicationTemplate(ApplicationTemplateDTO applicationTemplateDTO);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("commands/deploy-application")
    UUID deployApp(DeployAppCommandArgs applicationTemplateDTO);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("commands/create-app-deployment-plan")
    DeployAppCommandArgs.AppTemplateDeploymentPlanDTO createAppDeploymentPlan(
            CreateDeploymentPlanCommandArgs applicationTemplateDTO);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("commands/repurpose-application")
    UUID repurposeApp(RepurposeAppCommandArgs applicationTemplateDTO);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("commands/create-app-copy")
    UUID createAppCopy(CreateAppCopyCommandArgs args);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("commands/create-saved-image")
    UUID createSavedImage(CreateSavedImageCommandArgs args);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("commands/deploy-saved-image")
    UUID deploySavedImage(DeploySavedImageCommandArgs args);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("commands/add-site")
    String addSite(AddSiteToHubCommandArgs addSiteToHubCommandArgs);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("commands/stop-app")
    void stopApp(StopAppCommandArgs stopAppCommandArgs);

}
