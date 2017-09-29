// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice;

import com.emc.dpa.timer.Timer;
import com.emc.microservice.blobstore.BlobStoreConfiguration;
import com.emc.microservice.blobstore.BlobStoreLink;
import com.emc.microservice.blobstore.BlobStoreManager;
import com.emc.microservice.datasource.DatasourceConfiguration;
import com.emc.microservice.datasource.DatasourceManager;
import com.emc.microservice.dependency.AsyncCallbackServiceDependencyDescriptor;
import com.emc.microservice.dependency.SendAndForgetServiceDependencyDescriptor;
import com.emc.microservice.dependency.ServiceDependencyConfiguration;
import com.emc.microservice.dependency.ServiceDependencyDescriptor;
import com.emc.microservice.dependency.ServiceDependencyManager;
import com.emc.microservice.dservice.DynamicJavaServiceConfiguration;
import com.emc.microservice.dservice.DynamicJavaServiceManager;
import com.emc.microservice.health.HealthCheckManagerImpl;
import com.emc.microservice.healthcheck.MicroServiceHealthCheckRegistry;
import com.emc.microservice.input.InputDescriptor;
import com.emc.microservice.input.MessagingInputDescriptor;
import com.emc.microservice.messaging.DestinationConfiguration;
import com.emc.microservice.messaging.InputQueueConfiguration;
import com.emc.microservice.messaging.InputQueueDescriptor;
import com.emc.microservice.messaging.MessageDestinationManager;
import com.emc.microservice.messaging.QueuesManager;
import com.emc.microservice.metrics.MetricsRegistryImpl;
import com.emc.microservice.output.OutputDescriptor;
import com.emc.microservice.output.ServiceOutputDescriptor;
import com.emc.microservice.resource.AbstractResourceManager;
import com.emc.microservice.resource.ExternalResourceManager;
import com.emc.microservice.resource.ExternalResourceManagerWrapper;
import com.emc.microservice.resource.ResourceConfiguration;
import com.emc.microservice.resource.ResourceDescriptor;
import com.emc.microservice.resource.ResourceManager;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.microservice.restapi.MicroServiceWebServer;
import com.emc.microservice.restapi.RestResourceManager;
import com.emc.microservice.restapi.WebServerConfiguration;
import com.emc.microservice.schedule.SchedulerConfiguration;
import com.emc.microservice.schedule.SchedulerManager;
import com.emc.microservice.serialization.JacksonSerializationReader;
import com.emc.microservice.serialization.JacksonSerializationWriter;
import com.emc.microservice.serialization.SerializationManagerImpl;
import com.emc.microservice.singleton.SingletonConfiguration;
import com.emc.microservice.singleton.SingletonManager;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** This class is responsible for running and managing lifecycle of a micro service */
public class MicroServiceController {

    private static final ParametersBag.MicroServiceParameterDescriptor HEALTH_CHECK_PERIOD_IN_SECONDS_PARAM_DESCRIPTOR =
            new ParametersBag.MicroServiceParameterDescriptor(
                    MicroServiceHealthCheckRegistry.HEALTH_CHECK_PERIOD_PARAMETER_NAME,
                    "Interval for doing health checks",
                    String.valueOf(MicroServiceHealthCheckRegistry.DEFAULT_PERIOD),
                    true);

    private static final ParametersBag.MicroServiceParameterDescriptor PRINT_ALL_JSON_RESPONSES_PARAM_DESCRIPTOR =
            new ParametersBag.MicroServiceParameterDescriptor(
                    "print-all-json-requests",
                    "Print all requests/responses",
                    Boolean.toString(false),
                    true);

    private final ResourceProvider resourceProvider;
    private final MicroService serviceDescriptor;
    private final ParametersBag params;
    private final QueuesManager queuesManager;
    private final MessageDestinationManager destinationManager;
    private final DatasourceManager datasourceManager;
    private final BlobStoreManager blobStoreManager;
    private final ServiceDependencyManager dependencyManager;
    private final RestResourceManager restResourceManager;
    private final MetricsRegistryImpl metricRegistry;
    private final List<ExternalResourceManagerWrapper> externalResourceManagers;
    private final InputDescriptor inputDescriptor;
    private final OutputDescriptor outputDescriptor;
    private final HealthCheckManagerImpl healthCheckManager;
    private final SingletonManager singletonManager;
    private final DynamicJavaServiceManager dynamicJavaServiceManager;
    private final String baseURI;
    private final Timer timer;
    private final Logger logger;
    private final SerializationManagerImpl serializationManager;
    private final SchedulerManager schedulerManager;
    private List<AbstractResourceManager> resourceManagers = Collections.emptyList();
    private ContextImpl context = null;
    private MicroServiceState state = MicroServiceState.STOPPED;
    private MicroServiceWebServer webServer;

