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
import com.emc.microservice.dservice.DynamicJavaServiceConfiguration;
import com.emc.microservice.dservice.DynamicJavaServiceDescriptor;
import com.emc.microservice.dservice.ManagedDynamicJavaService;
import com.emc.microservice.health.HealthCheckManager;
import com.emc.microservice.input.InputDescriptor;
import com.emc.microservice.messaging.DefaultMessageReader;
import com.emc.microservice.messaging.DestinationConfiguration;
import com.emc.microservice.messaging.DestinationDescriptor;
import com.emc.microservice.messaging.InputQueueConfiguration;
import com.emc.microservice.messaging.InputQueueDescriptor;
import com.emc.microservice.messaging.ManagedInputQueue;
import com.emc.microservice.messaging.ManagedMessageDestination;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.messaging.MessageListener;
import com.emc.microservice.messaging.MessageListenerFactory;
import com.emc.microservice.messaging.MessageReader;
import com.emc.microservice.messaging.MessageSender;
import com.emc.microservice.messaging.MessageSenderImpl;
import com.emc.microservice.messaging.MessageWriter;
import com.emc.microservice.messaging.RuntimeMessageSender;
import com.emc.microservice.metrics.MetricsRegistry;
import com.emc.microservice.output.OutputDescriptor;
import com.emc.microservice.resource.ManagedResource;
import com.emc.microservice.resource.ResourceDescriptor;
import com.emc.microservice.resource.ResourceManager;
import com.emc.microservice.resource.ResourceProvider;
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
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by liebea on 5/10/15.
 * Drink responsibly
 */
public class MicroServiceSyncExecutor {
    private final Class<? extends MessageListener> inputMessageListener;

    // for now, supporting one, in future can have list or wrappers here
    private final List<MessageListener> messageListeners;
    private final SerializationManager serializationManager;
    private final Context context;
    private final ResourceProvider resourceProvider;

    public MicroServiceSyncExecutor(
            Class<? extends MessageListener> inputMessageListener,
            int concurrency,
            SerializationManager serializationManager,
            Context context,
            ResourceProvider resourceProvider) {
        this.inputMessageListener = inputMessageListener;
        this.serializationManager = serializationManager;
        this.context = context;
        this.messageListeners = new ArrayList<>(concurrency);
        this.resourceProvider = resourceProvider;
    }

    /***
     * Execute task synchronously
     */
    public void execute(
            final InputStream inputStream,
            OutputStream outputStream,
            final Map<String, String> messageHeaders,
            final Map<String, String> messageContext) {
        MessageListener messageListener;
        if (messageListeners.isEmpty()) {
            messageListener = MessageListenerFactory.createMessageListener(inputMessageListener, context);
            messageListeners.add(messageListener);
        } else {
            messageListener = messageListeners.get(0);
        }

        messageListener.onMessage(new Message() {
            @Override
            public String getMessageHeader(String headerName) {
                return messageHeaders.get(headerName);
            }

            @Override
            public Map<String, String> getMessageHeaders() {
                return messageHeaders;
            }

            @Override
            public String getContextValue(String key) {
                return messageContext.get(key);
            }

            @Override
            public Map<String, String> getMessageContext() {
                return messageContext;
            }

            @Override
            public void readMessage(MessageReader messageReader) {
                messageReader.read(inputStream);
            }

            @Override
            public <T> T readObject(Class<T> format) {
                DefaultMessageReader<T> messageReader =
                        new DefaultMessageReader<>(serializationManager.getReader(format), format);

                messageReader.read(inputStream);
                return messageReader.getResult();
            }

            @Override
            public Object getUnderlyingMessageObject() {
                return this;
            }
        }, new ContextWrapper(context, outputStream, serializationManager, resourceProvider));
    }

    private static final class ContextWrapper implements Context {

        private final Context context;
        private final OutputStream outputStream;
        private final SerializationManager serializationManager;
        private final ResourceProvider resourceProvider;

        private ContextWrapper(
                Context context,
                OutputStream outputStream,
                SerializationManager serializationManager,
                ResourceProvider resourceProvider) {
            this.context = context;
            this.outputStream = outputStream;
            this.serializationManager = serializationManager;
            this.resourceProvider = resourceProvider;
        }

        @Override
        public MessageSender getOutputMessageSender(Message inputMessage) {
            Map<String, String> messageContext = Collections.emptyMap();
            if (inputMessage != null) {
                messageContext = inputMessage.getMessageContext();
            }
            return getOutputMessageSender(messageContext);
        }

        @Override
        @NoJavadoc
        public MessageSender getOutputMessageSender(Map<String, String> messageContext) {
            return new MessageSenderImpl(
                    new SyncMessageSender(outputStream),
                    serializationManager,
                    resourceProvider,
                    this);
        }

