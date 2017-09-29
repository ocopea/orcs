// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* 
 * This computer code is copyright 2014 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.objectstore.pgsql;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceInitializationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object Store Micro Service
 */
public class ObjectStoreMicroService extends MicroService {

    private static final String SERVICE_NAME = "Object Store";
    private static final String SERVICE_IDENTIFIER = "objects";
    private static final String SERVICE_DESCRIPTION = "Object Store Service";
    private static final int SERVICE_VERSION = 1;
    private static final Logger logger = LoggerFactory.getLogger(ObjectStoreMicroService.class);

    /**
     * Initialize micro-service descriptor, used by subclasses to initialize design time parameters of a service
     */
    public ObjectStoreMicroService() {
        super(SERVICE_NAME, SERVICE_IDENTIFIER, SERVICE_DESCRIPTION, SERVICE_VERSION, logger,
                new MicroServiceInitializationHelper()
                        .withRestResource(ObjectStoreResource.class, "Object Store Resource")
                        .withDefaultBlobStore()
        );
    }
}
