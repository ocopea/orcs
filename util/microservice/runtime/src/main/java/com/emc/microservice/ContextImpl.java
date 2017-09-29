// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice;

import com.emc.microservice.blobstore.BlobStoreConfiguration;
import com.emc.microservice.blobstore.BlobStoreDescriptor;
import com.emc.microservice.blobstore.ManagedBlobStore;
import com.emc.microservice.cache.LocalCacheManager;
import com.emc.microservice.datasource.DatasourceConfiguration;
import com.emc.microservice.datasource.DatasourceDescriptor;
import com.emc.microservice.datasource.ManagedDatasource;
import com.emc.microservice.dependency.ManagedDependency;
import com.emc.microservice.dependency.ServiceDependencyConfiguration;
import com.emc.microservice.dependency.ServiceDependencyDescriptor;
import com.emc.microservice.discovery.ServiceDiscoveryManager;
import com.emc.microservice.discovery.ServiceDiscoveryManagerImpl;
import com.emc.microservice.dservice.DynamicJavaServiceConfiguration;
import com.emc.microservice.dservice.DynamicJavaServiceDescriptor;
import com.emc.microservice.dservice.ManagedDynamicJavaService;
import com.emc.microservice.health.HealthCheckManager;
import com.emc.microservice.input.InputDescriptor;
import com.emc.microservice.input.MessagingInputDescriptor;
import com.emc.microservice.messaging.DestinationConfiguration;
import com.emc.microservice.messaging.DestinationDescriptor;
import com.emc.microservice.messaging.InputQueueConfiguration;
import com.emc.microservice.messaging.InputQueueDescriptor;
import com.emc.microservice.messaging.ManagedInputQueue;
import com.emc.microservice.messaging.ManagedMessageDestination;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.messaging.MessageSender;
import com.emc.microservice.messaging.MessageSenderImpl;
import com.emc.microservice.messaging.MessagingProviderConfiguration;
import com.emc.microservice.messaging.RuntimeMessageSender;
import com.emc.microservice.metrics.MetricsRegistryImpl;
import com.emc.microservice.output.OutputDescriptor;
import com.emc.microservice.output.ServiceOutputDescriptor;
import com.emc.microservice.resource.ManagedResource;
import com.emc.microservice.resource.ResourceConfiguration;
import com.emc.microservice.resource.ResourceDescriptor;
import com.emc.microservice.resource.ResourceManager;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.microservice.restapi.MicroServiceWebServer;
import com.emc.microservice.restapi.RestResourceManager;
import com.emc.microservice.schedule.ManagedScheduler;
import com.emc.microservice.schedule.SchedulerConfiguration;
import com.emc.microservice.schedule.SchedulerDescriptor;
import com.emc.microservice.serialization.SerializationManagerImpl;
import com.emc.microservice.singleton.ManagedSingleton;
import com.emc.microservice.singleton.SingletonConfiguration;
import com.emc.microservice.singleton.SingletonDescriptor;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created with love by liebea on 4/7/2015.
 */
public class ContextImpl implements Context {
    public static final String MS_API_OUTPUT_QUEUE_NAME_HEADER = "_MS_API_OUTPUT_QUEUE_NAME";
    public static final String MS_API_ROUTING_PLAN_HEADER = "_MS_API_ROUTING_PLAN";

    private final MicroServiceController serviceController;
    private final MicroService serviceDescriptor;
    private final ParametersBag parametersBag;
    private final ResourceProvider resourceProvider;
    private final MetricsRegistryImpl metricsRegistry;
    private final ResourceManager<
            DestinationDescriptor,
            DestinationConfiguration,
            ManagedMessageDestination> destinationManager;
    private final ResourceManager<
            InputQueueDescriptor,
            InputQueueConfiguration,
            ManagedInputQueue> queuesManager;
    private final ResourceManager<
            DatasourceDescriptor,
            DatasourceConfiguration,
            ManagedDatasource> datasourceManager;
    private final ResourceManager<
            ServiceDependencyDescriptor,
            ServiceDependencyConfiguration,
            ManagedDependency> dependencyManager;
    private final ResourceManager<
            BlobStoreDescriptor,
            BlobStoreConfiguration,
            ManagedBlobStore> blobStoreManager;
    private final ResourceManager<
            SingletonDescriptor,
            SingletonConfiguration,
            ManagedSingleton> singletonManager;
    private final ResourceManager<
            DynamicJavaServiceDescriptor,
            DynamicJavaServiceConfiguration,
            ManagedDynamicJavaService> dynamicJavaServicesManager;
    private final ResourceManager<
            SchedulerDescriptor,
            SchedulerConfiguration,
            ManagedScheduler> schedulerManager;

