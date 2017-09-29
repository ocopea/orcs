// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.inspector;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceInitializationHelper;
import com.emc.microservice.messaging.MessagingStatsResourceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liebea on 1/7/15.
 * Drink responsibly
 */
public class InspectorMicroService extends MicroService {
    private static final Logger LOGGER = LoggerFactory.getLogger(InspectorMicroService.class);
    private static final String SERVICE_NAME = "Inspector";
    private static final int SERVICE_VERSION = 1;
    private static final String SERVICE_IDENTIFIER = "inspector";
    private static final String SERVICE_DESCRIPTION =
            "It's time to chew bubble gum, and I'm all out of gum...";

    public InspectorMicroService() {
        super(
                SERVICE_NAME,
                SERVICE_IDENTIFIER,
                SERVICE_DESCRIPTION,
                SERVICE_VERSION,
                LOGGER,
                new MicroServiceInitializationHelper()

                        // main rest resource
                        .withRestResource(
                                InspectorResource.class,
                                "Inspector main resource")
                        // main(2) :) rest resource
                        .withRestResource(
                                InspectorResource2.class,
                                "Inspector main resource(2)")

                        // Supporting messaging stats where supported
                        .withExternalResource(new MessagingStatsResourceDescriptor(), true)

        );

    }
}
