// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.standalone.web;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * A filter setting location header and status CREATED on successful relevant responses. To activate filter, add the
 * location as an attribute to the HttpServletRequest, using the public constant as key.
 */
@Provider
public class LocationHeaderFilter implements ContainerResponseFilter {
    public static final String REQUEST_CONTEXT_KEY = "NAZGUL-LOCATION-HEADER-FILTER";

    @Context
    private HttpServletRequest servletRequest;

    @Override
    public void filter(
            ContainerRequestContext requestContext,
            ContainerResponseContext responseContext) throws IOException {
        if (responseContext.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            Object property = servletRequest.getAttribute(REQUEST_CONTEXT_KEY);
            if (property != null) {
                responseContext.getHeaders().add(HttpHeaders.LOCATION, property.toString());
                responseContext.setStatusInfo(Response.Status.CREATED);
            }
        }
    }
}
