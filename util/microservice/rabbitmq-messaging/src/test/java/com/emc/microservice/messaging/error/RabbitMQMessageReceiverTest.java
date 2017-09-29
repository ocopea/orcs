// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2016 EMC Corporation All Rights Reserved
 */

package com.emc.microservice.messaging.error;

import com.emc.microservice.Context;
import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceState;
import com.emc.microservice.ParametersBag;
import com.emc.microservice.blobstore.BlobStoreAPI;
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
import com.emc.microservice.messaging.ManagedInputQueueImpl;
import com.emc.microservice.messaging.ManagedMessageDestination;
import com.emc.microservice.messaging.ManagedMessageListener;
import com.emc.microservice.messaging.ManagedMessageListenerImpl;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.messaging.MessageListener;
import com.emc.microservice.messaging.MessageSender;
import com.emc.microservice.messaging.MessagingConstants;
import com.emc.microservice.messaging.QueueReceiver;
import com.emc.microservice.metrics.MetricsRegistry;
import com.emc.microservice.metrics.MetricsRegistryImpl;
import com.emc.microservice.output.OutputDescriptor;
import com.emc.microservice.resource.ManagedResource;
import com.emc.microservice.resource.ResourceConfiguration;
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
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Created by zydowr on 18/02/2016.
 */
public class RabbitMQMessageReceiverTest {

    private static Logger log = LoggerFactory.getLogger(RabbitMQMessageReceiverTest.class);
    private static String[] queueConf = {
            "name", "test.queue.uri",
            "queueName", "test.queue.name",
            "exchangeName", "test.exchange",
            "destinationType", "QUEUE",
            "gzip", "false",
            "messageGrouping", "false",
            "x-message-ttl", "1000",
            "x-dead-letter-exchange", "test.dle",
            "x-dead-letter-routing-key", "test.dlq.name",
            "x-max-length", "100"};
    private static String[] deadLetterQueueConf1 = {
            "name", "test.dlq1.uri",
            "queueName", "test.dlq1.name",
            "exchangeName", "test.dle",
            "destinationType", "QUEUE",
            "gzip", "false",
            "messageGrouping", "false",
            "x-message-ttl", "1000",
            "x-dead-letter-exchange", "test.exchange",
            "x-dead-letter-routing-key", "test.queue.name",
            "x-max-length", "200"};
    private static String[] deadLetterQueueConf2 = {
            "name", "test.dlq2.uri",
            "queueName", "test.dlq2.name",
            "exchangeName", "test.dle",
            "destinationType", "QUEUE",
            "gzip", "false",
            "messageGrouping", "false",
            "x-message-ttl", "1000",
            "x-dead-letter-exchange", "test.exchange",
            "x-dead-letter-routing-key", "test.queue.name",
            "x-max-length", "500"};
    private static RabbitMQMessagingProviderConfiguration rabbitMQMessagingProviderConfiguration =
            new RabbitMQMessagingProviderConfiguration("rabbitmq.host",
                    "rabbitmq.vhost",
                    1337,
                    "rabbitmq.username",
                    "rabbitmq.password",
                    "rabbitmq.management.host",
                    1337,
                    "rabbitmq.management.username",
                    "rabbitmq.management.password",
                    "rabbitmq.management.path",
                    false);
    private static RabbitMQQueueConfiguration rabbitMQQueueConfiguration =
            ResourceConfiguration.asSpecificConfiguration(RabbitMQQueueConfiguration.class, queueConf);
    private static InputQueueConfiguration inputQueueConfiguration =
            new InputQueueConfiguration("test.queue.uri", 1, false, Arrays.asList("test.dlq1.uri", "test.dlq2.uri"));
    private static InputQueueDescriptor inputQueueDescriptor =
            new InputQueueDescriptor("test.queue.uri", "description", null, null);
    private static ManagedInputQueue managedInputQueue = new ManagedInputQueueImpl(inputQueueDescriptor,
            inputQueueConfiguration,
            Collections.<QueueReceiver>emptyList());
    private static Map<String, RabbitMQQueueConfiguration> deadLetterQueueConfigurations;

    static {
        deadLetterQueueConfigurations = new HashMap<>(2);
        deadLetterQueueConfigurations.put(
                "test.dlq1.uri",
                ResourceConfiguration.asSpecificConfiguration(RabbitMQQueueConfiguration.class, deadLetterQueueConf1));
        deadLetterQueueConfigurations.put(
                "test.dlq2.uri",
                ResourceConfiguration.asSpecificConfiguration(RabbitMQQueueConfiguration.class, deadLetterQueueConf2));
    }

