// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hackathon.idea;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

/**
 * Created by liebea on 5/29/15.
 * Drink responsibly
 */
@Path("/")
public interface SubmitIdeaWebAPI {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/idea")
    SubmittedIdeaStatus submit(IdeaForSubmission ideaForSubmission);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/idea")
    Collection<SubmittedIdea> list();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/version")
    String version();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("vote")
    Response vote(VoteInfo voteInfo);

}