    private final HealthCheckManager healthCheckManager;
    private final SerializationManagerImpl serializationManager;
    private final MicroServiceWebServer webServer;
    private final Map<Class<? extends ResourceDescriptor>, ResourceManager> resourceManagersByDescriptor;
    private final LocalCacheManager localCacheManager;
    private final List<ResourceManager> externalResourceManagers;
    private final RestResourceManager restResourceManager;
    private final MicroServiceSyncExecutor syncExecutor;
    private final ServiceDiscoveryManager serviceDiscoveryManager;
    private final WebAPIResolver webAPIResolver;

    private final InputDescriptor inputDescriptor;
    private final OutputDescriptor outputDescriptor;
    private final Logger logger;
    private MessagingProviderConfiguration defaultMessagingConfiguration = null;

    public ContextImpl(
            MicroServiceController serviceController,
            ParametersBag parametersBag,
            ResourceProvider resourceProvider,
            MetricsRegistryImpl metricsRegistry,
            ResourceManager<
                    DestinationDescriptor,
                    DestinationConfiguration,
                    ManagedMessageDestination> destinationManager,
            ResourceManager<
                    InputQueueDescriptor,
                    InputQueueConfiguration,
                    ManagedInputQueue> queuesManager,
            ResourceManager<
                    DatasourceDescriptor,
                    DatasourceConfiguration,
                    ManagedDatasource> datasourceManager,
            ResourceManager<
                    ServiceDependencyDescriptor,
                    ServiceDependencyConfiguration,
                    ManagedDependency> dependencyManager,
            ResourceManager<
                    BlobStoreDescriptor,
                    BlobStoreConfiguration,
                    ManagedBlobStore> blobStoreManager,
            ResourceManager<
                    SingletonDescriptor,
                    SingletonConfiguration,
                    ManagedSingleton> singletonManager,
            HealthCheckManager healthCheckManager,
            SerializationManagerImpl serializationManager,
            MicroServiceWebServer webServer,
            InputDescriptor inputDescriptor,
            OutputDescriptor outputDescriptor,
            Logger logger,
            List<ResourceManager> resourceManagers,
            List<ResourceManager> externalResourceManagers,
            RestResourceManager restResourceManager,
            ResourceManager<
                    DynamicJavaServiceDescriptor,
                    DynamicJavaServiceConfiguration,
                    ManagedDynamicJavaService> dynamicJavaServicesManager,
            ResourceManager<
                    SchedulerDescriptor,
                    SchedulerConfiguration,
                    ManagedScheduler> schedulerManager) {

        this.serviceController = serviceController;
        this.serviceDescriptor = serviceController.getServiceDescriptor();
        this.parametersBag = parametersBag;
        this.resourceProvider = resourceProvider;
        this.metricsRegistry = metricsRegistry;
        this.queuesManager = queuesManager;
        this.externalResourceManagers = externalResourceManagers;
        this.destinationManager = destinationManager;
        this.datasourceManager = datasourceManager;
        this.dependencyManager = dependencyManager;
        this.blobStoreManager = blobStoreManager;
        this.singletonManager = singletonManager;
        this.healthCheckManager = healthCheckManager;
        this.serializationManager = serializationManager;
        this.webServer = webServer;
        this.inputDescriptor = inputDescriptor;
        this.outputDescriptor = outputDescriptor;
        this.logger = logger;
        this.restResourceManager = restResourceManager;
        this.dynamicJavaServicesManager = dynamicJavaServicesManager;
        this.schedulerManager = schedulerManager;
        this.localCacheManager = new LocalCacheManager();
        this.resourceManagersByDescriptor = new HashMap<>(resourceManagers.size());
        for (ResourceManager currRM : resourceManagers) {
            //noinspection unchecked
            this.resourceManagersByDescriptor.put(currRM.getDescriptorClass(), currRM);
        }
        this.syncExecutor = initSyncExecutor();
        this.serviceDiscoveryManager = new ServiceDiscoveryManagerImpl(resourceProvider.getServiceRegistryApi(), this);
        this.webAPIResolver = resourceProvider.getWebAPIResolver();
    }

