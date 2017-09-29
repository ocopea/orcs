// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.resource;

import com.emc.microservice.Context;
import com.emc.microservice.ContextThreadLocal;
import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.microservice.blobstore.BlobStoreConfiguration;
import com.emc.microservice.blobstore.BlobStoreProvider;
import com.emc.microservice.config.ConfigurationAPI;
import com.emc.microservice.datasource.DatasourceConfiguration;
import com.emc.microservice.datasource.DatasourceProvider;
import com.emc.microservice.datasource.MicroServiceDataSource;
import com.emc.microservice.logging.LoggingProvider;
import com.emc.microservice.messaging.DestinationConfiguration;
import com.emc.microservice.messaging.InputQueueConfiguration;
import com.emc.microservice.messaging.ManagedMessageListener;
import com.emc.microservice.messaging.MessagingProvider;
import com.emc.microservice.messaging.MessagingProviderConfiguration;
import com.emc.microservice.messaging.QueueConfiguration;
import com.emc.microservice.messaging.QueueReceiverImpl;
import com.emc.microservice.messaging.RuntimeMessageSender;
import com.emc.microservice.registry.ServiceRegistryApi;
import com.emc.microservice.registry.ServiceRegistryImpl;
import com.emc.microservice.restapi.MicroServiceWebServer;
import com.emc.microservice.restapi.WebServerConfiguration;
import com.emc.microservice.restapi.WebServerProvider;
import com.emc.microservice.schedule.SchedulerConfiguration;
import com.emc.microservice.schedule.SchedulerProvider;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.microservice.schedule.SchedulerApi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Created with love by liebea on 5/28/2014.
 * Interface needs to be implemented by each deployment "stack". this will provide actual resources based on the
 * Descriptors defined by the micro-service itself and the configuration supplied in runtime by the resource provider
 * registry. For each "block" of the stack the resource provider will define it's own configuration format, supporting
 * having different implementation of the same API (e.g. HornetQ messaging v.s.
 * RabbitMQ messaging or different databases)
 */
public class ResourceProvider {

    private final ServiceRegistryApi serviceRegistryApi;
    private final ConfigurationAPI configurationAPI;

    private DatasourceProvider datasourceProvider;
    private MessagingProvider messagingProvider;
    private BlobStoreProvider blobStoreProvider;
    private LoggingProvider loggingProvider;
    private WebServerProvider webServerProvider;
    private SchedulerProvider schedulerProvider;

    public ResourceProvider(ConfigurationAPI configurationAPI) {
        this(configurationAPI, new ServiceRegistryImpl(configurationAPI));
    }

    public ResourceProvider(ConfigurationAPI configurationAPI, ServiceRegistryApi serviceRegistryApi) {
        this.serviceRegistryApi = serviceRegistryApi;
        this.configurationAPI = configurationAPI;
        ResourceProviderManager.setResourceProvider(this);
    }

    /***
     * Returns registry API implementation based on specific registry technology used by this stack
     * @return instance of registry api
     */
    public ServiceRegistryApi getServiceRegistryApi() {
        return serviceRegistryApi;
    }

    /**
     * Gets MessageSender implementation supporting streaming a message into the messaging system
     *
     * @param destinationConfiguration destination configuration of type
     * @param queueConf queue configuration supported by the stack
     * @param context Micro service context instance  @return proxy to send a message
     */
    public RuntimeMessageSender getMessageSender(
            MessagingProviderConfiguration messagingConfiguration,
            DestinationConfiguration destinationConfiguration,
            QueueConfiguration queueConf,
            Context context) {
        //noinspection unchecked
        return getMessagingProvider().getMessageSender(
                messagingConfiguration,
                destinationConfiguration,
                queueConf,
                context);
    }

    /**
     * Creates a queue on the underlying messaging system
     *
     * @param messagingConfiguration messaging configuration
     * @param queueConf queue configuration supported by the stack
     */
    public void createQueue(MessagingProviderConfiguration messagingConfiguration, QueueConfiguration queueConf) {
        //noinspection unchecked
        getMessagingProvider().createQueue(messagingConfiguration, queueConf);
    }

