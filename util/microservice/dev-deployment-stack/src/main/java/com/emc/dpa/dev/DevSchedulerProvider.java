// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.dpa.dev;

import com.emc.microservice.Context;
import com.emc.microservice.metrics.MetricsRegistryImpl;
import com.emc.microservice.schedule.SchedulerConfiguration;
import com.emc.microservice.schedule.SchedulerProvider;
import com.emc.ocopea.microservice.schedule.SchedulerApi;

/**
 * Implementation of dev mode scheduler
 */
public class DevSchedulerProvider implements SchedulerProvider<SchedulerConfiguration> {

    @Override
    public SchedulerApi getScheduler(
            SchedulerConfiguration schedulerConfiguration,
            Context context) {
        return new DevScheduler(schedulerConfiguration.getName(), context != null ? context.getMetricsRegistry() :
                new MetricsRegistryImpl("bob"));
    }

    @Override
    public Class<SchedulerConfiguration> getConfClass() {
        return SchedulerConfiguration.class;
    }
}
