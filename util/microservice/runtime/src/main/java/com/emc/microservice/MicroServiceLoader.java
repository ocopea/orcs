// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice;

import com.emc.microservice.blobstore.BlobStoreConfiguration;
import com.emc.microservice.datasource.DatasourceConfiguration;
import com.emc.microservice.dependency.ServiceDependencyConfiguration;
import com.emc.microservice.dependency.ServiceDependencyDescriptor;
import com.emc.microservice.dservice.DynamicJavaServiceConfiguration;
import com.emc.microservice.messaging.DestinationConfiguration;
import com.emc.microservice.messaging.InputQueueConfiguration;
import com.emc.microservice.messaging.InputQueueDescriptor;
import com.emc.microservice.registry.ServiceRegistryApi;
import com.emc.microservice.resource.ExternalResourceManagerWrapper;
import com.emc.microservice.resource.ResourceConfiguration;
import com.emc.microservice.resource.ResourceDescriptor;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.microservice.restapi.WebServerConfiguration;
import com.emc.microservice.schedule.SchedulerConfiguration;
import com.emc.microservice.singleton.SingletonConfiguration;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created with true love by liebea on 10/20/2014.
 */
class MicroServiceLoader {

    /***
     * Helper class containing all sort of configuration for resources needed by server
     */
    static class ServiceRuntimeData {
        private final WebServerConfiguration webServerConfiguration;
        private final Map<String, String> paramValues;
        private final Map<String, InputQueueConfiguration> queuesConfiguration;
        private final Map<String, DestinationConfiguration> messageDestinationConfiguration;
        private final Map<String, DatasourceConfiguration> datasourceConfigurations;
        private final Map<String, ServiceDependencyConfiguration> serviceDependencyConfiguration;
        private final Map<String, BlobStoreConfiguration> blobStoreConfiguration;
        private final Map<String, SingletonConfiguration> singletonsConfiguration;
        private final Map<String, DynamicJavaServiceConfiguration> dynamicJavaServiceConfigurationMap;
        private final Map<String, SchedulerConfiguration> schedulerConfigurationMap;
        private final Map<Class<? extends ResourceDescriptor>,
                Map<String, ResourceConfiguration>> externalResourceManagersConfigurations;

        private ServiceRuntimeData(
                WebServerConfiguration webServerConfiguration,
                Map<String, String> paramValues,
                Map<String, InputQueueConfiguration> queuesConfiguration,
                Map<String, DestinationConfiguration> messageDestinationConfiguration,
                Map<String, DatasourceConfiguration> datasourceConfigurations,
                Map<String, ServiceDependencyConfiguration> serviceDependencyConfiguration,
                Map<String, BlobStoreConfiguration> blobStoreConfiguration,
                Map<String, SingletonConfiguration> singletonsConfiguration,
                Map<String, DynamicJavaServiceConfiguration> dynamicJavaServiceConfigurationMap,
                Map<String, SchedulerConfiguration> schedulerConfigurationMap,
                Map<Class<? extends ResourceDescriptor>,
                        Map<String, ResourceConfiguration>> externalResourceManagersConfigurations) {

            this.webServerConfiguration = webServerConfiguration;
            this.paramValues = paramValues;
            this.queuesConfiguration = queuesConfiguration;
            this.messageDestinationConfiguration = messageDestinationConfiguration;
            this.datasourceConfigurations = datasourceConfigurations;
            this.serviceDependencyConfiguration = serviceDependencyConfiguration;
            this.blobStoreConfiguration = blobStoreConfiguration;
            this.singletonsConfiguration = singletonsConfiguration;
            this.dynamicJavaServiceConfigurationMap = dynamicJavaServiceConfigurationMap;
            this.schedulerConfigurationMap = schedulerConfigurationMap;
            this.externalResourceManagersConfigurations = externalResourceManagersConfigurations;
        }

