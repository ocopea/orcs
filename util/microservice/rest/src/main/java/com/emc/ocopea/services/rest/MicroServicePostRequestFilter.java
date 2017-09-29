// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.services.rest;

import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.metrics.MetricsRegistry;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.interception.PostMatchContainerRequestContext;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Created with true love by liebea on 10/23/2014.
 */
@Provider
@ServerInterceptor
public class MicroServicePostRequestFilter implements ContainerRequestFilter {
    private MetricsRegistry metricRegistry;

    public MicroServicePostRequestFilter(MicroServiceApplication microServiceApplication) {
        this.metricRegistry = microServiceApplication.getMicroServiceContext().getMetricsRegistry();
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        PostMatchContainerRequestContext pmContext = (PostMatchContainerRequestContext) containerRequestContext;
        Class<?> resourceClass = pmContext.getResourceMethod().getResourceClass();
        metricRegistry.getCounter("executed", resourceClass).inc();
    }
}
