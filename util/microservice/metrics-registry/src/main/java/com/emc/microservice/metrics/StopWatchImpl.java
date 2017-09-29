// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* StopWatch.java
 * 
 * This computer code is copyright 2014 EMC Corporation
 * All rights reserved
 */

package com.emc.microservice.metrics;

import static com.codahale.metrics.Timer.Context;

/**
 * This exposes Timer.Context as StopWatch. Timer.Context is already AutoCloseable
 * we are only giving it a nice name
 *
 * @author shresa
 */
public class StopWatchImpl implements StopWatch {

    private final Context context;
    private final TimerMetricImpl timerMetric;

    StopWatchImpl(TimerMetricImpl timerMetric, Context context) {
        this(timerMetric, context, MetricState.EMPTY);
    }

    StopWatchImpl(TimerMetricImpl timerMetric, Context context, MetricState metricState) {
        this.timerMetric = timerMetric;
        this.context = context;
    }

    @Override
    public void close() {
        stop();
    }

    @Override
    public void stop() {
        context.stop();
    }
}
