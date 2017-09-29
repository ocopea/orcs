// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.discovery;

import com.emc.microservice.Context;
import com.emc.microservice.ServiceConfig;
import com.emc.microservice.registry.ServiceRegistryApi;

/**
 * Created by liebea on 5/2/16.
 * Drink responsibly
 */
public class ServiceDiscoveryManagerImpl implements ServiceDiscoveryManager {

    private final Context context;
    private final ServiceRegistryApi serviceRegistryApi;

    public ServiceDiscoveryManagerImpl(ServiceRegistryApi serviceRegistryApi, Context context) {
        this.context = context;
        this.serviceRegistryApi = serviceRegistryApi;
    }

    @Override
    public DiscoveredService discoverService(String serviceURN) {
        ServiceConfig serviceConfig = getServiceConfig(serviceURN);
        if (serviceConfig == null) {
            return null;
        }

        // todo:parameters should be "bind-parameters here", should split it in ServiceConfig
        return new DiscoveredService(
                serviceConfig.getServiceURI(),
                serviceConfig.getRoute(),
                serviceConfig.getParameters());
    }

    private ServiceConfig getServiceConfig(String serviceName) {
        return serviceRegistryApi.getServiceConfig(serviceName);
    }

    @Override
    public WebAPIConnection discoverServiceConnection(String serviceURN) {
        return new WebAPIConnectionImpl(this, serviceURN, context.getWebAPIResolver());
    }
}
