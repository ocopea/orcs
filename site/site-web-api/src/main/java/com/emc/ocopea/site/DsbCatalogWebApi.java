// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/dsb/catalog")
public interface DsbCatalogWebApi {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Collection<SupportedServiceDto> getCatalog();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("instance")
    Collection<ServiceInstanceInfo> getInstancesByDsb();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("instance/{dsbUrn}")
    Collection<ServiceInstanceInfo> getInstancesByDsb(@PathParam("dsbUrn") String dsbUrn);

}
