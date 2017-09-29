// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.demo;

import com.emc.microservice.MicroServiceController;
import com.emc.microservice.config.ConfigurationAPI;
import com.emc.microservice.configuration.client.RemoteConfigurationClient;
import com.emc.microservice.resource.DefaultWebApiResolver;
import com.emc.microservice.runner.MicroServiceRunner;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.demo.dsb.h2.H2DSBMicroService;
import com.emc.ocopea.demo.dsb.shpanblob.ShpanBlobDSBMicroService;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.hub.ShpanPaaSResourceProvider;
import com.emc.ocopea.hub.psb.shpanpaas.ShpanPaasPsbMicroService;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

public class ShpanPaasPsbRemoteDevRunner {
    @NoJavadoc
    // TODO add javadoc
    public static void main(String[] args) throws IOException, SQLException {

        final WebAPIResolver apiResolver = new DefaultWebApiResolver();
        ConfigurationAPI remoteConfigurationClient =
                new RemoteConfigurationClient(
                    new RemoteConfigurationClient.RestClientResolver() {
                        @Override
                        public <T> T resolve(Class<T> webInterface, URI remoteService, boolean verifySSL) {
                            return apiResolver.getWebAPI(remoteService.toString(), webInterface);
                        }
                    },
                    URI.create("http://localhost:8081/configuration-api"),
                    false);
        ShpanPaaSResourceProvider devResourceProvider =
                new ShpanPaaSResourceProvider(Collections.emptyMap(), remoteConfigurationClient);

        Map<String, MicroServiceController> controllers = new MicroServiceRunner().run(
                devResourceProvider,
                new ShpanPaasPsbMicroService(),
                new ShpanBlobDSBMicroService(),
                new H2DSBMicroService()
        );
    }
}
