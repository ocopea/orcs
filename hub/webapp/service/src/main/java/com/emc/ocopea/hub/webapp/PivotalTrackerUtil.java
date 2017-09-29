// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.webapp;

import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.util.MapBuilder;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import java.util.Map;

/**
*/
public class PivotalTrackerUtil {
    private static final Logger log = LoggerFactory.getLogger(PivotalTrackerUtil.class);
    private final WebAPIResolver webAPIResolver;

    public PivotalTrackerUtil(WebAPIResolver resolver) {
        webAPIResolver = resolver;
    }

    /**
     * Retrieve PivotalTracker details
     */
    public String pivotalTrackerAdd(
            String url,
            String projectId,
            String token,
            String name,
            String storyType,
            String description) {

        PivotalTrackerAPI pivotalTrackerTarget = webAPIResolver.getWebAPI(url, PivotalTrackerAPI.class);
        PivotalTrackerResponse bugData = pivotalTrackerTarget.addBug(
                token,
                projectId,
                MapBuilder.<String, String>newHashMap()
                        .with("name", name)
                        .with("story_type", storyType)
                        .with("description", description)
                        .build());
        log.info("Bug Report url = " + bugData.url);
        return bugData.url;
    }

    interface PivotalTrackerAPI {
        @POST
        @Path("/projects/{projectId}/stories")
        @Produces(MediaType.APPLICATION_JSON)
        @Consumes(MediaType.APPLICATION_JSON)
        PivotalTrackerResponse addBug(
                @HeaderParam("X-TrackerToken") String token,
                @PathParam("projectId") String projectId,
                Map<String, String> credentials);
    }

    static class PivotalTrackerResponse {
        @JsonProperty
        String kind;
        @JsonProperty
        String id;
        @JsonProperty(value = "project_id")
        String projectId;
        @JsonProperty
        String name;
        @JsonProperty
        String description;
        @JsonProperty(value = "story_type")
        String storyType;
        @JsonProperty(value = "current_state")
        String currentState;
        @JsonProperty(value = "requested_by_id")
        String requestedById;
        @JsonProperty(value = "owner_ids")
        String []ownerIds;
        @JsonProperty
        String []labels;
        @JsonProperty(value = "created_at")
        String createdAt;
        @JsonProperty(value = "updated_at")
        String updatedAt;
        @JsonProperty
        String url;
    }

}
