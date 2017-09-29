// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.services.rest;

import com.emc.microservice.MicroServiceApplication;
import org.slf4j.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created with true love by liebea on 10/23/2014.
 */
@Provider
public class MicroServiceWriterInterceptor implements WriterInterceptor {

    private boolean printInput;
    private Logger logger;

    public MicroServiceWriterInterceptor(MicroServiceApplication microServiceApplication) {
        printInput = microServiceApplication.getMicroServiceContext()
                .getParametersBag().getParameterDescriptorsMap().containsKey("print-all-json-requests");
        logger = microServiceApplication.getMicroServiceContext().getLogger();
    }

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {

        if (printInput && MediaType.APPLICATION_JSON_TYPE.isCompatible(context.getMediaType())) {

            OutputStream originalStream = context.getOutputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            context.setOutputStream(baos);
            try {
                context.proceed();
            } finally {
                final Object o = MicroServicePreMatchingFilter.mapper.readValue(baos.toByteArray(), Object.class);
                logger.info("\nResponse body:\n " +
                        MicroServicePreMatchingFilter.mapper.writeValueAsString(o));

                baos.writeTo(originalStream);
                baos.close();
                context.setOutputStream(originalStream);
            }
        } else {
            context.proceed();
        }
    }
}