        public WebServerConfiguration getWebServerConfiguration() {
            return webServerConfiguration;
        }

        public Map<String, String> getParamValues() {
            return paramValues;
        }

        public Map<String, InputQueueConfiguration> getQueuesConfiguration() {
            return queuesConfiguration;
        }

        public Map<String, DestinationConfiguration> getMessageDestinationConfiguration() {
            return messageDestinationConfiguration;
        }

        public Map<String, DatasourceConfiguration> getDatasourceConfigurations() {
            return datasourceConfigurations;
        }

        public Map<String, ServiceDependencyConfiguration> getServiceDependencyConfiguration() {
            return serviceDependencyConfiguration;
        }

        public Map<String, SingletonConfiguration> getSingletonsConfiguration() {
            return singletonsConfiguration;
        }

        public Map<String, DynamicJavaServiceConfiguration> getDynamicJavaServiceConfigurationMap() {
            return dynamicJavaServiceConfigurationMap;
        }

        public Map<String, SchedulerConfiguration> getSchedulerConfigurationMap() {
            return schedulerConfigurationMap;
        }

        public Map<String, BlobStoreConfiguration> getBlobStoreConfiguration() {
            return blobStoreConfiguration;
        }

        public Map<Class<? extends ResourceDescriptor>,
                Map<String, ResourceConfiguration>> getExternalResourceManagersConfigurations() {

            return externalResourceManagersConfigurations;
        }
    }

    static ServiceRuntimeData loadServiceRuntimeData(
            MicroServiceController serviceController,
            ResourceProvider resourceProvider) {

        Logger logger = serviceController.getContext().createSubLogger(MicroServiceLoader.class);
        logger.debug("Parsing service configuration for service {}", serviceController.getName());

        // Getting Registry API proxy
        ServiceRegistryApi registryAPI = resourceProvider.getServiceRegistryApi();

        ServiceConfig serviceConfig = registryAPI.getServiceConfig(serviceController.getBaseURI());
        if (serviceConfig == null) {
            logger.warn("ServiceConfig cannot be found in registryAPI for baseURI " + serviceController.getBaseURI());
        }

        // Service parameters
        Map<String, String> serviceParameterValues = loadServiceParameters(serviceController, serviceConfig, logger);

        // Setting queue configurations
        Map<String, InputQueueConfiguration> queuesConfiguration =
                loadInputQueuesConfiguration(
                        serviceController,
                        serviceConfig,
                        logger,
                        registryAPI);

        // Setting destinations configuration
        Map<String, DestinationConfiguration> messageDestinationConfiguration =
                loadDestinationConfiguration(
                        logger,
                        serviceController,
                        serviceConfig,
                        registryAPI);

        // Setting the datasources configurations
        Map<String, DatasourceConfiguration> datasourceConfigurations =
                loadDatasourceConfiguration(logger, serviceController,
                        registryAPI, resourceProvider);

        // Setting blobstore configurations
        Map<String, BlobStoreConfiguration> blobStoreConfigurations =
                loadBlobStoreConfiguration(logger, serviceController,
                        registryAPI, resourceProvider);

        // Init Singleton configurations
        Map<String, SingletonConfiguration> singletonsConfiguration =
                loadSingletonConfiguration(
                        logger,
                        serviceController,
                        registryAPI);

        // Init Dynamic Java Services configurations
        Map<String, DynamicJavaServiceConfiguration> dynamicJavaServiceConfiguration =
                loadDynamicJavaServiceConfiguration(
                        logger,
                        serviceController,
                        registryAPI);

        Map<String, SchedulerConfiguration> schedulerConfigurationMap =
                loadSchedulerConfiguration(
                        logger,
                        serviceController,
                        registryAPI,
                        resourceProvider);

        // Setting the service dependencies configurations
        Map<String, ServiceDependencyConfiguration> dependencyConfiguration =
                loadServiceDependencyConfiguration(serviceController);

        // Loading external services configurations if exists
        Map<Class<? extends ResourceDescriptor>,
                Map<String, ResourceConfiguration>> externalResourceManagersConfigurations =
                loadExternalResourcesConfiguration(
                        serviceController,
                        logger,
                        registryAPI);

        return
                new ServiceRuntimeData(
                        registryAPI.getWebServerConfiguration(
                                resourceProvider.getWebServerConfigurationClass(),
                                serviceConfig == null ? "default" : serviceConfig.getWebServerName()),
                        serviceParameterValues,
                        queuesConfiguration,
                        messageDestinationConfiguration,
                        datasourceConfigurations,
                        dependencyConfiguration,
                        blobStoreConfigurations,
                        singletonsConfiguration,
                        dynamicJavaServiceConfiguration,
                        schedulerConfigurationMap,
                        externalResourceManagersConfigurations);
    }