    private static Context context = new ContextStub();

    @Test
    public void testNonRetryableNullPointerException() throws IOException {

        final MessageListener messageListener = new MessageListener() {
            @Override
            public void onMessage(Message message, Context context) {
                throw new NullPointerException();
            }

            @Override
            public void onErrorMessage(Message message, Context context) {
            }
        };

        ManagedMessageListener managedMessageListener =
                spy(new ManagedMessageListenerImpl(context, messageListener, managedInputQueue, 0));
        RabbitMQMessageReceiver receiver = new RabbitMQMessageReceiver(rabbitMQMessagingProviderConfiguration,
                rabbitMQQueueConfiguration,
                deadLetterQueueConfigurations,
                inputQueueConfiguration,
                managedMessageListener,
                context,
                "consumer1");

        Channel channel = Mockito.mock(Channel.class);
        QueueingConsumer queueingConsumer = new QueueingConsumer(channel);
        queueingConsumer.handleDelivery(
                "consumer.tag",
                new Envelope(0, false, "test.exchange", "test.queue.name"),
                new AMQP.BasicProperties(),
                new byte[0]);

        BlobStoreAPI blobStoreAPI = Mockito.mock(BlobStoreAPI.class);

        receiver.handleDelivery(
                queueingConsumer,
                channel,
                blobStoreAPI,
                log,
                managedMessageListener,
                context,
                null,
                false,
                false,
                inputQueueConfiguration.getDeadLetterQueues(),
                deadLetterQueueConfigurations);
        verify(channel).basicNack(0, false, false);

        ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
        verify(managedMessageListener).onErrorMessage(messageArgumentCaptor.capture(), eq(context));

        Message message = messageArgumentCaptor.getValue();
        Assert.assertEquals(
                "Expected failed message",
                "true",
                message.getMessageHeader(MessagingConstants.FAILED_HEADER));
        Assert.assertNotNull("Expected error message", message.getMessageHeader(MessagingConstants.ERROR_HEADER));
    }

    @Test
    public void testRequeue() throws IOException {

        final MessageListener messageListener = new MessageListener() {
            @Override
            public void onMessage(Message message, Context context) {
                throw new IllegalStateException();
            }

            @Override
            public void onErrorMessage(Message message, Context context) {
            }
        };

        ManagedMessageListener managedMessageListener =
                spy(new ManagedMessageListenerImpl(context, messageListener, managedInputQueue, 0));
        RabbitMQMessageReceiver receiver = new RabbitMQMessageReceiver(rabbitMQMessagingProviderConfiguration,
                rabbitMQQueueConfiguration,
                deadLetterQueueConfigurations,
                inputQueueConfiguration,
                managedMessageListener,
                context,
                "consumer1");

        Channel channel = Mockito.mock(Channel.class);
        QueueingConsumer queueingConsumer = new QueueingConsumer(channel);
        queueingConsumer.handleDelivery(
                "consumer.tag",
                new Envelope(0, false, "test.exchange", "test.queue.name"),
                new AMQP.BasicProperties(),
                new byte[0]);

        BlobStoreAPI blobStoreAPI = Mockito.mock(BlobStoreAPI.class);

        receiver.handleDelivery(
                queueingConsumer,
                channel,
                blobStoreAPI,
                log,
                managedMessageListener,
                context,
                null,
                false,
                false,
                inputQueueConfiguration.getDeadLetterQueues(),
                deadLetterQueueConfigurations);
        verify(channel).basicNack(0, false, true);
    }

