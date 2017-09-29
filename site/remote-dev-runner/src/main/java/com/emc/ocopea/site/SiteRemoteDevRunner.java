// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import com.emc.dpa.dev.DevResourceProvider;
import com.emc.microservice.config.ConfigurationAPI;
import com.emc.microservice.configuration.client.RemoteConfigurationClient;
import com.emc.microservice.resource.DefaultWebApiResolver;
import com.emc.microservice.runner.MicroServiceRunner;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.protection.ProtectionMicroService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.sql.SQLException;
import java.util.Collections;

/**
 * Created by liebea on 7/21/15.
 * Drink responsibly
 */
public class SiteRemoteDevRunner {

    /***
     * Main   dude.
     * @param args main args dudu.
     */
    public static void main(String[] args) throws IOException, SQLException {

        // Reading environment variables for setting site configuration
        String siteName = System.getenv("SITE_NAME");
        final String publicDNS = System.getenv("SITE_PUBLIC_DNS");
        final String region = System.getenv("SITE_REGION");

        if (siteName == null || siteName.isEmpty()) {
            siteName = InetAddress.getLocalHost().getHostName();
        }
        // Setting configuration variables to load as service configurations
        System.setProperty("site_site-name", siteName);
        if (publicDNS != null && publicDNS.length() > 0) {
            System.setProperty("site_public-load-balancer", publicDNS);
        }
        if (region != null && region.length() > 0) {
            System.setProperty("site_region", region);
        }

        DevResourceProvider devResourceProvider = getResourceProvider();
        new MicroServiceRunner().run(devResourceProvider,
                new ProtectionMicroService(),
                new SiteMicroService()
        );
    }

    private static DevResourceProvider getResourceProvider() throws IOException, SQLException {
        String nazConfigURL = System.getenv("NAZ_CONFIG_URL");
        if (nazConfigURL == null || nazConfigURL.isEmpty()) {
            throw new IllegalStateException("NAZ_CONFIG_URL env not set");
        }
        WebAPIResolver resolver = new DefaultWebApiResolver();
        ConfigurationAPI remoteConfigurationClient =
                new RemoteConfigurationClient(new RemoteConfigurationClient.RestClientResolver() {
                    @Override
                    public <T> T resolve(Class<T> webInterface, URI remoteService, boolean verifySSL) {
                        return resolver.getWebAPI(remoteService.toString(), webInterface);
                    }
                }, URI.create(nazConfigURL + "/configuration-api"), false);

        return new DevResourceProvider(Collections.emptyMap(), remoteConfigurationClient);
    }
}
