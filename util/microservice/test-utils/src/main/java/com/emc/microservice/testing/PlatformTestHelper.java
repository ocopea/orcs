// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.testing;

import com.emc.microservice.ServiceConfig;
import com.emc.microservice.registry.ServiceRegistryApi;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJacksonProvider;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by liebea on 1/4/15.
 * Drink responsibly
 */
public class PlatformTestHelper {
    private static final Logger log = LoggerFactory.getLogger(PlatformTestHelper.class);

    private final Map<String, ServiceConfig> serviceConfigurationMap;
    private final ResourceProvider resourceProvider;

    public PlatformTestHelper(Set<String> servicesNames, ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
        serviceConfigurationMap = new HashMap<>(servicesNames.size());
        for (String currServiceName : servicesNames) {
            serviceConfigurationMap.put(currServiceName, null);
        }
    }

    public void start() {
        start(0);
    }

    /***
     * Start the platform test and verify connectivity
     * @param initTimeOut timeout to wait for remote services to be responsive
     */
    public void start(int initTimeOut) {
        long startedAt = System.currentTimeMillis();
        // Creating the resource proxy
        // this initialization only needs to be done once per VM
        ResteasyProviderFactory resteasyProviderFactory = ResteasyProviderFactory.getInstance();
        resteasyProviderFactory.registerProvider(ResteasyJacksonProvider.class);
        RegisterBuiltin.register(resteasyProviderFactory);

        ServiceRegistryApi registryAPI = resourceProvider.getServiceRegistryApi();

        for (String currServiceURI : this.serviceConfigurationMap.keySet()) {
            log.debug("getting " + currServiceURI + " from registry");

            ServiceConfig currServiceConfig = null;

            // Polling registry until service is up or timed out
            while (currServiceConfig == null) {
                final boolean isLastTry = (System.currentTimeMillis() - startedAt) / 1000 >= initTimeOut;
                currServiceConfig = registryAPI.getServiceConfig(currServiceURI);
                if (currServiceConfig == null) {
                    if (isLastTry) {
                        throw new IllegalStateException(
                                "Failed retrieving service " + currServiceURI + " from registry");
                    } else {
                        log.debug(currServiceURI + " was not found in registry yet, retrying");
                    }
                } else {
                    /*
                    String route = getValidRoute(currServiceURI, currServiceConfig);
                    final ServiceState serviceState = resourceProvider.getWebAPIResolver().getWebAPI(
                    route, MicroServiceStateResource.class).getServiceState();
                    if (serviceState.getState() != ServiceState.ServiceStateEnum.RUNNING){
                        if (isLastTry) {
                            throw new IllegalStateException("Service " + currServiceURI +
                            " is not running, this is too long, not waiting bye");
                        }else {
                            log.debug(currServiceURI + " is in state " + serviceState.getState() + ", retrying");
                        }
                    }
                    */
                }
            }

            this.serviceConfigurationMap.put(currServiceURI, currServiceConfig);
        }
    }

    @NoJavadoc
    public <T> T createResource(String serviceBaseURI, Class<T> resourceClass) {
        ServiceConfig serviceConfig =
                Objects.requireNonNull(
                        serviceConfigurationMap.get(serviceBaseURI),
                        "Unsupported service " + serviceBaseURI);

        String route = getValidRoute(serviceBaseURI, serviceConfig);

        return resourceProvider.getWebAPIResolver().getWebAPI(route, resourceClass);

        //return ProxyFactory.create(resourceClass, route);
    }

    private String getValidRoute(String serviceBaseURI, ServiceConfig serviceConfig) {
        String route = serviceConfig.getRoute();
        if (route == null || route.isEmpty()) {
            throw new IllegalStateException("Service route for service " + serviceBaseURI + " must not be empty");
        } else if (!route.startsWith("http")) {
            throw new IllegalStateException("Invalid route format for service " + serviceBaseURI + ". route: " + route);
        } else if (!route.endsWith("/")) {
            route += "/";
        }
        return route;
    }
}
