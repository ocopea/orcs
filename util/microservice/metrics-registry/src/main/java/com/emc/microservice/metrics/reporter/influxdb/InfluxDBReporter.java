// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2015 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.metrics.reporter.influxdb;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.emc.microservice.metrics.DPAMetricDescriptor;
import com.emc.microservice.metrics.reporter.AbstractMetricReporterBuilder;
import com.emc.microservice.metrics.reporter.IDpaMetricDescriptorLookup;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * @author nivenb
 */
public final class InfluxDBReporter extends ScheduledReporter {

    private static final Logger log = LoggerFactory.getLogger(InfluxDBReporter.class);

    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    public static final class Builder extends AbstractMetricReporterBuilder<Builder> {

        private Map<String, String> tags;

        private IDpaMetricDescriptorLookup dpaMetricDescriptorLookup;

        private Builder(MetricRegistry registry) {
            super(registry);
        }

        @NoJavadoc
        public Builder withTag(String tagName, String tagValue) {
            if (tags == null) {
                tags = new HashMap<>();
            }
            log.debug("Adding tag {}={}", tagName, tagValue);
            tags.put(tagName, tagValue);
            return this;
        }

        public Builder withDPAMetricDescriptorLookup(IDpaMetricDescriptorLookup dpaMetricDescriptorLookup) {
            this.dpaMetricDescriptorLookup = dpaMetricDescriptorLookup;
            return this;
        }

        @NoJavadoc
        public InfluxDBReporter build(InfluxDBUdpClient influxDBUdpClient) {
            return new InfluxDBReporter(
                    getRegistry(),
                    influxDBUdpClient,
                    getClock(),
                    tags,
                    getRateUnit(),
                    getDurationUnit(),
                    getFilter(),
                    dpaMetricDescriptorLookup);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxDBReporter.class);

    private final InfluxDBUdpClient influxDBUdpClient;
    private final Clock clock;
    private final Map<String, String> tagsForAllMetrics;
    private IDpaMetricDescriptorLookup dpaMetricDescriptorLookup;

    private InfluxDBReporter(
            MetricRegistry registry,
            InfluxDBUdpClient influxDBUdpClient,
            Clock clock,
            Map<String, String> tagsForAllMetrics,
            TimeUnit rateUnit,
            TimeUnit durationUnit,
            MetricFilter filter,
            IDpaMetricDescriptorLookup dpaMetricDescriptorLookup) {
        super(registry, "influxdb-reporter", filter, rateUnit, durationUnit);
        this.influxDBUdpClient = influxDBUdpClient;
        this.clock = clock;
        this.tagsForAllMetrics = tagsForAllMetrics;
        this.dpaMetricDescriptorLookup = dpaMetricDescriptorLookup;
    }

    @Override
    public void report(
            SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
            SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters,
            SortedMap<String, Timer> timers) {
        final long timestamp = clock.getTime();
        try {
            try (InfluxDBUdpClientConnection connection = influxDBUdpClient.connect()) {
                for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
                    reportGauge(connection, entry.getKey(), entry.getValue(), timestamp);
                }

                for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                    reportCounter(connection, entry.getKey(), entry.getValue(), timestamp);
                }

                for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                    reportHistogram(connection, entry.getKey(), entry.getValue(), timestamp);
                }

                for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                    reportMetered(connection, entry.getKey(), entry.getValue(), timestamp);
                }

