// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice;

import com.emc.microservice.blobstore.BlobStoreDescriptor;
import com.emc.microservice.datasource.DatasourceDescriptor;
import com.emc.microservice.dependency.AsyncCallbackServiceDependencyDescriptor;
import com.emc.microservice.dependency.ServiceDependencyDescriptor;
import com.emc.microservice.dservice.DynamicJavaServiceDescriptor;
import com.emc.microservice.input.InputDescriptor;
import com.emc.microservice.input.MessagingInputDescriptor;
import com.emc.microservice.messaging.DestinationDescriptor;
import com.emc.microservice.messaging.InputQueueDescriptor;
import com.emc.microservice.messaging.MessageListener;
import com.emc.microservice.output.MessagingOutputDescriptor;
import com.emc.microservice.output.OutputDescriptor;
import com.emc.microservice.resource.ResourceDescriptor;
import com.emc.microservice.restapi.ManagedResourceDescriptor;
import com.emc.microservice.schedule.ScheduleListener;
import com.emc.microservice.schedule.SchedulerDescriptor;
import com.emc.microservice.serialization.SerializationReader;
import com.emc.microservice.serialization.SerializationWriter;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.microservice.singleton.SingletonDescriptor;

import javax.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MicroServiceInitializationHelper builds the descriptor of a micro-service.
 * It helps describing all the resources the service is dependent on including inputs, outputs
 * messaging, REST API, data sources, dependent services and external resources.
 */
public class MicroServiceInitializationHelper {
    private final List<DatasourceDescriptor> datasourceDescriptors = new ArrayList<>();
    private final List<ParametersBag.MicroServiceParameterDescriptor> parameterDescriptors = new ArrayList<>();
    private final List<InputQueueDescriptor> microServiceInputQueueDescriptors = new ArrayList<>();
    private final List<DestinationDescriptor> microServiceDestinationDescriptors = new ArrayList<>();
    private final List<ManagedResourceDescriptor> managedRestResources = new ArrayList<>();
    private final List<ManagedResourceDescriptor> managedRestProviders = new ArrayList<>();
    private final List<ServiceDependencyDescriptor> dependencyDescriptors = new ArrayList<>();
    private final Set<Class> managedWebSockets = new HashSet<>();
    private final List<BlobStoreDescriptor> blobStoreDescriptors = new ArrayList<>();
    private final List<ExternalResourceDescriptorWrapper> externalResourceDescriptors = new ArrayList<>();
    private final List<SingletonDescriptor> singletonDescriptors = new ArrayList<>();
    private final List<DynamicJavaServiceDescriptor> dynamicJavaServicesDescriptors = new ArrayList<>();
    private final Map<Class, SerializationWriter> customMessageWriters = new HashMap<>();
    private final Map<Class, SerializationReader> customMessageReaders = new HashMap<>();
    private final Set<Class> classesForJacksonSerialization = new HashSet<>();
    private final List<SchedulerDescriptor> schedulerDescriptors = new ArrayList<>();
    private final Map<String, Class<? extends ScheduleListener>> scheduleListenerClasses = new HashMap<>();
    private InputDescriptor inputDescriptor = null;
    private OutputDescriptor outputDescriptor = null;

    /***
     * Specifying the micro-service is dependent on a datasource
     * @param name datasource logical name as appears in the service registry
     * @param description datasource description - specific to current service. will be shown in /configuration resource
     *                    of the service
     * @return this instance of initialization helper
     */
    public MicroServiceInitializationHelper withDatasource(String name, String description) {
        this.datasourceDescriptors.add(new DatasourceDescriptor(name, description));
        return this;
    }

    public MicroServiceInitializationHelper withWebSocket(Class wsClass) {
        this.managedWebSockets.add(wsClass);
        return this;
    }

    /**
     * Require a scheduler for this microservice
     * @param name name of the scheduler configuration
     */
    public MicroServiceInitializationHelper withScheduler(String name) {
        if (schedulerDescriptors.stream().anyMatch(descriptor -> descriptor.getName().equals(name))) {
            throw new IllegalArgumentException("a scheduler with name=" + name + " already exists");
        }
        this.schedulerDescriptors.add(new SchedulerDescriptor(name));
        return this;
    }

