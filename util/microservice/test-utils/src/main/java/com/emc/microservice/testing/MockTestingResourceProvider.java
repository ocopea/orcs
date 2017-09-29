// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2016 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.testing;

import com.emc.microservice.Context;
import com.emc.microservice.MicroserviceIdentifier;
import com.emc.microservice.ParametersBag;
import com.emc.microservice.ServiceConfig;
import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.microservice.blobstore.BlobStoreConfiguration;
import com.emc.microservice.blobstore.BlobStoreDescriptor;
import com.emc.microservice.blobstore.BlobStoreProvider;
import com.emc.microservice.blobstore.MicroServiceBlobStore;
import com.emc.microservice.blobstore.impl.TempFileSystemBlobStore;
import com.emc.microservice.datasource.DatasourceConfiguration;
import com.emc.microservice.datasource.DatasourceDescriptor;
import com.emc.microservice.datasource.DatasourceProvider;
import com.emc.microservice.datasource.DatasourceWrapper;
import com.emc.microservice.datasource.MicroServiceDataSource;
import com.emc.microservice.dependency.AsyncCallbackServiceDependencyDescriptor;
import com.emc.microservice.dependency.ServiceDependencyDescriptor;
import com.emc.microservice.dependency.ServiceDependencyType;
import com.emc.microservice.input.InputDescriptor;
import com.emc.microservice.messaging.DefaultMessageReader;
import com.emc.microservice.messaging.DestinationConfiguration;
import com.emc.microservice.messaging.DestinationDescriptor;
import com.emc.microservice.messaging.InputQueueConfiguration;
import com.emc.microservice.messaging.InputQueueDescriptor;
import com.emc.microservice.messaging.LoggingOutputStream;
import com.emc.microservice.messaging.ManagedMessageListener;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.messaging.MessageListener;
import com.emc.microservice.messaging.MessageListenerFactory;
import com.emc.microservice.messaging.MessageReader;
import com.emc.microservice.messaging.MessageWriter;
import com.emc.microservice.messaging.MessagingConstants;
import com.emc.microservice.messaging.MessagingProvider;
import com.emc.microservice.messaging.MessagingProviderConfiguration;
import com.emc.microservice.messaging.MessagingSerializationHelper;
import com.emc.microservice.messaging.QueueConfiguration;
import com.emc.microservice.messaging.QueueReceiverImpl;
import com.emc.microservice.messaging.RuntimeMessageSender;
import com.emc.microservice.output.OutputDescriptor;
import com.emc.microservice.output.ServiceOutputDescriptor;
import com.emc.microservice.registry.ServiceRegistryApi;
import com.emc.microservice.resource.ResourceConfiguration;
import com.emc.microservice.resource.ResourceConfigurationProperty;
import com.emc.microservice.resource.ResourceConfigurationPropertyType;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.microservice.restapi.ManagedResourceDescriptor;
import com.emc.microservice.restapi.MicroServiceWebServer;
import com.emc.microservice.restapi.WebServerConfiguration;
import com.emc.microservice.restapi.WebServerProvider;
import com.emc.microservice.schedule.ScheduleListener;
import com.emc.microservice.schedule.SchedulerConfiguration;
import com.emc.microservice.serialization.SerializationManager;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.microservice.webclient.WebApiResolverBuilder;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.microservice.schedule.SchedulerApi;
import com.emc.ocopea.util.io.StreamUtil;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MockTestingResourceProvider extends ResourceProvider {

    protected static final String TESTING_OUTPUT_QUEUE_NAME = "MicroServiceTestingAPIOutputQueue";
    private static int PORT = 6666;
    private static final Logger log = LoggerFactory.getLogger(MockTestingResourceProvider.class);

    private final Map<String, Integer> destinationActivationCount = new HashMap<>();
    private final Map<String, MessageListenerInfo> registeredTestMessageListeners = new HashMap<>();
    private final Map<String, QueueConfiguration> messageDestinationConfiguration = new HashMap<>();
    private final Map<String, DatasourceConfiguration> datasourceConfigurations = new HashMap<>();
    private final Map<String, BlobStoreConfiguration> blobstoreConfigurations = new HashMap<>();
    private final Map<String, ServiceConfig> serviceConfigMap = new HashMap<>();
    private final Map<String, Map<String, String>> registeredServiceConfigurationHashMap = new HashMap<>();
    private final Map<String, Context> loadedServicesContext = new HashMap<>();
    private final Map<String, Map<String, Map<String, String>>> overriddenResourceProperties = new HashMap<>();
    private final Map<String, Map<Class, Object>> restResourceMockImplementationsByServiceUrn = new HashMap<>();
    private final Map<String, Map<Class, Object>> restResourceMockImplementationsByUrl = new HashMap<>();
    private static final String FAKE_URL_PREFIX = "fakettp://";
    private final MockScheduler scheduler = new MockScheduler();

    private final Map<String, Class<? extends ScheduleListener>> schedulerListeners = new HashMap<>();

    public void addSchedulerListenerMapping(
            String listenerIdentifier,
            Class<? extends ScheduleListener> listenerClass) {
        schedulerListeners.put(listenerIdentifier, listenerClass);
    }

    private static class MessageListenerInfo {
        String listeningServiceURI;

        MessageListener messageListener;

        public MessageListenerInfo(String listeningServiceURI, MessageListener messageListener) {
            this.listeningServiceURI = listeningServiceURI;
            this.messageListener = messageListener;
        }

    }

    public MockTestingResourceProvider(DataSource dataSource) {
        super(null, new MockTestingServiceRegistryApi());
        ((MockTestingServiceRegistryApi) this.getServiceRegistryApi()).provider = this;
        ((MockTestingDatasourceProvider) this.getDatasourceProvider()).setDataSource(dataSource);
    }

    @Override
    public <SchedulerConfT extends SchedulerConfiguration> Class<SchedulerConfT> getSchedulerConfigurationClass() {
        return (Class<SchedulerConfT>) SchedulerConfiguration.class;
    }

    private void addServiceInputQueue(MicroserviceIdentifier serviceIdentifier) {
        String dependentServiceInputQueueName = serviceIdentifier.getDefaultInputQueueName();
        if (!messageDestinationConfiguration.containsKey(dependentServiceInputQueueName)) {
            messageDestinationConfiguration.put(
                    dependentServiceInputQueueName,
                    new MockQueueConfiguration(QueueConfiguration.MessageDestinationType.QUEUE));
            destinationActivationCount.put(dependentServiceInputQueueName, 0);
        }
    }

    protected static class MockDatasourceConfiguration extends DatasourceConfiguration {
        private static final ResourceConfigurationProperty PROPERTY_TEST_LOOKUP = new ResourceConfigurationProperty(
                "testLookup",
                ResourceConfigurationPropertyType.STRING,
                "test lookup",
                true,
                false);
        private static final ResourceConfigurationProperty PROPERTY_DB_SCHEMA = new ResourceConfigurationProperty(
                "dbSchema",
                ResourceConfigurationPropertyType.STRING,
                "Database schema",
                true,
                false);

        public MockDatasourceConfiguration() {
            super("Mock Testing Datasource", Arrays.asList(PROPERTY_TEST_LOOKUP, PROPERTY_DB_SCHEMA));
        }

        public MockDatasourceConfiguration(String dsName) {
            this();
            setPropertyValues(propArrayToMap(
                    new String[]{
                            PROPERTY_TEST_LOOKUP.getName(), "test://DataSource//" + dsName,
                            PROPERTY_DB_SCHEMA.getName(), makeSchemaSafe(dsName)
                    }));
        }

        @Override
        public String getDatabaseSchema() {
            return getProperty(PROPERTY_DB_SCHEMA.getName());
        }
    }

    public static String makeSchemaSafe(String dsName) {
        return dsName.replaceAll("-", "_");
    }

    protected static class MockTestingWebServerConfiguration extends WebServerConfiguration {
        private static final ResourceConfigurationProperty PROPERTY_PORT =
                new ResourceConfigurationProperty("port", ResourceConfigurationPropertyType.INT, "port", true, false);

        public MockTestingWebServerConfiguration() {
            super("Mock WebServer", Arrays.asList(PROPERTY_PORT));
        }

        public MockTestingWebServerConfiguration(int port) {
            this();
            setPropertyValues(propArrayToMap(new String[]{PROPERTY_PORT.getName(), String.valueOf(port)}));
        }

        public int getPort() {
            return Integer.parseInt(getProperty(PROPERTY_PORT.getName()));
        }
    }

    protected static class MockTestingBlobStoreConfiguration extends BlobStoreConfiguration {
        private static final ResourceConfigurationProperty PROPERTY_NAME = new ResourceConfigurationProperty(
                "name",
                ResourceConfigurationPropertyType.STRING,
                "name",
                true,
                false);

        public MockTestingBlobStoreConfiguration(String blobStoreName) {
            this();
            setPropertyValues(propArrayToMap(new String[]{
                    PROPERTY_NAME.getName(), blobStoreName
            }));
        }

        public String getName() {
            return getProperty(PROPERTY_NAME.getName());
        }

        public MockTestingBlobStoreConfiguration() {
            super("Mock Blobstore configuration", Arrays.asList(PROPERTY_NAME));
        }
    }

    protected static class MockMessagingProviderConfiguration extends MessagingProviderConfiguration {

        public MockMessagingProviderConfiguration() {
            super("Test Mock Messaging", Collections.<ResourceConfigurationProperty>emptyList());
        }

        @Override
        public String getMessagingNode() {
            return "localhost";
        }
    }

    protected static class MockQueueConfiguration extends QueueConfiguration {
        private static final ResourceConfigurationProperty PROPERTY_DESTINATION_TYPE =
                new ResourceConfigurationProperty(
                        "destinationType",
                        ResourceConfigurationPropertyType.ENUM,
                        "destination object type",
                        true,
                        false);
        private static final ResourceConfigurationProperty PROPERTY_BLOBSTORE_NAME = new ResourceConfigurationProperty(
                "blobstoreName",
                ResourceConfigurationPropertyType.STRING,
                "Blobstore to use for large messages",
                false,
                false);

        private static final List<ResourceConfigurationProperty> PROPERTIES = Arrays.asList(
                PROPERTY_DESTINATION_TYPE, PROPERTY_BLOBSTORE_NAME);

        public MockQueueConfiguration() {
            super("Mock Testing Destination Messaging", PROPERTIES);
        }

        public MockQueueConfiguration(MessageDestinationType destinationType) {
            this();
            setPropertyValues(propArrayToMap(new String[]{
                    PROPERTY_DESTINATION_TYPE.getName(), destinationType.name()
            }));

        }

        @Override
        public MessageDestinationType getMessageDestinationType() {
            return MessageDestinationType.valueOf(getProperty(PROPERTY_DESTINATION_TYPE.getName()));
        }

        @Override
        public boolean isGzip() {
            return false;
        }

        @Override
        public String getBlobstoreName() {
            return getProperty(PROPERTY_BLOBSTORE_NAME.getName());
        }
    }

    private static class MockTestingServiceRegistryApi implements ServiceRegistryApi {
        MockTestingResourceProvider provider;

        @Override
        public ServiceConfig getServiceConfig(String serviceURI) {
            ServiceConfig serviceConfig = provider.serviceConfigMap.get(serviceURI);

            if (serviceConfig == null) {
                serviceConfig = ServiceConfig.generateServiceConfig(
                        serviceURI,
                        null,
                        FAKE_URL_PREFIX + serviceURI,
                        "DEBUG",
                        Collections.<String, String>emptyMap(),
                        null,
                        null,
                        new HashMap<>(),
                        new HashMap<>(),
                        new HashMap<>(),
                        new HashMap<String, Map<String, String>>());
            }
            return serviceConfig;
        }

        @Override
        public <DatasourceConfT extends DatasourceConfiguration> DatasourceConfT getDataSourceConfiguration(
                Class<DatasourceConfT> confClass,
                String dataSourceName) {
            return confClass.cast(provider.datasourceConfigurations.get(dataSourceName));
        }

        @Override
        public <MessagingConfT extends MessagingProviderConfiguration> MessagingConfT getMessagingProviderConfiguration(
                Class<MessagingConfT> confClass,
                String messagingSystemName) {
            return confClass.cast(new MockMessagingProviderConfiguration());
        }

        @Override
        public <SchedulerConfT extends SchedulerConfiguration> SchedulerConfT getSchedulerConfiguration(
                Class<SchedulerConfT> confClass,
                String name) {
            return confClass.cast(new SchedulerConfiguration());
        }

        @Override
        public <BlobstoreConfT extends BlobStoreConfiguration> BlobstoreConfT getBlobStoreConfiguration(
                Class<BlobstoreConfT> confClass,
                String blobstoreName) {
            BlobStoreConfiguration configuration = provider.blobstoreConfigurations.get(blobstoreName);
            if (configuration == null) {
                configuration = new MockTestingBlobStoreConfiguration(blobstoreName);
                provider.blobstoreConfigurations.put(blobstoreName, configuration);
            }
            return confClass.cast(configuration);
        }

        @Override
        public <QueueConfT extends QueueConfiguration> QueueConfT getQueueConfiguration(
                Class<QueueConfT> confClass,
                String queueName,
                Context context) {
            return confClass.cast(provider.messageDestinationConfiguration.get(queueName));
        }

        @Override
        public <WebserverConfT extends WebServerConfiguration> WebserverConfT getWebServerConfiguration(
                Class<WebserverConfT> confClass,
                String webServerName) {
            return confClass.cast(new MockTestingWebServerConfiguration(PORT));
        }

        @Override
        public <T extends ResourceConfiguration> T getExternalResourceConfiguration(
                String resourceName,
                Class<T> externalResourceConfigurationClass) {
            Map<String, String> externalResourceProperties = Objects.requireNonNull(
                    provider.registeredServiceConfigurationHashMap.get(resourceName),
                    "External resource not found in registry: " + resourceName);

            return ResourceConfiguration.asSpecificConfiguration(
                    externalResourceConfigurationClass,
                    externalResourceProperties);
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
            provider.registeredServiceConfigurationHashMap.put(name, serviceProperties);
        }

        @Override
        public void registerDataSource(String name, DatasourceConfiguration datasourceConf) {
            provider.datasourceConfigurations.put(name, datasourceConf);
        }

        @Override
        public void registerBlobStore(String name, BlobStoreConfiguration configuration) {
            provider.blobstoreConfigurations.put(name, configuration);
        }

        @Override
        public void registerQueue(String name, QueueConfiguration configuration) {
            provider.messageDestinationConfiguration.put(name, configuration);
        }

        @Override
        public void registerMessaging(String name, MessagingProviderConfiguration configuration) {
            throw new UnsupportedOperationException("No!");
        }

        @Override
        public void registerWebServer(String name, WebServerConfiguration configuration) {
            throw new UnsupportedOperationException("No!");
        }

        @Override
        public void registerExternalResource(String name, Map<String, String> properties) {
            provider.registeredServiceConfigurationHashMap.put(name, properties);
        }

        @Override
        public void registerScheduler(String name, SchedulerConfiguration schedulerConf) {
            throw new UnsupportedOperationException("No!");
        }

        @Override
        public Map<String, ServiceConfig> listServiceConfig() {
            return provider.serviceConfigMap;
        }

        @Override
        public <DatasourceConfT extends DatasourceConfiguration> Map<String, DatasourceConfT> listDataSources(
                Class<DatasourceConfT> confClass) {
            //noinspection unchecked
            return (Map<String, DatasourceConfT>) provider.datasourceConfigurations;
        }

        @Override
        public <BlobstoreConfT extends BlobStoreConfiguration> Map<String, BlobstoreConfT> listBlobStores(
                Class<BlobstoreConfT> confClass) {
            //noinspection unchecked
            return (Map<String, BlobstoreConfT>) provider.blobstoreConfigurations;
        }

        @Override
        public <QueueConfT extends QueueConfiguration> Map<String, QueueConfT> listQueues(Class<QueueConfT> confClass) {
            //noinspection unchecked
            return (Map<String, QueueConfT>) provider.messageDestinationConfiguration;
        }

        @Override
        public <MessagingConfT extends MessagingProviderConfiguration> Map<String, MessagingConfT> listMessagingSystems(
                Class<MessagingConfT> confClass) {
            Map<String, MessagingConfT> c = new HashMap<>();
            c.put(
                    MessagingProviderConfiguration.DEFAULT_MESSAGING_SYSTEM_NAME,
                    confClass.cast(new MockMessagingProviderConfiguration()));
            return c;
        }

        @Override
        public void registerServiceConfig(String serviceURI, ServiceConfig serviceConfig) {
            provider.serviceConfigMap.put(serviceURI, serviceConfig);
        }
    }

    private class MockTestingMessagingProvider
            implements MessagingProvider<MockQueueConfiguration, MockMessagingProviderConfiguration> {
        @Override
        public RuntimeMessageSender getMessageSender(
                MockMessagingProviderConfiguration messagingConfiguration,
                DestinationConfiguration destinationConfiguration,
                MockQueueConfiguration queueConf,
                Context context) {
            final String destinationName = destinationConfiguration.getDestinationQueueURI();
            return new RuntimeMessageSender() {

                @Override
                public void streamMessage(
                        final MessageWriter messageWriter,
                        final Map<String, String> messageHeaders,
                        String messageGroup) {
                    Integer activationCount = destinationActivationCount.get(destinationName);
                    Objects.requireNonNull(activationCount, "Undefined destination: " + destinationName);
                    destinationActivationCount.put(destinationName, activationCount + 1);

                    MessageListenerInfo registeredDestinationListener =
                            registeredTestMessageListeners.get(destinationName);

                    String blobKeyValue = null;
                    String blobStoreNameToUse = "SHMOKI";
                    if (queueConf.getBlobstoreName() != null) {
                        blobStoreNameToUse = queueConf.getBlobstoreName();
                    }
                    final BlobStoreAPI blobStoreAPI =
                            getBlobStore(new MockTestingBlobStoreConfiguration(blobStoreNameToUse), context);
                    if (queueConf.getBlobstoreName() != null) {

                        blobKeyValue = messageHeaders.get(destinationConfiguration.getBlobKeyHeaderName());
                        if (blobKeyValue == null) {
                            blobKeyValue = UUID.randomUUID().toString();
                        }

                        blobStoreAPI.create(getBlobNamespace(), blobKeyValue, messageHeaders, out -> {
                            if (queueConf.isGzip()) {
                                try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(out)) {
                                    messageWriter.writeMessage(gzipOutputStream);
                                } catch (IOException e) {
                                    throw new IllegalStateException(e);
                                }
                            } else {
                                messageWriter.writeMessage(out);
                            }
                        });
                    }

                    final String blobKey = blobKeyValue;

                    // Checking whether a test has registered for consuming this destination
                    final Message message = new Message() {
                        @Override
                        public String getMessageHeader(String headerName) {
                            return messageHeaders.get(headerName);
                        }

                        @Override
                        public Map<String, String> getMessageHeaders() {
                            return extractHeaders(MessagingSerializationHelper.HEADER_FILTER);
                        }

                        @Override
                        public Map<String, String> getMessageContext() {
                            return extractHeaders(MessagingSerializationHelper.CONTEXT_FILTER);
                        }

                        @Override
                        public String getContextValue(String key) {
                            return getMessageContext().get(key);
                        }

                        private Map<String, String> extractHeaders(MessagingSerializationHelper.Filter filter) {

                            Map<String, String> stringHeaders = new HashMap<>(messageHeaders.size());
                            for (Map.Entry<String, String> currEntry : messageHeaders.entrySet()) {
                                String key = currEntry.getKey();
                                if (filter.accept(key)) {
                                    Object value = currEntry.getValue();
                                    if (value == null) {
                                        stringHeaders.put(filter.transform(key), null);
                                    } else {
                                        stringHeaders.put(filter.transform(key), value.toString());
                                    }
                                }
                            }

                            return stringHeaders;
                        }

                        @Override
                        public void readMessage(final MessageReader messageReader) {

                            if (queueConf.getBlobstoreName() != null) {
                                //read the key and get the blob from blobstore
                                if (blobStoreAPI.isExists(getBlobNamespace(), blobKey)) {
                                    blobStoreAPI.readBlob(getBlobNamespace(), blobKey, in -> {
                                        if (queueConf.isGzip()) {
                                            try (GZIPInputStream gzipInputStream = new GZIPInputStream(in)) {
                                                messageReader.read(gzipInputStream);
                                            } catch (IOException e) {
                                                throw new IllegalStateException(e);
                                            }
                                        } else {
                                            messageReader.read(in);
                                        }
                                    });
                                } else {
                                    throw new IllegalStateException(
                                            "Failed reading message with id (id doesn't exist)" + blobKey);
                                }
                            } else {
                                // Simulate true streaming using Piped Input/Output streams
                                try (PipedInputStream in = new PipedInputStream()) {
                                    final PipedOutputStream out = new PipedOutputStream(in);
                                    new Thread(
                                            () -> {
                                                try {
                                                    try {
                                                        messageWriter.writeMessage(new LoggingOutputStream(out));
                                                    } finally {
                                                        out.close();
                                                    }
                                                } catch (IOException e) {
                                                    throw new IllegalStateException(
                                                            "Failed closing piped output stream",
                                                            e);
                                                }
                                            }
                                    ).start();

                                    messageReader.read(in);
                                } catch (IOException e) {
                                    throw new IllegalStateException(e);
                                }
                            }
                        }

                        @Override
                        public Object getUnderlyingMessageObject() {
                            return this;
                        }

                        @Override
                        public <T> T readObject(Class<T> format) {
                            DefaultMessageReader<T> messageReader = new DefaultMessageReader<>(
                                    context.getSerializationManager().getReader(format),
                                    format);
                            readMessage(messageReader);
                            return messageReader.getResult();
                        }

                    };

                    String errorKeyValue = messageHeaders.get(MessagingConstants.ERROR_HEADER);

                    if (registeredDestinationListener != null) {
                        if (errorKeyValue != null) {
                            registeredDestinationListener.messageListener
                                    .onErrorMessage(
                                            message,
                                            loadedServicesContext.get(
                                                    registeredDestinationListener.listeningServiceURI));
                        } else {
                            registeredDestinationListener.messageListener
                                    .onMessage(
                                            message,
                                            loadedServicesContext.get(
                                                    registeredDestinationListener.listeningServiceURI));
                        }
                    } else {
                        // If test not registered for this destination, simply printing the content of message
                        System.out.println(printMessage(message));
                    }
                }

                private String getBlobNamespace() {
                    return destinationConfiguration.getBlobNamespace() == null ?
                            "MUKI-D" :
                            destinationConfiguration.getBlobNamespace();
                }
            };
        }

        @Override
        public void createQueue(
                MockMessagingProviderConfiguration messagingConfiguration,
                MockQueueConfiguration queueConf) {
            throw new UnsupportedOperationException("No!");
        }

        @Override
        public QueueReceiverImpl createQueueReceiver(
                MockMessagingProviderConfiguration messagingConfiguration,
                InputQueueConfiguration inputQueueConfiguration,
                MockQueueConfiguration mockQueueConfiguration,
                Map<String, MockQueueConfiguration> deadLetterQueueConfs,
                ManagedMessageListener messageListener,
                Context context,
                String consumerName) {
            MessageReceiverMock messageReceiverMock = new MessageReceiverMock(
                    inputQueueConfiguration,
                    mockQueueConfiguration,
                    messageListener,
                    context,
                    consumerName);
            registerTestMessageListener(
                    inputQueueConfiguration.getInputQueueURI(),
                    new MessageListener() {
                        @Override
                        public void onMessage(Message message, Context context) {
                            messageListener.onMessage(message, context);
                        }

                        @Override
                        public void onErrorMessage(Message message, Context context) {
                            messageListener.onErrorMessage(message, context);
                        }
                    },
                    context.getMicroServiceBaseURI());
            return messageReceiverMock;
        }

        @Override
        public Class<MockQueueConfiguration> getQueueConfClass() {
            return MockQueueConfiguration.class;
        }

        @Override
        public Class<MockMessagingProviderConfiguration> getMessageConfClass() {
            return MockMessagingProviderConfiguration.class;
        }
    }

    private class MockTestingDatasourceProvider implements DatasourceProvider<MockDatasourceConfiguration> {
        private DataSource dataSource;

        @Override
        public MicroServiceDataSource getDatasource(MockDatasourceConfiguration mockDatasourceConfiguration) {
            return wrapDataSource(this.dataSource);
        }

        @Override
        public Class<MockDatasourceConfiguration> getConfClass() {
            return MockDatasourceConfiguration.class;
        }

        public void setDataSource(DataSource dataSource) {
            this.dataSource = dataSource;
        }
    }

    /**
     * Wrapping datasource with MS datasource avoiding tx
     */
    public static MicroServiceDataSource wrapDataSource(DataSource dataSource) {
        return new DatasourceWrapper(dataSource) {
            @Override
            public void beginTransaction() {
                log.info("Mock tx begin");
            }

            @Override
            public void commitTransaction() {
                log.info("Mock tx commit");
            }

            @Override
            public void rollbackTransaction() {
                log.info("Mock tx rollback");
            }

            @Override
            public boolean isInTransaction() {
                return false;
            }
        };
    }

    private class MockTestingBlobStoreProvider implements BlobStoreProvider<MockTestingBlobStoreConfiguration> {
        private final Map<String, TempFileSystemBlobStore> blobStores = new ConcurrentHashMap<>();

        @Override
        public BlobStoreAPI getBlobStore(
                MockTestingBlobStoreConfiguration configuration,
                SerializationManager serializationManager) {
            TempFileSystemBlobStore blobStore = blobStores.get(configuration.getName());
            if (blobStore == null) {
                blobStore = new TempFileSystemBlobStore();
                blobStores.put(configuration.getName(), blobStore);
            }
            return new MicroServiceBlobStore(blobStore, serializationManager);

        }

        @Override
        public Class<MockTestingBlobStoreConfiguration> getConfClass() {
            return MockTestingBlobStoreConfiguration.class;
        }
    }

    private class MockTestingWebServerProvider implements WebServerProvider<MockTestingWebServerConfiguration> {
        @Override
        public MicroServiceWebServer getWebServer(MockTestingWebServerConfiguration configuration) {
            return new MicroServiceWebServer() {

                @Override
                public void deployServiceApplication(Context context) {
                    //no real web server for testing at this point
                    System.out.println("Starting web server for " + context.getMicroServiceName());

                    loadedServicesContext.put(context.getMicroServiceBaseURI(), context);
                    Map<String, ServiceConfig.InputQueueConfig> inputQueueConfig = new HashMap<>();
                    Map<String, ServiceConfig.DestinationQueueConfig> destinationQueueConfig = new HashMap<>();

                    // Setting queue configurations
                    for (InputQueueDescriptor currQueueDesc : context.getQueuesManager().getDescriptors()) {
                        registerQ(currQueueDesc.getName());
                        inputQueueConfig.put(
                                currQueueDesc.getName(),
                                new ServiceConfig.InputQueueConfig(1, true, null));
                    }

                    // Setting destinations configuration
                    for (DestinationDescriptor currDestinationDesc : context.getDestinationManager().getDescriptors()) {
                        registerQ(currDestinationDesc.getName());
                        destinationQueueConfig.put(
                                currDestinationDesc.getName(),
                                new ServiceConfig.DestinationQueueConfig(null, null, true));
                    }

                    // Scanning service input type
                    if (context.getInputDescriptor() != null) {
                        if (context.getInputDescriptor().getInputType() ==
                                InputDescriptor.MicroServiceInputType.messaging) {
                            // Adding destination Configuration for queue input
                            String standardInputQueueName =
                                    context.getServiceDescriptor().getIdentifier().getDefaultInputQueueName();

                            registerQ(standardInputQueueName);
                            inputQueueConfig.put(
                                    standardInputQueueName,
                                    new ServiceConfig.InputQueueConfig(1, true, null));
                        }
                    }

                    // Scanning service output type
                    if (context.getOutputDescriptor() != null) {
                        if (context.getOutputDescriptor().getOutputType() ==
                                OutputDescriptor.MicroServiceOutputType.service) {
                            // Adding destination Configuration for queue input of output service
                            String standardInputQueueName =
                                    new MicroserviceIdentifier(((ServiceOutputDescriptor) context.getOutputDescriptor())
                                            .getServiceURI()).getDefaultInputQueueName();
                            registerQ(standardInputQueueName);
                            destinationQueueConfig.put(
                                    standardInputQueueName,
                                    new ServiceConfig.DestinationQueueConfig(null, null, true));
                        }
                    }

                    destinationActivationCount.put(TESTING_OUTPUT_QUEUE_NAME, 0);

                    // Setting the datasources configurations
                    for (DatasourceDescriptor currDSDescriptor : context.getDatasourceManager().getDescriptors()) {
                        datasourceConfigurations.put(
                                currDSDescriptor.getName(),
                                new MockDatasourceConfiguration(currDSDescriptor.getName()));
                    }

                    // Setting the blobstore configurations
                    for (BlobStoreDescriptor currBlobDesc : context.getBlobStoreManager().getDescriptors()) {
                        blobstoreConfigurations.put(
                                currBlobDesc.getName(),
                                new MockTestingBlobStoreConfiguration(currBlobDesc.getName()));
                    }

                    // Setting the service dependencies configurations
                    for (final ServiceDependencyDescriptor currServiceDependencyDescriptor : context
                            .getDependencyManager()
                            .getDescriptors()) {

                        addServiceInputQueue(currServiceDependencyDescriptor.getDependentServiceIdentifier());

                        if (currServiceDependencyDescriptor.getServiceDependencyType() ==
                                ServiceDependencyType.ASYNC_CALL) {
                            List<String> messageRoutingTable = currServiceDependencyDescriptor.getMessageRoutingTable();
                            for (String currDependentServiceShortName : messageRoutingTable) {
                                addServiceInputQueue(new MicroserviceIdentifier(currDependentServiceShortName));
                            }

                            String asyncCallResultQueue = context
                                    .getServiceDescriptor()
                                    .getIdentifier()
                                    .getDependencyCallbackQueueName(currServiceDependencyDescriptor.getLastRoute());
                            registerQ(asyncCallResultQueue);
                            inputQueueConfig.put(
                                    asyncCallResultQueue,
                                    new ServiceConfig.InputQueueConfig(1, true, null));

                            registerTestMessageListener(asyncCallResultQueue, new MessageListener() {
                                @Override
                                public void onMessage(Message message, Context context) {
                                    MessageListenerFactory.createMessageListener(
                                            ((AsyncCallbackServiceDependencyDescriptor) currServiceDependencyDescriptor)
                                                    .getServiceResultCallback(),
                                            context).onMessage(message, context);
                                }

                                @Override
                                public void onErrorMessage(Message message, Context context) {

                                }
                            }, context.getMicroServiceBaseURI());
                        } else if (currServiceDependencyDescriptor.getServiceDependencyType() ==
                                ServiceDependencyType.SEND_AND_FORGET) {
                            List<String> messageRoutingTable = currServiceDependencyDescriptor.getMessageRoutingTable();
                            for (String currDependentServiceShortName : messageRoutingTable) {
                                addServiceInputQueue(new MicroserviceIdentifier(currDependentServiceShortName));
                            }
                        }
                    }

                    // the way to override metadata loader for tests
                    Map<String, Map<String, String>> externalConfig = new HashMap<>();
                    final Map<String, Map<String, String>> overriddenExternal =
                            overriddenResourceProperties.get(ServiceRegistryApi.SERVICE_TYPE_EXTERNAL_RESOURCE);
                    if (overriddenExternal != null) {
                        final Map<String, String> overriddenForService =
                                overriddenExternal.get(context.getMicroServiceBaseURI());
                        if (overriddenForService != null) {
                            final String metadataLoaderImpl = overriddenForService.get("metadataLoaderImpl");
                            if (metadataLoaderImpl != null) {
                                externalConfig.put("dpa-metadata", overriddenForService);
                            }
                        }
                    }
                    serviceConfigMap.put(
                            context.getMicroServiceBaseURI(),
                            ServiceConfig.generateServiceConfig(
                                    context.getMicroServiceBaseURI(),
                                    null,
                                    FAKE_URL_PREFIX + context.getMicroServiceBaseURI(),
                                    "DEBUG",
                                    Collections.emptyMap(),
                                    inputQueueConfig,
                                    destinationQueueConfig,
                                    new HashMap<>(),
                                    new HashMap<>(),
                                    loadServiceParameters(context),
                                    externalConfig));
                }

                @Override
                public void unDeployServiceApplication(Context context) {
                    System.out.println("Stopping web server");
                }

                @Override
                public Set<String> listDeploymentURNs() {
                    return Collections.unmodifiableSet(loadedServicesContext.keySet());
                }

                @Override
                public int getPort() {
                    return PORT;
                }
            };
        }

        @Override
        public Class<MockTestingWebServerConfiguration> getConfClass() {
            return MockTestingWebServerConfiguration.class;
        }
    }

    @Override
    protected DatasourceProvider createDatasourceProvider() {
        return new MockTestingDatasourceProvider();
    }

    @Override
    protected MessagingProvider createMessagingProvider() {
        return new MockTestingMessagingProvider();
    }

    @Override
    protected BlobStoreProvider createBlobStoreProvider() {
        return new MockTestingBlobStoreProvider();
    }

    @Override
    protected WebServerProvider createWebServerProvider() {
        return new MockTestingWebServerProvider();
    }

    @Override
    public SchedulerApi getScheduler(
            SchedulerConfiguration schedulerConfiguration,
            Context context) {
        return scheduler;
    }

    private Map<String, String> loadServiceParameters(Context context) {
        Map<String, String> serviceParameterValues = new HashMap<>();
        for (Map.Entry<String, ParametersBag.MicroServiceParameterDescriptor> currParamEntry : context
                .getParametersBag()
                .getParameterDescriptorsMap()
                .entrySet()) {
            String propertyValue = System.getProperty(context.getMicroServiceBaseURI() + "_" + currParamEntry.getKey());
            if (propertyValue != null) {
                log.info("Overriding property value {} with {}", currParamEntry.getKey(), propertyValue);
            }
            serviceParameterValues.put(currParamEntry.getKey(), propertyValue);

        }
        return serviceParameterValues;
    }

    private void registerQ(String standardInputQueueName) {

        Map<String, String> stringStringMap = overrideResourceProperties(
                ServiceRegistryApi.SERVICE_TYPE_QUEUE,
                standardInputQueueName,
                new MockQueueConfiguration(QueueConfiguration.MessageDestinationType.QUEUE).getPropertyValues(),
                overriddenResourceProperties);
        messageDestinationConfiguration.put(
                standardInputQueueName,
                ResourceConfiguration.asSpecificConfiguration(MockQueueConfiguration.class, stringStringMap));
        destinationActivationCount.put(standardInputQueueName, 0);
    }

    private static Map<String, String> overrideResourceProperties(
            String type,
            String name,
            Map<String, String> propertyValues,
            Map<String, Map<String, Map<String, String>>> overridenResourceProperties) {
        Map<String, String> props = new HashMap<>(propertyValues);
        Map<String, Map<String, String>> byType = overridenResourceProperties.get(type);
        if (byType != null) {
            Map<String, String> byName = byType.get(name);
            if (byName != null) {
                props.putAll(byName);
            }
        }
        return props;

    }

    @Override
    public String getNodeAddress() {
        return "localhost";
    }

    @NoJavadoc
    public void registerTestMessageListener(
            String destinationName,
            MessageListener messageListener,
            String listeningServiceURI) {

        // In case this queue is not registered, registering it ourselves
        MockQueueConfiguration queueConfiguration =
                getServiceRegistryApi().getQueueConfiguration(MockQueueConfiguration.class, destinationName, null);
        if (queueConfiguration == null) {
            registerQ(destinationName);
        }
        registeredTestMessageListeners.put(
                destinationName,
                new MessageListenerInfo(listeningServiceURI, messageListener));
    }

    private static String printMessage(Message message) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            message.readMessage(in -> {
                StreamUtil.copyLarge(in, out);
            });

            return new String(out.toByteArray(), "UTF-8");
        } catch (IOException err) {
            err.printStackTrace();
            Assert.fail("failed reading message content " + err.getMessage());
        }
        return null;
    }

    public Map<String, Integer> getDestinationActivationCount() {
        return destinationActivationCount;
    }

    /**
     * Allows mocking specific service api proxy by providing service urn and implementation for an api class
     */
    public <T> void addMockRestResourceImplementation(String serviceUrn, Class<T> clazz, T impl) {
        restResourceMockImplementationsByServiceUrn.computeIfAbsent(serviceUrn, s -> new HashMap<>()).put(clazz, impl);
    }

    /**
     * Allows mocking specific service api proxy by providing raw url and an implementation class
     */
    public <T> void addMockRestResourceImplementationByUrl(String url, Class<T> clazz, T impl) {
        restResourceMockImplementationsByUrl.computeIfAbsent(url, s -> new HashMap<>()).put(clazz, impl);
    }

    @Override
    public WebAPIResolver getWebAPIResolver() {
        return new WebAPIResolver() {

            @Override
            public WebAPIResolver buildResolver(WebApiResolverBuilder builder) {
                return this;
            }

            @Override
            public <T> T getWebAPI(String url, Class<T> resourceWebApi) {
                if (url.startsWith(FAKE_URL_PREFIX)) {
                    return instantiateWebResource(url.substring(FAKE_URL_PREFIX.length()), resourceWebApi);
                }

                // Checking if mock by url exists

                //noinspection unchecked
                return Optional.ofNullable(
                        (T) restResourceMockImplementationsByUrl
                                .getOrDefault(url, Collections.emptyMap())
                                .get(resourceWebApi))
                        .orElseThrow(() -> new UnsupportedOperationException("No can do hombre, this is just a test"));
            }

            @Override
            public WebTarget getWebTarget(String url) {
                throw new UnsupportedOperationException("No can do hombre webTarget  , this is just a test");
            }
        };
    }

    private <T> T instantiateWebResource(String serviceURI, Class<T> resourceWebAPI) {
        Map<Class, Object> resources = restResourceMockImplementationsByServiceUrn.get(serviceURI);
        T impl;
        if (resources != null) {
            // First try a mock overridden by test code itself
            //noinspection unchecked
            return (T) resources.get(resourceWebAPI);
        }

        Context serviceContext = Objects.requireNonNull(
                loadedServicesContext.get(serviceURI),
                "No mock specified / service not loaded as part of test for resource " +
                        resourceWebAPI.getSimpleName() + " for service " + serviceURI);

        Class resourceClass = getManagedResourceDescriptor(resourceWebAPI, serviceContext);

        // Instantiate the resource and provide application context
        try {
            //noinspection unchecked
            impl = (T) resourceClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(
                    "Rest Resource " + resourceClass.getSimpleName() + " Must have a public default constructor",
                    e);
        }

        try {
            @SuppressWarnings("unchecked") Method setApplicationMethod =
                    resourceClass.getMethod("setApplication", Application.class);
            setApplicationMethod.invoke(impl, new MicroServiceTestingRestApplication(serviceContext));
        } catch (NoSuchMethodException e) {
            // In case we don't have a setApplication setter, simply skipping this...
            System.out.println("Resource " + resourceClass.getCanonicalName() + " does not implement setApplication");
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            throw new IllegalStateException(
                    "Failed Invoking resource " + resourceClass.getCanonicalName() + " setApplication Method",
                    e);
        }
        return impl;
    }

    private <T> Class getManagedResourceDescriptor(Class<T> resourceWebAPI, Context serviceContext) {
        for (ManagedResourceDescriptor currResource : serviceContext
                .getRestResourceManager()
                .getResourceDescriptorMap()
                .values()) {
            if (resourceWebAPI.isAssignableFrom(currResource.getResourceClass())) {
                return currResource.getResourceClass();
            }
        }
        throw new IllegalStateException("Invalid Service resource " + resourceWebAPI.getCanonicalName());
    }

    private static class MockScheduler implements SchedulerApi {
        private Set<String> jobs = new ConcurrentSkipListSet<>();
        private Map<String, Function<String, Boolean>> recurringTaskMappings = new HashMap<>();

        @Override
        public void scheduleRecurring(String name, int intervalInSeconds, String payload, String functionIdentifier) {
            jobs.add(name);
            Function<String, Boolean> taskFunction = recurringTaskMappings.get(functionIdentifier);
            new Thread(() -> {
                while (jobs.contains(name)) {
                    boolean continueRunning = true;
                    try {
                        continueRunning = taskFunction.apply(payload);
                    } catch (Exception ex) {
                        System.out.println("failed executing recurring task. name=" +
                                name + " payload=" + payload);
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // ha ha ha
                    }
                    if (!continueRunning) {
                        jobs.remove(name);
                    }
                }
            }).start();
        }

        @Override
        public void registerRecurringTask(
                String recurringTaskIdentifier,
                Function<String, Boolean> recurringTaskFunction) {
            recurringTaskMappings.put(recurringTaskIdentifier, recurringTaskFunction);
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {

        }
    }
}
