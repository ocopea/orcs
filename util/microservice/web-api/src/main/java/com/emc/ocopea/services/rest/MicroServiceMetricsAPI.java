// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id$
 * 
 * This computer code is copyright 2014 EMC Corporation
 * All rights reserved
 */

package com.emc.ocopea.services.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path(MicroServiceMetricsAPI.BASE_URI)
public interface MicroServiceMetricsAPI {

    public static final String BASE_URI = "/metrics";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response getMetricsOutput();

    /**
     * Returns metrics related to the operation of the datasource. Those metrics are datasource type specific, used
     * for troubleshooting purposes.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/ds/{ds}")
    Map<String, List> getDsMetrics(@PathParam("ds") String ds);
}
