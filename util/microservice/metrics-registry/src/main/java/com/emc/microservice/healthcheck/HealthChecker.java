// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id$
 *
 * This computer code is copyright 2014 EMC Corporation
 * All rights reserved
 */
package com.emc.microservice.healthcheck;

import com.emc.microservice.health.HealthCheckResult;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * @author shresa
 */
public class HealthChecker {
    private final String name;
    private final Callable<HealthCheckResult> checker;

    public HealthChecker(String name, Callable<HealthCheckResult> checker) {
        Objects.requireNonNull(name, "Health checker name can't be empty.");
        Objects.requireNonNull(checker, "Health checker can't be empty.");
        this.name = name;
        this.checker = checker;
    }

    public String getName() {
        return name;
    }

    public Callable<HealthCheckResult> getChecker() {
        return checker;
    }

    @Override
    public String toString() {
        return "HealthChecker{" + "name=" + name + ", checker=" + checker + '}';
    }
}
