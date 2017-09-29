// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.configuration.client;

import com.emc.microservice.configuration.ConfigurationDevRunner;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.microservice.restapi.MicroServiceWebServer;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by shresa on 05/08/15.
 * Test config service web api
 */
public class RemoteConfigurationClientTest {

    private static RemoteConfigurationClient client;

    @BeforeClass
    public static void setup() throws Exception {
        final ResourceProvider resourceProvider = ConfigurationDevRunner.run();
        final MicroServiceWebServer webServer = resourceProvider.getWebServer(null);
        URI serviceUri = new URI("http://localhost:" + webServer.getPort() + "/configuration-api/");
        client = new RemoteConfigurationClient(new RemoteConfigurationClient.RestClientResolver() {
            @Override
            public <T> T resolve(Class<T> webInterface, URI remoteService, boolean verifySSL) {
                ResteasyClientBuilder builder = new ResteasyClientBuilder();
                if (!verifySSL) {
                    builder.disableTrustManager();
                }
                final ResteasyClient client = builder.build();
                return client.target(remoteService).proxy(webInterface);

            }
        }, serviceUri, false);
    }

    @Test
    public void testWriteAndReadData() {
        client.writeData("/level1/level2/file1", "{'content': 'file1'}");
        String file2Content = "{'content': 'file2'}";
        client.writeData("/level1/level2/file2", file2Content);

        assertEquals(file2Content, client.readData("/level1/level2/file2"));

        final Collection<String> level1Children = client.list("/level1");
        assertEquals(1, level1Children.size());
        assertTrue(level1Children.contains("/level1/level2"));

        final Collection<String> level2Children = client.list("/level1/level2");
        assertEquals(2, level2Children.size());
        assertTrue(level2Children.contains("/level1/level2/file1"));
        assertTrue(level2Children.contains("/level1/level2/file2"));

    }

    @AfterClass
    public static void tearDown() {
        ConfigurationDevRunner.stop();
    }
}
