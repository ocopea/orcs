// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.inspector.graph;

import com.emc.microservice.ServiceConfig;
import com.emc.microservice.messaging.MessagingStatsConnection;
import com.emc.microservice.messaging.QueueConfiguration;
import com.emc.microservice.messaging.QueueStats;
import com.emc.microservice.registry.ServiceRegistryApi;
import com.emc.microservice.resource.ResourceProviderManager;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.services.rest.MicroServiceConfigurationAPI;
import com.emc.ocopea.services.rest.MicroServiceMetricsAPI;
import com.emc.ocopea.services.rest.MicroServiceStateAPI;
import com.emc.ocopea.services.rest.ServiceConfiguration;
import com.emc.ocopea.services.rest.ServiceState;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by liebea on 1/7/15.
 * Drink responsibly
 */
public class ServiceGraphBuilder {
    private static final Logger log = LoggerFactory.getLogger(ServiceGraphBuilder.class);

    private final ServiceRegistryApi serviceRegistryApi;
    private final MessagingStatsConnection messagingStatsConnection;

    public ServiceGraphBuilder(
            ServiceRegistryApi serviceRegistryApi,
            MessagingStatsConnection messagingStatsConnection) {
        this.serviceRegistryApi = serviceRegistryApi;
        this.messagingStatsConnection = messagingStatsConnection;
    }

    @NoJavadoc
    public ServiceGraph build() {
        ServiceGraph serviceGraph = new ServiceGraph();

        // Getting service instances from registry
        Map<String, ServiceConfig> allServiceConfigurations =
                this.serviceRegistryApi.listServiceConfig();

        for (ServiceConfig serviceConfig : allServiceConfigurations.values()) {
            String error = null;
            ServiceState serviceState = null;
            ServiceConfiguration serviceConfiguration = null;
            final Map<String, String> metrics = new HashMap<>();
            boolean responsive = true;
            try {
                // Getting service state directly from service state rest api
                MicroServiceStateAPI serviceStateAPI = ResourceProviderManager
                        .getResourceProvider()
                        .getWebAPIResolver()
                        .getWebAPI(serviceConfig.getRoute(), MicroServiceStateAPI.class);
                serviceState = serviceStateAPI.getServiceState();

                // Read service configuration
                MicroServiceConfigurationAPI configurationAPI = ResourceProviderManager
                        .getResourceProvider()
                        .getWebAPIResolver()
                        .getWebAPI(serviceConfig.getRoute(), MicroServiceConfigurationAPI.class);
                serviceConfiguration = configurationAPI.getServiceConfiguration();
                metrics.put("logger", serviceConfiguration.getLogger().getCategory());
                metrics.put("logLevel", serviceConfiguration.getLogger().getLevel());
            } catch (Exception ex) {
                responsive = false;
                error = ex.getMessage();
            }
            log.debug(
                    "Found Service: {} at : {} with state: {}",
                    serviceConfig.getServiceURI(),
                    serviceConfig.getRoute(),
                    error);

            Service service;
            if (serviceState == null) {
                service = serviceGraph.addUnresponsiveService(serviceConfig, error);
            } else {
                service = serviceGraph.addOnlineService(serviceConfig, serviceState, serviceConfiguration);
            }

            // Adding input queue configuration if available
            if (service.getInputQueueURI() != null) {
                addInputQueueConfiguration(serviceGraph, service.getInputQueueURI());
            }

            // Adding outputQueue configuration
            if (service.getOutputQueueURI() != null) {
                addInputQueueConfiguration(serviceGraph, service.getOutputQueueURI());
            }

            // If the service is responsive, trying to get more metrics/configuration
            if (responsive) {
                // Add metrics
                enhanceWithServiceMetrics(serviceConfig, metrics);
            }

            //Add version information from Manifest file
            if (serviceConfiguration != null) {
                metrics.put("version", serviceConfiguration.getVersion());
            }
            service.withMetrics(metrics);
        }
        return serviceGraph;
    }

    private void enhanceWithServiceMetrics(ServiceConfig serviceConfig, Map<String, String> counters) {
        try {
            MicroServiceMetricsAPI metricsAPI = ResourceProviderManager
                    .getResourceProvider()
                    .getWebAPIResolver()
                    .getWebAPI(serviceConfig.getRoute(), MicroServiceMetricsAPI.class);
            Response metricsOutput = metricsAPI.getMetricsOutput();
            try {
                InputStream inputStream = metricsOutput.readEntity(InputStream.class);
                final JsonFactory jsonFactory = new JsonFactory();
                final StringWriter stringWriter = new StringWriter();

                try (JsonParser parser = jsonFactory.createParser(inputStream)) {
                    ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
                    JsonNode o = objectMapper.readTree(parser);

                    JsonNode timers = o.with("metrics").with("timers");
                    Iterator<String> fieldNames = timers.fieldNames();
                    while (fieldNames.hasNext()) {
                        String timerName = fieldNames.next();
                        JsonNode jsonNode = timers.get(timerName);
                        JsonNode count = jsonNode.get("count");
                        if (count != null) {
                            counters.put(timerName + ".count", count.toString());
                        }
                    }

                    JsonNode countersNode = o.with("metrics").with("counters");
                    fieldNames = countersNode.fieldNames();
                    while (fieldNames.hasNext()) {
                        String counterName = fieldNames.next();
                        JsonNode jsonNode = countersNode.get(counterName);
                        JsonNode count = jsonNode.get("count");
                        if (count != null) {
                            counters.put(counterName + ".count", count.toString());
                        }
                    }

                    objectMapper.writerWithDefaultPrettyPrinter();
                    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                    objectMapper.writeValue(stringWriter, o);
                }
            } finally {
                metricsOutput.close();
            }
        } catch (Exception ex) {
            log.error("failed reading service metrics", ex);
        }
    }

    private void addInputQueueConfiguration(ServiceGraph serviceGraph, String inputQueueURI) {
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
