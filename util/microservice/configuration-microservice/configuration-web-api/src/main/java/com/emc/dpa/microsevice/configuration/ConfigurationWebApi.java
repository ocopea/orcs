// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.dpa.microsevice.configuration;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by shresa on 31/07/15.
 */
@Path("/configurations")
public interface ConfigurationWebApi {

    @GET
    @Path("{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    String read(@PathParam("path") String path);

    @POST
    @Path("{path:.*}")
    @Consumes(MediaType.APPLICATION_JSON)
    void write(@PathParam("path") String path, String data);

    @PUT
    @Path("{path:.*}")
    @Consumes(MediaType.APPLICATION_JSON)
    void overwrite(@PathParam("path") String path, String data);

    @DELETE
    @Path("{path:.*}")
    void delete(@PathParam("path") String path);
}
