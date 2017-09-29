// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2015-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice;

import org.slf4j.Logger;

/**
 * Created with love by liebea on 5/15/14.
 * Base class for all of DPA micro services
 * <p>
 * Note: All subclasses must implement a public default Constructor, this to allow the MS framework libraries to
 * instantiate it and read it's design time configuration.
 */
public abstract class MicroService {

    private final String name;
    private final MicroserviceIdentifier identifier;
    private final String description;
    private final int version;
    private final MicroServiceInitializationHelper initializationHelper;

    private final Logger logger;
    public static final String DEFAULT_BLOBSTORE_NAME = "DEFAULT";
    private MicroServiceState state = MicroServiceState.STOPPED;

    /***
     * Initialize micro-service descriptor, used by subclasses to initialize design time parameters of a service
     * @param name micro-service display name
     * @param shortName unique identifier used to identify the service in URI, resource list etc.
     * @param description Service description
     * @param version service version
     * @param logger logger to use for all logging done by the micro-service framework libraries.
     *               libraries will create sub-loggers from this logger
     * @param initializationHelper resource descriptor parameters
     */
    protected MicroService(
            String name,
            String shortName,
            String description,
            int version,
            Logger logger,
            MicroServiceInitializationHelper initializationHelper) {
        this(name, new MicroserviceIdentifier(shortName), description, version, logger, initializationHelper);
    }

    private MicroService(
            String name,
            MicroserviceIdentifier identifier,
            String description,
            int version,
            Logger logger,
            MicroServiceInitializationHelper initializationHelper) {
        this.name = name;
        this.identifier = identifier;
        this.description = description;
        this.version = version;
        this.logger = logger;
        this.initializationHelper = initializationHelper;
    }

    /**
     * Service display name
     * @return service name
     */
    public final String getName() {
        return name;
    }

    /**
     * Unique service identifier
     * @return service identifier
     */
    public MicroserviceIdentifier getIdentifier() {
        return identifier;
    }

    /**
     * Service public description
     * @return service description
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Returns service version
     * @return service version
     */
    public final int getVersion() {
        return version;
    }

    /**
     * Service state could be any of the enumeration MicroServiceState values
     * Service state is manipulated by the micro-service framework libraries internally to expose
     * current state of the service. when service resources are unavailable service changes to a paused state and
     * back to running when resources are available
     * @return service state
     */
    public final MicroServiceState getState() {
        return state;
    }

    /**
     * Service specific initialization code, allows service to override as part of startup sequence
     * this method is invoked after resource managers are loaded and before service state changes to RUNNING
     * @param context microservice context
     */
    protected void initializeService(Context context) {
    }

    /**
     * Service specific validator for service parameters
     * @param params service parameters
     */
    protected void validateParameters(ParametersBag params) {
    }

    public Logger getLogger() {
        return logger;
    }

    public MicroServiceInitializationHelper getInitializationHelper() {
        return initializationHelper;
    }

}
