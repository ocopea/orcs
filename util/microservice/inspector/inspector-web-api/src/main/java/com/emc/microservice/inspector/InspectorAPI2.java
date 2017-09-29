// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.inspector;

import com.emc.microservice.inspector.vis.Graph;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by liebea on 1/7/15.
 * Drink responsibly
 */
@Path("/graph2")
public interface InspectorAPI2 {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Graph getGraph();

    @Path("html")
    @Produces(MediaType.TEXT_HTML)
    @GET
    Response getHtml();

    @Path("html/vis.css")
    @Produces("text/css")
    @GET
    Response getCSS();

    @Path("html/vis.js")
    @Produces("text/javascript")
    @GET
    Response getCode();
}