                for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                    reportTimer(connection, entry.getKey(), entry.getValue(), timestamp);
                }
            }
        } catch (IOException err) {
            LOGGER.warn("Unable to report metrics to InfluxDB", influxDBUdpClient, err);
        }
    }

    private Map<String, String> buildBothGlobalAndMetricSpecificTags(DPAMetricDescriptor dpaMetricDescriptor) {
        Map<String, String> tags = new HashMap<>(tagsForAllMetrics);
        tags.putAll(dpaMetricDescriptor.getMetricTags());
        return tags;
    }

    private void reportGauge(InfluxDBUdpClientConnection connection, String metricName, Gauge gauge, long timestamp)
            throws IOException {
        final String v = format(gauge.getValue());
        if (v != null) {

            DPAMetricDescriptor dpaMetricDescriptor = dpaMetricDescriptorLookup.getMetricDescriptor(metricName);
            String metricShortName = dpaMetricDescriptor.getMetricShortName();
            Map<String, String> tags = buildBothGlobalAndMetricSpecificTags(dpaMetricDescriptor);

            connection.send(metricShortName, tags, v, timestamp);
        }
    }

    private void reportCounter(
            InfluxDBUdpClientConnection connection,
            String metricName,
            Counter counter,
            long timestamp) throws IOException {

        DPAMetricDescriptor dpaMetricDescriptor = dpaMetricDescriptorLookup.getMetricDescriptor(metricName);
        String metricShortName = dpaMetricDescriptor.getMetricShortName();
        Map<String, String> tags = buildBothGlobalAndMetricSpecificTags(dpaMetricDescriptor);

        connection.send(metricShortName + ".count", tags, format(counter.getCount()), timestamp);
    }

    private void reportHistogram(
            InfluxDBUdpClientConnection connection,
            String metricName,
            Histogram histogram,
            long timestamp) throws IOException {
        final Snapshot snapshot = histogram.getSnapshot();

        DPAMetricDescriptor dpaMetricDescriptor = dpaMetricDescriptorLookup.getMetricDescriptor(metricName);
        String metricShortName = dpaMetricDescriptor.getMetricShortName();
        Map<String, String> tags = buildBothGlobalAndMetricSpecificTags(dpaMetricDescriptor);

        connection.send(metricShortName + ".count", tags, format(histogram.getCount()), timestamp);
        connection.send(metricShortName + ".max", tags, format(snapshot.getMax()), timestamp);
        connection.send(metricShortName + ".mean", tags, format(snapshot.getMin()), timestamp);
        connection.send(metricShortName + ".min", tags, format(snapshot.getMin()), timestamp);
        connection.send(metricShortName + ".stddev", tags, format(snapshot.getStdDev()), timestamp);
        connection.send(metricShortName + ".p50", tags, format(snapshot.getMedian()), timestamp);
        connection.send(metricShortName + ".p75", tags, format(snapshot.get75thPercentile()), timestamp);
        connection.send(metricShortName + ".p95", tags, format(snapshot.get95thPercentile()), timestamp);
        connection.send(metricShortName + ".p98", tags, format(snapshot.get98thPercentile()), timestamp);
        connection.send(metricShortName + ".p99", tags, format(snapshot.get99thPercentile()), timestamp);
        connection.send(metricShortName + ".p999", tags, format(snapshot.get999thPercentile()), timestamp);
    }

    private void reportMetered(InfluxDBUdpClientConnection conn, String metricName, Metered meter, long timestamp)
            throws IOException {

        DPAMetricDescriptor dpaMetricDescriptor = dpaMetricDescriptorLookup.getMetricDescriptor(metricName);
        String metricShortName = dpaMetricDescriptor.getMetricShortName();
        Map<String, String> tags = buildBothGlobalAndMetricSpecificTags(dpaMetricDescriptor);

        conn.send(metricShortName + ".count", tags, format(meter.getCount()), timestamp);
        conn.send(metricShortName + ".m1_rate", tags, format(convertRate(meter.getOneMinuteRate())), timestamp);
        conn.send(metricShortName + ".m5_rate", tags, format(convertRate(meter.getFiveMinuteRate())), timestamp);
        conn.send(metricShortName + ".m15_rate", tags, format(convertRate(meter.getFifteenMinuteRate())), timestamp);
        conn.send(metricShortName + ".mean_rate", tags, format(convertRate(meter.getMeanRate())), timestamp);
    }

    private void reportTimer(InfluxDBUdpClientConnection conn, String metricName, Timer timer, long timestamp)
            throws IOException {
        final Snapshot snapshot = timer.getSnapshot();

        DPAMetricDescriptor dpaMetricDescriptor = dpaMetricDescriptorLookup.getMetricDescriptor(metricName);
        String metricShortName = dpaMetricDescriptor.getMetricShortName();
        Map<String, String> tags = buildBothGlobalAndMetricSpecificTags(dpaMetricDescriptor);

        conn.send(metricShortName + ".max", tags, format(convertDuration(snapshot.getMax())), timestamp);
        conn.send(metricShortName + ".mean", tags, format(convertDuration(snapshot.getMean())), timestamp);
        conn.send(metricShortName + ".min", tags, format(convertDuration(snapshot.getMin())), timestamp);
        conn.send(metricShortName + ".stddev", tags, format(convertDuration(snapshot.getStdDev())), timestamp);
        conn.send(metricShortName + ".p50", tags, format(convertDuration(snapshot.getMedian())), timestamp);
        conn.send(metricShortName + ".p75", tags, format(convertDuration(snapshot.get75thPercentile())), timestamp);
        conn.send(metricShortName + ".p95", tags, format(convertDuration(snapshot.get95thPercentile())), timestamp);
        conn.send(metricShortName + ".p98", tags, format(convertDuration(snapshot.get98thPercentile())), timestamp);
        conn.send(metricShortName + ".p99", tags, format(convertDuration(snapshot.get99thPercentile())), timestamp);
        conn.send(metricShortName + ".p999", tags, format(convertDuration(snapshot.get999thPercentile())), timestamp);

        reportMetered(conn, metricName, timer, timestamp);
    }

    private String format(Object o) {
        if (o instanceof Float || o instanceof Double) {
            return format(((Number) o).doubleValue());
        }
        if (o instanceof Number) {
            return format(((Number) o).longValue());
        }

        return null;
    }

    private String format(long n) {
        return Long.toString(n);
    }

    private String format(double v) {
        return String.format("%f", v);
    }
}
