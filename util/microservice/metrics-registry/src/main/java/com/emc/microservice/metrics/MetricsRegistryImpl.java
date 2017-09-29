// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id$
 * 
 * This computer code is copyright 2014 - 2015 EMC Corporation
 * All rights reserved
 */
package com.emc.microservice.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.emc.microservice.metrics.reporter.IDpaMetricDescriptorLookup;
import com.emc.microservice.metrics.reporter.influxdb.InfluxDBJvmTagParser;
import com.emc.microservice.metrics.reporter.influxdb.InfluxDBReporter;
import com.emc.microservice.metrics.reporter.influxdb.InfluxDBUdpClient;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created with love by liebea on 5/18/14.
 */
public class MetricsRegistryImpl implements MetricsRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsRegistryImpl.class);
    private final MetricRegistry metricRegistry = new MetricRegistry();

    private final String registryName;
    private final Map<String, TimerMetric> metricMap = new HashMap<>();
    private final Map<Class, StaticObjectMetric> staticObjectMetricMap = new ConcurrentHashMap<>();

    public static final String PARAM_INFLUXDB_HOST = "dpa.metrics.influxdb.host";

    /**
     * The UDP Port that is configured on the InfluxDB server. Note that the UDP configuration on the
     * server defines the database that socket connections bind to
     */
    public static final String PARAM_INFLUXDB_UDP_PORT = "dpa.metrics.influxdb.udp.port";

    /**
     * The period, in minutes, that we'll collect metrics to report to the InfluxDB instance
     */
    public static final String PARAM_INFLUXDB_PERIOD_MINUTES = "dpa.metrics.influxdb.period.minutes";

    /**
     * JVM-specific tags that will be added to all metrics that are reported to InfluxDB.
     * Tag/Values are separated by '='. Each tag/value pair is delimited with a comma
     * e.g.1 mytag=myvalue
     * e.g.2 mytag=myvalue,mytag2=myvalue2
     * Real world example: application.instance.id.on.my.paas=1
     */
    public static final String PARAM_INFLUXDB_JVM_TAGS = "dpa.metrics.influxdb.jvm.tags";

    public static final String PARAM_MONITOR_VM = "dpa.metrics.monitorvm";

    private static final int INFLUXDB_DEFAULT_PERIOD_MINS = 5;

    private final Map<String, DPAMetricDescriptor> codahaleMetricNamesToDPAMetricDescriptors = new HashMap<>();

    public MetricsRegistryImpl(String registryName) {
        this.registryName = registryName;
    }

    @NoJavadoc
    public void init(Map<String, String> parameters) {

        LOGGER.info("Starting metrics registry. parameters={}", parameters);

        setupVMMetrics(parameters);

        setupLogFileReporter(parameters);
        setupInfluxDBReporter(parameters);
    }

    private void setupVMMetrics(Map<String, String> parameters) {
        if ("true".equalsIgnoreCase(getParamValue(parameters, PARAM_MONITOR_VM))) {
            setupVMMemoryMetrics();
            // TODO (future) other vm metrics as desired
        }
    }

    private void setupVMMemoryMetrics() {
        MemoryUsageGaugeSet memoryMetrics = new MemoryUsageGaugeSet();
        Map<String, Metric> originalMemoryMetricsMap = memoryMetrics.getMetrics();

        // We want to prefix the codahale metrics with 'memory' to make them easily identifiable
        final Map<String, Metric> alteredMemoryMetricsMap = new HashMap<>();
        for (Map.Entry<String, Metric> entry : originalMemoryMetricsMap.entrySet()) {
            alteredMemoryMetricsMap.put("memory." + entry.getKey(), entry.getValue());
        }

        MetricSet metricSet = () -> alteredMemoryMetricsMap;

        metricRegistry.registerAll(metricSet);
    }

    private void setupLogFileReporter(Map<String, String> parameters) {
        if ("true".equalsIgnoreCase(getParamValue(parameters, "dpa.metrics.logfile"))) {
            Slf4jReporter reporter = Slf4jReporter.forRegistry(metricRegistry)
                    .outputTo(LoggerFactory.getLogger(getRegistryName() + ".metrics"))
                    .convertRatesTo(TimeUnit.MINUTES)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build();

            reporter.start(5, TimeUnit.MINUTES);
        }
    }

    private void setupInfluxDBReporter(Map<String, String> parameters) {

        String influxDBHost = getParamValue(parameters, PARAM_INFLUXDB_HOST);
        LOGGER.debug("influxDBHost={}", influxDBHost);

        if (influxDBHost == null || influxDBHost.isEmpty()) {
            return;
        }

        String portStr = getParamValue(parameters, PARAM_INFLUXDB_UDP_PORT);

        if (portStr == null) {
            LOGGER.warn("Unable to setup InfluxDB reporter as no port is specified");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException err) {
            LOGGER.error("Unable to parse InfluxDB port {}", portStr, err);
            return;
        }

        LOGGER.info("Setting up InfluxDB reporter to host {}, udp port {}", influxDBHost, port);

        InfluxDBReporter influxDBReporter;
        try {

            InfluxDBUdpClient influxDBClient = new InfluxDBUdpClient(InetAddress.getByName(influxDBHost), port);

            IDpaMetricDescriptorLookup dpaMetricDescriptorLookup = this::getDPAMetricDescriptor;

            InfluxDBReporter.Builder builder = InfluxDBReporter.forRegistry(metricRegistry)
                    .withTag("host", getHostName())
                    .withTag("registry", getRegistryName())
                    .withDPAMetricDescriptorLookup(dpaMetricDescriptorLookup);

            String jvmSpecificTags = getParamValue(parameters, PARAM_INFLUXDB_JVM_TAGS);
            LOGGER.info("InfluxDB JVM specific tags: {}", jvmSpecificTags);
            if (jvmSpecificTags != null) {
                try {
                    Map<String, String> jvmSpecificTagMap = InfluxDBJvmTagParser.parse(jvmSpecificTags);
                    for (Map.Entry<String, String> entry : jvmSpecificTagMap.entrySet()) {
                        builder = builder.withTag(entry.getKey(), entry.getValue());
                    }
                } catch (IllegalArgumentException iae) {
                    LOGGER.error("Could not add jvm tags in InfluxDBReporter", iae);
                }
            }

            influxDBReporter = builder.build(influxDBClient);

        } catch (UnknownHostException uhe) {
            LOGGER.warn("Unable to setup InfluxDB reporter. Unknown host '{}'", influxDBHost, uhe);
            return;
        }

        int period = INFLUXDB_DEFAULT_PERIOD_MINS;
        try {
            String value = getParamValue(parameters, PARAM_INFLUXDB_PERIOD_MINUTES);
            if (value != null) {
                period = Integer.parseInt(value);
            }
        } catch (NumberFormatException err) {
            LOGGER.debug("Unable to read InfluxDB period. Using default ({} minutes). ", period, err);
        }

        LOGGER.info("InfluxDB reporter will send metrics every {} minutes", period);
        influxDBReporter.start(period, TimeUnit.MINUTES);
    }

    private String getParamValue(Map<String, String> params, String param) {
        if (params.containsKey(param)) {
            return params.get(param);
        }

        // fall back to system property for now
        return System.getProperty(param);
    }

    private String getHostName() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }

    @Override
    public <T> StaticObjectMetric<T> getStaticObjectMetric(Class<T> clazz) {
        //noinspection unchecked
        return staticObjectMetricMap.computeIfAbsent(clazz, c -> new StaticObjectMetric());
    }

    @Override
    public String getRegistryName() {
        return registryName;
    }

    @Override
    public TimerMetric getTimerMetric(String metricName, Class ownerClass) {
        return getTimerMetric(metricName, Collections.emptyMap(), ownerClass);
    }

    @Override
    public TimerMetric getTimerMetric(String metricName, Map<String, String> metricTags, Class ownerClass) {

        // We'll use a name prefix/suffix here for the codahale metric name,
        // so as to distinguish between others in the same jvm
        // However..., we'll want to use the short name for InfluxDB reporting,
        // with the ownerClass and all other existing tags, as tags
        String codahaleMetricName = buildCodahaleMetricName(metricName, metricTags, ownerClass);
        LOGGER.debug("codahaleMetricName={}", codahaleMetricName);

        TimerMetric dpaTimerMetric = metricMap.get(codahaleMetricName);
        if (dpaTimerMetric == null) {
            dpaTimerMetric = new TimerMetricImpl(codahaleMetricName, metricRegistry.timer(codahaleMetricName));
            metricMap.put(codahaleMetricName, dpaTimerMetric);

            String shortName = metricName;
            Map<String, String> metricTagsWithAdditions = new HashMap<>(metricTags);
            if (ownerClass != null) {
                metricTagsWithAdditions.put("ownerClass", ownerClass.getSimpleName());
            }
            DPAMetricDescriptor dpaMetricDescriptor = new DPAMetricDescriptor(shortName, metricTagsWithAdditions);
            addCodahaleMetricNameToDescriptorMapping(codahaleMetricName, dpaMetricDescriptor);

        }
        return dpaTimerMetric;
    }

    private String buildCodahaleMetricName(String metricName, Map<String, String> metricTags, Class ownerClass) {
        String codahaleMetricName = metricName;
        for (String tagValue : metricTags.values()) {
            codahaleMetricName = MetricRegistry.name(codahaleMetricName, tagValue);
        }

        if (ownerClass == null) {
            return codahaleMetricName;
        }

        return MetricRegistry.name(ownerClass.getSimpleName(), codahaleMetricName);
    }

    @Override
    public CounterMetricImpl getCounter(String metricName, Class ownerClass) {
        return getCounter(metricName, Collections.emptyMap(), ownerClass);
    }

    @Override
    public CounterMetricImpl getCounter(String metricName, Map<String, String> metricTags, Class ownerClass) {

        // We'll use a name prefix/suffix here for the codahale metric name, so as to distinguish between others in
        // the same jvm However..., we'll want to use the short name for InfluxDB reporting,
        // with the ownerClass as a tag
        String codahaleMetricName = buildCodahaleMetricName(metricName, metricTags, ownerClass);

        CounterMetricImpl counterMetric = new CounterMetricImpl(metricRegistry.counter(codahaleMetricName));

        String shortName = metricName;
        Map<String, String> metricTagsWithAdditions = new HashMap<>(metricTags);
        if (ownerClass != null) {
            metricTagsWithAdditions.put("ownerClass", ownerClass.getSimpleName());
        }
        DPAMetricDescriptor dpaMetricDescriptor = new DPAMetricDescriptor(shortName, metricTagsWithAdditions);
        addCodahaleMetricNameToDescriptorMapping(codahaleMetricName, dpaMetricDescriptor);

        return counterMetric;
    }

    public MetricRegistry getRegistry() {
        return metricRegistry;
    }

    @Override
    public List<TimerMetric> getMetrics() {
        return new ArrayList<>(metricMap.values());
    }

    public void addCodahaleMetricNameToDescriptorMapping(
            String codahaleMetricName,
            DPAMetricDescriptor dpaMetricDescriptor) {
        codahaleMetricNamesToDPAMetricDescriptors.put(codahaleMetricName, dpaMetricDescriptor);
    }

    @NoJavadoc
    public DPAMetricDescriptor getDPAMetricDescriptor(String codahaleMetricName) {
        DPAMetricDescriptor dpaMetricDescriptor = codahaleMetricNamesToDPAMetricDescriptors.get(codahaleMetricName);
        if (dpaMetricDescriptor == null) {
            // metric name is unknown. This is expected if it is not one of our DPATimer etc metrics. i.e. It may be a
            // jvm metric
            return new DPAMetricDescriptor(codahaleMetricName, Collections.EMPTY_MAP);
        }
        return dpaMetricDescriptor;
    }
}

