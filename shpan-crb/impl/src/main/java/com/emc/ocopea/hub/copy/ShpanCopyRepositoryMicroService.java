// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.copy;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceInitializationHelper;
import com.emc.ocopea.crb.CopyMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liebea on 1/4/16.
 * Drink responsibly
 */
public class ShpanCopyRepositoryMicroService extends MicroService {
    private static final String SERVICE_NAME = "Shpan Copy Store";
    private static final String SERVICE_BASE_URI = "shpan-copy-store";
    private static final String SERVICE_DESCRIPTION = "Shpan copy store reference implementation";
    private static final int SERVICE_VERSION = 1;
    private static final Logger logger = LoggerFactory.getLogger(ShpanCopyRepositoryMicroService.class);

    public ShpanCopyRepositoryMicroService() {
        super(
                SERVICE_NAME,
                SERVICE_BASE_URI,
                SERVICE_DESCRIPTION,
                SERVICE_VERSION,
                logger,
                new MicroServiceInitializationHelper()

                        .withRestResource(ShpanCopyRepositoryResource.class, "CRB Rest API Implementation")
                        .withRestResource(ShpanCopyRepositoryDataResource.class, "CRB Data-API Implementation")
                        .withRestResource(ShpanCopyRepositoryDataResourceBackwardCompatibility.class,
                                "CRB Data-API backward compatibility")
                        .withBlobStore("copy-store")
                        .withJacksonSerialization(CopyMetaData.class)
        );

    }
}
