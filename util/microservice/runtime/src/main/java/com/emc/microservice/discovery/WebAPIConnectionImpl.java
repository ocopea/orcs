// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.discovery;

import com.emc.microservice.webclient.WebAPIResolver;

import javax.ws.rs.client.WebTarget;
import java.util.Objects;

/**
 * Created by liebea on 5/2/16.
 * Drink responsibly
 */
public class WebAPIConnectionImpl implements WebAPIConnection {

    private final ServiceDiscoveryManager serviceDiscoveryManager;
    private final String serviceName;
    private final WebAPIResolver webAPIResolver;

    public WebAPIConnectionImpl(
            ServiceDiscoveryManager serviceDiscoveryManager,
            String serviceName,
            WebAPIResolver webAPIResolver) {
        this.serviceDiscoveryManager = serviceDiscoveryManager;
        this.serviceName = serviceName;
        this.webAPIResolver = webAPIResolver;
    }

    @Override
    public <T> T resolve(Class<T> resourceClass) {
        return webAPIResolver.getWebAPI(getDiscoveredService().getServiceURL(), resourceClass);
    }

    private DiscoveredService getDiscoveredService() {
        return Objects.requireNonNull(
                serviceDiscoveryManager.discoverService(serviceName),
                "No service dude: " + serviceName);
    }

    @Override
    public WebTarget getWebTarget() {
        return webAPIResolver.getWebTarget(getDiscoveredService().getServiceURL());
    }

}
