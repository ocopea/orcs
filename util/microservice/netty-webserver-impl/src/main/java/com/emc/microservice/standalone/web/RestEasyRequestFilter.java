// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.standalone.web;

import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.MicroServiceState;
import com.emc.microservice.metrics.MetricsRegistry;
import com.emc.ocopea.services.rest.MicroServiceConfigurationAPI;
import com.emc.ocopea.services.rest.MicroServiceConfigurationResource;
import com.emc.ocopea.services.rest.MicroServiceMetricsAPI;
import com.emc.ocopea.services.rest.MicroServiceMetricsResource;
import com.emc.ocopea.services.rest.MicroServiceStateAPI;
import com.emc.ocopea.services.rest.MicroServiceStateResource;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.specimpl.BuiltResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with true love by liebea on 10/23/2014.
 */
@Provider
@ServerInterceptor
public class RestEasyRequestFilter implements PreProcessInterceptor, PostProcessInterceptor {

    /**
     * Set of resources that are considered system resources and we're allowing access to those
     * even if service is paused
     */
    private static final Set<Class> systemResourceClasses = new HashSet<>(Arrays.asList(
            MicroServiceConfigurationResource.class,
            MicroServiceStateResource.class,
            MicroServiceMetricsResource.class));

    private MicroServiceApplication microServiceApplication;
    private MetricsRegistry metricRegistry;

    public RestEasyRequestFilter(MicroServiceApplication microServiceApplication) {
        this.microServiceApplication = microServiceApplication;
        this.metricRegistry = microServiceApplication.getMicroServiceContext().getMetricsRegistry();
    }

    private boolean shouldRequestAlwaysBeServed(HttpRequest request) {
        return request.getUri().getPath().endsWith(MicroServiceStateAPI.BASE_URI) ||
                request.getUri().getPath().endsWith(MicroServiceConfigurationAPI.BASE_URI) ||
                request.getUri().getPath().endsWith(MicroServiceMetricsAPI.BASE_URI);

    }

    @Override
    public ServerResponse preProcess(HttpRequest httpRequest, ResourceMethodInvoker method)
            throws Failure, WebApplicationException {
        MicroServiceState state = this.microServiceApplication.getMicroServiceContext().getServiceState();
        boolean isSystemResource = systemResourceClasses.contains(method.getResourceClass());

        if (state != MicroServiceState.RUNNING) {

            if (!shouldRequestAlwaysBeServed(httpRequest) && !isSystemResource) {
                String msg =
                        this.microServiceApplication.getMicroServiceContext().getMicroServiceName() + " is ..." + state;
                metricRegistry.getCounter("rejected", method.getResourceClass()).inc();
                return new ServerResponse((BuiltResponse) ServerResponse
                        .status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(msg)
                        .build());
            }
        }

        //todo: change to non deprecated filter
        metricRegistry.getCounter("executed", method.getResourceClass()).inc();
        return null;
    }

    @Override
    public void postProcess(ServerResponse response) {
        //todo: change to non deprecated filter
        //metricRegistry.getCounter("executed", response.get() getResourceClass()).inc();
    }
}