    /***
     * Initialize micro-service descriptor, used by subclasses to initialize design time parameters of a service
     *
     * @param serviceDescriptor micro-service descriptor
     */
    public MicroServiceController(ResourceProvider resourceProvider, MicroService serviceDescriptor, String baseURI) {
        this.resourceProvider = resourceProvider;
        this.serviceDescriptor = serviceDescriptor;
        this.baseURI = baseURI;
        this.logger = serviceDescriptor.getLogger();

        // Initializing metrics registry
        this.metricRegistry = new MetricsRegistryImpl(baseURI);
        this.healthCheckManager = new HealthCheckManagerImpl(
                new MicroServiceHealthCheckRegistry(baseURI, logger),
                logger);

        this.params = initializeParameters(serviceDescriptor.getInitializationHelper());
        this.inputDescriptor = serviceDescriptor.getInitializationHelper().getInputDescriptor();
        this.outputDescriptor = initializeOutputDescriptor(
                serviceDescriptor.getInitializationHelper().getOutputDescriptor(),
                serviceDescriptor.getInitializationHelper());
        this.queuesManager = initializeQueueManager(logger, serviceDescriptor.getInitializationHelper());
        this.destinationManager = new MessageDestinationManager(
                logger,
                serviceDescriptor.getInitializationHelper().getMicroServiceDestinationDescriptors());
        this.datasourceManager = new DatasourceManager(
                serviceDescriptor.getInitializationHelper().getDatasourceDescriptors(),
                logger);
        this.blobStoreManager = new BlobStoreManager(
                serviceDescriptor.getInitializationHelper().getBlobStoreDescriptors(),
                logger);
        this.dependencyManager = initializeDependencyManager(
                logger,
                serviceDescriptor.getInitializationHelper());
        this.restResourceManager = new RestResourceManager(
                serviceDescriptor.getInitializationHelper().getManagedRestResources(),
                serviceDescriptor.getInitializationHelper().getManagedRestProviders(),
                serviceDescriptor.getInitializationHelper().getManagedWebSockets());
        this.singletonManager = new SingletonManager(
                serviceDescriptor.getInitializationHelper().getSingletonDescriptors(),
                logger);
        this.dynamicJavaServiceManager = new DynamicJavaServiceManager(
                serviceDescriptor.getInitializationHelper().getDynamicJavaServicesDescriptors(),
                logger);
        this.schedulerManager = new SchedulerManager(
                serviceDescriptor.getInitializationHelper().getSchedulerDescriptors(),
                serviceDescriptor.getInitializationHelper().getScheduleListenerClasses(),
                logger);
        this.externalResourceManagers = new ArrayList<>(
                serviceDescriptor.getInitializationHelper().getExternalResourceDescriptors().size());
        this.serializationManager = SerializationManagerImpl.initializeSerializationManager(
                serviceDescriptor.getInitializationHelper());

        this.timer = new Timer(getBaseURI(), metricRegistry);

    }

    private OutputDescriptor initializeOutputDescriptor(
            OutputDescriptor outputDescriptor,
            MicroServiceInitializationHelper helper) {
        if (outputDescriptor != null && outputDescriptor.getOutputBlobstoreDetails() != null) {
            helper.withBlobStore(outputDescriptor.getOutputBlobstoreDetails().getBlobstoreName());
            helper.withCustomSerialization(BlobStoreLink.class, new JacksonSerializationReader<>(BlobStoreLink.class),
                    new JacksonSerializationWriter<>());
        }
        return outputDescriptor;
    }

    public MicroService getServiceDescriptor() {
        return serviceDescriptor;
    }

