// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2015 - 2016 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.testing;

import com.emc.microservice.Context;
import com.emc.microservice.ContextImpl;
import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceController;
import com.emc.microservice.MicroServiceState;
import com.emc.microservice.MicroserviceIdentifier;
import com.emc.microservice.bootstrap.SchemaBootstrap;
import com.emc.microservice.bootstrap.SchemaBootstrapRunner;
import com.emc.microservice.input.InputDescriptor;
import com.emc.microservice.messaging.DestinationConfiguration;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.messaging.MessageListener;
import com.emc.microservice.messaging.MessageSender;
import com.emc.microservice.messaging.MessageSenderImpl;
import com.emc.microservice.messaging.MessagingConstants;
import com.emc.microservice.messaging.MessagingProviderConfiguration;
import com.emc.microservice.messaging.MessagingSerializationHelper;
import com.emc.microservice.messaging.QueueConfiguration;
import com.emc.microservice.messaging.RuntimeMessageSender;
import com.emc.microservice.messaging.error.ErrorsMessageHeader;
import com.emc.microservice.messaging.error.serialization.ErrorMessageHeaderSerializationException;
import com.emc.microservice.messaging.error.serialization.ErrorsMessageHeaderWriter;
import com.emc.microservice.output.OutputDescriptor;
import com.emc.microservice.output.ServiceOutputDescriptor;
import com.emc.microservice.resource.ResourceConfiguration;
import com.emc.microservice.restapi.ManagedResourceDescriptor;
import com.emc.microservice.runner.MicroServiceRunner;
import com.emc.microservice.schedule.ScheduleListener;
import com.emc.microservice.serialization.SerializationReader;
import com.emc.microservice.serialization.SerializationWriter;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.util.MapBuilder;
import com.emc.ocopea.util.database.BasicNativeQueryService;
import com.emc.ocopea.util.database.NativeQueryService;
import com.zaxxer.hikari.HikariDataSource;
import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.junit.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MicroServiceTestHelper {

    private final MicroService microService;
    private MicroServiceController serviceController;
    private final DataSource dataSource;
    private final NativeQueryService nativeQueryService;
    private MockTestingResourceProvider resourceProvider = null;
    private SecurityContext securityContext = null;

    public MicroServiceTestHelper(MicroService microService) {
        this(microService, microService.getClass().getSimpleName() + "-TestDB-" + System.nanoTime());
    }

    public MicroServiceTestHelper(MicroService microService, String dataSourceName) {
        this(microService, getDataSourceFromMockConfig(dataSourceName));
    }

    public MicroServiceTestHelper(MicroService microService, DataSource dataSource) {
        this.microService = microService;
        this.dataSource = dataSource;
        this.nativeQueryService = new BasicNativeQueryService(this.dataSource);
        this.resourceProvider = new MockTestingResourceProvider(this.dataSource);
    }

    public MicroServiceTestHelper withSchedulerListenerMapping(
            String listenerIdentifier,
            Class<? extends ScheduleListener> listener) {
        resourceProvider.addSchedulerListenerMapping(listenerIdentifier, listener);
        return this;
    }

    /**
     * @deprecated Please use alternate constructors moving forward, which are closer to production (they use resource
     *             providers as opposed to creating a NativeQueryService impl first)
     */
    public MicroServiceTestHelper(
            MicroService microService,
            MicroServiceTestNativeQueryServiceImpl nativeQueryService) {
        if (nativeQueryService == null) {
            MicroServiceTestNativeQueryServiceImpl microServiceTestNativeQueryService =
                    new MicroServiceTestNativeQueryServiceImpl(
                            microService.getClass().getSimpleName() + "-TestDB-" + System.currentTimeMillis());
            this.nativeQueryService = microServiceTestNativeQueryService;
            this.dataSource = microServiceTestNativeQueryService.getDataSource();
        } else {
            this.nativeQueryService = nativeQueryService;
            this.dataSource = nativeQueryService.getDataSource();
        }
        this.microService = microService;
        this.resourceProvider = new MockTestingResourceProvider(dataSource);
    }

    private static DataSource getDataSourceFromMockConfig(String dataSourceName) {
        MockTestingResourceProvider.MockDatasourceConfiguration mockDatasourceConfiguration =
                new MockTestingResourceProvider.MockDatasourceConfiguration(dataSourceName);
        return MicroServiceTestDataSourceProvider.getDataSource(mockDatasourceConfiguration);
    }

    public Context getContext() {
        return serviceController.getContext();
    }

    public void createOrUpgrdaeSchema(SchemaBootstrap schemaBootstrap) throws IOException, SQLException {
        SchemaBootstrapRunner.runBootstrap(
                dataSource,
                schemaBootstrap,
                MockTestingResourceProvider.makeSchemaSafe(schemaBootstrap.getSchemaName()),
                "some_role");
    }

    public void setSecurityContext(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    /**
     * Start the service with the testing resource provider
     */
    public void startServiceInTestMode() {
        Map<String, MicroServiceController> serviceControllers =
                new MicroServiceRunner().run(resourceProvider, microService);
        serviceController = serviceControllers.values().iterator().next();

        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hds = ((HikariDataSource) dataSource);
            if (hds.getMetricRegistry() == null) {
                hds.setMetricRegistry(serviceController.getMetricRegistry().getRegistry());
            }
        }
    }

    private static MessageSender constructMessageSender(
            RuntimeMessageSender runtimeMessageSender, MockTestingResourceProvider resourceProvider,
            Context context) {
        return new MessageSenderImpl(
                runtimeMessageSender,
                context.getSerializationManager(), resourceProvider, context);
    }

    @NoJavadoc
    public static void sendMessageToQueue(
            String queueName, Map<String, String> messageHeaders, MockTestingResourceProvider resourceProvider,
            final InputStream messageInputStream, Map<String, String> messageContext, Context context) {
        MessageSender messageSender =
                constructMessageSender(
                        resourceProvider.getMessageSender(resourceProvider.getServiceRegistryApi()
                                        .getMessagingProviderConfiguration(
                                                resourceProvider.getMessagingConfigurationClass(),
                                                MessagingProviderConfiguration.DEFAULT_MESSAGING_SYSTEM_NAME),
                                new DestinationConfiguration(queueName, null, null, true),
                                resourceProvider.getServiceRegistryApi().getQueueConfiguration(
                                        resourceProvider.getQueueConfigurationClass(), queueName, context), context),
                        resourceProvider, context);

        messageSender.streamMessage(
                MessagingSerializationHelper.getInputStreamMessageWriter(messageInputStream),
                messageHeaders,
                messageContext);
    }

    @NoJavadoc
    public void sendMessageToQueue(
            String queueName,
            Map<String, String> messageHeaders,
            final InputStream messageInputStream) {
        sendMessageToQueue(
                queueName,
                messageHeaders,
                resourceProvider,
                messageInputStream,
                Collections.<String, String>emptyMap(),
                getContext());
    }

    @NoJavadoc
    public <T> void sendMessageToQueue(
            String queueName,
            Map<String, String> messageHeaders,
            T objectToSend,
            Class<T> format) {
        sendMessageToQueue(
                queueName,
                messageHeaders,
                resourceProvider,
                objectToSend,
                format,
                Collections.<String, String>emptyMap(),
                getContext());
    }

    @NoJavadoc
    public static <T> void sendMessageToQueue(
            String queueName, Map<String, String> messageHeaders, MockTestingResourceProvider resourceProvider,
            T objectToSend, Class<T> format, Map<String, String> messageContext, Context context) {
        MockTestingResourceProvider.MockMessagingProviderConfiguration messagingProviderConfiguration =
                resourceProvider.getServiceRegistryApi()
                        .getMessagingProviderConfiguration(
                                resourceProvider.getMessagingConfigurationClass(),
                                MessagingProviderConfiguration.DEFAULT_MESSAGING_SYSTEM_NAME);
        MessageSender messageSender =
                constructMessageSender(
                        resourceProvider.getMessageSender(
                                messagingProviderConfiguration,
                                new DestinationConfiguration(queueName, null, null, true),
                                resourceProvider
                                        .getServiceRegistryApi()
                                        .getQueueConfiguration(
                                                resourceProvider.getQueueConfigurationClass(),
                                                queueName,
                                                context),
                                context),
                        resourceProvider, context);

        messageSender.sendMessage(format, objectToSend, messageHeaders, messageContext);
    }

    @NoJavadoc
    public static <T> void sendMessageToErrorQueue(
            String queueName,
            ErrorsMessageHeader errorsMessageHeader,
            Map<String, String> messageHeaders,
            MockTestingResourceProvider resourceProvider,
            T objectToSend,
            Class<T> format,
            Map<String, String> messageContext,
            Context context) {
        MessageSender messageSender =
                constructMessageSender(
                        resourceProvider.getMessageSender(resourceProvider.getServiceRegistryApi()
                                        .getMessagingProviderConfiguration(
                                                resourceProvider.getMessagingConfigurationClass(),
                                                MessagingProviderConfiguration.DEFAULT_MESSAGING_SYSTEM_NAME),
                                new DestinationConfiguration(queueName, null, null, true),
                                resourceProvider.getServiceRegistryApi().getQueueConfiguration(
                                        resourceProvider.getQueueConfigurationClass(), queueName, context), context),
                        resourceProvider, context);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            new ErrorsMessageHeaderWriter().writeObject(errorsMessageHeader, outputStream);
        } catch (ErrorMessageHeaderSerializationException e) {
            e.printStackTrace();
        }
        messageHeaders.put(MessagingConstants.ERROR_HEADER, outputStream.toString());

        messageSender.sendMessage(format, objectToSend, messageHeaders, messageContext);
    }

    public void stopTestMode() {
        if (serviceController != null) {
            serviceController.stop();
        }
    }

    public int getDestinationTriggerCount(String destinationName) {
        return Objects.requireNonNull(
                resourceProvider.getDestinationActivationCount().get(destinationName),
                "Undefined destination: " + destinationName);
    }

    @NoJavadoc
    public <T> T getServiceResource(Class<T> resourceClass) {
        ManagedResourceDescriptor managedResourceDescriptor =
                serviceController.getContext().getRestResourceManager().getResourceDescriptorMap()
                        .get(resourceClass);
        if (managedResourceDescriptor == null) {
            throw new IllegalArgumentException("Invalid Service resource " + resourceClass.getCanonicalName());
        }

        // Instantiate the resource and provide application context
        T t = null;
        try {
            t = injectContextVariables(resourceClass.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            Assert.fail("Rest Resource " + resourceClass.getSimpleName() + " Must have a public default constructor");
        }

        try {
            Method setApplicationMethod = resourceClass.getMethod("setApplication", Application.class);
            setApplicationMethod.invoke(t, new MicroServiceTestingRestApplication(serviceController.getContext()));
        } catch (NoSuchMethodException e) {
            // In case we don't have a setApplication setter, simply skipping this...
            System.out.println("Resource " + resourceClass.getCanonicalName() + " does not implement setApplication");
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            Assert.fail("Failed Invoking resource " + resourceClass.getCanonicalName() + " setApplication Method");
        }
        return Objects.requireNonNull(t);
    }

    private <T> T injectContextVariables(T resource) {
        MapBuilder<Class, Object> injectionsMapBuilder = MapBuilder.<Class, Object>newHashMap()
                .with(HttpServletRequest.class, new MockHttpServletRequest())
                .with(UriInfo.class, new ResteasyUriInfo(
                        URI.create("http://localhost:8080/" + microService.getIdentifier().getRestURI())));
        if (securityContext != null) {
            injectionsMapBuilder.with(SecurityContext.class, securityContext);
        }
        injectFields(resource,
                injectionsMapBuilder
                        .build());
        return resource;

    }

    private <T> void injectFields(T resource, Map<Class, Object> valuesToInject) {
        for (Field field : getAllDeclaredFields(resource.getClass()).values()) {
            if (field.getAnnotation(javax.ws.rs.core.Context.class) != null) {
                Optional<Object> injectedValue = valuesToInject.entrySet().stream()
                        .filter(pair -> field.getType().equals(pair.getKey()))
                        .map(Map.Entry::getValue)
                        .findFirst();
                if (injectedValue.isPresent()) {
                    try {
                        field.setAccessible(true);
                        field.set(resource, injectedValue.get());
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("failure in injection for field " + field.getName()
                                + " in " + resource.getClass().getSimpleName(), e);
                    }
                } else {
                    System.out.print("WARNING - missing injection for " + field.getName()
                            + " in " + resource.getClass().getSimpleName());
                }
            }
        }
    }

    private Map<String, Field> getAllDeclaredFields(Class clazz) {
        if (clazz == null) {
            return new HashMap<>();
        }
        Map<String, Field> superFields = getAllDeclaredFields(clazz.getSuperclass());
        for (Field field: clazz.getDeclaredFields()) {
            superFields.put(field.getName(), field);
        }
        return superFields;
    }

    @NoJavadoc
    public <T> void executeService(
            T objectToSend,
            Class<T> format,
            Map<String, String> messageHeaders,
            MessageListener messageListener) throws IOException {
        executeService(
                this.microService,
                this.resourceProvider,
                messageHeaders,
                Collections.<String, String>emptyMap(),
                messageListener,
                null,
                format,
                objectToSend,
                getContext());
    }

    public void executeService(InputStream messageInputStream, Map<String, String> messageProperties)
            throws IOException {
        executeService(messageInputStream, messageProperties, null);
    }

    @NoJavadoc
    public static <T> void executeService(
            MicroService microService, MockTestingResourceProvider resourceProvider, T objectToSend, Class<T> format,
            Map<String, String> messageHeaders, Context context) throws IOException {
        executeService(
                microService,
                resourceProvider,
                messageHeaders,
                Collections.<String, String>emptyMap(),
                null,
                null,
                format,
                objectToSend,
                context);
    }

    @NoJavadoc
    public <T> void executeService(T objectToSend, Class<T> format, Map<String, String> messageHeaders)
            throws IOException {
        executeService(
                this.microService,
                this.resourceProvider,
                messageHeaders,
                Collections.<String, String>emptyMap(),
                null,
                null,
                format,
                objectToSend,
                getContext());
    }

    @NoJavadoc
    public <T> void executeService(
            T objectToSend,
            Class<T> format,
            Map<String, String> messageHeaders,
            Map<String, String> messageContext) throws IOException {
        executeService(
                this.microService,
                this.resourceProvider,
                messageHeaders,
                messageContext,
                null,
                null,
                format,
                objectToSend,
                getContext());
    }

    @NoJavadoc
    public int executeService(
            InputStream messageInputStream,
            Map<String, String> messageProperties,
            MessageListener messageListener) {
        return executeService(
                this.microService,
                this.resourceProvider,
                messageProperties,
                messageListener,
                messageInputStream,
                getContext());
    }

    @NoJavadoc
    public static int executeService(
            MicroService microService, MockTestingResourceProvider resourceProvider, Map<String, String> messageHeaders,
            MessageListener messageListener, InputStream messageInputStream, Context context) {
        return executeService(
                microService,
                resourceProvider,
                messageHeaders,
                Collections.<String, String>emptyMap(),
                messageListener,
                messageInputStream,
                null,
                null,
                context);
    }

    @NoJavadoc
    private static int executeService(
            MicroService microService,
            MockTestingResourceProvider resourceProvider,
            Map<String, String> messageHeaders,
            Map<String, String> messageContext,
            MessageListener messageListener,
            InputStream messageInputStream,
            Class format,
            Object objectToSend,
            Context context) {
        CountExecutionsMessageListener countExecutionsMessageListener = null;

        // Verifying service has public input
        InputDescriptor inputDescriptor = microService.getInitializationHelper().getInputDescriptor();
        if (inputDescriptor == null) {
            throw new IllegalStateException("for using this helper method, service must declare public input");
        }

        Map<String, String> messageContextToSend;
        if (messageContext == null || messageContext.isEmpty()) {
            messageContextToSend = new HashMap<>();
        } else {
            messageContextToSend = new HashMap<>(messageContext);
        }
        if (messageHeaders == null) {
            messageHeaders = new HashMap<>();
        }

        // If we expect a result, we should populate the result queue configuration
        String outputQueueName = null;
        if (messageListener != null) {

            switch (microService.getInitializationHelper().getOutputDescriptor().getOutputType()) {
                case service:
                    outputQueueName = new MicroserviceIdentifier(
                            ((ServiceOutputDescriptor) microService
                                    .getInitializationHelper()
                                    .getOutputDescriptor()).getServiceURI())
                            .getDefaultInputQueueName();
                    break;
                case messaging:
                    outputQueueName = MockTestingResourceProvider.TESTING_OUTPUT_QUEUE_NAME;
                    Map<String, String> routingContextProps = getStringStringMap(outputQueueName);

                    messageContextToSend.put(
                            ContextImpl.MS_API_OUTPUT_QUEUE_NAME_HEADER + "_" + context.getMicroServiceBaseURI(),
                            ResourceConfiguration.propsToPersistentFormat(routingContextProps));
                    break;
                default:
                    throw new IllegalStateException("Unsupported output type " +
                            microService.getInitializationHelper().getOutputDescriptor().getOutputType());
            }

            countExecutionsMessageListener = new CountExecutionsMessageListener(messageListener);
            resourceProvider.registerTestMessageListener(
                    outputQueueName,
                    countExecutionsMessageListener,
                    context.getMicroServiceBaseURI());
        } else {

            // In case we don't care about output, we send something so that we won't get an exception :)
            if (microService.getInitializationHelper().getOutputDescriptor() != null
                    && microService.getInitializationHelper().getOutputDescriptor().getOutputType() ==
                    OutputDescriptor.MicroServiceOutputType.messaging) {
                Map<String, String> routingContextProps =
                        getStringStringMap(MockTestingResourceProvider.TESTING_OUTPUT_QUEUE_NAME);
                messageContextToSend.put(
                        ContextImpl.MS_API_OUTPUT_QUEUE_NAME_HEADER + "_" + context.getMicroServiceBaseURI(),
                        ResourceConfiguration.propsToPersistentFormat(routingContextProps));
            }
        }

        // Sending the message to the service
        switch (inputDescriptor.getInputType()) {
            case messaging:
                if (objectToSend == null) {
                    sendMessageToQueue(
                            microService.getIdentifier().getDefaultInputQueueName(),
                            messageHeaders,
                            resourceProvider,
                            messageInputStream,
                            messageContextToSend,
                            context);
                } else {
                    sendMessageToQueue(
                            microService.getIdentifier().getDefaultInputQueueName(),
                            messageHeaders,
                            resourceProvider,
                            objectToSend,
                            format,
                            messageContextToSend,
                            context);
                }
                break;
            default:
                throw new IllegalStateException(
                        "Not implemented yet service input testing with type " + inputDescriptor.getInputType());
        }

        // If we expect a result, we should verify we received one
        if (countExecutionsMessageListener != null) {
            Assert.assertEquals(
                    "Service did not write an output",
                    1,
                    countExecutionsMessageListener.getExecutionsCount());

            return countExecutionsMessageListener.getExecutionsCount();
        }
        return 0;
    }

    @NoJavadoc
    public static int executeErrorService(
            MicroService microService,
            MockTestingResourceProvider resourceProvider,
            ErrorsMessageHeader errorMessages,
            Map<String, String> messageHeaders,
            Map<String, String> messageContext,
            MessageListener messageListener,
            Class format,
            Object objectToSend,
            Context context) {
        CountExecutionsMessageListener countExecutionsMessageListener = null;

        // Verifying service has public input
        InputDescriptor inputDescriptor = microService.getInitializationHelper().getInputDescriptor();
        if (inputDescriptor == null) {
            throw new IllegalStateException("for using this helper method, service must declare public input");
        }

        Map<String, String> messageContextToSend;
        if (messageContext == null || messageContext.isEmpty()) {
            messageContextToSend = new HashMap<>();
        } else {
            messageContextToSend = new HashMap<>(messageContext);
        }
        if (messageHeaders == null) {
            messageHeaders = new HashMap<>();
        }

        // If we expect a result, we should populate the result queue configuration
        String outputQueueName = null;
        final OutputDescriptor.MicroServiceOutputType type =
                microService.getInitializationHelper().getOutputDescriptor().getOutputType();
        if (messageListener != null) {

            switch (type) {
                case service:
                    outputQueueName = new MicroserviceIdentifier(
                            ((ServiceOutputDescriptor) microService
                                    .getInitializationHelper()
                                    .getOutputDescriptor()).getServiceURI())
                            .getDefaultInputQueueName();
                    break;
                case messaging:
                    outputQueueName = MockTestingResourceProvider.TESTING_OUTPUT_QUEUE_NAME;
                    Map<String, String> routingContextProps = getStringStringMap(outputQueueName);

                    messageContextToSend.put(
                            ContextImpl.MS_API_OUTPUT_QUEUE_NAME_HEADER + "_" + context.getMicroServiceBaseURI(),
                            ResourceConfiguration.propsToPersistentFormat(routingContextProps));
                    break;
                default:
                    throw new IllegalStateException("Unsupported output type " + type);
            }

            countExecutionsMessageListener = new CountExecutionsMessageListener(messageListener);
            resourceProvider.registerTestMessageListener(
                    outputQueueName,
                    countExecutionsMessageListener,
                    context.getMicroServiceBaseURI());
        } else {

            // In case we don't care about output, we send something so that we won't get an exception :)
            if (microService.getInitializationHelper().getOutputDescriptor() != null
                    && type ==
                    OutputDescriptor.MicroServiceOutputType.messaging) {
                Map<String, String> routingContextProps =
                        getStringStringMap(MockTestingResourceProvider.TESTING_OUTPUT_QUEUE_NAME);
                messageContextToSend.put(
                        ContextImpl.MS_API_OUTPUT_QUEUE_NAME_HEADER + "_" + context.getMicroServiceBaseURI(),
                        ResourceConfiguration.propsToPersistentFormat(routingContextProps));
            }
        }

        // Sending the message to the service
        switch (inputDescriptor.getInputType()) {
            case messaging:
                if (errorMessages != null) {
                    sendMessageToErrorQueue(
                            microService.getIdentifier().getDefaultInputQueueName(),
                            errorMessages,
                            messageHeaders,
                            resourceProvider,
                            objectToSend,
                            format,
                            messageContextToSend,
                            context);
                }
                break;
            default:
                throw new IllegalStateException(
                        "Not implemented yet service input testing with type " + inputDescriptor.getInputType());
        }

        // If we expect a result, we should verify we received one
        if (countExecutionsMessageListener != null) {
            Assert.assertEquals(
                    "Service did not write an output",
                    1,
                    countExecutionsMessageListener.getExecutionsCount());

            return countExecutionsMessageListener.getExecutionsCount();
        }
        return 0;
    }

    @NoJavadoc
    public <T, R> R executeServiceAndReturnResult(
            Class<T> format,
            T objectToSend,
            Map<String, String> messageProperties,
            final Class<R> resultClass)
            throws IOException {
        ResultMessageListener<R> messageListener = new ResultMessageListener<>(resultClass);
        executeService(objectToSend, format, messageProperties, messageListener);

        // Result must be here, since we've asserted for result listener to execute..
        // (and services are sync from input to output)
        return messageListener.getResult();
    }

    @NoJavadoc
    public <T> T executeServiceAndReturnResult(
            MicroService microService, MockTestingResourceProvider resourceProvider, InputStream messageInputStream,
            Map<String, String> messageProperties, final Class<T> resultClass) {
        ResultMessageListener<T> messageListener = new ResultMessageListener<>(resultClass);
        executeService(
                microService,
                resourceProvider,
                messageProperties,
                messageListener,
                messageInputStream,
                getContext());

        // Result must be here, since we've asserted for result listener to execute..
        // (and services are sync from input to output)
        return messageListener.getResult();
    }

    @NoJavadoc
    private static Map<String, String> getStringStringMap(String outputQueueName) {
        MockTestingResourceProvider.MockQueueConfiguration mockQueueConfiguration =
                new MockTestingResourceProvider.MockQueueConfiguration(
                        QueueConfiguration.MessageDestinationType.QUEUE);
        Map<String, String> routingContextProps = new HashMap<>();
        routingContextProps.putAll(mockQueueConfiguration.getPropertyValues());
        routingContextProps.putAll(new DestinationConfiguration(outputQueueName, null, null, true).getPropertyValues());
        return routingContextProps;
    }

    public NativeQueryService getNativeQueryService() {
        return nativeQueryService;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public interface ServiceCallbackMock<T> {
        T mockDependentServiceCallback(Message message, Context context);
    }

    public <T> void mockDependentServiceRestResource(String serviceUrn, Class<T> restResourceType, T restResourceImpl) {
        resourceProvider.addMockRestResourceImplementation(serviceUrn, restResourceType, restResourceImpl);
    }

    public <T> void mockDependentServiceRestResourceByUrl(String url, Class<T> restResourceType, T restResourceImpl) {
        resourceProvider.addMockRestResourceImplementationByUrl(url, restResourceType, restResourceImpl);
    }

    @NoJavadoc
    public void mockDependentService(final String dependentServiceURI, final MessageListener messageListener) {
        resourceProvider.registerTestMessageListener(
                new MicroserviceIdentifier(dependentServiceURI).getDefaultInputQueueName(),
                new MessageListener() {
                    @Override
                    public void onMessage(Message message, Context context) {
                        messageListener.onMessage(message, context);
                    }

                    @Override
                    public void onErrorMessage(Message message, Context context) {
                        // TODO
                    }
                },
                getContext().getMicroServiceBaseURI());
    }

    @NoJavadoc
    public <T> void mockDependentService(
            final String dependentServiceURI,
            final Class<T> format,
            final ServiceCallbackMock<T> serviceCallbackMock) {
        resourceProvider.registerTestMessageListener(
                new MicroserviceIdentifier(dependentServiceURI).getDefaultInputQueueName(),
                new MessageListener() {
                    @Override
                    public void onMessage(Message message, Context context) {
                        String queueConfiguration = message.getContextValue(
                                ContextImpl.MS_API_OUTPUT_QUEUE_NAME_HEADER + "_" + dependentServiceURI);
                        //noinspection unchecked
                        MessageSender messageSender =
                                constructMessageSender(
                                        resourceProvider.getMessageSender(resourceProvider.getServiceRegistryApi()
                                                        .getMessagingProviderConfiguration(
                                                                resourceProvider.getMessagingConfigurationClass(),
                                                                MessagingProviderConfiguration
                                                                        .DEFAULT_MESSAGING_SYSTEM_NAME),
                                                ResourceConfiguration.asSpecificConfiguration(
                                                        DestinationConfiguration.class,
                                                        queueConfiguration.split(",")),
                                                ResourceConfiguration.asSpecificConfiguration(
                                                        MockTestingResourceProvider.MockQueueConfiguration.class,
                                                        queueConfiguration.split(",")), context),
                                        resourceProvider, context);

                        T mockOutput = serviceCallbackMock.mockDependentServiceCallback(message, context);
                        messageSender.sendMessage(
                                format,
                                mockOutput,
                                message.getMessageHeaders(),
                                message.getMessageContext());

                    }

                    @Override
                    public void onErrorMessage(Message message, Context context) {
                        // TODO
                    }
                },
                getMicroServiceController().getContext().getMicroServiceBaseURI());
    }

    public <T> void mockDependentServiceRest(final String dependentServiceURI, Class<T> apiClass, T mock) {

    }

    public <T> void mockDependentServiceWithStaticOutput(
            final String dependentServiceURI,
            final T staticObjectToReturn,
            final Class<T> format) {
        mockDependentService(dependentServiceURI, format, (message, context) -> staticObjectToReturn);
    }

    public MockTestingResourceProvider getResourceProvider() {
        return resourceProvider;
    }

    public void registerTestMessageListener(String destinationName, MessageListener messageListener) {
        resourceProvider.registerTestMessageListener(
                destinationName,
                messageListener,
                getContext().getMicroServiceBaseURI());
    }

    private static class ResultMessageListener<T> implements MessageListener {
        private final Class<T> resultClass;
        T result;

        public ResultMessageListener(Class<T> resultClass) {
            this.resultClass = resultClass;
        }

        @Override
        public void onMessage(Message message, Context context) {
            this.result = message.readObject(resultClass);
        }

        @Override
        public void onErrorMessage(Message message, Context context) {
            // TODO
        }

        T getResult() {
            return this.result;
        }
    }

    private static class CountExecutionsMessageListener implements MessageListener {
        private int executionsCount = 0;
        private final MessageListener messageListener;

        public CountExecutionsMessageListener(MessageListener messageListener) {
            this.messageListener = messageListener;
        }

        @Override
        public void onMessage(Message message, Context context) {
            ++this.executionsCount;
            messageListener.onMessage(message, context);
        }

        @Override
        public void onErrorMessage(Message message, Context context) {
            // TODO
        }

        public int getExecutionsCount() {
            return executionsCount;
        }
    }

    public MicroServiceState getMicroServiceControllerState() {
        return serviceController.getState();
    }

    public <T> void withCustomSerialization(
            Class<T> clazz,
            SerializationReader<T> reader,
            SerializationWriter<T> writer) {
        serviceController.getContext().getSerializationManager().register(clazz, reader, writer);
    }

    public MicroServiceController getMicroServiceController() {
        return serviceController;
    }
}
