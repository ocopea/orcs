// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.jmsl.dev;

import com.emc.dpa.timer.Timer;
import com.emc.microservice.metrics.MetricsRegistry;
import com.emc.ocopea.microservice.schedule.SchedulerApi;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RemoteDevScheduler implements SchedulerApi {
    private final Timer timer;
    private Map<String, Function<String, Boolean>> recurrentTaskFunctionMapping = new HashMap<>();

    public RemoteDevScheduler(String name, MetricsRegistry metricsRegistry) {
        this.timer = new Timer(name, metricsRegistry);
    }

    @Override
    public void scheduleRecurring(
            String name,
            int intervalInSeconds,
            String payload,
            String functionIdentifier) {
        Function<String, Boolean> taskFunction = recurrentTaskFunctionMapping.get(functionIdentifier);
        timer.add(
                name,
                () -> {
                    boolean continueRunning = true;
                    try {
                        continueRunning = taskFunction.apply(payload);
                    } catch (Exception e) {
                        System.out.println("failed executing recurring task. name=" +
                                name + " payload=" + payload);
                    }
                    if (!continueRunning) {
                        timer.remove(name);
                    }
                },
                intervalInSeconds);
    }

    @Override
    public void start() {
        this.timer.start();
    }

    @Override
    public void stop() {
        this.timer.stop();
    }

    @Override
    public void registerRecurringTask(
            String recurringTaskIdentifier, Function<String, Boolean> recurringTaskFunction) {
        recurrentTaskFunctionMapping.put(recurringTaskIdentifier, recurringTaskFunction);
    }
}
