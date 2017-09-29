// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * This computer code is copyright 2014 EMC Corporation
 * All rights reserved
 */
package com.emc.ocopea.services.rest;

import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author shresa
 */
public class MicroServiceExceptionMapper implements ExceptionMapper<WebApplicationException> {
    private static final Logger LOG = LoggerFactory.getLogger(MicroServiceExceptionMapper.class);

    @Context
    Request request;

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(WebApplicationException e) {
        Response response = e.getResponse();

        if (response != null) {
            // Non server error do not need logging
            if (response.getStatusInfo().getFamily() == Response.Status.Family.SERVER_ERROR) {
                log(e);
            } else {
                LOG.info(
                        "{} to {} failed with code {}. Message: {}",
                        request.getMethod(),
                        uriInfo.getPath(),
                        e.getResponse().getStatus(),
                        e.getMessage());
            }
            Response.ResponseBuilder builder;

            if (response instanceof ClientResponse) {
                builder = Response.status(response.getStatus());
                builder.type(MediaType.TEXT_PLAIN_TYPE);
                builder.entity(e.getMessage());
            } else {
                builder = Response.fromResponse(response);
                builder.type(MediaType.TEXT_PLAIN_TYPE);
                if (response.getEntity() == null) {
                    builder.entity(e.getMessage());
                }
            }
            return builder.build();
        }

        log(e);
        Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
        builder.entity(getStackTrace(e));
        builder.type(MediaType.TEXT_PLAIN_TYPE);
        return builder.build();
    }

    private void log(Exception e) {
        String msg = e.getMessage();
        if (msg == null || msg.trim().length() == 0) {
            msg = "Unknown server error";
        }
        LOG.error(msg, e);
    }

    private String getStackTrace(Exception e) {
        try (StringWriter writer = new StringWriter()) {
            try (PrintWriter pw = new PrintWriter(writer)) {
                e.printStackTrace(pw);
            }
            return writer.toString();
        } catch (IOException err) {
            return err.getMessage();
        }
    }
}
