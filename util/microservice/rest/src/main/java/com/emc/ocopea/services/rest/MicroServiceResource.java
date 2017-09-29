// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.services.rest;

import com.emc.microservice.MicroServiceApplication;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

/**
 * Created with love by liebea on 6/18/2014.
 */
public abstract class MicroServiceResource {

    private MicroServiceApplication microServiceApplication;

    @Context
    public void setApplication(Application application) {
        this.microServiceApplication = (MicroServiceApplication) application;
    }

    public MicroServiceApplication getMicroServiceApplication() {
        return microServiceApplication;
    }
}