    @Test
    public void testNonRetryableNoDeadLetterQueuesInServiceConfig() throws IOException {

        final MessageListener messageListener = new MessageListener() {
            @Override
            public void onMessage(Message message, Context context) {
                throw new IllegalStateException();
            }

            @Override
            public void onErrorMessage(Message message, Context context) {
            }
        };

        // Overwrite inputQueueConfiguration
        InputQueueConfiguration inputQueueConfiguration = new InputQueueConfiguration("test.queue.uri", 1, false, null);

        ManagedMessageListener managedMessageListener =
                spy(new ManagedMessageListenerImpl(context, messageListener, managedInputQueue, 0));
        RabbitMQMessageReceiver receiver = new RabbitMQMessageReceiver(rabbitMQMessagingProviderConfiguration,
                rabbitMQQueueConfiguration,
                deadLetterQueueConfigurations,
                inputQueueConfiguration,
                managedMessageListener,
                context,
                "consumer1");

        Channel channel = Mockito.mock(Channel.class);
        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties();
        QueueingConsumer queueingConsumer = new QueueingConsumer(channel);
        queueingConsumer.handleDelivery(
                "consumer.tag",
                new Envelope(0, true, "test.exchange", "test.queue.name"),
                basicProperties,
                new byte[0]);

        BlobStoreAPI blobStoreAPI = Mockito.mock(BlobStoreAPI.class);

        receiver.handleDelivery(
                queueingConsumer,
                channel,
                blobStoreAPI,
                log,
                managedMessageListener,
                context,
                null,
                false,
                false,
                inputQueueConfiguration.getDeadLetterQueues(),
                deadLetterQueueConfigurations);
        verify(channel).basicNack(0, false, false);

        ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
        verify(managedMessageListener).onErrorMessage(messageArgumentCaptor.capture(), eq(context));

        Message message = messageArgumentCaptor.getValue();
        Assert.assertEquals(
                "Expected failed message",
                "true",
                message.getMessageHeader(MessagingConstants.FAILED_HEADER));
        Assert.assertNotNull("Expected error message", message.getMessageHeader(MessagingConstants.ERROR_HEADER));
    }

    @Test
    public void testFirstRetry() throws IOException {

        final MessageListener messageListener = new MessageListener() {
            @Override
            public void onMessage(Message message, Context context) {
                throw new IllegalStateException();
            }

            @Override
            public void onErrorMessage(Message message, Context context) {
            }
        };

        ManagedMessageListener managedMessageListener =
                spy(new ManagedMessageListenerImpl(context, messageListener, managedInputQueue, 0));
        RabbitMQMessageReceiver receiver = new RabbitMQMessageReceiver(rabbitMQMessagingProviderConfiguration,
                rabbitMQQueueConfiguration,
                deadLetterQueueConfigurations,
                inputQueueConfiguration,
                managedMessageListener,
                context,
                "consumer1");

        Channel channel = Mockito.mock(Channel.class);
        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties();
        QueueingConsumer queueingConsumer = new QueueingConsumer(channel);
        queueingConsumer.handleDelivery(
                "consumer.tag",
                new Envelope(0, true, "test.exchange", "test.queue.name"),
                basicProperties,
                new byte[0]);

        BlobStoreAPI blobStoreAPI = Mockito.mock(BlobStoreAPI.class);

        receiver.handleDelivery(
                queueingConsumer,
                channel,
                blobStoreAPI,
                log,
                managedMessageListener,
                context,
                null,
                false,
                false,
                inputQueueConfiguration.getDeadLetterQueues(),
                deadLetterQueueConfigurations);
        ArgumentCaptor<AMQP.BasicProperties> propertiesCaptor = ArgumentCaptor.forClass(AMQP.BasicProperties.class);
        verify(channel).basicPublish(eq("test.dle"), eq("test.dlq1.name"), propertiesCaptor.capture(), eq(new byte[0]));

        // Validate ERROR header
        AMQP.BasicProperties actualProperties = propertiesCaptor.getValue();
        Assert.assertTrue(
                "Expected ERROR header not found",
                actualProperties.getHeaders().containsKey(MessagingConstants.ERROR_HEADER));

        verify(channel).basicAck(0, false);
        verify(managedMessageListener, Mockito.never()).onErrorMessage(any(Message.class), eq(context));
    }

