// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.psb.shpanpaas;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceInitializationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liebea on 1/4/16.
 * Drink responsibly
 */
public class ShpanPaasPsbMicroService extends MicroService {
    public static final String SERVICE_BASE_URI = "shpanpaas-psb";
    private static final String SERVICE_NAME = "ShpanPaaS PSB";
    private static final String SERVICE_DESCRIPTION = "ShpanPaaS PSB Reference implementation";
    private static final int SERVICE_VERSION = 1;
    private static final Logger logger = LoggerFactory.getLogger(ShpanPaasPsbMicroService.class);

    public ShpanPaasPsbMicroService() {
        super(
                SERVICE_NAME,
                SERVICE_BASE_URI,
                SERVICE_DESCRIPTION,
                SERVICE_VERSION,
                logger,
                new MicroServiceInitializationHelper()
                        .withParameter("public-load-balancer", "Public DNS", "")
                        .withRestResource(ShpanPaasPsbResource.class, "PSB Web API Implementation")
                        .withRestResource(ShpanPaasArtifactRegistryResource.class, "Artifact registry implementation")
                        .withSingleton(ApplicationServiceManager.class)
                        .withSingleton(ShpanPaasArtifactRegistrySingleton.class)
                        .withWebSocket(ShpanPaaSWebSocket.class)
        );

    }
}
