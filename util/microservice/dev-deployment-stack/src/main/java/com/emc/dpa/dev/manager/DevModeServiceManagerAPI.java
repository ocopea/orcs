// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.dpa.dev.manager;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Handmade code created by ohanaa Date: 3/3/15 Time: 2:29 PM
 */
@Path("/services")
public interface DevModeServiceManagerAPI {

    @POST
    @Path("{serviceURI}")
    void startService(@PathParam("serviceURI") String serviceURI);

    @PUT
    @Path("datasource/{dsName}/pause")
    void pauseDB(@PathParam("dsName") String dsName);

    @PUT
    @Path("datasource/{dsName}/resume")
    void resumeDB(@PathParam("dsName") String dsName);

    @POST
    @Path("datasource/{dsName}/query")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    Response executeQuery(@PathParam("dsName") String dsName, String sql);
}
