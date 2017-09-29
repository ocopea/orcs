// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.inspector.graph;

import com.emc.microservice.ServiceConfig;
import com.emc.microservice.blobstore.BlobStoreConfiguration;
import com.emc.microservice.datasource.DatasourceConfiguration;
import com.emc.microservice.messaging.MessagingStatsConnection;
import com.emc.microservice.messaging.QueueConfiguration;
import com.emc.microservice.messaging.QueueStats;
import com.emc.microservice.registry.ServiceRegistryApi;
import com.emc.microservice.resource.ResourceProviderManager;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liebea on 1/7/15.
 * Drink responsibly
 */
public class ServiceGraphBuilder2 {
    private static final Logger log = LoggerFactory.getLogger(ServiceGraphBuilder2.class);

    private final ServiceRegistryApi serviceRegistryApi;
    private final MessagingStatsConnection messagingStatsConnection;

    public ServiceGraphBuilder2(
            ServiceRegistryApi serviceRegistryApi,
            MessagingStatsConnection messagingStatsConnection) {
        this.serviceRegistryApi = serviceRegistryApi;
        this.messagingStatsConnection = messagingStatsConnection;
    }

    @NoJavadoc
    public ServiceGraph2 build() {
        ServiceGraph2 serviceGraph = new ServiceGraph2();

        // Getting service instances from registry
        Map<String, ServiceConfig> allServiceConfigurations =
                this.serviceRegistryApi.listServiceConfig();

        for (ServiceConfig currSC : allServiceConfigurations.values()) {
            final Map<String, String> metrics = new HashMap<>();
            metrics.put("logLevel", currSC.getGlobalLoggingConfig());

            serviceGraph.addOnlineService(currSC);

            // Adding input queue configuration if available
            for (Map.Entry<String, ServiceConfig.InputQueueConfig> currInputQ :
                    currSC.getInputQueueConfig().entrySet()) {
                addInputQueueConfiguration(serviceGraph, currInputQ.getKey());
            }
            for (Map.Entry<String, ServiceConfig.DestinationQueueConfig> currDestination :
                    currSC.getDestinationQueueConfig().entrySet()) {
                addInputQueueConfiguration(serviceGraph, currDestination.getKey());
            }

            // Adding datasources
            final Map<String, DatasourceConfiguration> stringDatasourceConfigurationMap =
                    serviceRegistryApi.listDataSources(
                            ResourceProviderManager.getResourceProvider().getDatasourceConfigurationClass());
            for (Map.Entry<String, DatasourceConfiguration> curr : stringDatasourceConfigurationMap.entrySet()) {
                serviceGraph.addDataSourceProperties(curr.getKey(), curr.getValue().getPublicPropertyValues());
            }

            // Adding blob stores
            for (Map.Entry<String, ? extends BlobStoreConfiguration> curr :
                    serviceRegistryApi.listBlobStores(ResourceProviderManager.getResourceProvider()
                            .getBlobStoreConfigurationClass()).entrySet()) {
                serviceGraph.addBlobstoreProperties(curr.getKey(), curr.getValue().getPublicPropertyValues());
            }

            metrics.putAll(currSC.getParameters());
        }
        return serviceGraph;
    }

    private void addInputQueueConfiguration(ServiceGraph2 serviceGraph, String inputQueueURI) {
        QueueConfiguration inputQueueConfiguration = serviceRegistryApi.getQueueConfiguration(
                ResourceProviderManager.getResourceProvider().getQueueConfigurationClass(), inputQueueURI, null);
        if (inputQueueConfiguration != null) {
            QueueStats queueStats = null;
            if (messagingStatsConnection != null) {
                try {
                    queueStats = messagingStatsConnection.getQueueStats(inputQueueURI);
                } catch (Exception ex) {
                    log.warn("Failed getting queue stats for queue " + inputQueueURI, ex);
                }
            }

            serviceGraph.addInputQueueConfiguration(inputQueueURI, inputQueueConfiguration, queueStats);
            if (log.isDebugEnabled()) {
                log.debug("InputQueue " + inputQueueConfiguration.toString());
            }
        }
    }
}