    private MicroServiceSyncExecutor initSyncExecutor() {
        if (inputDescriptor != null &&
                inputDescriptor.getInputType() == InputDescriptor.MicroServiceInputType.messaging) {
            return new MicroServiceSyncExecutor(
                    ((MessagingInputDescriptor) inputDescriptor).getMessageListener(),
                    1,
                    serializationManager,
                    this,
                    resourceProvider);
        } else {
            return null;
        }

    }

    @Override
    public ParametersBag getParametersBag() {
        return parametersBag;
    }

    @Override
    public MetricsRegistryImpl getMetricsRegistry() {
        return metricsRegistry;
    }

    @Override
    public ResourceManager<
            DestinationDescriptor,
            DestinationConfiguration,
            ManagedMessageDestination> getDestinationManager() {
        return destinationManager;
    }

    @Override
    public ResourceManager<DatasourceDescriptor, DatasourceConfiguration, ManagedDatasource> getDatasourceManager() {
        return datasourceManager;
    }

    @Override
    public ResourceManager<
            ServiceDependencyDescriptor,
            ServiceDependencyConfiguration,
            ManagedDependency> getDependencyManager() {
        return dependencyManager;
    }

    @Override
    public ResourceManager<BlobStoreDescriptor, BlobStoreConfiguration, ManagedBlobStore> getBlobStoreManager() {
        return blobStoreManager;
    }

    @Override
    public ResourceManager<SingletonDescriptor, SingletonConfiguration, ManagedSingleton> getSingletonManager() {
        return singletonManager;
    }

    @Override
    public ResourceManager<
            DynamicJavaServiceDescriptor,
            DynamicJavaServiceConfiguration,
            ManagedDynamicJavaService> getDynamicJavaServicesManager() {
        return dynamicJavaServicesManager;
    }

    @Override
    public ResourceManager<SchedulerDescriptor, SchedulerConfiguration, ManagedScheduler> getSchedulerManager() {
        return schedulerManager;
    }

    @Override
    public HealthCheckManager getHealthCheckManager() {
        return healthCheckManager;
    }

    @Override
    public SerializationManagerImpl getSerializationManager() {
        return serializationManager;
    }

    @Override
    public MicroServiceWebServer getWebServer() {
        return webServer;
    }

    @Override
    public InputDescriptor getInputDescriptor() {
        return inputDescriptor;
    }

