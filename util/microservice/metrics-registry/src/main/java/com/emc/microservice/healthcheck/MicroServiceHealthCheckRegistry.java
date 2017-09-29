// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* MicroServiceHealthCheckRegistry
 *
 * This computer code is copyright 2014 EMC Corporation
 * All rights reserved
 */
package com.emc.microservice.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.emc.microservice.health.HealthCheckResult;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Health Check Registry
 * <p>
 * If checker will be called periodically. If checker returns false for health
 * check, it will call the error handler. If error handler is null, nothing
 * will be done.
 * <p>
 * If health check had previously failed and all health checks are now passing
 * registry will call resume handler if it is not null.
 *
 * @author shresa
 */
public class MicroServiceHealthCheckRegistry {
    public static final String HEALTH_CHECK_PERIOD_PARAMETER_NAME = "health-check-period-in-seconds";
    public static final int DEFAULT_PERIOD = 60; // every minute
    private final HealthCheckRegistry registry = new HealthCheckRegistry();
    private final String registryName;
    private final Logger logger;

    private ScheduledExecutorService executor = null;
    private Runnable errorHandler;
    private Runnable resumeHandler;

    private boolean lastAllHealthy = true;

    public MicroServiceHealthCheckRegistry(String registryName, Logger logger) {
        this.registryName = registryName;
        this.logger = LoggerFactory.getLogger(logger.getName() + "." + this.getClass().getSimpleName());
    }

    public String getRegistryName() {
        return registryName;
    }

    @NoJavadoc
    public void init(int period) {
        if (period <= 0) {
            logger.warn("Health check period is not valid {}. Setting to default value {}", period, DEFAULT_PERIOD);
            period = DEFAULT_PERIOD;
        }

        //todo:Ashish- shouldn't we use the new timers library we have?
        executor = Executors.newScheduledThreadPool(1, new CheckThreadFactory());
        executor.scheduleAtFixedRate(() -> runAllChecks(), period, period, TimeUnit.SECONDS);
    }

    public void shutDown() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    public void addCheck(HealthChecker healthChecker) {
        String name = healthChecker.getName();
        logger.info("Initializing health check {}", name);
        registry.register(name, new HealthCheckWrapper(healthChecker));
    }

    /***
     * Execute check
     */
    public synchronized void runCheck(String name) {
        HealthCheck.Result result = registry.runHealthCheck(name);
        if (!result.isHealthy() && errorHandler != null) {
            logger.warn("Health check \"{}\" failed for {}. calling error handler.", name, registryName);
            errorHandler.run();
            lastAllHealthy = false;
        }
    }

    /***
     * This method is marking the system as unhealthy until next health check executions run
     * The purpose of this is to allow all health checks to run before we continue running if we suspect there is
     * a failure
     * @param message message to print
     */
    public synchronized void markAsUnhealthyAndRunAllTests(String message) {
        if (errorHandler != null) {
            logger.warn("Health checks marked as failed due to {}", message);
            errorHandler.run();
            lastAllHealthy = false;
        }

        executor.schedule(() -> {
            runAllChecks();
            return null;
        }, 0L, TimeUnit.MILLISECONDS);
    }

    /***
     * Run all checks
     */
    public synchronized void runAllChecks() {
        boolean allHealthy = true;
        Map<String, HealthCheck.Result> results = registry.runHealthChecks();
        for (HealthCheck.Result result : results.values()) {
            allHealthy &= result.isHealthy();
        }

        if (!allHealthy && errorHandler != null) {
            logger.warn("Health checks failed. calling error handler for {}", registryName);
            errorHandler.run();
        }

        if (allHealthy && !lastAllHealthy && resumeHandler != null) {
            logger.info("Health check all passed since last failure. Calling resume handler for {}", registryName);
            resumeHandler.run();
        }

        lastAllHealthy = allHealthy;
    }

    public synchronized void registerErrorHandler(Runnable errorHandler) {
        this.errorHandler = errorHandler;
    }

    public synchronized void registerResumeHandler(Runnable resumeHandler) {
        this.resumeHandler = resumeHandler;
    }

    private class HealthCheckWrapper extends HealthCheck {

        private final HealthChecker healthChecker;

        HealthCheckWrapper(HealthChecker checker) {
            this.healthChecker = checker;
        }

        @Override
        protected Result check() throws Exception {
            Callable<HealthCheckResult> checker = healthChecker.getChecker();
            HealthCheckResult result = checker.call();
            if (result.isPass()) {
                return Result.healthy();
            }

            String message = "Health check for " + healthChecker.getName()
                    + " failed with message: " + result.getMessage();
            logger.warn(message);
            Throwable throwable = result.getException();
            if (logger.isDebugEnabled() && throwable != null) {
                logger.debug(message, throwable);
            }
            return Result.unhealthy(message);
        }

    }

    private static class CheckThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            Thread t = new Thread(runnable, "dpa-health-check-" + threadNumber.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }
}
