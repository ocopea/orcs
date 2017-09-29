// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.services.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

/**
 * Created by liebea on 5/10/15.
 * Drink responsibly
 */
@Path("/exec")
public interface MicroServiceExecAPI {

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    Response execute(InputStream serviceInput, @Context HttpHeaders headers);

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @Path("hack")
    //todo:withAshish:help need a nice generic way to pass:
    // 1 ) multi headers
    // 2 ) stream context (multipart?)
    Response execute2(InputStream serviceInput, @HeaderParam("headers") String headers);

}
