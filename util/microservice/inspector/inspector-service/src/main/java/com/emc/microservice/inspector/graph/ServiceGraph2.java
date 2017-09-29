// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.inspector.graph;

import com.emc.microservice.ServiceConfig;
import com.emc.microservice.messaging.QueueConfiguration;
import com.emc.microservice.messaging.QueueStats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liebea on 1/7/15.
 * Drink responsibly
 */
public class ServiceGraph2 {
    private final Map<String, ServiceConfig> servicesByShortName = new HashMap<>();
    private final Map<String, Queue> inputQueuesByURI = new HashMap<>();
    private final Map<String, Map<String, String>> datasourceParameters = new HashMap<>();
    private final Map<String, Map<String, String>> blobstoreParameters = new HashMap<>();

    public void addOnlineService(ServiceConfig serviceConfig) {
        servicesByShortName.put(serviceConfig.getServiceURI(), serviceConfig);
    }

    public void addInputQueueConfiguration(
            String queueURI,
            QueueConfiguration inputQueueConfiguration,
            QueueStats queueStats) {
        inputQueuesByURI.put(queueURI, new Queue(queueURI, inputQueueConfiguration, queueStats));
    }

    public void addDataSourceProperties(String name, Map<String, String> properties) {
        datasourceParameters.put(name, properties);
    }

    public void addBlobstoreProperties(String name, Map<String, String> properties) {
        blobstoreParameters.put(name, properties);
    }

    public Map<String, Queue> getInputQueuesByURI() {
        return inputQueuesByURI;
    }

    public Collection<ServiceConfig> getServices() {
        return new ArrayList<>(servicesByShortName.values());
    }

    public Map<String, Map<String, String>> getDatasourceParameters() {
        return datasourceParameters;
    }

    public Map<String, Map<String, String>> getBlobstoreParameters() {
        return blobstoreParameters;
    }
}
