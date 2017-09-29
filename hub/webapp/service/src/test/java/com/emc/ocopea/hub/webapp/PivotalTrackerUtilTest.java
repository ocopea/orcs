// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.webapp;

import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.microservice.webclient.WebApiResolverBuilder;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.WebTarget;
import java.util.Map;

public class PivotalTrackerUtilTest {

    @Test
    public void testPivotalTracker() {
        PivotalTrackerUtil pivotalTrackerUtil = new PivotalTrackerUtil(new MyWebAPIResolver());

        final String pivotalTrackerData = pivotalTrackerUtil.pivotalTrackerAdd(
                "https://www.pivotaltracker.com/services/v5",
                "2016043",
                "ff33d6e6e138d66834a52be6bf19f6f1",
                "Best Awesome Bug",
                "bug",
                "The most awesome ever test of adding bug");

        Assert.assertEquals("https://www.pivotaltracker.com/story/show/145901733", pivotalTrackerData);

    }


    private static class MyWebAPIResolver implements WebAPIResolver {
        private final boolean valid;

        MyWebAPIResolver() {
            this(true);
        }

        MyWebAPIResolver(boolean valid) {
            this.valid = valid;
        }

        @Override
        public WebAPIResolver buildResolver(WebApiResolverBuilder builder) {
            return this;
        }

        @Override
        public <T> T getWebAPI(String s, Class<T> aClass) {
            //noinspection unchecked
            return (T)new PivotalTrackerUtil.PivotalTrackerAPI(){

                @Override
                public PivotalTrackerUtil.PivotalTrackerResponse addBug(
                        @HeaderParam("X-TrackerToken") String token,
                        @PathParam("projectId") String projectId,
                        Map<String, String> credentials) {
                    final PivotalTrackerUtil.PivotalTrackerResponse rsp =
                            new PivotalTrackerUtil.PivotalTrackerResponse();

                    rsp.url = "https://www.pivotaltracker.com/story/show/145901733";
                    rsp.updatedAt = "2017-05-23T02:30:34Z";
                    rsp.createdAt = "2017-05-23T02:30:34Z";
                    //rsp.labels =  "";
                    //rsp.owner_ids = "";
                    rsp.kind = "story";
                    rsp.id = "145901733";
                    rsp.projectId = "2016043";
                    rsp.name = "Addition of the first defect";
                    rsp.description = "Begin logging issues";
                    rsp.storyType = "bug";
                    rsp.currentState = "unscheduled";
                    rsp.requestedById = "2896935";

                    return rsp;
                }
            };
        }

        @Override
        public WebTarget getWebTarget(String s) {
            return null;
        }
    }
}