    /**
     * Register a ScheduleListener to be used by scheduler
     * @param listenerIdentifier a unique identifier for this listener. Might be persisted, so avoid changing it
     */
    public MicroServiceInitializationHelper withSchedulerListenerMapping(
            String listenerIdentifier,
            Class<? extends ScheduleListener> listenerClass) {
        if (scheduleListenerClasses.containsKey(listenerIdentifier)) {
            throw new IllegalArgumentException("listenerIdentifier=" + listenerIdentifier + " already registered");
        }
        this.scheduleListenerClasses.put(listenerIdentifier, listenerClass);
        return this;
    }

    /**
     * Specify this service is dependent on default object store. The object
     * store is a key-value store.
     *
     * @return this instance of initialization helper
     */
    public MicroServiceInitializationHelper withDefaultBlobStore() {
        return withBlobStore(MicroService.DEFAULT_BLOBSTORE_NAME);
    }

    /**
     * Specify this service is dependent on an object store. The object
     * store is a key-value store.
     *
     * @return this instance of initialization helper
     */
    public MicroServiceInitializationHelper withBlobStore(String blobstoreName) {
        this.blobStoreDescriptors.add(new BlobStoreDescriptor(blobstoreName));
        return this;
    }

    /**
     * Parameter represents system settings or setting required for the micro-service to
     * operate.
     *
     * @param name         Name of the parameter
     * @param description  Description for the parameter
     * @param defaultValue Default value of the parameter. May be null.
     * @return this instance of initialization helper
     */
    public MicroServiceInitializationHelper withParameter(String name, String description, Object defaultValue) {
        return withParameter(name, description, defaultValue, true);
    }

    /**
     * Parameter represents system settings or setting required for the micro-service to
     * operate.
     *
     * @param name         Name of the parameter
     * @param description  Description for the parameter
     * @param defaultValue Default value of the parameter. May be null.
     * @param mandatory    Whether this parameter is mandatory, or nulls are welcome?
     * @return this instance of initialization helper
     */
    public MicroServiceInitializationHelper withParameter(String name,
                                                          String description,
                                                          Object defaultValue,
                                                          boolean mandatory) {
        this.parameterDescriptors.add(
                new ParametersBag.MicroServiceParameterDescriptor(
                        name,
                        description,
                        defaultValue == null ? null : defaultValue.toString(),
                        mandatory));
        return this;
    }

    /**
     * Add messaging queue the service listens on.
     *
     * @param queueName                Logical name of the queue. This name is mapped to appropriate name in the
     *                                 underlying messaging system.
     * @param queueDescription         Description of the queue
     * @param messageListener          Message listener class, may implement ServiceLifecycle interface for supporting
     *                                 injection and lifecycle callbacks.
     * @param messagesSelector         Key used to filter the messages from the queue
     * @param messageHeadersForLogging Key names that are used to get message headers which are used for logging.
     *                                 Each message is logged with the identifier built using these message
     *                                 header fields. This allows us to trace a specific request or event through
     *                                 the system.
     * @return this instance of initialization helper.
     */
    public MicroServiceInitializationHelper withInputQueue(String queueName,
                                                           String queueDescription,
                                                           Class<? extends MessageListener> messageListener,
                                                           String messagesSelector,
                                                           String... messageHeadersForLogging) {
        this.microServiceInputQueueDescriptors.add(
                new InputQueueDescriptor(
                        queueName,
                        queueDescription,
                        messageListener,
                        messagesSelector,
                        messageHeadersForLogging));
        return this;
    }

    /**
     * Add messaging queue or topic service publishes to.
     *
     * @param name        Name of the messaging destination. The name is logical name and maps to name underlying
     *                    messaging system.
     * @param description Description of the destination.
     * @return this
     */
    public MicroServiceInitializationHelper withDestination(String name, String description) {
        this.microServiceDestinationDescriptors.add(new DestinationDescriptor(name, description));
        return this;
    }

    /**
     * Add REST resource provided by the micro-service
     *
     * @param resourceClass The REST resource class. This is the class with JAX-RS annotation or implements an
     *                      interface with the annotation.
     * @param description   Description of the REST resource
     * @return this
     */
    public MicroServiceInitializationHelper withRestResource(Class<?> resourceClass, String description) {
        this.managedRestResources.add(new ManagedResourceDescriptor(resourceClass, description));
        return this;
    }

    /**
     * Add REST provider provided by the micro-service.
     *
     * @param providerClass The REST provider class. This is the class with JAX-RS @{@link Provider} annotation or
     *                      implements an interface with the annotation.
     * @param description   Description of the REST provider
     * @return this
     */
    public MicroServiceInitializationHelper withRestProvider(Class<?> providerClass, String description) {
        this.managedRestProviders.add(new ManagedResourceDescriptor(providerClass, description));
        return this;
    }

