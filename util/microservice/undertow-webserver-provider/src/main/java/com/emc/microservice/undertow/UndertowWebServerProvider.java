// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2014 - 2016 EMC Corporation
 * All rights reserved
 */
package com.emc.microservice.undertow;

import com.emc.microservice.restapi.MicroServiceWebServer;
import com.emc.microservice.restapi.WebServerProvider;
import com.emc.microservice.standalone.web.UndertowRestEasyWebServer;
import com.emc.microservice.standalone.web.UndertowWebServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with true love by liebea on 10/12/2014.
 */
public class UndertowWebServerProvider implements WebServerProvider<UndertowWebServerConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(UndertowWebServerProvider.class);
    private UndertowRestEasyWebServer undertowRestEasyWebServer;

    @Override
    public MicroServiceWebServer getWebServer(UndertowWebServerConfiguration webServerConfiguration) {
        if (undertowRestEasyWebServer == null) {
            // Possible override deployServiceApplication
            undertowRestEasyWebServer = new UndertowRestEasyWebServer(webServerConfiguration);
        }
        return undertowRestEasyWebServer;
    }

    @Override
    public Class<UndertowWebServerConfiguration> getConfClass() {
        return UndertowWebServerConfiguration.class;
    }
}
