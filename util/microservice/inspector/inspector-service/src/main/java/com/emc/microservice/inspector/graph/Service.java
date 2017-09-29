// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.inspector.graph;

import com.emc.microservice.ServiceConfig;
import com.emc.ocopea.services.rest.ResourceConfig;
import com.emc.ocopea.services.rest.ServiceConfiguration;
import com.emc.ocopea.services.rest.ServiceState;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liebea on 1/7/15.
 * Drink responsibly
 */
public class Service {
    private final ServiceConfig serviceConfig;
    private final ServiceState serviceState;
    private final ServiceConfiguration serviceConfiguration;
    private final String inputQueueURI;
    private final String outputQueueURI;

    private String error;
    private Map<String, String> metrics = new HashMap<>();

    public Service(
            ServiceConfig serviceConfig,
            ServiceState serviceState,
            ServiceConfiguration serviceConfiguration,
            String inputQueueURI,
            String outputQueueURI) {
        this.serviceConfig = serviceConfig;
        this.serviceState = serviceState;
        this.serviceConfiguration = serviceConfiguration;
        this.inputQueueURI = inputQueueURI;
        this.outputQueueURI = outputQueueURI;
    }

    public ServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    public ServiceState getServiceState() {
        return serviceState;
    }

    public String getInputQueueURI() {
        return inputQueueURI;
    }

    public String getOutputQueueURI() {
        return outputQueueURI;
    }

    public Service withError(String error) {
        this.error = error;
        return this;
    }

    public Service withMetrics(Map<String, String> metrics) {
        this.metrics.putAll(metrics);
        return this;
    }

    public String getError() {
        return error;
    }

    public Map<String, String> getMetrics() {
        return metrics;
    }

    public String getStateMessage() {
        if (serviceState != null) {
            return this.serviceState.getState().name();
        } else {
            return error;
        }
    }

    public Map<String, ResourceConfig> getDatasourcesByName() {
        return getResourceConfigsByName();
    }

    private Map<String, ResourceConfig> getResourceConfigsByName() {
        if (serviceConfiguration == null) {
            return Collections.emptyMap();
        }
        return getResourceConfigByName(serviceConfiguration.getDatasources());
    }

    private Map<String, ResourceConfig> getResourceConfigByName(List<ResourceConfig> resourceConfigs) {
        Map<String, ResourceConfig> resConfigByName = new HashMap<>();
        if (resourceConfigs != null) {
            for (ResourceConfig currDS : resourceConfigs) {
                resConfigByName.put(currDS.getName(), currDS);
            }
        }
        return resConfigByName;
    }

    public Map<String, ResourceConfig> getBlobStoresByName() {
        return serviceConfiguration == null ? Collections.<String, ResourceConfig>emptyMap()
                : getResourceConfigByName(serviceConfiguration.getBlobStores());
    }

    public Map<String, ResourceConfig> getServiceDependencyConfigurations() {
        return serviceConfiguration == null ? Collections.<String, ResourceConfig>emptyMap()
                : getResourceConfigByName(serviceConfiguration.getServiceDependencies());
    }

    public Map<String, ResourceConfig> getDestinationConfigurations() {
        return serviceConfiguration == null ? Collections.<String, ResourceConfig>emptyMap()
                : getResourceConfigByName(serviceConfiguration.getDestinations());
    }

    public Map<String, ResourceConfig> getInputQueuesConfigurations() {
        return serviceConfiguration == null ? Collections.<String, ResourceConfig>emptyMap()
                : getResourceConfigByName(serviceConfiguration.getInputQueues());
    }

    public Map<String, ResourceConfig> getExternalResourceConfiguraitons() {
        return serviceConfiguration == null ? Collections.<String, ResourceConfig>emptyMap()
                : getResourceConfigByName(serviceConfiguration.getExternalResources());
    }

}
