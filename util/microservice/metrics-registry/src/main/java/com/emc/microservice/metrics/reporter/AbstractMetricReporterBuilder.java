// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2015 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.metrics.reporter;

import com.codahale.metrics.Clock;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;

import java.util.concurrent.TimeUnit;

/**
 * @author nivenb
 */
public abstract class AbstractMetricReporterBuilder<T extends AbstractMetricReporterBuilder> {

    private final MetricRegistry registry;
    private Clock clock;
    private String prefix;
    private TimeUnit rateUnit;
    private TimeUnit durationUnit;
    private MetricFilter filter;

    protected AbstractMetricReporterBuilder(MetricRegistry registry) {
        this.registry = registry;
        this.clock = Clock.defaultClock();
        this.prefix = null;
        this.rateUnit = TimeUnit.MINUTES;
        this.durationUnit = TimeUnit.MILLISECONDS;
        this.filter = MetricFilter.ALL;
    }

    public T withClock(Clock clock) {
        this.clock = clock;
        return (T) this;
    }

    public T prefixedWith(String prefix) {
        this.prefix = prefix;
        return (T) this;
    }

    public T convertRatesTo(TimeUnit rateUnit) {
        this.rateUnit = rateUnit;
        return (T) this;
    }

    public T convertDurationsTo(TimeUnit durationUnit) {
        this.durationUnit = durationUnit;
        return (T) this;
    }

    public T filter(MetricFilter filter) {
        this.filter = filter;
        return (T) this;
    }

    public MetricRegistry getRegistry() {
        return registry;
    }

    public Clock getClock() {
        return clock;
    }

    public String getPrefix() {
        return prefix;
    }

    public TimeUnit getRateUnit() {
        return rateUnit;
    }

    public TimeUnit getDurationUnit() {
        return durationUnit;
    }

    public MetricFilter getFilter() {
        return filter;
    }

}
