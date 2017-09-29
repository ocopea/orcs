// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hackathon.html;

import org.apache.commons.io.IOUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by liebea on 5/30/15.
 * Drink responsibly
 */
@Path("html")
public class CommitteeHtmlResource {

    private Response readStaticResource(final String resourceName) {
        StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(resourceName);
                IOUtils.copy(resourceAsStream, output);
            }
        };

        return Response.ok(streamingOutput).build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("cui")
    public Response welcomeNUI() {
        return readStaticResource("cui/index.html");
    }

    @GET
    @Path("cui/{path:.*}")
    public Response getNUIResource(@PathParam("path") String path) {
        return readStaticResource("cui/" + path);
    }

}
