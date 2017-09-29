// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id$
 * 
 * This computer code is copyright 2014 EMC Corporation
 * All rights reserved
 */

package com.emc.ocopea.services.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(MicroServiceConfigurationAPI.BASE_URI)
public interface MicroServiceConfigurationAPI {

    public static final String BASE_URI = "/configuration";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ServiceConfiguration getServiceConfiguration();

    @Path("logger")
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    Response setLogLevel();
}
