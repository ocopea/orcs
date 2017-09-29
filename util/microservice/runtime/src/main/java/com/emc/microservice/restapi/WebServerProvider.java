// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.restapi;

/**
 * Provides web server. Implementations should be registered in META-INF/services.
 */
public interface WebServerProvider<C extends WebServerConfiguration> {

    /**
     * Returns, possibly creating, a web server.
     *
     * @param configuration the web server configuration
     *
     * @return web server
     */
    MicroServiceWebServer getWebServer(C configuration);

    Class<C> getConfClass();
}
