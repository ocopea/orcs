// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.metrics;

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
public interface CounterMetric extends AutoCloseable {
    CounterMetric inc();

    CounterMetric dec();

    @Override
    void close();
}
