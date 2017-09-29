// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.demo.dsb.shpanblob;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceInitializationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liebea on 1/4/16.
 * This duplicated-dsb is a copy of the shpanblob-dsb and is meant to mock scenario in which we have
 * More than one dsb implementing same protocol
 */
public class DuplicateShpanBlobDSBMicroService extends MicroService {
    private static final String SERVICE_NAME = "Duplicate ShpanBlob DSB";
    private static final String SERVICE_BASE_URI = "shpanblob-dup-dsb";
    private static final String SERVICE_DESCRIPTION = "ShpanBlob Duplicate DSB Reference implementation";
    private static final int SERVICE_VERSION = 1;
    private static final Logger logger = LoggerFactory.getLogger(DuplicateShpanBlobDSBMicroService.class);

    public DuplicateShpanBlobDSBMicroService() {
        super(
                SERVICE_NAME,
                SERVICE_BASE_URI,
                SERVICE_DESCRIPTION,
                SERVICE_VERSION,
                logger,
                new MicroServiceInitializationHelper()

                        .withRestResource(ShpanBlobDuplicateDSBResource.class, "(Duplicate) DSB API implementation")
                        .withSingleton("shpanblob-dsb-singleton", "singleton for fun", ShpanBlobDSBSingleton.class)
        );

    }
}