    private static Map<Class<? extends ResourceDescriptor>,
            Map<String, ResourceConfiguration>> loadExternalResourcesConfiguration(
            final MicroServiceController service,
            Logger logger, ServiceRegistryApi registryAPI) {

        Map<Class<? extends ResourceDescriptor>,
                Map<String, ResourceConfiguration>> externalResourceManagersConfigurations = new HashMap<>();

        for (final ExternalResourceManagerWrapper<?, ?, ?> currExternalResourceManager :
                service.getExternalResourceManagers()) {

            @SuppressWarnings("unchecked")
            Map<String, ResourceConfiguration> externalResourceConfigurationMap = loadConfiguration(
                    logger,
                    registryAPI,
                    currExternalResourceManager.getDescriptors(), (ConfigurationLoader) (registryAPI1, descriptor) -> {
                        // first try to take from the service config
                        final Map<String, Map<String, String>> externalResourceConfig =
                                registryAPI1.getServiceConfig(service.getBaseURI()).getExternalResourceConfig();
                        if (externalResourceConfig != null) {
                            final Map<String, String> externalConfig = externalResourceConfig.get(descriptor.getName());
                            if (externalConfig != null) {
                                return ResourceConfiguration.asSpecificConfiguration(
                                        currExternalResourceManager.getResourceConfigurationClass(),
                                        externalConfig);
                            }
                        }
                        // not there - take the global one
                        return registryAPI1
                                .getExternalResourceConfiguration(
                                        descriptor.getName(),
                                        currExternalResourceManager.getResourceConfigurationClass());
                    },
                    currExternalResourceManager.getResourceTypeName());

            externalResourceManagersConfigurations.put(
                    currExternalResourceManager.getDescriptorClass(),
                    externalResourceConfigurationMap);
        }
        return externalResourceManagersConfigurations;
    }

    private static Map<String, ServiceDependencyConfiguration> loadServiceDependencyConfiguration(
            MicroServiceController service) {

        Map<String, ServiceDependencyConfiguration> dependencyConfiguration = new HashMap<>();
        for (ServiceDependencyDescriptor currServiceDependencyDescriptor :
                service.getDependencyManager().getDescriptors()) {

            // todo:amit-ashish: service dependency configurations - where to take from?
            dependencyConfiguration.put(
                    currServiceDependencyDescriptor.getName(),
                    new ServiceDependencyConfiguration(1));
        }
        return dependencyConfiguration;
    }

