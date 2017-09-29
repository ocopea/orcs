// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.registry;

import com.emc.microservice.Context;
import com.emc.microservice.ServiceConfig;
import com.emc.microservice.blobstore.BlobStoreConfiguration;
import com.emc.microservice.datasource.DatasourceConfiguration;
import com.emc.microservice.messaging.MessagingProviderConfiguration;
import com.emc.microservice.messaging.QueueConfiguration;
import com.emc.microservice.resource.ResourceConfiguration;
import com.emc.microservice.restapi.WebServerConfiguration;
import com.emc.microservice.schedule.SchedulerConfiguration;

import java.util.Map;

public interface ServiceRegistryApi {

    String SERVICE_TYPE_DATASOURCE = "datasource";
    String SERVICE_TYPE_MESSAGING = "messaging";
    String SERVICE_TYPE_SERVICE_CONFIG = "serviceconfig";
    String SERVICE_TYPE_BLOBSTORE = "blobstore";
    String SERVICE_TYPE_EXTERNAL_RESOURCE = "external";
    String SERVICE_TYPE_QUEUE = "queue";
    String SERVICE_TYPE_WEBSERVER = "webserver";
    String SERVICE_TYPE_SCHEDULER = "scheduler";

    /**
     * Returns service specific configuration like:
     * log level
     * concurrency or message listeners
     * size/limit of connection pools for data sources
     * service specific parameters
     *
     * @param serviceURI service uri
     *
     * @return Service configuration descriptor
     */
    ServiceConfig getServiceConfig(String serviceURI);

    /**
     * Returns concrete data source configuration matching the runtime stack implementation
     *
     * @param dataSourceName name of the registered datasource
     *
     * @return see description
     */
    <DatasourceConfT extends DatasourceConfiguration> DatasourceConfT getDataSourceConfiguration(
            Class<DatasourceConfT> confClass,
            String dataSourceName);

    /**
     * Returns messaging provider configuration matching the runtime stack implementation
     *
     * @param messagingSystemName messaging system name
     *
     * @return see description
     */
    <MessagingConfT extends MessagingProviderConfiguration> MessagingConfT getMessagingProviderConfiguration(
            Class<MessagingConfT> confClass,
            String messagingSystemName);

    /**
     * Returns concrete BlobStore configuration matching the runtime stack implementation
     *
     * @param blobstoreName name of the registered blobstore
     *
     * @return see description
     */
    <BlobstoreConfT extends BlobStoreConfiguration> BlobstoreConfT getBlobStoreConfiguration(
            Class<BlobstoreConfT> confClass,
            String blobstoreName);

    <QueueConfT extends QueueConfiguration> QueueConfT getQueueConfiguration(
            Class<QueueConfT> confClass,
            String queueName,
            Context context);

    default <WebserverConfT extends WebServerConfiguration> WebserverConfT getWebServerConfiguration(
            Class<WebserverConfT> confClass) {
        return getWebServerConfiguration(confClass, "default");
    }

    <WebserverConfT extends WebServerConfiguration> WebserverConfT getWebServerConfiguration(
            Class<WebserverConfT> confClass,
            String webServerName);

    <SchedulerConfT extends SchedulerConfiguration> SchedulerConfT getSchedulerConfiguration(
            Class<SchedulerConfT> confClass,
            String name);

    /**
     * Returns concrete external resource configuration matching the runtime stack implementation
     * External resources are used by services that use non-built in resources but want to leverage the management
     * and lifecycle of the resource managers
     *
     * @param <T> configuration generic class type
     * @param resourceName resource name as registered in the registry service
     * @param externalResourceConfigurationClass configuration class  @return see description
     */
    <T extends ResourceConfiguration> T getExternalResourceConfiguration(
            String resourceName,
            Class<T> externalResourceConfigurationClass);

    /**
     * This is used to register any kind of resource
     *
     * @param resourceId resource id - unique url friendly string. this will be used to identify the registered service
     *                   in the registry. all services that want to use this resource will have to know this id.
     * @param name logical resource name
     * @param type resource type name
     * @param version resource version
     * @param apiVersion resource api version if available
     * @param node optional node in which the service run on
     * @param route url - how to get to this service root
     * @param serviceProperties key value pairs describing the resource configuration
     */
    @Deprecated
    void registerServiceDeprecated(
            String resourceId,
            String name,
            String type,
            int version,
            String apiVersion,
            String node,
            String route,
            Map<String, String> serviceProperties);

    Map<String, ServiceConfig> listServiceConfig();

    <DatasourceConfT extends DatasourceConfiguration> Map<String, DatasourceConfT> listDataSources(
            Class<DatasourceConfT> confClass);

    <BlobstoreConfT extends BlobStoreConfiguration> Map<String, BlobstoreConfT> listBlobStores(
            Class<BlobstoreConfT> confClass);

    <QueueConfT extends QueueConfiguration> Map<String, QueueConfT> listQueues(Class<QueueConfT> confClass);

    <MessagingConfT extends MessagingProviderConfiguration> Map<String, MessagingConfT> listMessagingSystems(
            Class<MessagingConfT> confClass);

    void registerServiceConfig(String serviceURI, ServiceConfig serviceConfig);

    void registerDataSource(String name, DatasourceConfiguration datasourceConf);

    void registerBlobStore(String name, BlobStoreConfiguration blobDatasourceConf);

    void registerQueue(String name, QueueConfiguration queueConf);

    void registerMessaging(String name, MessagingProviderConfiguration messagingConf);

    void registerWebServer(String name, WebServerConfiguration webserverConf);

    void registerExternalResource(String name, Map<String, String> properties);

    void registerScheduler(String name, SchedulerConfiguration schedulerConf);
}