    /**
     * Adds a callback handler. The callback handler is invoked with the dependent micro-service has finished processing
     * the request.
     *
     * @param asyncCallbackServiceDependencyDescriptor async communication descriptor
     * @return this
     */
    public MicroServiceInitializationHelper withAsyncServiceCallbackDependency(
            AsyncCallbackServiceDependencyDescriptor asyncCallbackServiceDependencyDescriptor) {
        this.dependencyDescriptors.add(asyncCallbackServiceDependencyDescriptor);
        return this;
    }

    /**
     * Adds a "send and forget" service dependency
     *
     * @param serviceDependencyDescriptor async communication descriptor
     * @return this
     */
    public MicroServiceInitializationHelper withServiceDependency(
            ServiceDependencyDescriptor serviceDependencyDescriptor) {
        this.dependencyDescriptors.add(serviceDependencyDescriptor);
        return this;
    }

    /**
     * Add description of an input to the micro-service. It may be a messaging input or REST API.
     *
     * @param inputDescriptor input description for micro-service
     * @return this
     * @see MicroServiceInitializationHelper () and withInputQueue()
     */
    public MicroServiceInitializationHelper withMainInput(InputDescriptor inputDescriptor) {
        this.inputDescriptor = inputDescriptor;
        return this;
    }

    /***
     * Indicate that the "main" input for this microservice works via messaging (input queue).
     *
     * @param format message format in java class representation. in case there is no java implementation,
     *               specify either InputStream.class or simply null
     * @param description description of the queue content
     * @param messageListener message listener class, will be used to create message listeners (one for every
     *                               concurrency level (thread), class may implement ServiceLifecycle
     * @param messageHeadersForLogging optional - expected message headers as part of input. will be used for logging
     *                                 and tracking messages
     */
    public MicroServiceInitializationHelper withMainInput(Class format,
                                                          String description,
                                                          Class<? extends MessageListener> messageListener,
                                                          String[] messageHeadersForLogging) {
        this.inputDescriptor =
                new MessagingInputDescriptor(
                        description,
                        format,
                        messageListener,
                        messageHeadersForLogging);
        return this;
    }


    /**
     * Add description of micro-service's output.
     *
     * @param outputDescriptor main output descriptor for service
     */
    public MicroServiceInitializationHelper withMainOutput(OutputDescriptor outputDescriptor) {
        this.outputDescriptor = outputDescriptor;
        return this;
    }

    /***
     * Indicate that the "main" output for this microservice works via messaging (output queue).
     * @param format message content format in java representation. in case there is no java representation,
     *               pass OutputStream.class or simply null
     * @param description description of output content
     */
    public MicroServiceInitializationHelper withMainOutput(Class format, String description) {
        this.outputDescriptor = new MessagingOutputDescriptor(format, description);
        return this;
    }

    public <T> MicroServiceInitializationHelper withCustomSerialization(Class<T> clazz,
                                                                        SerializationReader<T> reader,
                                                                        SerializationWriter<T> writer) {
        customMessageReaders.put(clazz, reader);
        customMessageWriters.put(clazz, writer);
        return this;
    }

    /**
     * Mark class for jackson serialization throught the entire microservice persistence frameworks
     * (messaging/object/rest/scheduling)
     */
    public <T> MicroServiceInitializationHelper withJacksonSerialization(Class<T> clazz) {
        classesForJacksonSerialization.add(clazz);
        return this;
    }

    /***
     * Define dependency on external resource
     * @param externalResourceDescriptor external resource descriptor
     */
    public MicroServiceInitializationHelper withExternalResource(ResourceDescriptor externalResourceDescriptor) {
        return withExternalResource(externalResourceDescriptor, false);
    }

    /***
     * Define dependency on external resource.
     * @param externalResourceDescriptor external resource descriptor
     * @param onlyWhereSupported optional. (defaults to false) - when true the resource provider will enforce this
     *                           dependency only if resource provider supports this type of external resource,
     *                           else will allow the service to start.
     */
    public MicroServiceInitializationHelper withExternalResource(ResourceDescriptor externalResourceDescriptor,
                                                                 boolean onlyWhereSupported) {
        this.externalResourceDescriptors.add(
                new ExternalResourceDescriptorWrapper(
                        externalResourceDescriptor,
                        onlyWhereSupported));
        return this;
    }

