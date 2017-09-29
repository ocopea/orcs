// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.artifact;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

/**
 * Created by liebea on 1/15/17.
 * Drink responsibly
 */
@Path("/")
public interface ArtifactRegistryWebApi {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{artifactId}")
    Collection<String> listVersions(@PathParam("artifactId") String artifactId);
}
