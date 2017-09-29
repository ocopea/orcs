// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.jmsl.dev.server;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceInitializationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liebea on 5/6/16.
 * Drink responsibly
 */
public class RemoteDevServerMicroService extends MicroService {
    private static final Logger logger = LoggerFactory.getLogger(RemoteDevServerMicroService.class);
    private static final String SERVICE_NAME = "Remote DevMode Server";
    private static final int SERVICE_VERSION = 1;
    private static final String SERVICE_IDENTIFIER = "remote-dev-server";
    private static final String SERVICE_DESCRIPTION = "Manage Remote Dev Services";

    public RemoteDevServerMicroService() {
        super(
                SERVICE_NAME,
                SERVICE_IDENTIFIER,
                SERVICE_DESCRIPTION,
                SERVICE_VERSION,
                logger,
                new MicroServiceInitializationHelper()

                        // main rest resource
                        .withRestResource(
                                RemoteDevServiceResource.class,
                                "do what Amit tells me to")

        );
    }
}
