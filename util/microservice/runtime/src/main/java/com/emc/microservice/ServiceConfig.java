// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice;

import com.emc.ocopea.devtools.checkstyle.NoJavadoc;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liebea on 5/25/15.
 * Drink responsibly
 */
public class ServiceConfig {
    private final String serviceURI;
    private final String webServerName;
    private final String route;
    private final String globalLoggingConfig;
    private final Map<String, String> correlationLoggingConfig;
    private final Map<String, InputQueueConfig> inputQueueConfig;
    private final Map<String, DestinationQueueConfig> destinationQueueConfig;
    private final Map<String, DataSourceConfig> dataSourceConfig;
    private final Map<String, DataSourceConfig> blobstoreConfig;
    private final Map<String, String> parameters;
    private final Map<String, Map<String, String>> externalResourceConfig;

    /**
     * Ctor for  jackson
     */
    private ServiceConfig() {
        this(null, null, null, null, new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(),
                new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    private ServiceConfig(
            String serviceURI,
            String webServerName,
            String route,
            String globalLoggingConfig,
            Map<String, String> correlationLoggingConfig,
            Map<String, InputQueueConfig> inputQueueConfig,
            Map<String, DestinationQueueConfig> destinationQueueConfig,
            Map<String, DataSourceConfig> dataSourceConfig,
            Map<String, DataSourceConfig> blobstoreConfig,
            Map<String, String> parameters,
            Map<String, Map<String, String>> externalResourceConfig) {
        this.serviceURI = serviceURI;
        this.webServerName = webServerName;
        this.route = route;
        this.globalLoggingConfig = globalLoggingConfig;
        this.correlationLoggingConfig = correlationLoggingConfig;
        this.inputQueueConfig = inputQueueConfig;
        this.destinationQueueConfig = destinationQueueConfig;
        this.dataSourceConfig = dataSourceConfig;
        this.blobstoreConfig = blobstoreConfig;
        this.parameters = parameters;
        this.externalResourceConfig = externalResourceConfig;
    }

    @NoJavadoc
    public static ServiceConfig generateServiceConfig(ServiceConfig baseServiceConfig) {
        return generateServiceConfig(
                baseServiceConfig.getServiceURI(),
                baseServiceConfig.getWebServerName(),
                baseServiceConfig.getRoute(),
                baseServiceConfig.getGlobalLoggingConfig(),
                baseServiceConfig.getCorrelationLoggingConfig(),
                baseServiceConfig.getInputQueueConfig(),
                baseServiceConfig.getDestinationQueueConfig(),
                baseServiceConfig.getDataSourceConfig(),
                baseServiceConfig.getBlobstoreConfig(),
                baseServiceConfig.getParameters(),
                baseServiceConfig.getExternalResourceConfig());
    }

    @NoJavadoc
    public static ServiceConfig generateServiceConfig(
            String serviceURI,
            String webServerName,
            String route,
            String globalLoggingConfig,
            Map<String, String> correlationLoggingConfig,
            Map<String, InputQueueConfig> inputQueueConfig,
            Map<String, DestinationQueueConfig> destinationQueueConfig,
            Map<String, DataSourceConfig> dataSourceConfig,
            Map<String, DataSourceConfig> blobstoreConfig,
            Map<String, String> parameters,
            Map<String, Map<String, String>> externalResourceConfig) {
        return new ServiceConfig(
                serviceURI,
                webServerName == null ? "default" : webServerName,
                route,
                globalLoggingConfig,
                (correlationLoggingConfig == null) ? new HashMap<>() : correlationLoggingConfig,
                (inputQueueConfig == null) ? new HashMap<>() : inputQueueConfig,
                (destinationQueueConfig == null) ? new HashMap<>() : destinationQueueConfig,
                (dataSourceConfig == null) ? new HashMap<>() : dataSourceConfig,
                (blobstoreConfig == null) ? new HashMap<>() : blobstoreConfig,
                (parameters == null) ? new HashMap<>() : parameters,
                (externalResourceConfig == null) ? new HashMap<>() : externalResourceConfig);
    }

    public static class DataSourceConfig {
        private DataSourceConfig() {
            this(0, null);
        }

        private final int maxConnections;
        private final Map<String, String> properties;

        public DataSourceConfig(int maxConnections, Map<String, String> properties) {
            this.maxConnections = maxConnections;
            this.properties = properties;
        }

        public int getMaxConnections() {
            return maxConnections;
        }

        public Map<String, String> getProperties() {
            return properties;
        }
    }

    public static class InputQueueConfig {

        private final int numberOfConsumers;
        private final boolean logInDebug;
        private final List<String> deadLetterQueues;

        public InputQueueConfig() {
            this(0, false, Collections.<String>emptyList());
        }

        public InputQueueConfig(int numberOfConsumers, boolean logInDebug, List<String> deadLetterQueues) {
            this.numberOfConsumers = numberOfConsumers;
            this.logInDebug = logInDebug;
            this.deadLetterQueues = deadLetterQueues;
        }

        public int getNumberOfConsumers() {
            return numberOfConsumers;
        }

        public boolean isLogInDebug() {
            return logInDebug;
        }

        public List<String> getDeadLetterQueues() {
            return deadLetterQueues;
        }
    }

    public static class DestinationQueueConfig {
        private final String blobstoreNameSpace;
        private final String blobstoreKeyHeaderName;
        private boolean logInDebug;

        public DestinationQueueConfig() {
            this(null, null, false);
        }

        public DestinationQueueConfig(String blobstoreNameSpace, String blobstoreKeyHeaderName, boolean logInDebug) {
            this.blobstoreNameSpace = blobstoreNameSpace;
            this.blobstoreKeyHeaderName = blobstoreKeyHeaderName;
            this.logInDebug = logInDebug;
        }

        public String getBlobstoreNameSpace() {
            return blobstoreNameSpace;
        }

        public String getBlobstoreKeyHeaderName() {
            return blobstoreKeyHeaderName;
        }

        public boolean isLogInDebug() {
            return logInDebug;
        }
    }

    public String getServiceURI() {
        return serviceURI;
    }

    public String getWebServerName() {
        return webServerName;
    }

    public String getRoute() {
        return route;
    }

    public String getGlobalLoggingConfig() {
        return globalLoggingConfig;
    }

    public Map<String, String> getCorrelationLoggingConfig() {
        return correlationLoggingConfig;
    }

    public Map<String, InputQueueConfig> getInputQueueConfig() {
        return inputQueueConfig;
    }

    public Map<String, DestinationQueueConfig> getDestinationQueueConfig() {
        return destinationQueueConfig;
    }

    public Map<String, DataSourceConfig> getDataSourceConfig() {
        return dataSourceConfig;
    }

    public Map<String, DataSourceConfig> getBlobstoreConfig() {
        return blobstoreConfig;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public Map<String, Map<String, String>> getExternalResourceConfig() {
        return externalResourceConfig;
    }
}