    private List<AbstractResourceManager> instantiateResourceManagers(ResourceProvider resourceProvider) {
        List<AbstractResourceManager> builtInResourceManagers = new ArrayList<>(Arrays.<AbstractResourceManager>asList(
                datasourceManager,
                blobStoreManager,
                destinationManager,
                queuesManager,
                singletonManager,
                dynamicJavaServiceManager,
                schedulerManager,
                dependencyManager

        ));

        builtInResourceManagers.addAll(instantiateExternalResourceManagers(resourceProvider));
        return new ArrayList<>(builtInResourceManagers);
    }

    private Collection<? extends AbstractResourceManager> instantiateExternalResourceManagers(
            ResourceProvider resourceProvider) {

        List<MicroServiceInitializationHelper.ExternalResourceDescriptorWrapper> externalResourceDescriptors =
                serviceDescriptor.getInitializationHelper()
                        .getExternalResourceDescriptors();
        Map<ExternalResourceManager, List<ResourceDescriptor>> externalResourceManagerClassToDescriptorsMap =
                new HashMap<>(
                        externalResourceDescriptors.size());
        for (MicroServiceInitializationHelper.ExternalResourceDescriptorWrapper currWrapper :
                externalResourceDescriptors) {
            ResourceDescriptor currDescriptor = currWrapper.getResourceDescriptor();
            ExternalResourceManager externalResourceManager =
                    resourceProvider.getExternalResourceManager(currDescriptor.getClass());

            if (externalResourceManager == null) {
                if (!currWrapper.isRequiredOnlyWhenSupported()) {
                    throw new IllegalStateException(
                            "Unsupported resource descriptor type " + currDescriptor.getName() + " of type: " +
                                    currDescriptor.getClass().getCanonicalName() + " for resource provider " +
                                    resourceProvider.getClass().getSimpleName());
                } else {
                    logger.info(
                            "Skipping dependency {} since it is not supported by current resource provider",
                            currDescriptor.getName());
                }
            } else {

                List<ResourceDescriptor> resourceDescriptors =
                        externalResourceManagerClassToDescriptorsMap.get(externalResourceManager);
                if (resourceDescriptors == null) {
                    resourceDescriptors = new ArrayList<>();
                    //noinspection unchecked
                    externalResourceManagerClassToDescriptorsMap.put(externalResourceManager, resourceDescriptors);
                }
                resourceDescriptors.add(currDescriptor);
            }
        }

        //noinspection unchecked
        this.externalResourceManagers.addAll(externalResourceManagerClassToDescriptorsMap
                .entrySet()
                .stream()
                .map(currExternalRMEntry ->
                        new ExternalResourceManagerWrapper(
                                currExternalRMEntry.getValue(),
                                getLogger(),
                                currExternalRMEntry.getKey()))
                .collect(Collectors.toList()));

        return this.externalResourceManagers;
    }

    private ServiceDependencyManager initializeDependencyManager(
            Logger logger,
            MicroServiceInitializationHelper initializationHelper) {
        List<ServiceDependencyDescriptor> dependencyDescriptors =
                new ArrayList<>(initializationHelper.getDependencyDescriptors());
        if (initializationHelper.getOutputDescriptor() != null
                && initializationHelper.getOutputDescriptor().getOutputType() ==
                OutputDescriptor.MicroServiceOutputType.service) {
            ServiceOutputDescriptor serviceOutputDescriptor =
                    (ServiceOutputDescriptor) initializationHelper.getOutputDescriptor();
            dependencyDescriptors.add(new SendAndForgetServiceDependencyDescriptor(
                    serviceOutputDescriptor.getServiceURI(),
                    true,
                    initializationHelper.getOutputDescriptor().getFormat()));
        }
        return new ServiceDependencyManager(dependencyDescriptors, logger);
    }

    private QueuesManager initializeQueueManager(Logger logger, MicroServiceInitializationHelper initializationHelper) {
        List<InputQueueDescriptor> microServiceInputQueueDescriptors =
                initializationHelper.getMicroServiceInputQueueDescriptors();
        handleServiceInput(microServiceInputQueueDescriptors, initializationHelper);
        return new QueuesManager(logger, microServiceInputQueueDescriptors);
    }

