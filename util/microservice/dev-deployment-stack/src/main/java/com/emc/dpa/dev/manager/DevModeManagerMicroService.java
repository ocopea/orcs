// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.dpa.dev.manager;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceInitializationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liebea on 2/23/15.
 * Drink responsibly
 */
public class DevModeManagerMicroService extends MicroService {
    private static final Logger logger = LoggerFactory.getLogger(DevModeManagerMicroService.class);
    private static final String SERVICE_NAME = "DevMode Manager";
    private static final int SERVICE_VERSION = 1;
    private static final String SERVICE_DESCRIPTION = "Manage Dev Mode Runtime";

    public static final String SERVICE_IDENTIFIER = "dev-mode-manager";

    public DevModeManagerMicroService() {
        super(
                SERVICE_NAME,
                SERVICE_IDENTIFIER,
                SERVICE_DESCRIPTION,
                SERVICE_VERSION,
                logger,
                new MicroServiceInitializationHelper()

                        // main rest resource
                        .withRestResource(
                                DevModeServiceManagerResource.class,
                                "Manage services")

        );
    }
}
