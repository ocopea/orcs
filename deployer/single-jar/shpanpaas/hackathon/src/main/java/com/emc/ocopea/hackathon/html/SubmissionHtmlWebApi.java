// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hackathon.html;

import org.jboss.resteasy.annotations.cache.Cache;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

/**
 * Created by liebea on 5/30/15.
 * Drink responsibly
 */
@Path("/html")
public interface SubmissionHtmlWebApi {

    @POST
    @Path("submitNewIdea")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response submitNewIdea(MultipartFormDataInput input);

    @GET
    @Path("ideaDoc/{ideaId}")
    @Cache
    Response getIdeaDoc(@PathParam("ideaId") UUID ideaId);

    @GET
    @Path("nui/{path:.*}")
    Response getNUIResource(@PathParam("path") String path);

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("nui")
    Response welcomeNUI();

}
