// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.health;

/**
 * @author shresa
 */
public class HealthCheckResult {

    private final boolean pass;
    private final String message;
    private final Throwable exception;

    public HealthCheckResult(boolean pass) {
        this(pass, "OK", null);
    }

    public HealthCheckResult(boolean pass, String message) {
        this(pass, message, null);
    }

    public HealthCheckResult(boolean pass, String message, Throwable exception) {
        this.pass = pass;
        this.message = message;
        this.exception = exception;
    }

    public boolean isPass() {
        return pass;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getException() {
        return exception;
    }

    public static HealthCheckResult healthy() {
        return OK;
    }

    private static final HealthCheckResult OK = new HealthCheckResult(true);
}
