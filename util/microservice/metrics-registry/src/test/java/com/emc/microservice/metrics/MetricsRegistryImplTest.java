// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id$
 *
 * This computer code is copyright 2015 EMC Corporation
 * All rights reserved
 */
package com.emc.microservice.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by nivenb on 17/12/2015.
 */
public class MetricsRegistryImplTest {

    /**
     * Test that our memory metric names are prefixed with 'memory.' as opposed to the raw codahale-generated default
     * names
     */
    @Test
    public void testMemoryMetricNames() throws Exception {

        MetricsRegistryImpl registry = new MetricsRegistryImpl(getClass().getName());
        Map<String, String> params = new HashMap<>();
        params.put(MetricsRegistryImpl.PARAM_MONITOR_VM, "true");
        registry.init(params);

        Map<String, Metric> rawCodahaleMemoryMetricsMap = new MemoryUsageGaugeSet().getMetrics();
        Set<String> rawCodahaleMemoryMetricNames = rawCodahaleMemoryMetricsMap.keySet();

        Map<String, Metric> ourMetricsMap = registry.getRegistry().getMetrics();
        Set<String> ourMemoryMetricNames = ourMetricsMap.keySet();

        for (String rawCodahaleMemoryMetricName : rawCodahaleMemoryMetricNames) {

            // ensure that no raw memory metric names were added
            Assert.assertFalse(
                    "ourMemoryMetricNames contains raw metric name " + rawCodahaleMemoryMetricName,
                    ourMemoryMetricNames.contains(rawCodahaleMemoryMetricName));

            // Next, ensure that for each raw codahale metric name, we've added one with a 'memory.' prefix
            String expectedMetricName = "memory." + rawCodahaleMemoryMetricName;
            Assert.assertTrue(
                    "ourMemoryMetricNames does not contain " + expectedMetricName,
                    ourMemoryMetricNames.contains(expectedMetricName));
        }

        // Some sanity
        Assert.assertTrue(ourMemoryMetricNames.contains("memory.heap.committed"));
        Assert.assertTrue(ourMemoryMetricNames.contains("memory.heap.init"));
        Assert.assertTrue(ourMemoryMetricNames.contains("memory.heap.max"));
        Assert.assertTrue(ourMemoryMetricNames.contains("memory.heap.used"));
        Assert.assertTrue(ourMemoryMetricNames.contains("memory.total.committed"));
        Assert.assertTrue(ourMemoryMetricNames.contains("memory.total.init"));
        Assert.assertTrue(ourMemoryMetricNames.contains("memory.total.max"));
        Assert.assertTrue(ourMemoryMetricNames.contains("memory.total.used"));
    }

}
