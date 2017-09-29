// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.inspector.graph;

import com.emc.microservice.ServiceConfig;
import com.emc.microservice.messaging.QueueConfiguration;
import com.emc.microservice.messaging.QueueStats;
import com.emc.ocopea.services.rest.ServiceConfiguration;
import com.emc.ocopea.services.rest.ServiceState;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liebea on 1/7/15.
 * Drink responsibly
 */
public class ServiceGraph {
    private final Map<String, Service> servicesByRoute = new HashMap<>();
    private final Map<String, Queue> inputQueuesByURI = new HashMap<>();

    public Service addUnresponsiveService(ServiceConfig serviceConfig, String error) {
        return createService(serviceConfig, null, null).withError(error);

    }

    public Service addOnlineService(
            ServiceConfig serviceConfig,
            ServiceState serviceState,
            ServiceConfiguration serviceConfiguration) {
        return createService(serviceConfig, serviceState, serviceConfiguration);
    }

    private Service createService(
            ServiceConfig serviceConfig,
            ServiceState serviceState,
            ServiceConfiguration serviceConfiguration) {
        String inputQueueName = null;
        String outputQueueName = null;
        Service service =
                new Service(serviceConfig, serviceState, serviceConfiguration, inputQueueName, outputQueueName);
        servicesByRoute.put(serviceConfig.getRoute(), service);
        return service;
    }

    public void addInputQueueConfiguration(
            String queueURI,
            QueueConfiguration inputQueueConfiguration,
            QueueStats queueStats) {
        inputQueuesByURI.put(queueURI, new Queue(queueURI, inputQueueConfiguration, queueStats));
    }

    public Map<String, Service> getServicesByRoute() {
        return servicesByRoute;
    }

    public Map<String, Queue> getInputQueuesByURI() {
        return inputQueuesByURI;
    }

    public Collection<Service> getServices() {
        return servicesByRoute.values();
    }

}