    @Test
    public void testSecondRetry() throws IOException {

        final MessageListener messageListener = new MessageListener() {
            @Override
            public void onMessage(Message message, Context context) {
                throw new IllegalStateException();
            }

            @Override
            public void onErrorMessage(Message message, Context context) {
                throw new IllegalStateException();
            }
        };

        ManagedMessageListener managedMessageListener =
                spy(new ManagedMessageListenerImpl(context, messageListener, managedInputQueue, 0));
        RabbitMQMessageReceiver receiver = new RabbitMQMessageReceiver(rabbitMQMessagingProviderConfiguration,
                rabbitMQQueueConfiguration,
                deadLetterQueueConfigurations,
                inputQueueConfiguration,
                managedMessageListener,
                context,
                "consumer1");

        Channel channel = Mockito.mock(Channel.class);
        AMQP.BasicProperties.Builder basicPropertiesBuilder = new AMQP.BasicProperties.Builder();

        Map<String, Object> xDeath = new HashMap<>();
        xDeath.put("reason", "expired");
        xDeath.put("count", 1L);
        xDeath.put("exchange", "test.dle");
        xDeath.put("time", new Date(System.currentTimeMillis()));
        xDeath.put("routing-keys", Arrays.asList("test.dlq1.name"));
        xDeath.put("queue", "test.dlq1.name");

        List<Object> xDeaths = new ArrayList<>();
        xDeaths.add(xDeath);

        Map<String, Object> headers = new HashMap<>();
        headers.put("x-death", xDeaths);

        basicPropertiesBuilder.headers(headers);
        AMQP.BasicProperties basicProperties = basicPropertiesBuilder.build();

        QueueingConsumer queueingConsumer = new QueueingConsumer(channel);
        queueingConsumer.handleDelivery(
                "consumer.tag",
                new Envelope(0, true, "test.exchange", "test.queue.name"),
                basicProperties,
                new byte[0]);

        BlobStoreAPI blobStoreAPI = Mockito.mock(BlobStoreAPI.class);

        receiver.handleDelivery(
                queueingConsumer,
                channel,
                blobStoreAPI,
                log,
                managedMessageListener,
                context,
                null,
                false,
                false,
                inputQueueConfiguration.getDeadLetterQueues(),
                deadLetterQueueConfigurations);

        ArgumentCaptor<AMQP.BasicProperties> propertiesCaptor = ArgumentCaptor.forClass(AMQP.BasicProperties.class);
        verify(channel).basicPublish(eq("test.dle"), eq("test.dlq2.name"), propertiesCaptor.capture(), eq(new byte[0]));

        // Validate ERROR header
        AMQP.BasicProperties actualProperties = propertiesCaptor.getValue();
        Assert.assertTrue(
                "Expected ERROR header not found",
                actualProperties.getHeaders().containsKey(MessagingConstants.ERROR_HEADER));

        verify(channel).basicAck(0, false);
        verify(managedMessageListener, Mockito.never()).onErrorMessage(any(Message.class), eq(context));
    }

    @Test
    public void testFinalRetry() throws IOException {

        final MessageListener messageListener = new MessageListener() {
            @Override
            public void onMessage(Message message, Context context) {
                throw new IllegalStateException();
            }

            @Override
            public void onErrorMessage(Message message, Context context) {
            }
        };

        ManagedMessageListener managedMessageListener =
                spy(new ManagedMessageListenerImpl(context, messageListener, managedInputQueue, 0));
        RabbitMQMessageReceiver receiver = new RabbitMQMessageReceiver(rabbitMQMessagingProviderConfiguration,
                rabbitMQQueueConfiguration,
                deadLetterQueueConfigurations,
                inputQueueConfiguration,
                managedMessageListener,
                context,
                "consumer1");

        Channel channel = Mockito.mock(Channel.class);
        AMQP.BasicProperties.Builder basicPropertiesBuilder = new AMQP.BasicProperties.Builder();

        List<Object> xDeaths = new ArrayList<>();

        // *** RabbitMQ sorts xDeaths most recent first ***
        Map<String, Object> xDeath = new HashMap<>();
        xDeath.put("reason", "expired");
        xDeath.put("count", 1L);
        xDeath.put("exchange", "test.dle");
        xDeath.put("time", new Date(System.currentTimeMillis()));
        xDeath.put("routing-keys", Arrays.asList("test.dlq2.name"));
        xDeath.put("queue", "test.dlq2.name");
        xDeaths.add(xDeath);

        xDeath = new HashMap<>();
        xDeath.put("reason", "expired");
        xDeath.put("count", 1L);
        xDeath.put("exchange", "test.dle");
        xDeath.put("time", new Date(System.currentTimeMillis()));
        xDeath.put("routing-keys", Arrays.asList("test.dlq1.name"));
        xDeath.put("queue", "test.dlq1.name");
        xDeaths.add(xDeath);

        Map<String, Object> headers = new HashMap<>();
        headers.put("x-death", xDeaths);

        basicPropertiesBuilder.headers(headers);
        AMQP.BasicProperties basicProperties = basicPropertiesBuilder.build();

        QueueingConsumer queueingConsumer = new QueueingConsumer(channel);
        queueingConsumer.handleDelivery(
                "consumer.tag",
                new Envelope(0, true, "test.exchange", "test.queue.name"),
                basicProperties,
                new byte[0]);

        BlobStoreAPI blobStoreAPI = Mockito.mock(BlobStoreAPI.class);

        receiver.handleDelivery(
                queueingConsumer,
                channel,
                blobStoreAPI,
                log,
                managedMessageListener,
                context,
                null,
                false,
                false,
                inputQueueConfiguration.getDeadLetterQueues(),
                deadLetterQueueConfigurations);
        verify(channel).basicNack(0, false, false);

        ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
        verify(managedMessageListener).onErrorMessage(messageArgumentCaptor.capture(), eq(context));

        Message message = messageArgumentCaptor.getValue();
        Assert.assertEquals(
                "Expected failed message",
                "true",
                message.getMessageHeader(MessagingConstants.FAILED_HEADER));
        Assert.assertNotNull("Expected error message", message.getMessageHeader(MessagingConstants.ERROR_HEADER));
    }

