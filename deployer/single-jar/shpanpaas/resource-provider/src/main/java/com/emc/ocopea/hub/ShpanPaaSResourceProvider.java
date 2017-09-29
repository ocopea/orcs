// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id$
 *
 * This computer code is copyright 2014 EMC Corporation
 * All rights reserved
 */

package com.emc.ocopea.hub;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import com.emc.dpa.dev.DevBlobStoreConfiguration;
import com.emc.dpa.dev.DevModeHelper;
import com.emc.dpa.dev.DevResourceProvider;
import com.emc.dpa.dev.H2DatasourceConfiguration;
import com.emc.dpa.dev.registry.DevModeConfigurationImpl;
import com.emc.microservice.Context;
import com.emc.microservice.ContextThreadLocal;
import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceController;
import com.emc.microservice.MicroserviceIdentifier;
import com.emc.microservice.ParametersBag;
import com.emc.microservice.ServiceConfig;
import com.emc.microservice.blobstore.BlobStoreConfiguration;
import com.emc.microservice.blobstore.BlobStoreDescriptor;
import com.emc.microservice.bootstrap.AbstractSchemaBootstrap;
import com.emc.microservice.bootstrap.SchemaBootstrapRunner;
import com.emc.microservice.config.ConfigurationAPI;
import com.emc.microservice.datasource.DatasourceConfiguration;
import com.emc.microservice.datasource.DatasourceDescriptor;
import com.emc.microservice.dependency.ServiceDependencyDescriptor;
import com.emc.microservice.dependency.ServiceDependencyType;
import com.emc.microservice.messaging.DestinationDescriptor;
import com.emc.microservice.messaging.InputQueueDescriptor;
import com.emc.microservice.messaging.MessagingProvider;
import com.emc.microservice.messaging.MessagingProviderConfiguration;
import com.emc.microservice.messaging.QueueConfiguration;
import com.emc.microservice.registry.ServiceRegistryApi;
import com.emc.microservice.registry.ServiceRegistryImpl;
import com.emc.microservice.resource.ResourceConfiguration;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.microservice.runner.MicroServiceRunner;
import com.emc.microservice.schedule.SchedulerProvider;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.hub.application.AppServiceDependency;
import com.emc.ocopea.hub.application.ShpanPaaSAppTemplate;
import com.emc.ocopea.messaging.PersistentMessagingConfiguration;
import com.emc.ocopea.messaging.PersistentMessagingProvider;
import com.emc.ocopea.messaging.PersistentQueueConfiguration;
import com.emc.ocopea.psb.PSBLogMessageDTO;
import com.emc.ocopea.scheduler.PersistentSchedulerConfiguration;
import com.emc.ocopea.scheduler.PersistentSchedulerProvider;
import com.emc.ocopea.util.PostgresUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import javax.websocket.Session;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author shpandrak
 */
public class ShpanPaaSResourceProvider extends DevResourceProvider {

    private static final Logger log = LoggerFactory.getLogger(ShpanPaaSResourceProvider.class);
    private static final Map<String, String> baseURIShortNameTypeAndNameToName = new HashMap<>();
    private static final Map<String, Session> shpanPaasBaseUriToSession = new HashMap<>();

    public ShpanPaaSResourceProvider() throws IOException, SQLException {
        this(Collections.<String, AbstractSchemaBootstrap>emptyMap());
    }

    public ShpanPaaSResourceProvider(Map<String, AbstractSchemaBootstrap> schemaBootstrapMap)
            throws IOException, SQLException {

        this(schemaBootstrapMap, new DevModeConfigurationImpl());
    }

    @Override
    protected MessagingProvider createMessagingProvider() {
        return new PersistentMessagingProvider();
    }

    @Override
    protected SchedulerProvider createSchedulerProvider() {
        return new PersistentSchedulerProvider();
    }

