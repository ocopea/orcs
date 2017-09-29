// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id$
 *
 * This computer code is copyright 2015 EMC Corporation
 * All rights reserved
 */
package com.emc.microservice.metrics.reporter.influxdb;

import com.emc.microservice.metrics.MetricsRegistryImpl;
import com.emc.microservice.metrics.StopWatch;
import com.emc.microservice.metrics.TimerMetric;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This is not a Unit test. This class seeks to verify that when ran manually, it works in a specific InfluxDB
 * installation
 * <p>
 * Created by nivenb on 19/11/2015.
 */
public class DPAInfluxDBTest {

    public static void main(String[] args) throws Exception {

        System.out.println("Setting up metrics");
        MetricsRegistryImpl registry = new MetricsRegistryImpl("dpa_test");
        Map<String, String> params = new HashMap<>();
        params.put(MetricsRegistryImpl.PARAM_INFLUXDB_HOST, "dpavqlxaxhixh.datadomain.com");
        params.put(MetricsRegistryImpl.PARAM_INFLUXDB_UDP_PORT, "4045");
        params.put(MetricsRegistryImpl.PARAM_INFLUXDB_PERIOD_MINUTES, "1");
        params.put(MetricsRegistryImpl.PARAM_INFLUXDB_JVM_TAGS, "app.instance=1");
        registry.init(params);

        System.out.println("Adding test metric");
        TimerMetric timerMetric = registry.getTimerMetric("test.timer", DPAInfluxDBTest.class);
        try (StopWatch stopWatch = timerMetric.getStopWatch()) {
            Thread.sleep(100L);
        }
        System.out.println("Test metric finished. Waiting for 2 minutes");
        TimeUnit.MINUTES.sleep(2); // wait for reporter to kick in
    }
}