    /***
     * Load input queues configuration
     */
    private static Map<String, InputQueueConfiguration> loadInputQueuesConfiguration(
            MicroServiceController service,
            final ServiceConfig serviceConfig,
            Logger logger,
            ServiceRegistryApi registryAPI) {

        ConfigurationLoader<InputQueueConfiguration, InputQueueDescriptor> queueConfig = (registryAPI1, descriptor) -> {
            ServiceConfig.InputQueueConfig inputQueueConfig =
                    Objects.requireNonNull(
                            Objects.requireNonNull(
                                    serviceConfig.getInputQueueConfig(),
                                    "Service input queues configuration not loaded").get(descriptor.getName()),
                            "Missing input queue service configuration for " + descriptor.getName());

            return new InputQueueConfiguration(
                    descriptor.getName(),
                    inputQueueConfig.getNumberOfConsumers(),
                    inputQueueConfig.isLogInDebug(),
                    inputQueueConfig.getDeadLetterQueues() == null ? null : inputQueueConfig.getDeadLetterQueues());
        };
        return loadConfiguration(
                logger,
                registryAPI,
                service.getQueuesManager().getDescriptors(),
                queueConfig,
                "input queue");
    }

    /***
     * Load Blobstore configuration
     */
    private static Map<String, BlobStoreConfiguration> loadBlobStoreConfiguration(
            Logger logger,
            MicroServiceController service,
            ServiceRegistryApi registryAPI,
            ResourceProvider resourceProvider) {
        return loadConfiguration(
                logger,
                registryAPI,
                service.getBlobStoreManager().getDescriptors(),
                (registryAPI1, descriptor) ->
                        registryAPI1.getBlobStoreConfiguration(
                                resourceProvider.getBlobStoreConfigurationClass(),
                                descriptor.getName()),
                "blobstore");
    }

    private static Map<String, SingletonConfiguration> loadSingletonConfiguration(
            Logger logger,
            MicroServiceController service,
            ServiceRegistryApi registryAPI) {

        return loadConfiguration(
                logger,
                registryAPI,
                service.getSingletonManager().getDescriptors(),
                (registryAPI1, descriptor) -> new SingletonConfiguration(),
                "singleton");
    }

    private static Map<String, DynamicJavaServiceConfiguration> loadDynamicJavaServiceConfiguration(
            Logger logger,
            MicroServiceController service,
            ServiceRegistryApi registryAPI) {

        return loadConfiguration(
                logger,
                registryAPI,
                service.getDynamicJavaServiceManager().getDescriptors(),
                (registryAPI1, descriptor) -> new DynamicJavaServiceConfiguration(), "dynamicJavaService");
    }

    private static Map<String, SchedulerConfiguration> loadSchedulerConfiguration(
            Logger logger,
            MicroServiceController service,
            ServiceRegistryApi registryAPI, ResourceProvider resourceProvider) {

        return loadConfiguration(
                logger,
                registryAPI,
                service.getSchedulerManager().getDescriptors(),
                (registryAPI1, descriptor) -> registryAPI.getSchedulerConfiguration(
                        resourceProvider.getSchedulerConfigurationClass(),
                        descriptor.getName()),
                "scheduler");
    }

    /***
     * Loading datasource configuration
     */
    private static Map<String, DatasourceConfiguration> loadDatasourceConfiguration(
            Logger logger,
            MicroServiceController service,
            ServiceRegistryApi registryAPI,
            ResourceProvider resourceProvider) {
        return loadConfiguration(
                logger,
                registryAPI,
                service.getDatasourceManager().getDescriptors(),
                (registryAPI1, descriptor) -> registryAPI1.getDataSourceConfiguration(
                        resourceProvider.getDatasourceConfigurationClass(),
                        descriptor.getName()),
                "datasource");
    }

    private interface ConfigurationLoader<C extends ResourceConfiguration, D extends ResourceDescriptor> {
        C loadConfiguration(ServiceRegistryApi registryAPI, D descriptor);
    }

