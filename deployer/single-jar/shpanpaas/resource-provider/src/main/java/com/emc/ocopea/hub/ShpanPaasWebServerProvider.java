// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub;

import com.emc.microservice.Context;
import com.emc.microservice.resource.ResourceProviderManager;
import com.emc.microservice.restapi.MicroServiceWebServer;
import com.emc.microservice.restapi.WebServerProvider;
import com.emc.microservice.standalone.web.UndertowRestEasyWebServer;
import com.emc.microservice.standalone.web.UndertowWebServerConfiguration;

import java.io.IOException;
import java.sql.SQLException;

/**
 * An implementation of {@link WebServerProvider}, only works with {@link ShpanPaaSResourceProvider}. Uses
 * {@link UndertowRestEasyWebServer} and uses ugly shpan-hack for web-root-path.
 */
public class ShpanPaasWebServerProvider implements WebServerProvider<UndertowWebServerConfiguration> {
    private UndertowRestEasyWebServer undertowRestEasyWebServer;

    @Override
    public MicroServiceWebServer getWebServer(UndertowWebServerConfiguration webServerConfiguration) {
        if (undertowRestEasyWebServer == null) {
            undertowRestEasyWebServer = new UndertowRestEasyWebServer(webServerConfiguration) {

                private String getServiceURI(Context context) {
                    ShpanPaaSResourceProvider resourceProvider =
                            (ShpanPaaSResourceProvider) ResourceProviderManager.getResourceProvider();
                    String shpanPaaSBaseURI = ShpanPaaSResourceProvider.extractPaasBaseURI(context);
                    if (shpanPaaSBaseURI != null && !shpanPaaSBaseURI.isEmpty()) {
                        return shpanPaaSBaseURI + "/" + super.getWebRootPath(context);
                    } else {
                        return resourceProvider.getServiceREstURI(context);
                    }
                }

                @Override
                public void deployServiceApplication(Context context) {
                    ShpanPaaSResourceProvider resourceProvider =
                            (ShpanPaaSResourceProvider) ResourceProviderManager.getResourceProvider();
                    try {
                        resourceProvider.registerServiceDependencies(
                                context,
                                resourceProvider.getSchemaBootstrapMap(),
                                resourceProvider.getOverridenResourceProperties(),
                                getPort());
                    } catch (IOException | SQLException e) {
                        throw new IllegalStateException("Failed setting up dev-mode", e);
                    }
                    super.deployServiceApplication(context);
                }

                @Override
                protected String getWebRootPath(Context context) {
                    return getServiceURI(context);
                }

                @Override
                public void unDeployServiceApplication(Context context) {
                    //nop
                }
            };
        }
        return undertowRestEasyWebServer;
    }

    @Override
    public Class<UndertowWebServerConfiguration> getConfClass() {
        return UndertowWebServerConfiguration.class;
    }
}
