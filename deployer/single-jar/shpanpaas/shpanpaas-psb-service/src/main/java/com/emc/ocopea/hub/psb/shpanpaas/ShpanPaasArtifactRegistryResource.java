// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.psb.shpanpaas;

import com.emc.microservice.Context;
import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceApplication;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.util.Pair;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

/**
 * Created by liebea on 1/15/17.
 * Drink responsibly
 */
@Path("artifact-registry")
public class ShpanPaasArtifactRegistryResource {

    private ShpanPaasArtifactRegistrySingleton artifactRegistry;

    @javax.ws.rs.core.Context
    @NoJavadoc
    public void setApplication(Application application) {
        Context context = ((MicroServiceApplication) application).getMicroServiceContext();
        artifactRegistry = context
                .getSingletonManager()
                .getManagedResourceByName(ShpanPaasArtifactRegistrySingleton.class.getSimpleName())
                .getInstance();

    }

    @NoJavadoc
    // TODO add javadoc
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{artifactId}")
    public Collection<String> listVersions(@PathParam("artifactId") String artifactId) {
        final Pair<MicroService, Collection<String>> serviceVersions = artifactRegistry.getServiceVersions(artifactId);
        if (serviceVersions == null) {
            throw new NotFoundException("Could not find artifact " + artifactId);
        } else {
            return serviceVersions.getValue();
        }
    }
}
