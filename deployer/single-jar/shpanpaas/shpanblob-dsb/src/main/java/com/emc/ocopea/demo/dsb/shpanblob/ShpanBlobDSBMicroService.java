// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.demo.dsb.shpanblob;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceInitializationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liebea on 1/4/16.
 * Drink responsibly
 */
public class ShpanBlobDSBMicroService extends MicroService {
    private static final String SERVICE_NAME = "ShpanBlob DSB";
    private static final String SERVICE_BASE_URI = "shpanblob-dsb";
    private static final String SERVICE_DESCRIPTION = "ShpanBlob DSB Reference implementation";
    private static final int SERVICE_VERSION = 1;
    private static final Logger logger = LoggerFactory.getLogger(ShpanBlobDSBMicroService.class);

    public ShpanBlobDSBMicroService() {
        super(
                SERVICE_NAME,
                SERVICE_BASE_URI,
                SERVICE_DESCRIPTION,
                SERVICE_VERSION,
                logger,
                new MicroServiceInitializationHelper()

                        .withRestResource(ShpanBlobDSBResource.class, "DSB API implementation")
                        .withSingleton("shpanblob-dsb-singleton", "singleton for fun", ShpanBlobDSBSingleton.class)
        );

    }
}