    private static <C extends ResourceConfiguration, D extends ResourceDescriptor> Map<String, C> loadConfiguration(
            Logger logger,
            ServiceRegistryApi registryAPI,
            List<D> descriptors,
            ConfigurationLoader<C, D> configurationLoader,
            String configurationName) {

        Map<String, C> configurations = new HashMap<>();
        for (D currDescriptor : descriptors) {
            C configuration = null;

            /* numOfFailed10SecAttempts : variable that counts number of times the loop was run.
             * Will be reset after a message is logged
             * loggingThreshold : threshold at which the message should be logged.
             *              Need to log at 10th min, 20th min, 40th min, and then every 60th min.
             *              Set to 5 so it can then be easily doubled to 10, 20, 40.
             */
            int numOfFailed10SecAttempts = 0;
            int loggingThreshold = 5;

            while (configuration == null) {
                try {
                    configuration = Objects.requireNonNull(
                            configurationLoader.loadConfiguration(registryAPI, currDescriptor),
                            "Could not locate " + configurationName + " configuration in service registry for \"" +
                                    currDescriptor.getName() + "\"");

                    configurations.put(currDescriptor.getName(), configuration);
                } catch (Exception ex) {
                    logger.debug("Failed loading " + currDescriptor.getName() + " " + configurationName +
                            " configuration, retrying", ex);

                    if (numOfFailed10SecAttempts == 0 || numOfFailed10SecAttempts / 6 >= loggingThreshold) {
                        logger.info(
                                "Failed loading " + currDescriptor.getName() + " " + configurationName +
                                        " configuration: " + ex.getMessage() + " retrying");
                        // message logged, reset the numOfFailed10SecAttempts variable
                        numOfFailed10SecAttempts = 0;
                        // logic to determine the loggingThreshold increments.
                        // ie it will be set to 10, 20, 40 and then 60 always
                        loggingThreshold = Math.min(loggingThreshold * 2, 60);
                    }

                    numOfFailed10SecAttempts++;
                    sleepNoException(10000);
                }
            }

        }
        return configurations;
    }

    /***
     * Loading destination configurations
     */
    private static Map<String, DestinationConfiguration> loadDestinationConfiguration(
            Logger logger,
            MicroServiceController service,
            final ServiceConfig serviceConfig,
            ServiceRegistryApi registryAPI) {

        return loadConfiguration(logger, registryAPI, service.getDestinationManager().getDescriptors(),
                (registryAPI1, descriptor) -> {
                    ServiceConfig.DestinationQueueConfig destinationConfig = Objects.requireNonNull(
                            Objects.requireNonNull(
                                    serviceConfig.getDestinationQueueConfig(),
                                    "Service destination queues configuration not loaded")
                                    .get(descriptor.getName()),
                            "Missing destination service configuration for " + descriptor.getName());

                    return new DestinationConfiguration(descriptor.getName(), destinationConfig.getBlobstoreNameSpace(),
                            destinationConfig.getBlobstoreKeyHeaderName(), destinationConfig.isLogInDebug());
                }, "destination");
    }

    /***
     * Getting overriding service parameter values if such defined as system properties
     *
     * @param service       the micro-service descriptor
     * @param serviceConfig service configuration
     * @param logger        logger to use for logging messages @return map of property name to overridden property value
     */
    private static Map<String, String> loadServiceParameters(
            MicroServiceController service,
            ServiceConfig serviceConfig,
            Logger logger) {

        Map<String, String> serviceParameterValues = new HashMap<>();

        if (serviceConfig != null &&
                serviceConfig.getParameters() != null && !serviceConfig.getParameters().isEmpty()) {

            for (Map.Entry<String, ParametersBag.MicroServiceParameterDescriptor> currParamEntry :
                    service.getParams().getParameterDescriptorsMap().entrySet()) {

                String propertyValue = serviceConfig.getParameters().get(currParamEntry.getValue().getName());
                if (propertyValue != null) {
                    logger.info("Overriding property value {} with {}", currParamEntry.getKey(), propertyValue);
                } else {
                    propertyValue = currParamEntry.getValue().getDefaultValue();
                }
                serviceParameterValues.put(currParamEntry.getKey(), propertyValue);
            }
        }
        return serviceParameterValues;
    }

    private static void sleepNoException(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            // Ignoring
        }
    }

}
