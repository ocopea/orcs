// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.services.rest;

import com.emc.microservice.ContextThreadLocal;
import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.MicroServiceState;
import com.emc.microservice.metrics.StaticObjectMetric;
import com.emc.ocopea.util.io.StreamUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * This filter is responsible for blocking http requests when the service is not in "started" state
 * Also implements the "print-all-json-requests" feature printing json request/responses to the log
 * Also counts number of rest requests are currently running. this class is created per microservice and there is only
 * one instance of it per microservice lifetime
 */
@Provider
@ServerInterceptor
@PreMatching
public class MicroServicePreMatchingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    static ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private Boolean printInput = null;
    private final StaticObjectMetric<RestCallInfo> restCallInfoStaticObjectMetric;
    /***
     * Set of resources that are considered system resources and we're allowing access to those
     * even if service is paused
     */
    private static final Set<String> systemResourceClasses =
            new HashSet<>(Arrays.asList(
                    MicroServiceConfigurationResource.BASE_URI,
                    MicroServiceStateResource.BASE_URI,
                    MicroServiceMetricsResource.BASE_URI,
                    APIMetaResource.BASE_URI));

    private MicroServiceApplication microServiceApplication;
    private Logger logger;

    public MicroServicePreMatchingFilter(MicroServiceApplication microServiceApplication) {
        this.microServiceApplication = microServiceApplication;
        logger = microServiceApplication.getMicroServiceContext().getLogger();
        restCallInfoStaticObjectMetric = microServiceApplication.getMicroServiceContext().getMetricsRegistry()
                .getStaticObjectMetric(RestCallInfo.class);
    }

    private boolean isPrintInput() {
        if (printInput != null) {
            return printInput;
        }
        printInput = microServiceApplication.getMicroServiceContext()
                .getParametersBag().getBoolean("print-all-json-requests");
        return printInput != null && printInput;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        ContextThreadLocal.setContext(microServiceApplication.getMicroServiceContext());
        final UUID requestUuid = UUID.randomUUID();
        final String rawPath = containerRequestContext.getUriInfo()
                .getAbsolutePath().getRawPath();
        containerRequestContext.setProperty("msReqId", requestUuid);
        restCallInfoStaticObjectMetric.put(
                requestUuid,
                new RestCallInfo(new Date(), rawPath, containerRequestContext.getMethod()));
        try {
            if (isPrintInput() &&
                    MediaType.APPLICATION_JSON_TYPE.isCompatible(containerRequestContext.getMediaType())) {

                try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {

                    // Reading the request into memory
                    StreamUtil.copy(containerRequestContext.getEntityStream(), output);

                    // Since we already read the request, we need to re-insert an equivalent input stream to
                    // the request object so that the real consumer could enjoy the data too :)
                    final ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());
                    containerRequestContext.setEntityStream(inputStream);

                    logger.info("\n" +
                            containerRequestContext.getMethod() + "\n" +
                            rawPath + "\n" +
                            "Request:\n" +
                            getStringFromBaos(output));
                }

            }
        } catch (Exception ex) {
            logger.warn("failed parsing json for logging request for the \"print-all-json-requests\" feature - " +
                    ex.getMessage());
            logger.debug("failed parsing json for logging request for the \"print-all-json-requests\" feature", ex);
        }

        MicroServiceState state = this.microServiceApplication.getMicroServiceContext().getServiceState();
        boolean isSystemResource = systemResourceClasses.contains(containerRequestContext.getUriInfo().getPath());

        if (state != MicroServiceState.RUNNING) {

            if (!isSystemResource) {
                String msg =
                        this.microServiceApplication.getMicroServiceContext().getMicroServiceName() + " is ..." + state;
                containerRequestContext.abortWith(ServerResponse
                        .status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(msg)
                        .build());
            }
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {

        try {
            //noinspection SuspiciousMethodCalls
            restCallInfoStaticObjectMetric.remove((UUID) requestContext.getProperty("msReqId"));
            if (isPrintInput() &&
                    MediaType.APPLICATION_JSON_TYPE.isCompatible(responseContext.getMediaType())) {
                final MessageBodyWriter messageBodyWriter =
                        ResteasyProviderFactory
                                .getInstance()
                                .getMessageBodyWriter(
                                        responseContext.getEntityClass(),
                                        responseContext.getEntityType(),
                                        responseContext.getEntityAnnotations(),
                                        responseContext.getMediaType());
                if (messageBodyWriter != null) {

                    try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                        //noinspection unchecked
                        messageBodyWriter.writeTo(
                                responseContext.getEntity(),
                                responseContext.getEntityClass(),
                                responseContext.getEntityType(),
                                responseContext.getEntityAnnotations(),
                                responseContext.getMediaType(),
                                responseContext.getHeaders(),
                                output);

                        logger.info("\n" +
                                requestContext.getMethod() + "\n" +
                                requestContext.getUriInfo().getAbsolutePath().getRawPath() + "\n" +
                                "Response:\n" +
                                getStringFromBaos(output));
                    }

                }
            }
        } catch (Exception ex) {
            logger.warn("failed parsing json for logging response for the \"print-all-json-requests\" feature - " +
                    ex.getMessage());
            logger.debug("failed parsing json for logging response for the \"print-all-json-requests\" feature", ex);
        }
    }

    private String getStringFromBaos(ByteArrayOutputStream output) throws IOException {
        final Object o = mapper.readValue(output.toByteArray(), Object.class);
        return mapper.writeValueAsString(o);
    }
}
