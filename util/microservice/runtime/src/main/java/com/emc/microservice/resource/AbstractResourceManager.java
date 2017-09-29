// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.resource;

import com.emc.microservice.Context;
import com.emc.microservice.ContextImpl;
import com.emc.microservice.LoggingHelper;
import com.emc.microservice.health.HealthCheck;
import com.emc.microservice.health.HealthCheckManagerImpl;
import com.emc.microservice.health.HealthCheckResult;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by liebea on 9/21/2014. Enjoy it
 * Abstract helper class for managing instantiation, life cycle and health of managed resources
 * <p>
 * Managed resources include things like queues, data store, blob store, other services etc.
 *
 * @param <DescT> Descriptor for managed resource.
 * @param <ConfT> Configuration for the resource.
 * @param <ResT> Managed resource
 */
public abstract class AbstractResourceManager
        <DescT extends ResourceDescriptor,
                ConfT extends ResourceConfiguration,
                ResT extends ManagedResource<DescT, ConfT>>
        implements ResourceManager<DescT, ConfT, ResT>, ExternalResourceManager<DescT, ConfT, ResT> {

    // Static descriptor list describing the resource (defined mostly on design time)
    protected final Map<String, DescT> descriptors;

    // Map containing the physical resources being managed by this manager
    protected final Map<String, ResT> managedResources;

    // Resource Provider
    protected final ResourceProvider resourceProvider;

    // Logger extending the micro-service logger we're attached to
    protected final Logger logger;

    /***
     * Initialize the resource manager
     * @param descriptors Static descriptor list describing the resource (defined on design time)
     * @param microServiceLogger logger that belongs to the micro-service instance we're attached to
     */
    protected AbstractResourceManager(
            List<DescT> descriptors,
            Logger microServiceLogger) {
        this.descriptors = new LinkedHashMap<>(descriptors.size());
        for (DescT curDesc : descriptors) {
            this.descriptors.put(curDesc.getName(), curDesc);
        }
        managedResources = new HashMap<>(descriptors.size());
        this.logger = LoggingHelper.createSubLogger(microServiceLogger, this.getClass());
        this.resourceProvider = ResourceProviderManager.getResourceProvider();
    }

    /***
     * This method will be invoked by the micro-service library when a service is initializing
     * within this method we'll initialize physical resources and initialize health checks
     * we can avoid resource starting to serve by overriding the start method and allowing it there,
     * however at this stage, we should verify all physical connectivity to the resource
     * @param configurationMap configuration entry for every descriptor defined by micro-service design time
     * @param context instance of micro-service context
     * @param serviceHealthCheckManager health check is used to add custom health checks related to the
     *                                  resource this manger is responsible for
     */
    public void init(
            Map<String, ConfT> configurationMap,
            ContextImpl context,
            HealthCheckManagerImpl serviceHealthCheckManager) {
        boolean succeed = false;

        /* numOfFailed1MinAttempts : variable that counts number of times the loop was run. Will be reset after
         * a message is loggedloggingThreshold : threshold at which the message should be logged.
         *              Need to log at 10th min, 20th min, 40th min, and then every 60th min.
         *              Set to 5 so it can then be easily doubled to 10, 20, 40.
         */
        int numOfFailed1MinAttempts = 0;
        int loggingThreshold = 5;

        logger.debug("Initializing {}", getResourceTypeNamePlural());
        // Retrying connect forever until success or until stopped
        while (!succeed) {
            try {
                // Creating receivers list for every managed queue according to number of listener requested
                for (DescT resourceDescriptor : descriptors.values()) {
                    String resourceName = resourceDescriptor.getName();
                    ConfT resourceConfiguration = configurationMap.get(resourceName);
                    if (resourceConfiguration == null) {
                        throw new IllegalArgumentException(
                                "configuration for resource " + resourceName + " not provided");
                    }

                    // Initializing the resource, this method will throw an exception if we have no connectivity
                    // or any significant issues instantiating connection
                    ResT res = initializeResource(resourceDescriptor, resourceConfiguration, context);

                    //storing the managed resource instance in map
                    managedResources.put(resourceDescriptor.getName(), res);

                    // Initializing health check and running it once to see we are good to go :)
                    try {
                        initializeAndRunHealthCheck(res, serviceHealthCheckManager, context);
                    } catch (Exception ex) {
                        logger.warn("Failed initializing health check for {} {}", resourceName, getResourceTypeName());
                        managedResources.remove(resourceDescriptor.getName());
                        throw ex;
                    }

                    logger.info("Initialized {} {}", resourceName, getResourceTypeName());
                }

                for (DescT resourceDescriptor : descriptors.values()) {
                    final String resourceName = resourceDescriptor.getName();
                    final ResT res = managedResources.get(resourceName);
                    ConfT resourceConfiguration = configurationMap.get(resourceName);

                    // Call post init
                    try {
                        postInitResource(resourceDescriptor, resourceConfiguration, res, context);
                    } catch (Exception ex) {
                        logger.warn("Failed running post init for {} {}", resourceName, getResourceTypeName());
                        managedResources.remove(resourceName);
                        throw ex;
                    }

                    logger.info("Post init succeeded for {} {}", resourceName, getResourceTypeName());
                }

                succeed = true;
            } catch (Exception e) {
                if (numOfFailed1MinAttempts == 0 || numOfFailed1MinAttempts >= loggingThreshold) {
                    if (numOfFailed1MinAttempts == 0) {
                        logger.error("Failed initializing {}, retrying", getResourceTypeNamePlural(), e);
                    } else {
                        logger.error(
                                "Failed initializing {}, retrying. {}",
                                getResourceTypeNamePlural(),
                                e.getMessage());
                    }
                    // message logged, reset the numOfFailed1MinAttempts variable
                    numOfFailed1MinAttempts = 0;
                    // logic to determine the loggingThreshold increments. ie it will be set to 10, 20, 40
                    // and then 60 always
                    loggingThreshold = Math.min(loggingThreshold * 2, 60);
                }

                try {
                    stop();
                } catch (Exception ignored) {
                    logger.warn("Error cleaning {} up after error", getResourceTypeNamePlural(), e);
                }

                numOfFailed1MinAttempts++;
                sleepNoException(1000 * 60);
            }
        }
        logger.debug("Done Initializing {}", getResourceTypeNamePlural());
    }

    private void initializeAndRunHealthCheck(
            ResT res,
            HealthCheckManagerImpl serviceHealthCheckManager,
            Context context) {

        // Getting health check from resource
        Collection<HealthCheck> resourceHealthChecks = getResourceHealthChecks(res);

        // for every check running it and adding to scheduler
        for (HealthCheck resourceHealthCheck : resourceHealthChecks) {

            // "Manually running health check once to ensure connectivity
            runInitialCheck(resourceHealthCheck, context);

            // Once successful - adding to health check manager
            serviceHealthCheckManager.addCheck(resourceHealthCheck);
        }
    }

    /**
     * Check resource state during initialization, including validating physical connectivity and checking resource is
     * available for immediate consumption when out of this method
     *
     * @param healthCheck resource to check
     * @param context context to run first health check
     */
    private void runInitialCheck(HealthCheck healthCheck, Context context) {
        HealthCheckResult result = healthCheck.check(context);
        if (!result.isPass()) {
            throw new IllegalStateException(
                    "Failed to init resource. Check " + healthCheck.getName() + " failed: " + result.getMessage(),
                    result.getException());
        }
    }

    /**
     * Stopping and cleaning up used resources by this service
     */
    public final void stop() {
        logger.info("Stopping {}", getResourceTypeNamePlural());
        try {
            for (ResT currResource : managedResources.values()) {
                try {
                    logger.debug("Stopping {} {}", currResource.getDescriptor().getName(), getResourceTypeName());
                    cleanUpResource(currResource);
                    logger.debug("{} {} Stopped", currResource.getDescriptor().getName(), getResourceTypeName());
                } catch (Exception ignored) {
                    logger.debug("Exception while cleaning up {}", getResourceTypeNamePlural(), ignored);
                }
            }
        } finally {
            managedResources.clear();
            logger.info("Done Stopping {}", getResourceTypeNamePlural());
        }
    }

    private void sleepNoException(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            // Ignoring
        }
    }

    @Override
    public List<DescT> getDescriptors() {
        return new ArrayList<>(descriptors.values());
    }

    @Override
    public List<ResT> getManagedResources() {
        return new ArrayList<>(managedResources.values());
    }

    @Override
    public ResT getManagedResourceByName(String name) {
        return Objects.requireNonNull(
                managedResources.get(name),
                "Unable to find " + getResourceTypeName() + ": " + name);
    }

    /**
     * Pausing the resource manager, due to service pausing. pause allows quick resume
     */
    public final void pause() {

        logger.debug("Pausing {}", getResourceTypeNamePlural());
        try {
            for (ResT currResource : managedResources.values()) {
                try {
                    logger.info("Pausing {} {}", currResource.getDescriptor().getName(), getResourceTypeName());
                    pauseResource(currResource);
                    logger.info("{} {} Paused", currResource.getDescriptor().getName(), getResourceTypeName());
                } catch (Exception ignored) {
                    logger.warn("Exception while pausing {}", currResource.getDescriptor().getName(), ignored);
                }
            }
        } finally {
            logger.debug("Done pausing {}", getResourceTypeNamePlural());
        }
    }

    /***
     * Start serving, will be invoked on normal initialization as final stage,
     * and during resume after manager was paused
     */
    public final void start() {
        logger.debug("Starting {}", getResourceTypeNamePlural());
        try {
            for (ResT currResource : managedResources.values()) {
                try {
                    logger.trace("starting {} {}", currResource.getDescriptor().getName(), getResourceTypeName());
                    startResource(currResource);
                    logger.trace("{} {} started", currResource.getDescriptor().getName(), getResourceTypeName());
                } catch (Exception ignored) {
                    logger.warn("Exception while pausing {}", currResource.getDescriptor().getName(), ignored);
                }
            }
        } finally {
            logger.debug("Done starting {}", getResourceTypeNamePlural());
        }
    }

    /***
     * While most resources should, and are known via descriptors,
     * in some cases we require adding a resource dynamically in runtime
     * @param desc descriptor for the resource being added
     * @param conf runtime configuration
     * @param context microservice context
     */
    @Override
    public void addResourceDynamically(DescT desc, ConfT conf, Context context) {
        try {
            logger.debug("Initializing dynamic resource {} {}", desc.getName(), getResourceTypeName());
            if (descriptors.containsKey(desc.getName()) && managedResources.containsKey(desc.getName())) {
                throw new IllegalArgumentException(
                        "Duplicate resource " + desc.getName() + " of type " + getResourceTypeName() +
                                " added dynamically");
            }
            descriptors.put(desc.getName(), desc);
            ResT res = initializeResource(desc, conf, context);
            postInitResource(desc, conf, res, context);
            managedResources.put(desc.getName(), res);
            logger.info("Successfully Initialized dynamic resource {} {}", desc.getName(), getResourceTypeName());
        } catch (Exception ex) {
            logger.debug("Failed Initializing dynamic resource " + desc.getName() + " " + getResourceTypeName(), ex);
            throw new IllegalStateException(
                    "Failed Initializing dynamic resource " + desc.getName() + " " + getResourceTypeName(),
                    ex);
        }
    }

    @Override
    public boolean hasResource(String resourceName) {
        return managedResources.containsKey(resourceName);
    }

    @Override
    public Collection<HealthCheck> getResourceHealthChecks(ResT managedResource) {
        return Collections.emptyList();
    }
}
