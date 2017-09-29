// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.services.rest;

import com.emc.microservice.restapi.MicroServiceWebServer;

import javax.ws.rs.core.Application;

/**
 * Created by liebea on 2/9/17.
 * Drink responsibly
 */
public class MicroServiceRootApplication extends Application {
    private final MicroServiceWebServer webServer;

    public MicroServiceRootApplication(MicroServiceWebServer webServer) {
        this.webServer = webServer;
    }

    public MicroServiceWebServer getWebServer() {
        return webServer;
    }
}
