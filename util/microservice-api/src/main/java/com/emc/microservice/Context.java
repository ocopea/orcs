// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
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
import com.emc.microservice.dservice.DynamicJavaServiceConfiguration;
import com.emc.microservice.dservice.DynamicJavaServiceDescriptor;
import com.emc.microservice.dservice.ManagedDynamicJavaService;
import com.emc.microservice.health.HealthCheckManager;
import com.emc.microservice.input.InputDescriptor;
import com.emc.microservice.messaging.DestinationConfiguration;
import com.emc.microservice.messaging.DestinationDescriptor;
import com.emc.microservice.messaging.InputQueueConfiguration;
import com.emc.microservice.messaging.InputQueueDescriptor;
import com.emc.microservice.messaging.ManagedInputQueue;
import com.emc.microservice.messaging.ManagedMessageDestination;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.messaging.MessageSender;
import com.emc.microservice.metrics.MetricsRegistry;
import com.emc.microservice.output.OutputDescriptor;
import com.emc.microservice.resource.ManagedResource;
import com.emc.microservice.resource.ResourceDescriptor;
import com.emc.microservice.resource.ResourceManager;
import com.emc.microservice.restapi.MicroServiceWebServer;
import com.emc.microservice.restapi.RestResourceManager;
import com.emc.microservice.schedule.ManagedScheduler;
import com.emc.microservice.schedule.SchedulerConfiguration;
import com.emc.microservice.schedule.SchedulerDescriptor;
import com.emc.microservice.serialization.SerializationManager;
import com.emc.microservice.singleton.ManagedSingleton;
import com.emc.microservice.singleton.SingletonConfiguration;
import com.emc.microservice.singleton.SingletonDescriptor;
import com.emc.microservice.webclient.WebAPIResolver;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Created with love by liebea on 5/28/2014.
 */
public interface Context {

    ParametersBag getParametersBag();

    MetricsRegistry getMetricsRegistry();

    ServiceDiscoveryManager getServiceDiscoveryManager();

    ResourceManager<DestinationDescriptor, DestinationConfiguration, ManagedMessageDestination> getDestinationManager();

    ResourceManager<DatasourceDescriptor, DatasourceConfiguration, ManagedDatasource> getDatasourceManager();

    ResourceManager<
            ServiceDependencyDescriptor,
            ServiceDependencyConfiguration,
            ManagedDependency> getDependencyManager();

    ResourceManager<BlobStoreDescriptor, BlobStoreConfiguration, ManagedBlobStore> getBlobStoreManager();

    ResourceManager<SingletonDescriptor, SingletonConfiguration, ManagedSingleton> getSingletonManager();

    ResourceManager<
            DynamicJavaServiceDescriptor,
            DynamicJavaServiceConfiguration,
            ManagedDynamicJavaService> getDynamicJavaServicesManager();

    ResourceManager<
            SchedulerDescriptor,
            SchedulerConfiguration,
            ManagedScheduler> getSchedulerManager();

    HealthCheckManager getHealthCheckManager();

    SerializationManager getSerializationManager();

    MicroServiceWebServer getWebServer();

    InputDescriptor getInputDescriptor();

    OutputDescriptor getOutputDescriptor();

    Logger getLogger();

    Logger createSubLogger(Class loggerClass);

    MessageSender getOutputMessageSender(final Message inputMessage);

    MessageSender getOutputMessageSender(final Map<String, String> messageContext);

    boolean isOutputMsgSenderDefined(final Message inputMessage);

    String getMicroServiceName();

    String getMicroServiceBaseURI();

    <D extends ResourceDescriptor, R extends ManagedResource<D, ?>> R getManagedResourceByDescriptor(
            Class<D> descriptorClass,
            String name);

    <D extends ResourceDescriptor> boolean isSupportingResource(Class<D> resourceDescriptorClass);

    LocalCacheManager getLocalCacheManager();

    MicroServiceState getServiceState();

    ResourceManager<InputQueueDescriptor, InputQueueConfiguration, ManagedInputQueue> getQueuesManager();

    List<ResourceManager> getExternalResourceManagers();

    String getServiceDescription();

    RestResourceManager getRestResourceManager();

    MicroService getServiceDescriptor();

    WebAPIResolver getWebAPIResolver();

}
