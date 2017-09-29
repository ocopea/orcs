// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hackathon.committee;

import com.emc.ocopea.hackathon.idea.SubmittedIdea;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

/**
 * Created by liebea on 3/1/17.
 * Drink responsibly
 */
@Path("/")
public interface CommitteeWebApi {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/review")
    Response review(IdeaReviewStatus ideaReviewStatus);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/idea")
    Collection<SubmittedIdea> list();

}
