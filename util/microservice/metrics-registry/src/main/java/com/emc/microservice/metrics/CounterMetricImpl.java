// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id$
 * 
 * This compupter code is copyright 2014 EMC Corporation
 * All rights reserved
 */

package com.emc.microservice.metrics;

import com.codahale.metrics.Counter;

/**
 * Metric type to enable counting events or objects. Can be incremented or
 * decremented.
 * <p>
 * This can be used with automatic resource management to automatically decrement
 * counts.
 * <p>
 * For example:
 * <code>
 * try (CounterMetric counter = registry.getCounter("activeJobs")) {
 * counter.inc();
 * Job job = jobQueue.pop();
 * job.run();
 * }
 * </code>
 * Will automatically decrement count when done.
 * <p>
 * This wraps the counter from Codahale metric library to provide an abstraction.
 *
 * @author shresa
 */
public class CounterMetricImpl implements CounterMetric {
    private final Counter counter;

    public CounterMetricImpl(Counter counter) {
        this.counter = counter;
    }

    public CounterMetricImpl inc() {
        counter.inc();
        return this;
    }

    public CounterMetricImpl dec() {
        counter.dec();
        return this;
    }

    @Override
    public void close() {
        dec();
    }
}
