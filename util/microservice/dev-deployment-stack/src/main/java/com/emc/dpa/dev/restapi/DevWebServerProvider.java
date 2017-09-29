// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.dpa.dev.restapi;

import com.emc.dpa.dev.DevModeHelper;
import com.emc.dpa.dev.DevResourceProvider;
import com.emc.microservice.Context;
import com.emc.microservice.resource.ResourceProviderManager;
import com.emc.microservice.restapi.MicroServiceWebServer;
import com.emc.microservice.restapi.WebServerProvider;
import com.emc.microservice.standalone.web.UndertowRestEasyWebServer;
import com.emc.microservice.standalone.web.UndertowWebServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

/**
 * An implementation of {@link WebServerProvider}. Uses {@link UndertowRestEasyWebServer}.
 */
public class DevWebServerProvider implements WebServerProvider<UndertowWebServerConfiguration> {
    private static final Logger logger = LoggerFactory.getLogger(DevWebServerProvider.class);
    private UndertowRestEasyWebServer undertowRestEasyWebServer;

    @Override
    public MicroServiceWebServer getWebServer(UndertowWebServerConfiguration webServerConfiguration) {
        if (undertowRestEasyWebServer == null) {
            DevResourceProvider resourceProvider = (DevResourceProvider) ResourceProviderManager.getResourceProvider();
            if (resourceProvider != null) {
                undertowRestEasyWebServer = new UndertowRestEasyWebServer(webServerConfiguration) {
                    @Override
                    public void deployServiceApplication(Context context) {
                        try {
                            DevModeHelper.registerServiceDependencies(
                                    context,
                                    getPort(),
                                    resourceProvider.getSchemaBootstrapMap(),
                                    resourceProvider,
                                    resourceProvider.getOverridenResourceProperties());

                        } catch (IOException | SQLException e) {
                            throw new IllegalStateException("Failed setting up dev-mode", e);
                        }
                        super.deployServiceApplication(context);
                        resourceProvider.addDeployedMicroServiceClassByServiceURI(
                                context.getMicroServiceBaseURI(),
                                context.getServiceDescriptor().getClass());
                    }
                };
            } else {
                undertowRestEasyWebServer = new UndertowRestEasyWebServer(webServerConfiguration);
            }
        }
        return undertowRestEasyWebServer;
    }

    @Override
    public Class<UndertowWebServerConfiguration> getConfClass() {
        return UndertowWebServerConfiguration.class;
    }
}