    public ShpanPaaSResourceProvider(
            Map<String, AbstractSchemaBootstrap> schemaBootstrapMap,
            ConfigurationAPI configAPIImpl) throws IOException, SQLException {
        super(schemaBootstrapMap, configAPIImpl, new ShpanPaaSRegistry(configAPIImpl));

        // Registering messaging configuration
        getServiceRegistryApi().registerMessaging(
                MessagingProviderConfiguration.DEFAULT_MESSAGING_SYSTEM_NAME,
                new PersistentMessagingConfiguration(null, false));

        // Registering scheduler configuration
        getServiceRegistryApi().registerScheduler(
                "default",
                new PersistentSchedulerConfiguration(null, false)
        );

        ch.qos.logback.classic.Logger logger =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        final Appender<ILoggingEvent> appender = new AppenderBase<ILoggingEvent>() {

            @Override
            protected void append(ILoggingEvent eventObject) {
                final Context context = ContextThreadLocal.getContext();
                if (context != null) {
                    final ParametersBag.MicroServiceParameterDescriptor uri =
                            context.getParametersBag().getParameterDescriptorsMap().get("shpanPaaSBaseURI");
                    if (uri != null) {
                        final Session session = shpanPaasBaseUriToSession.get(uri.getDefaultValue());
                        if (session != null) {
                            try {
                                final String messageStr = new ObjectMapper().writeValueAsString(new PSBLogMessageDTO(
                                        eventObject.getFormattedMessage(),
                                        eventObject.getTimeStamp(),
                                        PSBLogMessageDTO.MessageType.out,
                                        uri.getDefaultValue()
                                ));

                                session.getBasicRemote().sendText(messageStr);
                            } catch (IOException e) {
                                logger.warn("well, who cares", e);
                            }
                        }
                    }
                }
            }
        };
        appender.setContext(logger.getLoggerContext());
        appender.setName("shpanAppender");
        appender.start();
        logger.addAppender(appender);

    }

    static String extractPaasBaseURI(Context context) {
        ParametersBag.MicroServiceParameterDescriptor prm = context == null ?
                null : context.getParametersBag().getParameterDescriptorsMap().get("shpanPaaSBaseURI");
        return prm == null ? null : prm.getDefaultValue();
    }

    private static Map<String, String> overrideResourceProperties(
            String type, String name,
            Map<String, String> propertyValues,
            Map<String, Map<String, Map<String, String>>> overriddenResourceProperties) {

        Map<String, String> props = new HashMap<>(propertyValues);
        Map<String, Map<String, String>> byType = overriddenResourceProperties.get(type);
        if (byType != null) {
            Map<String, String> byName = byType.get(name);
            if (byName != null) {
                props.putAll(byName);
            }
        }
        return props;

    }

    private static void addQueueProps(Map<String, Map<String, String>> messagingConfiguration, String queueURI) {
        Map<String, String> messagingProps = messagingConfiguration.get(queueURI);
        if (messagingProps == null) {
            messagingProps = new HashMap<>();
            messagingConfiguration.put(queueURI, messagingProps);
        }
        messagingProps.putAll(
                new PersistentQueueConfiguration(
                        QueueConfiguration.MessageDestinationType.QUEUE,
                        queueURI,
                        1000,
                        1,
                        1)
                        .getPropertyValues());
    }

    private static void addServiceInputQueue(
            Map<String, Map<String, String>> messagingConfiguration,
            MicroserviceIdentifier serviceIdentifier) {

        String standardInputQueueName = serviceIdentifier.getDefaultInputQueueName();
        addQueueProps(messagingConfiguration, standardInputQueueName);
    }

    public void bindService(
            String serviceShortName,
            String resourceType,
            String logicalResourceName,
            String physicalResourceName,
            String baseURI) {

        baseURIShortNameTypeAndNameToName.put(
                baseURI + "|" + serviceShortName + "|" + resourceType + "|" + logicalResourceName,
                physicalResourceName);
    }