    private ParametersBag initializeParameters(MicroServiceInitializationHelper initializationHelper) {
        // Adding global params
        List<ParametersBag.MicroServiceParameterDescriptor> parameterDescriptors =
                initializationHelper.getParameterDescriptors();
        if (parameterDescriptors == null) {
            parameterDescriptors = new ArrayList<>(1);
        } else {
            parameterDescriptors = new ArrayList<>(parameterDescriptors);
        }
        parameterDescriptors.add(HEALTH_CHECK_PERIOD_IN_SECONDS_PARAM_DESCRIPTOR);
        parameterDescriptors.add(PRINT_ALL_JSON_RESPONSES_PARAM_DESCRIPTOR);

        return new ParametersBag(parameterDescriptors);
    }

    /***
     * Adding implicit input queue descriptors resulting by having dependencies
     *
     * @param inputQueueDescriptors input queue descriptor list
     * @param initializationHelper  resource descriptor parameters
     */
    private void handleServiceInput(
            List<InputQueueDescriptor> inputQueueDescriptors,
            MicroServiceInitializationHelper initializationHelper) {

        // If public input is defined, need to add resource descriptors
        if (initializationHelper.getInputDescriptor() != null) {
            switch (initializationHelper.getInputDescriptor().getInputType()) {
                case messaging:
                    // In case input is of type messaging - adding input queue descriptor to the list
                    MessagingInputDescriptor messagingInputDescriptor =
                            (MessagingInputDescriptor) initializationHelper.getInputDescriptor();
                    inputQueueDescriptors.add(new InputQueueDescriptor(
                            getStandardInputQueueName(this.getBaseURI()),
                            messagingInputDescriptor.getDescription(),
                            messagingInputDescriptor.getMessageListener(),
                            null,
                            messagingInputDescriptor.getMessageHeadersForLogging()));
                    break;
                default:
                    break;
            }
        }

        // Getting message listeners from dependencies
        if (initializationHelper.getDependencyDescriptors() != null) {
            for (ServiceDependencyDescriptor currDependencyDescriptor :
                    initializationHelper.getDependencyDescriptors()) {
                switch (currDependencyDescriptor.getServiceDependencyType()) {
                    case ASYNC_CALL:
                        final AsyncCallbackServiceDependencyDescriptor asyncCallbackServiceDependencyDescriptor =
                                (AsyncCallbackServiceDependencyDescriptor) currDependencyDescriptor;
                        inputQueueDescriptors.add(new InputQueueDescriptor(getDependencyCallbackQueueName(
                                this.getBaseURI(),
                                currDependencyDescriptor.getLastRoute()),
                                "Callback queue for results from service " + currDependencyDescriptor.getLastRoute(),
                                asyncCallbackServiceDependencyDescriptor.getServiceResultCallback(), null));
                        break;
                    default:
                        // this is used by tests only
                }
            }
        }
    }

    /***
     * Service display name
     *
     * @return service name
     */
    public final String getName() {
        return serviceDescriptor.getName();
    }

    /***
     * Service base uri
     *
     * @return service URI
     */
    public final String getBaseURI() {
        return baseURI;
    }

    /***
     * Returns service version
     *
     * @return service version
     */
    public final int getVersion() {
        return serviceDescriptor.getVersion();
    }

    /***
     * Service state could be any of the enumeration MicroServiceState values
     * Service state is manipulated by the micro-service framework libraries internally to expose
     * current state of the service. when service resources are unavailable service changes to a paused state and
     * back to running when resources are available
     *
     * @return service state
     */
    public final MicroServiceState getState() {
        return state;
    }

