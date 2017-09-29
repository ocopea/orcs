// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: TimerMetric.java 90398 2014-09-21 10:28:46Z liebea $
 *
 * This computer code is copyright 2014 EMC Corporation
 * All rights reserved
 */
package com.emc.microservice.metrics;

import com.codahale.metrics.Timer;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created with love by liebea on 5/18/14.
 */
public class TimerMetricImpl implements TimerMetric {
    private final String metricName;
    private final Timer timer;
    private final SortedMap<Long, MetricState> slowest = new TreeMap<>();

    public TimerMetricImpl(String metricName, Timer timer) {
        this.metricName = metricName;
        this.timer = timer;
    }

    @Override
    public StopWatchImpl getStopWatch() {
        return new StopWatchImpl(this, timer.time());
    }

    @Override
    public StopWatchImpl getStopWatch(MetricState metricState) {
        if (metricState.getStateValues().isEmpty()) {
            return getStopWatch();
        } else {
            return new StopWatchImpl(this, timer.time(), metricState);
        }
    }

    @Override
    public String getMetricName() {
        return metricName;
    }

    @Override
    public SortedMap<Long, MetricState> getSlowest() {
        return slowest;
    }
}