    /***
     * Declare managed singleton
     * @param name name of the singleton
     * @param description singleton description
     * @param singletonClass java class implementing Singleton interface
     */
    public <T extends ServiceLifecycle> MicroServiceInitializationHelper withSingleton(String name,
                                                                                       String description,
                                                                                       Class<T> singletonClass) {
        this.singletonDescriptors.add(new SingletonDescriptor<>(name, description, singletonClass));
        return this;
    }

    /***
     * Declare managed singleton
     * @param singletonClass java class implementing Singleton interface
     */
    public <T extends ServiceLifecycle> MicroServiceInitializationHelper withSingleton(Class<T> singletonClass) {
        this.singletonDescriptors.add(
                new SingletonDescriptor<>(
                        singletonClass.getSimpleName(),
                        singletonClass.getSimpleName() + " Singleton", singletonClass));
        return this;
    }

    /***
     * Declare managed java service
     * @param name name of the java service
     * @param description service description
     * @param serviceInterface java interface for service
     */
    public MicroServiceInitializationHelper withDynamicJavaService(String name,
                                                                   String description,
                                                                   Class serviceInterface) {
        this.dynamicJavaServicesDescriptors.add(new DynamicJavaServiceDescriptor(name, description, serviceInterface));
        return this;
    }

    /***
     * Declare managed java service
     * @param serviceInterface java interface for service
     */
    public MicroServiceInitializationHelper withDynamicJavaService(Class serviceInterface) {
        this.dynamicJavaServicesDescriptors.add(
                new DynamicJavaServiceDescriptor(
                        serviceInterface.getSimpleName(),
                        serviceInterface.getSimpleName() + " Implementation", serviceInterface));
        return this;
    }

    public List<DatasourceDescriptor> getDatasourceDescriptors() {
        return datasourceDescriptors;
    }

    public List<ParametersBag.MicroServiceParameterDescriptor> getParameterDescriptors() {
        return parameterDescriptors;
    }

    public List<InputQueueDescriptor> getMicroServiceInputQueueDescriptors() {
        return microServiceInputQueueDescriptors;
    }

    public List<DestinationDescriptor> getMicroServiceDestinationDescriptors() {
        return microServiceDestinationDescriptors;
    }

    public List<ExternalResourceDescriptorWrapper> getExternalResourceDescriptors() {
        return externalResourceDescriptors;
    }

    public List<ServiceDependencyDescriptor> getDependencyDescriptors() {
        return dependencyDescriptors;
    }

    public List<ManagedResourceDescriptor> getManagedRestResources() {
        return managedRestResources;
    }

    public List<ManagedResourceDescriptor> getManagedRestProviders() {
        return managedRestProviders;
    }

    public List<SingletonDescriptor> getSingletonDescriptors() {
        return singletonDescriptors;
    }

    public List<DynamicJavaServiceDescriptor> getDynamicJavaServicesDescriptors() {
        return dynamicJavaServicesDescriptors;
    }

    public List<SchedulerDescriptor> getSchedulerDescriptors() {
        return schedulerDescriptors;
    }

    public Map<String, Class<? extends ScheduleListener>> getScheduleListenerClasses() {
        return scheduleListenerClasses;
    }

    public InputDescriptor getInputDescriptor() {
        return inputDescriptor;
    }

    public OutputDescriptor getOutputDescriptor() {
        return outputDescriptor;
    }

    public Map<Class, SerializationWriter> getCustomMessageWriters() {
        return customMessageWriters;
    }

    public Map<Class, SerializationReader> getCustomMessageReaders() {
        return customMessageReaders;
    }

    public List<BlobStoreDescriptor> getBlobStoreDescriptors() {
        return blobStoreDescriptors;
    }

    public Set<Class> getManagedWebSockets() {
        return managedWebSockets;
    }

    public Set<Class> getClassesForJacksonSerialization() {
        return classesForJacksonSerialization;
    }

    public static class ExternalResourceDescriptorWrapper {
        private ResourceDescriptor resourceDescriptor;
        private boolean requiredOnlyWhenSupported;

        public ExternalResourceDescriptorWrapper(ResourceDescriptor resourceDescriptor,
                                                 boolean requiredOnlyWhenSupported) {
            this.resourceDescriptor = resourceDescriptor;
            this.requiredOnlyWhenSupported = requiredOnlyWhenSupported;
        }

        public ResourceDescriptor getResourceDescriptor() {
            return resourceDescriptor;
        }

        public boolean isRequiredOnlyWhenSupported() {
            return requiredOnlyWhenSupported;
        }
    }
}