    /***
     * Start the micro-service
     * Yey
     */
    public synchronized void start() {
        try {
            // Set status to starting
            state = MicroServiceState.STARTING;

            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

            // Getting All Resource manager implementations
            resourceManagers = instantiateResourceManagers(resourceProvider);

            context = buildContext();
            ContextThreadLocal.setContext(context);

            resourceProvider.preRunServiceHook(context);

            ServiceConfig serviceConfig = pollFetching(
                    "Service Config",
                    () -> resourceProvider.getServiceRegistryApi().getServiceConfig(getBaseURI()));

            WebServerConfiguration webServerConfiguration = pollFetching(
                    "Webserver Configuration",
                    () -> resourceProvider.getServiceRegistryApi().getWebServerConfiguration(
                            resourceProvider.getWebServerConfigurationClass(),
                            serviceConfig == null ? "default" : serviceConfig.getWebServerName()));
            webServer = resourceProvider.getWebServer(webServerConfiguration);
            context = buildContext();
            ContextThreadLocal.setContext(context);

            // Start serving the web app. state & configuration resources will be available from this point
            startWebApp();

            // Resolving service runtime dependencies via registry
            MicroServiceLoader.ServiceRuntimeData serviceRuntimeData =
                    MicroServiceLoader.loadServiceRuntimeData(this, resourceProvider);

            // Start the service (status will be set internally)
            doStart(
                    resourceProvider,
                    serviceRuntimeData.getParamValues(),
                    serviceRuntimeData.getQueuesConfiguration(),
                    serviceRuntimeData.getMessageDestinationConfiguration(),
                    serviceRuntimeData.getDatasourceConfigurations(),
                    serviceRuntimeData.getBlobStoreConfiguration(),
                    serviceRuntimeData.getServiceDependencyConfiguration(),
                    serviceRuntimeData.getSingletonsConfiguration(),
                    serviceRuntimeData.getDynamicJavaServiceConfigurationMap(),
                    serviceRuntimeData.getSchedulerConfigurationMap(),
                    serviceRuntimeData.getExternalResourceManagersConfigurations()
            );
        } finally {
            // If service is still starting - meaning something has failed - setting to stopped
            if (MicroServiceState.STARTING.equals(state)) {
                state = MicroServiceState.STOPPED;
            }
        }
    }

    private <T> T pollFetching(String entityDesc, Supplier<T> supplier) {
        int attempt = 1;
        long firstTimeTried = System.currentTimeMillis();
        long lastTimeLogged = System.currentTimeMillis();
        long pollingSleepInterval = 5000L;
        while (true) {
            Exception ex = null;
            try {
                T entity = supplier.get();
                if (entity != null) {
                    return entity;
                }
            } catch (Exception e) {
                ex = e;
            }

            final long currentTimeMillis = System.currentTimeMillis();
            String errorStr = ex == null ? "no exception, returned null" : ex.getMessage();
            if (attempt == 1) {
                logPollingState(entityDesc, attempt, pollingSleepInterval, errorStr);

                logger.debug("Failed getting " + entityDesc, ex);
                lastTimeLogged = currentTimeMillis;
            } else {
                if (currentTimeMillis - lastTimeLogged > 1000 * 60 * 5) {
                    lastTimeLogged = currentTimeMillis;
                    logPollingState(entityDesc, attempt, pollingSleepInterval, errorStr);

                    // After 15 minutes of waiting we poll every 30 seconds,
                    // after 30 minutes we start polling every 3 minutes
                    // this is done to avoid putting load for services that will not start for a while
                    if (currentTimeMillis - firstTimeTried > 1000 * 60 * 15) {
                        pollingSleepInterval = 30000L;
                    } else if (currentTimeMillis - firstTimeTried > 1000 * 60 * 30) {
                        pollingSleepInterval = 1000 * 60 * 3;
                    }
                }
            }

            sleepNoException(pollingSleepInterval);
            attempt++;
        }
    }

    private void logPollingState(String entityDesc, int attempt, long pollingSleepInterval, String errorStr) {
        logger.info(
                "Polling for {}. attempt {} - {},  Polling silently every {} seconds, " +
                        "logging polling  status every 5 minutes",
                entityDesc,
                attempt,
                errorStr,
                pollingSleepInterval);
    }