    /***
     * Creates a proxy for consuming messages from the messaging system using the messaging technology of the stack
     * @param inputQueueConfiguration input queue configuration
     * @param queueConf queue configuration Supported by the stack
     * @param deadLetterQueueConfs dead letter queue configurations supported by the stack
     * @param messageListener message listener implementation to call once receiving a message
     * @param context Micro service context instance   @return QueueReceiver handle
     */
    public <QueueConfT extends QueueConfiguration> QueueReceiverImpl createQueueReceiver(
            MessagingProviderConfiguration messagingConfiguration,
            InputQueueConfiguration inputQueueConfiguration,
            QueueConfT queueConf,
            Map<String, QueueConfT> deadLetterQueueConfs,
            ManagedMessageListener messageListener,
            Context context,
            String consumerName) {
        //noinspection unchecked
        return getMessagingProvider().createQueueReceiver(
                messagingConfiguration,
                inputQueueConfiguration,
                queueConf,
                deadLetterQueueConfs,
                messageListener,
                context,
                consumerName);
    }

    /**
     * Gets a java DataSource matching the requested configuration.
     * DataSource type will be the one provided by the stack
     *
     * @param configuration datasource configuration supported by the stack
     *
     * @return java Datasource
     *
     * @throws UnsupportedOperationException if datasource is not supported by the runtime stack
     */
    public MicroServiceDataSource getDataSource(DatasourceConfiguration configuration) {
        //noinspection unchecked
        return getDatasourceProvider().getDatasource(configuration);
    }

    private <T> T createProvider(Class<T> providerClass) {
        final ServiceLoader<T> providers = ServiceLoader.load(providerClass);
        Iterator<T> iterator = providers.iterator();
        if (!iterator.hasNext()) {
            throw new IllegalStateException("Could not find provider for " + providerClass.getName());
        }
        T provider = iterator.next();
        if (iterator.hasNext()) {
            throw new IllegalStateException(
                    "Fool! Duplicate providers for " + providerClass.getName() +
                            "! (including " + provider.getClass().getName() + " and " +
                            iterator.next().getClass().getName() + ")");
        }

        if (ServiceLifecycle.class.isAssignableFrom(providerClass)) {
            final Context context = ContextThreadLocal.getContext();
            if (context != null) {
                ((ServiceLifecycle)provider).init(context);
            }
        }

        return provider;
    }

    /***
     * Get datasource provider
     */
    public final DatasourceProvider getDatasourceProvider() {
        if (datasourceProvider == null) {
            synchronized (this) {
                if (datasourceProvider == null) {
                    datasourceProvider = createDatasourceProvider();
                }
            }
        }
        return datasourceProvider;
    }

    protected DatasourceProvider createDatasourceProvider() {
        return createProvider(DatasourceProvider.class);
    }

    /***
     * Get messaging provider
     */
    public final MessagingProvider getMessagingProvider() {
        if (messagingProvider == null) {
            synchronized (this) {
                if (messagingProvider == null) {
                    messagingProvider = createMessagingProvider();
                }
            }
        }
        return messagingProvider;
    }

    protected MessagingProvider createMessagingProvider() {
        return createProvider(MessagingProvider.class);
    }

    /***
     * Get Blobstore provider
     */
    public final BlobStoreProvider getBlobStoreProvider() {
        if (blobStoreProvider == null) {
            synchronized (this) {
                if (blobStoreProvider == null) {
                    blobStoreProvider = createBlobStoreProvider();
                }
            }
        }
        return blobStoreProvider;
    }

    protected BlobStoreProvider createBlobStoreProvider() {
        return createProvider(BlobStoreProvider.class);
    }

    /***
     * Get logging provider
     */
    public final LoggingProvider getLoggingProvider() {
        if (loggingProvider == null) {
            synchronized (this) {
                if (loggingProvider == null) {
                    loggingProvider = createProvider(LoggingProvider.class);
                }
            }
        }
        return loggingProvider;
    }

    /***
     * Get webserver provider
     */
    public final WebServerProvider getWebServerProvider() {
        if (webServerProvider == null) {
            synchronized (this) {
                if (webServerProvider == null) {
                    webServerProvider = createWebServerProvider();
                }
            }
        }
        return webServerProvider;
    }

    protected WebServerProvider createWebServerProvider() {
        return createProvider(WebServerProvider.class);
    }

    /***
     * Get scheduler provider
     */
    public final SchedulerProvider getSchedulerProvider() {
        if (schedulerProvider == null) {
            synchronized (this) {
                if (schedulerProvider == null) {
                    schedulerProvider = createSchedulerProvider();
                }
            }
        }
        return schedulerProvider;
    }

