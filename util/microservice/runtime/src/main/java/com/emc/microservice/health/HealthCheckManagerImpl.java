// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id$
 *
 * This computer code is copyright 2014 EMC Corporation
 * All rights reserved
 */
package com.emc.microservice.health;

import com.emc.microservice.Context;
import com.emc.microservice.LoggingHelper;
import com.emc.microservice.MicroServiceController;
import com.emc.microservice.healthcheck.HealthChecker;
import com.emc.microservice.healthcheck.MicroServiceHealthCheckRegistry;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by liebea on 7/31/2014. Enjoy it
 */
public class HealthCheckManagerImpl implements HealthCheckManager {
    private final MicroServiceHealthCheckRegistry healthCheckRegistry;
    private final Logger logger;
    private final AtomicBoolean didIPauseTheService = new AtomicBoolean(false);
    private final List<HealthCheck> checks = new ArrayList<>();
    private MicroServiceController serviceController = null;

    @Override
    public void flagAsUnhealthy(String reason) {
        this.healthCheckRegistry.markAsUnhealthyAndRunAllTests(reason);
    }

    public HealthCheckManagerImpl(MicroServiceHealthCheckRegistry healthCheckRegistry, Logger logger) {
        this.healthCheckRegistry = healthCheckRegistry;
        this.logger = LoggingHelper.createSubLogger(logger, this.getClass());
    }

    @NoJavadoc
    public void init(final MicroServiceController serviceController, final Context context) {
        this.serviceController = serviceController;

        for (final HealthCheck currCheck : checks) {
            Callable<HealthCheckResult> checker = () -> runCheck(currCheck, context);
            healthCheckRegistry.addCheck(new HealthChecker(currCheck.getName(), checker));
        }

        Runnable errorHandler = this::handleError;
        healthCheckRegistry.registerErrorHandler(errorHandler);
        Runnable resumeHandler = this::restartService;
        healthCheckRegistry.registerResumeHandler(resumeHandler);
        Integer checkPeriod = context.getParametersBag()
                .getInt(MicroServiceHealthCheckRegistry.HEALTH_CHECK_PERIOD_PARAMETER_NAME);
        if (checkPeriod == null) {
            checkPeriod = 120;
        }

        healthCheckRegistry.init(checkPeriod);
    }

    public void shutDown() {
        this.healthCheckRegistry.shutDown();
    }

    private void handleError() {
        boolean didIJustReallyPausedTheService = serviceController.pause();
        if (didIJustReallyPausedTheService) {
            pauseService();
        }
    }

    private synchronized void pauseService() {
        didIPauseTheService.set(true);
    }

    private synchronized void restartService() {
        if (didIPauseTheService.get()) {
            serviceController.resume();
            didIPauseTheService.set(false);
        }
    }

    private HealthCheckResult runCheck(HealthCheck currCheck, Context context) {
        String currCheckName = currCheck.getName();
        logger.debug("Running health check {}", currCheckName);
        return currCheck.check(context);
    }

    @Override
    public void addCheck(HealthCheck check) {
        checks.add(check);
    }

}
