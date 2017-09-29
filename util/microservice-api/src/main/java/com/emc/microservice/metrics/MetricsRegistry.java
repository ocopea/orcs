// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2015-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.metrics;

import java.util.List;
import java.util.Map;

/**
 * Created by liebea on 4/19/15.
 * Drink responsibly
 */
public interface MetricsRegistry {

    <T> StaticObjectMetric<T> getStaticObjectMetric(Class<T> clazz);

    String getRegistryName();

    TimerMetric getTimerMetric(String metricName, Class ownerClass);

    TimerMetric getTimerMetric(String metricName, Map<String, String> metricTags, Class ownerClass);

    CounterMetric getCounter(String metricName, Class ownerClass);

    CounterMetric getCounter(String metricName, Map<String, String> metricTags, Class ownerClass);

    List<TimerMetric> getMetrics();
}