    @Override
    public OutputDescriptor getOutputDescriptor() {
        return outputDescriptor;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public Logger createSubLogger(Class loggerClass) {
        return LoggingHelper.createSubLogger(logger, loggerClass);
    }

    @Override
    public boolean isOutputMsgSenderDefined(final Message inputMessage) {
        String queueConfiguration = inputMessage.getMessageContext().get(
                MS_API_OUTPUT_QUEUE_NAME_HEADER + "_" + getMicroServiceBaseURI());

        return !(queueConfiguration == null || queueConfiguration.isEmpty());
    }

    @Override
    public MessageSender getOutputMessageSender(final Message inputMessage) {
        return getOutputMessageSender(inputMessage.getMessageContext());
    }

    @Override
    public MessageSender getOutputMessageSender(final Map<String, String> messageContext) {
        final MessageSender messageSender;
        if (outputDescriptor == null) {
            throw new IllegalStateException("Unable to support message output for service with no public output");
        }

        switch (outputDescriptor.getOutputType()) {
            case messaging:
                String queueConfiguration = messageContext.get(
                        MS_API_OUTPUT_QUEUE_NAME_HEADER + "_" + getMicroServiceBaseURI());

                // Validating input message contains output destination configuration
                if (queueConfiguration == null || queueConfiguration.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Caller service did not supply output destination " +
                                    "configuration to which redirect service output");
                }

                Map<String, String> destinationProps =
                        ResourceConfiguration.propArrayToMap(queueConfiguration.split(","));

                //noinspection unchecked
                RuntimeMessageSender runtimeMessageSender = resourceProvider.getMessageSender(
                        getDefaultMessagingConfiguration(),
                        ResourceConfiguration.asSpecificConfiguration(
                                DestinationConfiguration.class,
                                destinationProps),
                        ResourceConfiguration.asSpecificConfiguration(
                                resourceProvider.getQueueConfigurationClass(),
                                destinationProps),
                        this);

                messageSender = new MessageSenderImpl(
                        runtimeMessageSender,
                        serializationManager,
                        resourceProvider,
                        this);

                break;
            case service:
                // Getting the service message sender
                messageSender = getDependencyManager().getManagedResourceByName(
                        ((ServiceOutputDescriptor) outputDescriptor).getServiceURI()).getMessageSender();
                break;

            default:
                throw new IllegalStateException("Unsupported" + outputDescriptor.getOutputType() + " output type");
        }

        return messageSender;
    }

    @Override
    public String getMicroServiceName() {
        return serviceDescriptor.getName();
    }

    @Override
    public String getMicroServiceBaseURI() {
        return serviceController.getBaseURI();
    }

    @NoJavadoc
    public <D extends ResourceDescriptor, R extends ManagedResource<D, ?>> R getManagedResourceByDescriptor(
            Class<D> descriptorClass,
            String name) {

        ResourceManager rm = Objects.requireNonNull(
                this.resourceManagersByDescriptor.get(descriptorClass),
                "Unsupported resource descriptor of type " + descriptorClass.getSimpleName() + " for " + name);

        //noinspection unchecked
        return (R) rm.getManagedResourceByName(name);

    }

    @Override
    public <D extends ResourceDescriptor> boolean isSupportingResource(Class<D> resourceDescriptorClass) {
        return resourceProvider.getExternalResourceManager(resourceDescriptorClass) != null;
    }

    @Override
    public LocalCacheManager getLocalCacheManager() {
        return localCacheManager;
    }

    @Override
    public ResourceManager<InputQueueDescriptor, InputQueueConfiguration, ManagedInputQueue> getQueuesManager() {
        return queuesManager;
    }

    @Override
    public List<ResourceManager> getExternalResourceManagers() {
        return externalResourceManagers;
    }

    @Override
    public String getServiceDescription() {
        return serviceDescriptor.getDescription();
    }

    @Override
    public RestResourceManager getRestResourceManager() {
        return restResourceManager;
    }

    @Override
    public MicroService getServiceDescriptor() {
        return serviceDescriptor;
    }

    @Override
    public MicroServiceState getServiceState() {
        return serviceController.getState();
    }

    public MicroServiceSyncExecutor getSyncExecutor() {
        return syncExecutor;
    }

    @NoJavadoc
    public MessagingProviderConfiguration getDefaultMessagingConfiguration() {
        if (defaultMessagingConfiguration == null) {
            defaultMessagingConfiguration = Objects.requireNonNull(
                    resourceProvider.getServiceRegistryApi().getMessagingProviderConfiguration(
                            resourceProvider.getMessagingConfigurationClass(),
                            MessagingProviderConfiguration.DEFAULT_MESSAGING_SYSTEM_NAME),
                    "Messaging system not available from registry required for initializing output message sender");

        }
        return defaultMessagingConfiguration;
    }

    @Override
    public ServiceDiscoveryManager getServiceDiscoveryManager() {
        return serviceDiscoveryManager;
    }

    @Override
    public WebAPIResolver getWebAPIResolver() {
        return webAPIResolver;
    }
}
