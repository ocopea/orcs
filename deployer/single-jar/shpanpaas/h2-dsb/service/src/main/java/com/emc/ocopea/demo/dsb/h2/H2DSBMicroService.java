// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.demo.dsb.h2;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceInitializationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liebea on 1/4/16.
 * Drink responsibly
 */
public class H2DSBMicroService extends MicroService {
    private static final String SERVICE_NAME = "H2 DSB";
    private static final String SERVICE_BASE_URI = "h2-dsb";
    private static final String SERVICE_DESCRIPTION = "H2 DSB Reference implementation";
    private static final int SERVICE_VERSION = 1;
    private static final Logger logger = LoggerFactory.getLogger(H2DSBMicroService.class);

    public H2DSBMicroService() {
        super(
                SERVICE_NAME,
                SERVICE_BASE_URI,
                SERVICE_DESCRIPTION,
                SERVICE_VERSION,
                logger,
                new MicroServiceInitializationHelper()

                        .withRestResource(H2DSBResource.class, "DSB API implementation")
                        .withSingleton("h2-dsb-singleton", "singleton for fun", H2DSBSingleton.class)
        );

    }
}
