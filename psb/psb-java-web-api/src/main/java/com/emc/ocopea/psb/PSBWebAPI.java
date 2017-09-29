// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.psb;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * PSB API Each PaaS has to implement.
 */
@Path("psb")
public interface PSBWebAPI {

    @GET
    @Path("info")
    @Produces(MediaType.APPLICATION_JSON)
    PSBInfoDTO getPSBInfo();

    @GET
    @Path("app-services/{space}/{appServiceId}")
    @Produces(MediaType.APPLICATION_JSON)
    PSBAppServiceInstanceDTO getAppService(@PathParam("space") String space,
                                           @PathParam("appServiceId") String appServiceId);

    @GET
    @Path("app-services/{space}/{appServiceId}/logs")
    @Produces(MediaType.APPLICATION_JSON)
    PSBLogsWebSocketDTO getAppServiceLogsWebSocket(@PathParam("space") String space,
                                                   @PathParam("appServiceId") String appServiceId);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("app-services")
    DeployAppServiceResponseDTO deployApplicationService(DeployAppServiceManifestDTO appServiceManifest);

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("app-services/{space}/{appServiceId}")
    DeployAppServiceResponseDTO stopApp(@PathParam("space") String space,
                                        @PathParam("appServiceId") String appServiceId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("spaces")
    List<PSBSpaceDTO> listSpaces();
}