    protected SchedulerProvider createSchedulerProvider() {
        return createProvider(SchedulerProvider.class);
    }

    /***
     * Return Blobstore api to use by the service
     * @param configuration Concrete blobstore configuration
     * @param context micro-service context
     * @return Blobstore implementation
     */
    public BlobStoreAPI getBlobStore(BlobStoreConfiguration configuration, Context context) {
        //noinspection unchecked
        return getBlobStoreProvider().getBlobStore(
                configuration,
                context == null ? null : context.getSerializationManager());
    }

    /***
     * Returns Webserver implementation of the current stack
     * @param webServerConfiguration webserver configuration descriptor
     * @return see description
     */
    public MicroServiceWebServer getWebServer(WebServerConfiguration webServerConfiguration) {
        //noinspection unchecked
        return getWebServerProvider().getWebServer(webServerConfiguration);
    }

    /**
     * Returns scheduler implementation of the current stack
     *
     * @param schedulerConfiguration scheduler configuration object
     *
     * @return Scheduler duh
     */
    public SchedulerApi getScheduler(
            SchedulerConfiguration schedulerConfiguration,
            Context context) {
        return getSchedulerProvider().getScheduler(schedulerConfiguration, context);
    }

    /***
     * Getting external resource manager class matching specific external resource descriptor
     * @param <D> descriptor class generics param
     * @param externalResourceDescriptorClass descriptor class for external resource
     * @return External Resource implementation class
     */
    public <D extends ResourceDescriptor> ExternalResourceManager getExternalResourceManager(
            Class<D> externalResourceDescriptorClass) {

        final ServiceLoader<ExternalResourceManager> providers = ServiceLoader.load(ExternalResourceManager.class);

        for (ExternalResourceManager manager : providers) {
            if (manager.getDescriptorClass().equals(externalResourceDescriptorClass)) {
                return manager;
            }
        }
        throw new IllegalStateException(
                "Failed locating external resource manager for resource type " +
                        externalResourceDescriptorClass.getCanonicalName());
    }

    /***
     * Set the log level
     * @param level log level
     * @param category optional - category to set logger level for
     * @param variables optional - context variables allowing setting specific loggers to the desired level
     */
    public void setLogLevel(LoggingProvider.LogLevel level, String category, Map<String, String> variables) {
        getLoggingProvider().setLogLevel(level, category, variables);
    }

    /**
     * Returns the network address for the current node(host) this resource provider is running on
     *
     * @return current node address
     */
    public String getNodeAddress() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            // maybe return "localhost"?
            throw new IllegalStateException(e);
        }
    }

    /**
     * Rest Client proxy support
     *
     * @return http client rest proxy implementation
     */
    public WebAPIResolver getWebAPIResolver() {
        return new DefaultWebApiResolver();
    }

    public ConfigurationAPI getConfigurationAPI() {
        return configurationAPI;
    }

    // Boilerplate methods that java needs :)

    public <DatasourceConfT extends DatasourceConfiguration> Class<DatasourceConfT> getDatasourceConfigurationClass() {
        //noinspection unchecked
        return getDatasourceProvider().getConfClass();
    }

    public <MessagingConfT extends
            MessagingProviderConfiguration> Class<MessagingConfT> getMessagingConfigurationClass() {
        //noinspection unchecked
        return getMessagingProvider().getMessageConfClass();
    }

    public <SchedulerConfT extends SchedulerConfiguration> Class<SchedulerConfT> getSchedulerConfigurationClass() {
        //noinspection unchecked
        return getSchedulerProvider().getConfClass();
    }

    public <QueueConfT extends QueueConfiguration> Class<QueueConfT> getQueueConfigurationClass() {
        //noinspection unchecked
        return getMessagingProvider().getQueueConfClass();
    }

    public <BlobstoreConfT extends BlobStoreConfiguration> Class<BlobstoreConfT> getBlobStoreConfigurationClass() {
        //noinspection unchecked
        return getBlobStoreProvider().getConfClass();
    }

    public <WebserverConfT extends WebServerConfiguration> Class<WebserverConfT> getWebServerConfigurationClass() {
        //noinspection unchecked
        return getWebServerProvider().getConfClass();
    }

    /**
     * Called before each service is run.
     */
    public void preRunServiceHook(Context context) {}
}
