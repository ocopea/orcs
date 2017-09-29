// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.registry;

import com.emc.microservice.Context;
import com.emc.microservice.ServiceConfig;
import com.emc.microservice.blobstore.BlobStoreConfiguration;
import com.emc.microservice.config.ConfigurationAPI;
import com.emc.microservice.datasource.DatasourceConfiguration;
import com.emc.microservice.messaging.MessagingProviderConfiguration;
import com.emc.microservice.messaging.QueueConfiguration;
import com.emc.microservice.resource.ResourceConfiguration;
import com.emc.microservice.restapi.WebServerConfiguration;
import com.emc.microservice.schedule.SchedulerConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liebea on 7/20/15.
 * Drink responsibly
 */
public class ServiceRegistryImpl implements ServiceRegistryApi {

    private static final Logger log = LoggerFactory.getLogger(ServiceRegistryImpl.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private final ConfigurationAPI configurationAPI;

    private interface ResourceConverter<T> {
        T convert(String confStr) throws IOException;
    }

    private static class ResourceConfigurationResourceConverter<T extends ResourceConfiguration>
            implements ResourceConverter<T> {
        private final Class<T> confClass;

        public ResourceConfigurationResourceConverter(Class<T> confClass) {
            this.confClass = confClass;
        }

        @Override
        public T convert(String confStr) throws IOException {
            @SuppressWarnings("unchecked")
            Map<String, String> propMap = mapper.readValue(confStr, Map.class);
            return ResourceConfiguration.asSpecificConfiguration(confClass, propMap);
        }
    }

    private static class ServiceConfigResourceConverter implements ResourceConverter<ServiceConfig> {
        @Override
        public ServiceConfig convert(String confStr) throws IOException {
            return ServiceConfig.generateServiceConfig(mapper.readValue(confStr, ServiceConfig.class));
        }
    }

    public ServiceRegistryImpl(ConfigurationAPI configurationAPI) {
        this.configurationAPI = configurationAPI;
    }

    @Override
    public ServiceConfig getServiceConfig(String serviceURI) {
        return readConfiguration(SERVICE_TYPE_SERVICE_CONFIG, serviceURI, new ServiceConfigResourceConverter());
    }

    @Override
    public <DatasourceConfT extends DatasourceConfiguration> DatasourceConfT getDataSourceConfiguration(
            Class<DatasourceConfT> confClass,
            String dataSourceName) {
        return readConfiguration(SERVICE_TYPE_DATASOURCE, dataSourceName, confClass);
    }

    @Override
    public <MessagingConfT extends MessagingProviderConfiguration> MessagingConfT getMessagingProviderConfiguration(
            Class<MessagingConfT> confClass,
            String messagingSystemName) {
        return readConfiguration(SERVICE_TYPE_MESSAGING, messagingSystemName, confClass);
    }

    @Override
    public <SchedulerConfT extends SchedulerConfiguration> SchedulerConfT getSchedulerConfiguration(
            Class<SchedulerConfT> confClass,
            String name) {
        return readConfiguration(SERVICE_TYPE_SCHEDULER, name, confClass);
    }

    @Override
    public <BlobstoreConfT extends BlobStoreConfiguration> BlobstoreConfT getBlobStoreConfiguration(
            Class<BlobstoreConfT> confClass,
            String blobstoreName) {
        return readConfiguration(SERVICE_TYPE_BLOBSTORE, blobstoreName, confClass);
    }

    @Override
    public <QueueConfT extends QueueConfiguration> QueueConfT getQueueConfiguration(
            Class<QueueConfT> confClass,
            String queueName,
            Context context) {
        return readConfiguration(SERVICE_TYPE_QUEUE, queueName, confClass);
    }

    @Override
    public <WebserverConfT extends WebServerConfiguration> WebserverConfT getWebServerConfiguration(
            Class<WebserverConfT> confClass,
            String webServerName) {
        return readConfiguration(SERVICE_TYPE_WEBSERVER, webServerName, confClass);
    }

    private <T extends ResourceConfiguration> T readConfiguration(
            String resourceType,
            String resourceName,
            Class<T> confClass) {
        return readConfiguration(resourceType, resourceName, new ResourceConfigurationResourceConverter<>(confClass));
    }

    private <T> T readConfiguration(String resourceType, String resourceName, ResourceConverter<T> converter) {
        return readConfFromPath(resourceType + "/" + resourceName, converter);
    }

    private <T> T readConfFromPath(String resourcePath, ResourceConverter<T> converter) {
        String confStr = configurationAPI.readData(resourcePath);
        if (confStr == null) {
            log.warn("got null configuration for {}", resourcePath);
            return null;
        }

        try {
            return converter.convert(confStr);

        } catch (IOException e) {
            throw new IllegalStateException("Failed parsing " + resourcePath + " configuration", e);
        }
    }

    private <T extends ResourceConfiguration> Collection<T> readConfigurations(
            Class<T> confClass,
            String resourceType) {
        Collection<String> pathList = configurationAPI.list(resourceType);
        if (pathList == null || pathList.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<T> confList = new ArrayList<>(pathList.size());
            for (String currPath : pathList) {
                confList.add(readConfFromPath(currPath, new ResourceConfigurationResourceConverter<>(confClass)));
            }
            return confList;
        }
    }

    private <T> Map<String, T> readConfigurationsMap(String resourceType, ResourceConverter<T> converter) {
        Collection<String> pathList = configurationAPI.list(resourceType);
        if (pathList == null || pathList.isEmpty()) {
            return Collections.emptyMap();
        } else {
            Map<String, T> confMap = new HashMap<>(pathList.size());
            int l = resourceType.length() + 1;
            for (String currPath : pathList) {
                confMap.put(currPath.substring(l), readConfFromPath(currPath, converter));
            }
            return confMap;
        }
    }

    @Override
    public <T extends ResourceConfiguration> T getExternalResourceConfiguration(
            String resourceName,
            Class<T> externalResourceConfigurationClass) {
        //todo: need to add resource type rather than just "external" - can have it under the external folder...
        return readConfiguration(SERVICE_TYPE_EXTERNAL_RESOURCE, resourceName, externalResourceConfigurationClass);
    }

    @Override
    public void registerServiceDeprecated(
            String resourceId,
            String name,
            String type,
            int version,
            String apiVersion,
            String node,
            String route,
            Map<String, String> serviceProperties) {
        try {
            String confStr = mapper.writeValueAsString(serviceProperties);
            configurationAPI.writeData(type + "/" + name, confStr);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed serializing " + resourceId + " " + type, e);
        }
    }

    private void registerInternal(String resourceId, String name, String type, Map<String, String> serviceProperties) {
        try {
            String confStr = mapper.writeValueAsString(serviceProperties);
            configurationAPI.writeData(type + "/" + name, confStr);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed serializing " + resourceId + " " + type, e);
        }
    }

    @Override
    public void registerServiceConfig(String serviceURI, ServiceConfig serviceConfig) {
        try {
            configurationAPI.writeData(
                    SERVICE_TYPE_SERVICE_CONFIG + "/" + serviceURI,
                    mapper.writeValueAsString(serviceConfig));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed serializing service config");
        }
    }

    @Override
    public Map<String, ServiceConfig> listServiceConfig() {
        return readConfigurationsMap(SERVICE_TYPE_SERVICE_CONFIG, new ServiceConfigResourceConverter());
    }

    @Override
    public <DatasourceConfT extends DatasourceConfiguration> Map<String, DatasourceConfT> listDataSources(
            Class<DatasourceConfT> confClass) {
        return readConfigurationsMap(SERVICE_TYPE_DATASOURCE, new ResourceConfigurationResourceConverter<>(confClass));
    }

    @Override
    public <BlobstoreConfT extends BlobStoreConfiguration> Map<String, BlobstoreConfT> listBlobStores(
            Class<BlobstoreConfT> confClass) {
        return readConfigurationsMap(SERVICE_TYPE_BLOBSTORE, new ResourceConfigurationResourceConverter<>(confClass));
    }

    @Override
    public <QueueConfT extends QueueConfiguration> Map<String, QueueConfT> listQueues(Class<QueueConfT> confClass) {
        return readConfigurationsMap(SERVICE_TYPE_QUEUE, new ResourceConfigurationResourceConverter<>(confClass));
    }

    @Override
    public <MessagingConfT extends MessagingProviderConfiguration> Map<String, MessagingConfT> listMessagingSystems(
            Class<MessagingConfT> confClass) {
        return readConfigurationsMap(SERVICE_TYPE_MESSAGING, new ResourceConfigurationResourceConverter<>(confClass));
    }

    @Override
    public void registerDataSource(String name, DatasourceConfiguration datasourceConf) {
        registerInternal(name, name, SERVICE_TYPE_DATASOURCE, datasourceConf.getPropertyValues());
    }

    @Override
    public void registerBlobStore(String name, BlobStoreConfiguration blobstoreConf) {
        registerInternal(name, name, SERVICE_TYPE_BLOBSTORE, blobstoreConf.getPropertyValues());
    }

    @Override
    public void registerQueue(String name, QueueConfiguration queueConf) {
        registerInternal(name, name, SERVICE_TYPE_QUEUE, queueConf.getPropertyValues());
    }

    @Override
    public void registerMessaging(String name, MessagingProviderConfiguration messagingConf) {
        registerInternal(name, name, SERVICE_TYPE_MESSAGING, messagingConf.getPropertyValues());
    }

    @Override
    public void registerWebServer(String name, WebServerConfiguration webserverConf) {
        registerInternal(name, name, SERVICE_TYPE_WEBSERVER, webserverConf.getPropertyValues());
    }

    @Override
    public void registerExternalResource(String name, Map<String, String> properties) {
        registerInternal(name, name, SERVICE_TYPE_EXTERNAL_RESOURCE, properties);
    }

    @Override
    public void registerScheduler(String name, SchedulerConfiguration schedulerConf) {
        registerInternal(name, name, SERVICE_TYPE_SCHEDULER, schedulerConf.getPropertyValues());
    }
}
