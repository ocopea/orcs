// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.psb.shpanpaas;

import com.emc.microservice.MicroServiceController;

import java.util.Collection;

/**
 * Created by liebea on 7/22/15.
 * Drink responsibly
 */
public class ApplicationInstance {
    private final String name;
    private final String appInstanceId;
    private final String version;
    private final String baseURI;
    private final Collection<ServiceMapping> serviceMappings;
    private final MicroServiceController controller;

    public ApplicationInstance(
            String name,
            String appInstanceId,
            String version,
            String baseURI,
            Collection<ServiceMapping> serviceMappings,
            MicroServiceController controller) {
        this.name = name;
        this.appInstanceId = appInstanceId;
        this.version = version;
        this.baseURI = baseURI;
        this.serviceMappings = serviceMappings;
        this.controller = controller;
    }

    public String getName() {
        return name;
    }

    public String getAppInstanceId() {
        return appInstanceId;
    }

    public String getVersion() {
        return version;
    }

    public String getBaseURI() {
        return baseURI;
    }

    public Collection<ServiceMapping> getServiceMappings() {
        return serviceMappings;
    }

    public MicroServiceController getController() {
        return controller;
    }
}