    /**
     * Almost exact duplication
     * of {@link DevModeHelper#registerServiceDependencies(Context, int, Map, ResourceProvider, Map)}
     */
    public void registerServiceDependencies(
            Context context,
            Map<String, AbstractSchemaBootstrap> schemaBootstrapMap,
            Map<String, Map<String, Map<String, String>>> overriddenResourceProperties,
            int port)
            throws IOException, SQLException {

        Map<String, ServiceConfig.InputQueueConfig> inputQueueConfig = new HashMap<>();
        Map<String, ServiceConfig.DestinationQueueConfig> destinationQueueConfig = new HashMap<>();

        Map<String, Map<String, String>> messagingConfiguration = new HashMap<>();

        // Registering input queues as both input and output
        for (InputQueueDescriptor currQueueDesc : context.getQueuesManager().getDescriptors()) {
            addQueueProps(messagingConfiguration, currQueueDesc.getName());
            inputQueueConfig.put(
                    currQueueDesc.getName(),
                    new ServiceConfig.InputQueueConfig(1, true, Collections.emptyList()));
        }

        // Setting destinations configuration
        for (DestinationDescriptor currDestinationDesc : context.getDestinationManager().getDescriptors()) {
            addQueueProps(messagingConfiguration, currDestinationDesc.getName());
            destinationQueueConfig.put(
                    currDestinationDesc.getName(),
                    new ServiceConfig.DestinationQueueConfig(null, null, true));
        }

        // Registering Datasources
        if (!context.getParametersBag().getParameterDescriptorsMap().containsKey("shpanPaaSBaseURI")) {
            for (DatasourceDescriptor currDSDescriptor : context.getDatasourceManager().getDescriptors()) {
                // Loading physical datasource using resource provider instance
                H2DatasourceConfiguration h2DatasourceConfiguration =
                        new H2DatasourceConfiguration(
                                currDSDescriptor.getName(),
                                PostgresUtil.sanitizeIdentifier(currDSDescriptor.getName()));

                DataSource dataSource = this.getDataSource(h2DatasourceConfiguration);

                AbstractSchemaBootstrap schemaBootstrap = Objects.requireNonNull(
                        schemaBootstrapMap.get(currDSDescriptor.getName()),
                        currDSDescriptor.getName() + " Schema bootstrap was not provided");
                SchemaBootstrapRunner.runBootstrap(
                        dataSource,
                        schemaBootstrap,
                        h2DatasourceConfiguration.getDatabaseSchema(),
                        null);

                Map<String, String> propertyValues = overrideResourceProperties(
                        ServiceRegistryApi.SERVICE_TYPE_DATASOURCE,
                        currDSDescriptor.getName(),
                        h2DatasourceConfiguration.getPropertyValues(),
                        overriddenResourceProperties);

                getServiceRegistryApi().registerDataSource(
                        currDSDescriptor.getName(),
                        ResourceConfiguration.asSpecificConfiguration(
                                getDatasourceConfigurationClass(),
                                propertyValues));
            }
        }

        // Registering blobStores
        for (BlobStoreDescriptor currBlobStoreDec : context.getBlobStoreManager().getDescriptors()) {
            Map<String, String> propertyValues = overrideResourceProperties(
                    ServiceRegistryApi.SERVICE_TYPE_BLOBSTORE,
                    currBlobStoreDec.getName(),
                    new DevBlobStoreConfiguration(
                            currBlobStoreDec.getName()).getPropertyValues(),
                    overriddenResourceProperties);

            getServiceRegistryApi().registerBlobStore(
                    currBlobStoreDec.getName(),
                    DevBlobStoreConfiguration.asSpecificConfiguration(DevBlobStoreConfiguration.class, propertyValues));
        }

        // Setting the service dependencies configurations
        for (final ServiceDependencyDescriptor currServiceDependencyDescriptor :
                context.getDependencyManager().getDescriptors()) {
            addServiceInputQueue(
                    messagingConfiguration,
                    currServiceDependencyDescriptor.getDependentServiceIdentifier());

            if (currServiceDependencyDescriptor.getServiceDependencyType() == ServiceDependencyType.ASYNC_CALL) {
                List<String> messageRoutingTable = currServiceDependencyDescriptor.getMessageRoutingTable();
                for (String currDependentServiceURI : messageRoutingTable) {
                    MicroserviceIdentifier serviceIdentifier = new MicroserviceIdentifier(currDependentServiceURI);
                    addServiceInputQueue(messagingConfiguration, serviceIdentifier);
                    destinationQueueConfig.put(
                            serviceIdentifier.getDefaultInputQueueName(),
                            new ServiceConfig.DestinationQueueConfig(null, null, true));
                }

                String asyncCallResultQueue =
                        context.getServiceDescriptor().getIdentifier().getDependencyCallbackQueueName(
                                currServiceDependencyDescriptor.getLastRoute());

                addQueueProps(messagingConfiguration, asyncCallResultQueue);
                inputQueueConfig.put(
                        asyncCallResultQueue,
                        new ServiceConfig.InputQueueConfig(
                                1,
                                true,
                                Collections.emptyList()));
            }

        }

        // Registering queues
        for (Map.Entry<String, Map<String, String>> currMessagingConf : messagingConfiguration.entrySet()) {
            log.info("Bootstrapping messaging descriptor {}", currMessagingConf.getKey());
            Map<String, String> propertyValues = overrideResourceProperties(
                    ServiceRegistryApi.SERVICE_TYPE_QUEUE,
                    currMessagingConf.getKey(),
                    currMessagingConf.getValue(),
                    overriddenResourceProperties);

            getServiceRegistryApi().registerQueue(
                    currMessagingConf.getKey(),
                    PersistentQueueConfiguration.asSpecificConfiguration(
                            PersistentQueueConfiguration.class,
                            propertyValues));
        }

        getServiceRegistryApi().registerServiceConfig(
                context.getMicroServiceBaseURI(),
                ServiceConfig.generateServiceConfig(
                        context.getMicroServiceBaseURI(),
                        null,
                        "http://" + getNodeAddress() + ":" + port + "/" + getServiceREstURI(context),
                        "DEBUG",
                        Collections.emptyMap(),
                        inputQueueConfig,
                        destinationQueueConfig,
                        new HashMap<>(),
                        new HashMap<>(),
                        DevModeHelper.loadServiceParameters(context),
                        new HashMap<>()));
    }