    private void sleepNoException(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Don't care
        }
    }

    private void startWebApp() {
        this.webServer.deployServiceApplication(context);
    }

    /***
     * Start the microservice
     * Service will keep trying to start until succeed or forced to stop
     *  @param resourceProvider                       implementation for getting actual resources
     * @param serviceParameterValues                 service parameter values overriding defaults
     * @param queuesConfiguration                    configuration for micro service input queue(s) if any
     * @param messageDestinationConfiguration        configuration for micro service destination(s) if any
     * @param datasourceConfigurations               datasources configurations for any declared datasource descriptors
     *                                               if any
     * @param blobStoreConfigurations                blobstore Configurations
     * @param serviceDependencyConfigurations        inter process dependency configurations
     * @param singletonConfigurations                singleton configurations
     * @param dynamicJavaServiceConfigurations       dynamic java services configurations
     * @param externalResourceManagersConfigurations external services configurations for non-built in
     *                                               ExternalResourceManagers
     */
    private void doStart(
            ResourceProvider resourceProvider,
            Map<String, String> serviceParameterValues,
            Map<String, InputQueueConfiguration> queuesConfiguration,
            Map<String, DestinationConfiguration> messageDestinationConfiguration,
            Map<String, DatasourceConfiguration> datasourceConfigurations,
            Map<String, BlobStoreConfiguration> blobStoreConfigurations,
            Map<String, ServiceDependencyConfiguration> serviceDependencyConfigurations,
            Map<String, SingletonConfiguration> singletonConfigurations,
            Map<String, DynamicJavaServiceConfiguration> dynamicJavaServiceConfigurations,
            Map<String, SchedulerConfiguration> schedulerConfigurations,
            Map<Class<? extends ResourceDescriptor>,
                    Map<String, ResourceConfiguration>> externalResourceManagersConfigurations) {
        if (resourceProvider == null) {
            throw new IllegalArgumentException("Mandatory argument resource provider not supplied");
        }

        // Setting parameters
        setParameters(serviceParameterValues);

        // Starting metrics registry
        metricRegistry.init(serviceParameterValues);

        // Start the datasources manager
        datasourceManager.init(datasourceConfigurations, context, healthCheckManager);

        // Initializing default BlobStore configuration
        blobStoreManager.init(blobStoreConfigurations, context, healthCheckManager);

        // Start message destinations
        destinationManager.init(messageDestinationConfiguration, context, healthCheckManager);

        // Start Service Dependency Manager
        dependencyManager.init(serviceDependencyConfigurations, context, healthCheckManager);

        // Initializing external (non built-in) resource managers
        initializeExternalResourceManagers(externalResourceManagersConfigurations, context, healthCheckManager);

        // Start dynamic java services
        dynamicJavaServiceManager.init(dynamicJavaServiceConfigurations, context, healthCheckManager);

        // Start schedulers
        schedulerManager.init(schedulerConfigurations, context, healthCheckManager);

        // Start singletons
        singletonManager.init(singletonConfigurations, context, healthCheckManager);

        // Start Listening on queues
        queuesManager.init(queuesConfiguration, context, healthCheckManager);

        // Initializing service (service specific initialization code)
        serviceDescriptor.initializeService(context);

        // Setting the status to running
        state = MicroServiceState.RUNNING;

        // Inform the world we are ready to play
        logger.info("{} Micro Service Version {} Started", getName(), getVersion());

        // Initialize health checks
        healthCheckManager.init(this, context);

        // Marking all resources as available to start
        startResourceManagers();
    }

    private void initializeExternalResourceManagers(
            Map<Class<? extends ResourceDescriptor>,
                    Map<String, ResourceConfiguration>> externalResourceManagersConfigurations,
            ContextImpl context,
            HealthCheckManagerImpl healthCheckManager) {

        for (ExternalResourceManagerWrapper currExternalResourceManager : this.externalResourceManagers) {
            Map<String, ResourceConfiguration> externalResourceConfigurations = Objects.requireNonNull(
                    externalResourceManagersConfigurations.get(currExternalResourceManager.getDescriptorClass()),
                    "Failed to load configuration for external resource manager " +
                            currExternalResourceManager.getDescriptorClass().getSimpleName() + ": "
                            + currExternalResourceManager.getResourceTypeName());

            //noinspection unchecked
            currExternalResourceManager.init(externalResourceConfigurations, context, healthCheckManager);
        }
    }

    private void startResourceManagers() {
        // Start serving - messages will start being processed
        resourceManagers.forEach(AbstractResourceManager::start);
        // Starting timers which should actually be resource managers...
        timer.start();

    }

    private void pauseResourceManagers() {
        // Start serving - messages will start being processed
        resourceManagers.forEach(AbstractResourceManager::pause);

        // Pausing timers which should actually be resource managers...
        timer.pause();

    }

    private void resumeResourceManagers() {
        // Start serving - messages will start being processed
        resourceManagers.forEach(AbstractResourceManager::start);

        // resuming timers which should actually be resource managers...
        timer.resume();

    }

    private void stopResourceManagers() {
        // Stop serving
        resourceManagers.forEach(AbstractResourceManager::stop);

        // Stopping timers which should actually be resource managers...
        if (timer != null) {
            timer.stop();
        }

    }

    public Context getContext() {
        return context;
    }

    private ContextImpl buildContext() {
        List<ResourceManager> rm = new ArrayList<>(MicroServiceController.this.resourceManagers.size());
        rm.addAll(MicroServiceController.this.resourceManagers);
        List<ResourceManager> ext = new ArrayList<>(externalResourceManagers.size());
        ext.addAll(this.externalResourceManagers);

        return new ContextImpl(
                this,
                params,
                resourceProvider,
                metricRegistry,
                destinationManager,
                queuesManager,
                datasourceManager,
                dependencyManager,
                blobStoreManager,
                singletonManager,
                healthCheckManager,
                serializationManager,
                webServer,
                inputDescriptor,
                outputDescriptor,
                logger,
                rm,
                ext,
                restResourceManager,
                dynamicJavaServiceManager,
                schedulerManager) {
            @Override
            public MicroServiceState getServiceState() {
                return getState();
            }
        };
    }

    private void setParameters(Map<String, String> serviceParameterValues) {

        // Overriding default parameters
        params.setParameterValues(serviceParameterValues, serviceDescriptor);

        // Validate parameters before starting
        serviceDescriptor.validateParameters(params);

        // Printing parameter values in debug
        if (logger.isDebugEnabled()) {
            logger.debug(getName() + " Parameters:\n" + params.formatParams("\n"));
        }
    }

    /***
     * Resume a service after being paused
     */
    public synchronized void resume() {
        switch (state) {
            case PAUSED:
                resumeResourceManagers();
                state = MicroServiceState.RUNNING;
                break;
            default:
                break;
        }
    }

    /***
     * Pause the service
     */
    public synchronized boolean pause() {
        if (state != MicroServiceState.RUNNING) {
            return false;
        }
        // Stopping queue manager
        try {
            pauseResourceManagers();
            return true;
        } finally {
            this.state = MicroServiceState.PAUSED;
            // Informing service is down
            logger.info("{} Micro Service Version {} Paused", getName(), getVersion());
        }

    }

    /***
     * Stop the service
     */
    public synchronized void stop() {

        // Stopping queue manager
        try {
            stopResourceManagers();
            healthCheckManager.shutDown();
            if (this.webServer != null) {
                this.webServer.unDeployServiceApplication(context);
            }
        } catch (Exception ex) {
            logger.info("Error while stopping " + getName() + " MicroService", ex);
        } finally {
            state = MicroServiceState.STOPPED;
            // Informing service is down
            logger.info("{} Micro Service Version {} Stopped", getName(), getVersion());
        }
    }

    public ParametersBag getParams() {
        return params;
    }

    public Logger getLogger() {
        return logger;
    }

    public final QueuesManager getQueuesManager() {
        return queuesManager;
    }

    public BlobStoreManager getBlobStoreManager() {
        return blobStoreManager;
    }

    public final MessageDestinationManager getDestinationManager() {
        return destinationManager;
    }

    public final DatasourceManager getDatasourceManager() {
        return datasourceManager;
    }

    public final ServiceDependencyManager getDependencyManager() {
        return dependencyManager;
    }

    public final RestResourceManager getRestResourceManager() {
        return restResourceManager;
    }

    public List<ExternalResourceManagerWrapper> getExternalResourceManagers() {
        return externalResourceManagers;
    }

    public static String getStandardInputQueueName(String serviceBaseURI) {
        return serviceBaseURI + ".queues.input";
    }

    public static String getDependencyCallbackQueueName(String serviceURI, String dependentServiceURI) {
        return serviceURI + ".queues.dependency-callback." + dependentServiceURI;
    }

    public SingletonManager getSingletonManager() {
        return singletonManager;
    }

    public DynamicJavaServiceManager getDynamicJavaServiceManager() {
        return dynamicJavaServiceManager;
    }

    public SchedulerManager getSchedulerManager() {
        return schedulerManager;
    }

    public MetricsRegistryImpl getMetricRegistry() {
        return metricRegistry;
    }

    public String toString() {
        return this.getName() + "@" + super.toString();
    }
}
