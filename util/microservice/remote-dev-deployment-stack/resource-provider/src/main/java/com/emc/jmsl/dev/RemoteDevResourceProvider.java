// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id$
 *
 * This computer code is copyright 2014 EMC Corporation
 * All rights reserved
 */

package com.emc.jmsl.dev;

import com.emc.jmsl.dev.messaging.DevMessagingProviderConfiguration;
import com.emc.microservice.Context;
import com.emc.microservice.MicroService;
import com.emc.microservice.bootstrap.AbstractSchemaBootstrap;
import com.emc.microservice.configuration.client.RemoteConfigurationClient;
import com.emc.microservice.messaging.MessagingProvider;
import com.emc.microservice.metrics.MetricsRegistryImpl;
import com.emc.microservice.resource.DefaultWebApiResolver;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.microservice.schedule.SchedulerConfiguration;
import com.emc.microservice.standalone.web.UndertowWebServerConfiguration;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.microservice.schedule.SchedulerApi;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author shresa This resource provider can be used to connect to a remote dev server TODO: probably requires new
 *         implementations for {@link MessagingProvider} and {@link com.emc.microservice.blobstore.BlobStoreProvider}
 */
public class RemoteDevResourceProvider extends ResourceProvider {
    private final Map<String, Class<? extends MicroService>> deployedMicroServiceClassesByServiceURI =
            new ConcurrentHashMap<>();

    private final Map<String, Map<String, Map<String, String>>> overridenResourceProperties = new HashMap<>();

    public RemoteDevResourceProvider(String devServerURL) throws IOException, SQLException {
        this(devServerURL, Collections.emptyMap());
    }

    public RemoteDevResourceProvider(String devServerURL, Map<String, AbstractSchemaBootstrap> schemaBootstrapMap)
            throws IOException, SQLException {
        this(
                devServerURL,
                schemaBootstrapMap,
                new RemoteConfigurationClient(new RemoteConfigurationClient.RestClientResolver() {
                    @Override
                    public <T> T resolve(Class<T> webInterface, URI remoteService, boolean verifySSL) {
                        return new DefaultWebApiResolver().getWebAPI(remoteService.toString(), webInterface);
                    }
                }, URI.create(devServerURL + "/configuration-api"), false));
    }

    private RemoteDevResourceProvider(
            String devServerURL,
            Map<String, AbstractSchemaBootstrap> schemaBootstrapMap,
            RemoteConfigurationClient configurationClient) throws IOException, SQLException {
        super(configurationClient);

        // Registering web server configuration
        getServiceRegistryApi().registerWebServer(
                "default",
                new UndertowWebServerConfiguration(8080));

        //new MicroServiceRunner().run(this, new DevModeManagerMicroService());
    }

    @NoJavadoc
    public void overrideResourceProperties(String type, String name, String propName, String propValue) {
        Map<String, Map<String, String>> byType = overridenResourceProperties.get(type);
        if (byType == null) {
            byType = new HashMap<>();
            overridenResourceProperties.put(type, byType);
        }
        Map<String, String> byName = byType.get(name);
        if (byName == null) {
            byName = new HashMap<>();
            byType.put(name, byName);
        }
        byName.put(propName, propValue);
    }

    public Map<String, Map<String, Map<String, String>>> getOverridenResourceProperties() {
        return overridenResourceProperties;
    }

    @Override
    public SchedulerApi getScheduler(
            SchedulerConfiguration schedulerConfiguration,
            Context context) {
        return new RemoteDevScheduler(
                schedulerConfiguration.getName(),
                context != null ? context.getMetricsRegistry() : new MetricsRegistryImpl("bob"));
    }

    @Override
    public final Class<DevMessagingProviderConfiguration> getMessagingConfigurationClass() {
        return DevMessagingProviderConfiguration.class;
    }

    @Override
    public final Class<RemoteDevQueueConfiguration> getQueueConfigurationClass() {
        return RemoteDevQueueConfiguration.class;
    }

    @Override
    public final Class<RemoteDevBlobStoreConfiguration> getBlobStoreConfigurationClass() {
        return RemoteDevBlobStoreConfiguration.class;
    }

    @Override
    public final Class<UndertowWebServerConfiguration> getWebServerConfigurationClass() {
        return UndertowWebServerConfiguration.class;
    }

    @Override
    public final String getNodeAddress() {
        return "localhost";
    }

    @Override
    public WebAPIResolver getWebAPIResolver() {
        return new DefaultWebApiResolver();
    }

}
