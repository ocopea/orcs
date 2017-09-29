// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2015-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.metrics;

import java.util.SortedMap;

/**
 * Created by liebea on 4/19/15.
 * Drink responsibly
 */
public interface TimerMetric {
    StopWatch getStopWatch();

    StopWatch getStopWatch(MetricState metricState);

    String getMetricName();

    SortedMap<Long, MetricState> getSlowest();
}
