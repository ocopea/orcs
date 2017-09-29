// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.messaging;

import com.emc.microservice.Context;
import com.emc.microservice.datasource.DatasourceConfiguration;
import com.emc.microservice.messaging.DestinationConfiguration;
import com.emc.microservice.messaging.InputQueueConfiguration;
import com.emc.microservice.messaging.ManagedMessageListener;
import com.emc.microservice.messaging.MessagingProvider;
import com.emc.microservice.messaging.QueueReceiverImpl;
import com.emc.microservice.messaging.RuntimeMessageSender;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.microservice.resource.ResourceProviderManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liebea on 4/12/17.
 * Drink responsibly
 */
public class PersistentMessagingProvider
        implements MessagingProvider<PersistentQueueConfiguration, PersistentMessagingConfiguration> {

    private Map<String, PersistentMessagingServer> persistentMessagingServerByDatasourceName = new HashMap<>();
    private PersistentMessagingServer inMemoryServer = null;

    @Override
    public RuntimeMessageSender getMessageSender(
            PersistentMessagingConfiguration messageConfiguration,
            DestinationConfiguration destinationConfiguration,
            PersistentQueueConfiguration queueConfiguration,
            Context context) {

        // Creating the queue if it does not yet exist
        createQueue(messageConfiguration, queueConfiguration);

        return (writer, headers, messageGroup) ->
                getMessagingServer(messageConfiguration).sendMessage(
                        queueConfiguration.getQueueName(),
                        writer::writeMessage,
                        headers);
    }

    @Override
    public synchronized void createQueue(
            PersistentMessagingConfiguration messagingConfiguration,
            PersistentQueueConfiguration queueConf) {

        getMessagingServer(messagingConfiguration).createQueue(
                queueConf.getQueueName(),
                queueConf.getMemoryBufferMaxMessages(),
                queueConf.getSecondsToSleepBetweenRetries(),
                queueConf.getMaxRetries());
    }

    @Override
    public QueueReceiverImpl createQueueReceiver(
            PersistentMessagingConfiguration messagingConfiguration,
            InputQueueConfiguration inputQueueConfiguration,
            PersistentQueueConfiguration queueConf,
            Map<String, PersistentQueueConfiguration> deadLetterQueueConfigurations,
            ManagedMessageListener messageListener,
            Context context,
            String consumerName) {

        // Creating the queue if it does not yet exist
        createQueue(messagingConfiguration, queueConf);

        return new PersistentQueueReceiver(
                inputQueueConfiguration,
                queueConf,
                deadLetterQueueConfigurations,
                messageListener,
                context,
                consumerName,
                getMessagingServer(messagingConfiguration));
    }

    @Override
    public Class<PersistentQueueConfiguration> getQueueConfClass() {
        return PersistentQueueConfiguration.class;
    }

    @Override
    public Class<PersistentMessagingConfiguration> getMessageConfClass() {
        return PersistentMessagingConfiguration.class;
    }

    private synchronized PersistentMessagingServer getMessagingServer(
            PersistentMessagingConfiguration messagingConfiguration) {

        // Creating in memory or persistent server
        if (!messagingConfiguration.isPersistMessages()) {
            if (inMemoryServer == null) {
                inMemoryServer = new PersistentMessagingServer(new InMemoryMessagePersister());
            }
            return inMemoryServer;
        } else {
            String datasourceName = messagingConfiguration.getDatasourceName();
            if (datasourceName == null) {
                throw new IllegalStateException("For using persistent messaging datasource must be defined");
            }

            return  persistentMessagingServerByDatasourceName.computeIfAbsent(
                    datasourceName,
                    dsName -> new PersistentMessagingServer(createPostgresMessagePersister(dsName)));
        }
    }

    private PostgresMessagePersister createPostgresMessagePersister(String datasourceName) {
        final ResourceProvider rp = ResourceProviderManager.getResourceProvider();
        final DatasourceConfiguration dataSourceConfiguration = rp
                .getServiceRegistryApi()
                .getDataSourceConfiguration(rp.getDatasourceConfigurationClass(), datasourceName);

        final DataSource dataSource = rp.getDataSource(dataSourceConfiguration);

        if (dataSource == null) {
            throw new IllegalStateException("Failed creating persistent messaging message persister. dataSource " +
                    datasourceName + " does not exist");
        }

        return new PostgresMessagePersister(dataSource, dataSourceConfiguration.getDatabaseSchema());
    }
}
