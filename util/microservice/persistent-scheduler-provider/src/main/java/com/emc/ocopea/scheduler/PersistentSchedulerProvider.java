// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scheduler;

import com.emc.microservice.Context;
import com.emc.microservice.datasource.DatasourceConfiguration;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.microservice.resource.ResourceProviderManager;
import com.emc.microservice.schedule.SchedulerProvider;
import com.emc.ocopea.microservice.schedule.SchedulerApi;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PersistentSchedulerProvider implements SchedulerProvider<PersistentSchedulerConfiguration> {

    private final Map<String, PersistentScheduler> serviceSchedulers = new HashMap<>();
    private TaskPersister persister;

    private TaskPersister getTaskPersister(PersistentSchedulerConfiguration configuration, String microServiceUri) {
        if (persister != null) {
            return persister;
        }

        if (configuration.isPersistTasks()) {
            String datasourceName = configuration.getDatasourceName();
            if (datasourceName == null) {
                throw new IllegalArgumentException("Persistent scheduler requires a non-null datasource name");
            }
            persister = createPostgresTaskPersister(datasourceName, microServiceUri);
        } else {
            persister = new InMemoryTaskPersister();
        }
        return persister;
    }

    @Override
    public synchronized SchedulerApi getScheduler(PersistentSchedulerConfiguration configuration, Context ctx) {
        PersistentScheduler persistentScheduler = serviceSchedulers
                .computeIfAbsent(
                        ctx.getMicroServiceBaseURI(),
                        s -> new PersistentSchedulerImpl(getTaskPersister(configuration, ctx.getMicroServiceBaseURI()))
                );

        return new SchedulerApi() {
            @Override
            public void start() {
                persistentScheduler.start();
            }

            @Override
            public void stop() {
                persistentScheduler.stop();
            }

            @Override
            public void registerRecurringTask(
                    String recurringTaskIdentifier,
                    Function<String, Boolean> recurringTaskFunction) {
                persistentScheduler.registerRecurringTask(recurringTaskIdentifier, recurringTaskFunction);
            }

            @Override
            public void scheduleRecurring(
                    String name,
                    int intervalInSeconds,
                    String payload,
                    String functionIdentifier) {
                persistentScheduler.scheduleRecurring(name, intervalInSeconds, payload, functionIdentifier);
            }
        };
    }

    @Override
    public Class<PersistentSchedulerConfiguration> getConfClass() {
        return PersistentSchedulerConfiguration.class;
    }

    private PostgresTaskPersister createPostgresTaskPersister(String datasourceName, String microServiceUri) {
        final ResourceProvider rp = ResourceProviderManager.getResourceProvider();
        final DatasourceConfiguration dataSourceConfiguration = rp
                .getServiceRegistryApi()
                .getDataSourceConfiguration(rp.getDatasourceConfigurationClass(), datasourceName);

        final DataSource dataSource = rp.getDataSource(dataSourceConfiguration);
        if (dataSource == null) {
            throw new IllegalStateException("Failed creating persistent scheduler task persister. dataSource " +
                    datasourceName + " does not exist");
        }

        return new PostgresTaskPersister(dataSource, dataSourceConfiguration.getDatabaseSchema(), microServiceUri);
    }
}
