// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2015 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.metrics.reporter.influxdb;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.emc.microservice.metrics.DPAMetricDescriptor;
import com.emc.microservice.metrics.reporter.IDpaMetricDescriptorLookup;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

/**
 * @author nivenb
 */
public class InfluxDBReporterTest {

    private static final Logger log = LoggerFactory.getLogger(InfluxDBReporterTest.class);

    @Test
    public void test() throws Exception {
        MetricRegistry registry = new MetricRegistry();

        IDpaMetricDescriptorLookup dpaMetricDescriptorLookup = new IDpaMetricDescriptorLookup() {
            @Override
            public DPAMetricDescriptor getMetricDescriptor(String metricName) {
                return new DPAMetricDescriptor(metricName, Collections.<String, String>emptyMap());
            }
        };

        TestInfluxDBUdpClient testInfluxDBUDPClient = new TestInfluxDBUdpClient(InetAddress.getLocalHost(), 1234);
        InfluxDBReporter reporter = InfluxDBReporter.forRegistry(registry)
                .withTag("host", "myhost")
                .withTag("myothertag", "myothervalue")
                .withDPAMetricDescriptorLookup(dpaMetricDescriptorLookup)
                .build(testInfluxDBUDPClient);
        Timer timer = registry.timer("timer");
        Timer.Context context = timer.time();
        Thread.sleep(5);
        context.stop();
        reporter.report();

        log.debug("testInfluxDBUDPClient.metrics={}", testInfluxDBUDPClient.metrics);
        Assert.assertEquals(15, testInfluxDBUDPClient.metrics.size());
    }

    static class TestInfluxDBUdpClient extends InfluxDBUdpClient {

        Collection<String> metrics = new HashSet<>();
        InetAddress address;
        int port;

        @Override
        InfluxDBUdpClientConnection connect() throws IOException {
            return new MockInfluxDBUdpClientConnection(address, port, metrics);
        }

        public TestInfluxDBUdpClient(InetAddress address, int port) {
            super(address, port);
            this.address = address;
            this.port = port;
        }
    }

    private static class MockInfluxDBUdpClientConnection extends InfluxDBUdpClientConnection {

        private Collection<String> metrics;

        public MockInfluxDBUdpClientConnection(InetAddress address, int port, Collection<String> metrics)
                throws IOException {
            super(address, port);
            this.metrics = metrics;
        }

        @Override
        void send(String metric, Map<String, String> tags, String value, long timestampInMillis) throws IOException {
            log.debug("metric={}, value={}, timestamp={}", metric, value, timestampInMillis);
            metrics.add(metric);
        }
    }
}
