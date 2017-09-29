// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.UUID;

@Path("/")
public interface ProtectionWebAPI {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("commands/protect-application")
    void protectApplication(ProtectApplicationInstanceInfoDTO protectApplicationInstanceInfo);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("commands/create-app-copy")
    UUID createAppCopy(ProtectApplicationInstanceInfoDTO protectApplicationInstanceInfo);

    /**
     * Returns a list of app instance copies
     * @param appInstanceId app instance id
     */
    @GET
    @Path("app/{appInstanceId}/copies")
    @Produces(MediaType.APPLICATION_JSON)
    Collection<ProtectionAppCopyDTO> listAppInstanceCopies(
            @PathParam("appInstanceId") UUID appInstanceId
    );

    /**
     * Returns a list of app instance copies within the half open dates interval [intervalStart, intervalEnd).
     * @param appInstanceId app instance id
     * @param intervalStart filter copy time start (use -1 for no limit)
     * @param intervalEnd filter copy time end (use -1 for no limit)
     * @throws javax.ws.rs.BadRequestException if intervalEnd smaller than intervalStart
     */
    @GET
    @Path("app/{appInstanceId}/copies")
    @Produces(MediaType.APPLICATION_JSON)
    Collection<ProtectionAppCopyDTO> listAppInstanceCopies(
            @PathParam("appInstanceId") UUID appInstanceId,
            @QueryParam("intervalStart") @DefaultValue("-1") Long intervalStart,
            @QueryParam("intervalEnd") @DefaultValue("-1") Long intervalEnd
    );

    @GET
    @Path("copy/{copyId}")
    @Produces(MediaType.APPLICATION_JSON)
    ProtectionAppCopyDTO getCopy(@PathParam("copyId") UUID copyId);
}
