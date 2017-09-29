// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.webapp;

import com.emc.ocopea.util.io.StreamUtil;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by liebea on 7/28/15.
 * Drink responsibly
 */
@Path("html")
public class HubWebAppHtmlResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/")
    public Response welcome() {
        return readStaticResource("static/index.html");
    }

    @GET
    @Path("/{path:.*}")
    public Response get(@PathParam("path") String path) {
        return readStaticResource("static/" + path);
    }

    private Response readStaticResource(final String resourceName) {
        final URL resourceUrl = this.getClass().getClassLoader().getResource(resourceName);
        if (resourceUrl == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        StreamingOutput streamingOutput = output -> {
            try (InputStream resourceAsStream = resourceUrl.openStream()) {
                StreamUtil.copy(resourceAsStream, output);
            }
        };

        return Response.ok(streamingOutput).build();
    }
}