    String getServiceREstURI(Context context) {
        return context.getMicroServiceBaseURI() + "-api";
    }

    @NoJavadoc
    // TODO add javadoc
    public MicroServiceController runApplication(
            ShpanPaaSAppTemplate shpanPaaSAppTemplate,
            Map<String, String> serviceInstances,
            String baseURI,
            Map<String, String> environmentVariables) {

        // Validate dependencies
        for (AppServiceDependency currDependency : shpanPaaSAppTemplate.getDependencies()) {
            if (!serviceInstances.containsKey(currDependency.getName())) {
                throw new IllegalArgumentException(
                        "Missing dependency " + currDependency.getType() + "/" + currDependency.getName() +
                                " for running " + shpanPaaSAppTemplate.getName());
            }
        }

        for (AppServiceDependency currDependency : shpanPaaSAppTemplate.getDependencies()) {
            String serviceNameToBind = serviceInstances.get(currDependency.getName());

            // Registering in service registry
            bindService(
                    shpanPaaSAppTemplate.getMicroServiceDescriptor().getIdentifier().getShortName(),
                    currDependency.getType(),
                    currDependency.getName(),
                    serviceNameToBind,
                    baseURI);
        }

        try {
            MicroService microService = shpanPaaSAppTemplate.getMicroServiceDescriptor().getClass().newInstance();
            microService.getInitializationHelper().withParameter("shpanPaaSBaseURI", "override uri", baseURI);

            environmentVariables.entrySet().forEach(
                    var ->
                            microService.getInitializationHelper().withParameter(
                                    var.getKey(),
                                    var.getKey(),
                                    var.getValue()));

            return new MicroServiceRunner().run(
                    this,
                    microService).values().iterator().next();

        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Failed initializing microservice", e);
        }
    }

    public void addLogAppender(Session session, String appServiceId) {
        shpanPaasBaseUriToSession.put(appServiceId, session);
    }

    @NoJavadoc
    // TODO add javadoc
    public void removeSession(Session session) {
        final Optional<Map.Entry<String, Session>> anyEntry =
                shpanPaasBaseUriToSession
                        .entrySet()
                        .stream()
                        .filter(s -> s.getValue().getId().equals(session.getId()))
                        .findAny();
        if (anyEntry.isPresent()) {
            shpanPaasBaseUriToSession.remove(anyEntry.get().getKey());
        }
    }

    private static class ShpanPaaSRegistry extends ServiceRegistryImpl {

        public ShpanPaaSRegistry(ConfigurationAPI configAPIImpl) {
            super(configAPIImpl);
        }

        @Override
        public <DatasourceConfT extends DatasourceConfiguration> DatasourceConfT getDataSourceConfiguration(
                Class<DatasourceConfT> datasourceConfTClass,
                String dataSourceName) {
            Context c = ContextThreadLocal.getContext();
            String shpanPaaSBaseURI = extractPaasBaseURI(c);
            if (shpanPaaSBaseURI != null && !shpanPaaSBaseURI.isEmpty()) {
                String physicalResource = ShpanPaaSResourceProvider.baseURIShortNameTypeAndNameToName.get(
                        shpanPaaSBaseURI + "|" + c.getMicroServiceBaseURI() + "|" +
                                ServiceRegistryApi.SERVICE_TYPE_DATASOURCE + "|" + dataSourceName);
                if (physicalResource != null) {
                    dataSourceName = physicalResource;
                }
            }
            return super.getDataSourceConfiguration(datasourceConfTClass, dataSourceName);
        }

        @Override
        public <BlobstoreConfT extends BlobStoreConfiguration> BlobstoreConfT getBlobStoreConfiguration(
                Class<BlobstoreConfT> confClass,
                String blobstoreName) {

            Context context = ContextThreadLocal.getContext();
            String shpanPaaSBaseURI = extractPaasBaseURI(context);
            if (shpanPaaSBaseURI != null && !shpanPaaSBaseURI.isEmpty()) {

                String physicalResource = baseURIShortNameTypeAndNameToName.get(
                        shpanPaaSBaseURI + "|" + context.getMicroServiceBaseURI() + "|" +
                                ServiceRegistryApi.SERVICE_TYPE_BLOBSTORE + "|" + blobstoreName);

                if (physicalResource != null) {
                    blobstoreName = physicalResource;
                }
            }
            return super.getBlobStoreConfiguration(confClass, blobstoreName);
        }
    }
}