        @Override
        public boolean isOutputMsgSenderDefined(Message inputMessage) {
            return context.isOutputMsgSenderDefined(inputMessage);
        }

        @Override
        public ParametersBag getParametersBag() {
            return context.getParametersBag();
        }

        @Override
        public MetricsRegistry getMetricsRegistry() {
            return context.getMetricsRegistry();
        }

        @Override
        public ServiceDiscoveryManager getServiceDiscoveryManager() {
            return context.getServiceDiscoveryManager();
        }

        @Override
        public WebAPIResolver getWebAPIResolver() {
            return context.getWebAPIResolver();
        }

        @Override
        public ResourceManager<DestinationDescriptor,
                DestinationConfiguration,
                ManagedMessageDestination> getDestinationManager() {
            return context.getDestinationManager();
        }

        @Override
        public ResourceManager<
                DatasourceDescriptor,
                DatasourceConfiguration,
                ManagedDatasource> getDatasourceManager() {
            return context.getDatasourceManager();
        }

        @Override
        public ResourceManager<
                ServiceDependencyDescriptor,
                ServiceDependencyConfiguration,
                ManagedDependency> getDependencyManager() {
            return context.getDependencyManager();
        }

        @Override
        public ResourceManager<BlobStoreDescriptor, BlobStoreConfiguration, ManagedBlobStore> getBlobStoreManager() {
            return context.getBlobStoreManager();
        }

        @Override
        public ResourceManager<SingletonDescriptor, SingletonConfiguration, ManagedSingleton> getSingletonManager() {
            return context.getSingletonManager();
        }

        @Override
        public ResourceManager<
                DynamicJavaServiceDescriptor,
                DynamicJavaServiceConfiguration,
                ManagedDynamicJavaService> getDynamicJavaServicesManager() {
            return context.getDynamicJavaServicesManager();
        }

        @Override
        public ResourceManager<SchedulerDescriptor, SchedulerConfiguration, ManagedScheduler> getSchedulerManager() {
            return context.getSchedulerManager();
        }

        @Override
        public HealthCheckManager getHealthCheckManager() {
            return context.getHealthCheckManager();
        }

        @Override
        public SerializationManager getSerializationManager() {
            return context.getSerializationManager();
        }

        @Override
        public MicroServiceWebServer getWebServer() {
            return context.getWebServer();
        }

        @Override
        public InputDescriptor getInputDescriptor() {
            return context.getInputDescriptor();
        }

        @Override
        public OutputDescriptor getOutputDescriptor() {
            return context.getOutputDescriptor();
        }

        @Override
        public Logger getLogger() {
            return context.getLogger();
        }

        @Override
        public Logger createSubLogger(Class loggerClass) {
            return context.createSubLogger(loggerClass);
        }

        @Override
        public String getMicroServiceName() {
            return context.getMicroServiceName();
        }

        @Override
        public String getMicroServiceBaseURI() {
            return context.getMicroServiceBaseURI();
        }

        @Override
        public <D extends ResourceDescriptor, R extends ManagedResource<D, ?>> R getManagedResourceByDescriptor(
                Class<D> descriptorClass,
                String name) {
            return context.getManagedResourceByDescriptor(descriptorClass, name);
        }

        @Override
        public <D extends ResourceDescriptor> boolean isSupportingResource(Class<D> resourceDescriptorClass) {
            return context.isSupportingResource(resourceDescriptorClass);
        }

        @Override
        public LocalCacheManager getLocalCacheManager() {
            return context.getLocalCacheManager();
        }

        @Override
        public MicroServiceState getServiceState() {
            return context.getServiceState();
        }

        @Override
        public ResourceManager<InputQueueDescriptor, InputQueueConfiguration, ManagedInputQueue> getQueuesManager() {
            return context.getQueuesManager();
        }

        @Override
        public List<ResourceManager> getExternalResourceManagers() {
            return context.getExternalResourceManagers();
        }

        @Override
        public String getServiceDescription() {
            return context.getServiceDescription();
        }

        @Override
        public RestResourceManager getRestResourceManager() {
            return context.getRestResourceManager();
        }

        @Override
        public MicroService getServiceDescriptor() {
            return context.getServiceDescriptor();
        }

        private static final class SyncMessageSender implements RuntimeMessageSender {
            private final OutputStream outputStream;

            private SyncMessageSender(OutputStream outputStream) {
                this.outputStream = outputStream;
            }

            @Override
            public void streamMessage(
                    MessageWriter messageWriter,
                    Map<String, String> messageHeaders,
                    String messageGroup) {
                messageWriter.writeMessage(outputStream);
            }
        }
    }
}
