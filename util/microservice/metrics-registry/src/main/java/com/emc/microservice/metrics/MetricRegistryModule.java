// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * This computer code is copyright 2014 EMC Corporation
 * All rights reserved
 */
package com.emc.microservice.metrics;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleSerializers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by liebea on 9/17/2014. Enjoy it
 */
public class MetricRegistryModule extends Module {
    @Override
    public String getModuleName() {
        return MetricRegistryModule.class.getSimpleName();
    }

    @Override
    public Version version() {
        return new Version(1, 0, 0, null, "com.emc.dpa", "micro-service-metrics");
    }

    @Override
    public void setupModule(SetupContext setupContext) {
        setupContext.addSerializers(new SimpleSerializers(Arrays.asList(
                new MicroServiceMetricRegistrySerializer()
        )));
    }

    private static class MicroServiceMetricRegistrySerializer extends JsonSerializer<MetricsRegistryImpl> {

        @Override
        public void serialize(
                MetricsRegistryImpl metricsRegistry,
                JsonGenerator generator,
                SerializerProvider serializerProvider) throws IOException {
            generator.writeStartObject();
            generator.writeStringField("name", metricsRegistry.getRegistryName());
            generator.writeObjectField("metrics", metricsRegistry.getRegistry());
            writeStats(metricsRegistry, generator);

            generator.writeEndObject();

        }

        private void writeStats(MetricsRegistry metricsRegistry, JsonGenerator generator) throws IOException {
            List<TimerMetric> metrics = metricsRegistry.getMetrics();

            if (!metrics.isEmpty()) {
                generator.writeObjectFieldStart("stats");
                try {
                    for (TimerMetric currMetric : metrics) {
                        if (!currMetric.getSlowest().isEmpty()) {
                            generator.writeObjectFieldStart(currMetric.getMetricName() + ".longestExecuting");
                            int idx = 1;
                            for (Map.Entry<Long, MetricState> currEntry : currMetric.getSlowest().entrySet()) {
                                generator.writeObjectFieldStart("longest-" + idx);
                                ++idx;
                                generator.writeNumberField("duration", currEntry.getKey());
                                for (Map.Entry<String, String> currIdentifierField :
                                        currEntry.getValue().getStateValues().entrySet()) {
                                    generator.writeStringField(
                                            currIdentifierField.getKey(),
                                            currIdentifierField.getValue());
                                }
                                generator.writeEndObject();
                            }
                            generator.writeEndObject();

                        }
                    }
                } finally {
                    generator.writeEndObject();
                }
            }
        }

        @Override
        public Class<MetricsRegistryImpl> handledType() {
            return MetricsRegistryImpl.class;
        }
    }
}