    private static class ContextStub implements Context {

        @Override
        public ParametersBag getParametersBag() {
            return null;
        }

        @Override
        public MetricsRegistry getMetricsRegistry() {
            return new MetricsRegistryImpl("test.registry");
        }

        @Override
        public ServiceDiscoveryManager getServiceDiscoveryManager() {
            return null;
        }

        @Override
        public WebAPIResolver getWebAPIResolver() {
            return null;
        }

        @Override
        public ResourceManager<DestinationDescriptor, DestinationConfiguration, ManagedMessageDestination> getDestinationManager() {
            return null;
        }

        @Override
        public ResourceManager<DatasourceDescriptor, DatasourceConfiguration, ManagedDatasource> getDatasourceManager() {
            return null;
        }

        @Override
        public ResourceManager<ServiceDependencyDescriptor, ServiceDependencyConfiguration, ManagedDependency> getDependencyManager() {
            return null;
        }

        @Override
        public ResourceManager<BlobStoreDescriptor, BlobStoreConfiguration, ManagedBlobStore> getBlobStoreManager() {
            return null;
        }

        @Override
        public ResourceManager<SingletonDescriptor, SingletonConfiguration, ManagedSingleton> getSingletonManager() {
            return null;
        }

        @Override
        public ResourceManager<DynamicJavaServiceDescriptor, DynamicJavaServiceConfiguration, ManagedDynamicJavaService> getDynamicJavaServicesManager() {
            return null;
        }

        @Override
        public ResourceManager<SchedulerDescriptor, SchedulerConfiguration, ManagedScheduler> getSchedulerManager() {
            return null;
        }

        @Override
        public HealthCheckManager getHealthCheckManager() {
            return Mockito.mock(HealthCheckManager.class);
        }

        @Override
        public SerializationManager getSerializationManager() {
            return null;
        }

        @Override
        public MicroServiceWebServer getWebServer() {
            return null;
        }

        @Override
        public InputDescriptor getInputDescriptor() {
            return null;
        }

        @Override
        public OutputDescriptor getOutputDescriptor() {
            return null;
        }

        @Override
        public Logger getLogger() {
            return log;
        }

        @Override
        public Logger createSubLogger(Class loggerClass) {
            return log;
        }

        @Override
        public MessageSender getOutputMessageSender(Message inputMessage) {
            return null;
        }

        @Override
        public boolean isOutputMsgSenderDefined(Message inputMessage) {
            return false;
        }

        @Override
        public MessageSender getOutputMessageSender(Map<String, String> messageContext) {
            return null;
        }

        @Override
        public String getMicroServiceName() {
            return "microservice-name";
        }

        @Override
        public String getMicroServiceBaseURI() {
            return "microservice-uri";
        }

        @Override
        public <D extends ResourceDescriptor, R extends ManagedResource<D, ?>> R getManagedResourceByDescriptor(
                Class<D> descriptorClass,
                String name) {
            return null;
        }

        @Override
        public <D extends ResourceDescriptor> boolean isSupportingResource(Class<D> resourceDescriptorClass) {
            return false;
        }

        @Override
        public LocalCacheManager getLocalCacheManager() {
            return null;
        }

        @Override
        public MicroServiceState getServiceState() {
            return null;
        }

        @Override
        public ResourceManager<InputQueueDescriptor, InputQueueConfiguration, ManagedInputQueue> getQueuesManager() {
            return null;
        }

        @Override
        public List<ResourceManager> getExternalResourceManagers() {
            return null;
        }

        @Override
        public String getServiceDescription() {
            return null;
        }

        @Override
        public RestResourceManager getRestResourceManager() {
            return null;
        }

        @Override
        public MicroService getServiceDescriptor() {
            return null;
        }

    }

}
