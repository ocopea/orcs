// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id$
 *
 * This computer code is copyright 2014 EMC Corporation
 * All rights reserved
 */

package com.emc.dpa.dev;

import com.emc.dpa.dev.manager.DevModeManagerMicroService;
import com.emc.dpa.dev.messaging.DevMessagingProviderConfiguration;
import com.emc.dpa.dev.messaging.DevMessagingServer;
import com.emc.dpa.dev.registry.DevModeConfigurationImpl;
import com.emc.microservice.Context;
import com.emc.microservice.MicroService;
import com.emc.microservice.bootstrap.AbstractSchemaBootstrap;
import com.emc.microservice.config.ConfigurationAPI;
import com.emc.microservice.messaging.MessagingProviderConfiguration;
import com.emc.microservice.messaging.MessagingStatsResourceDescriptor;
import com.emc.microservice.registry.ServiceRegistryApi;
import com.emc.microservice.registry.ServiceRegistryImpl;
import com.emc.microservice.resource.DefaultWebApiResolver;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.microservice.runner.MicroServiceRunner;
import com.emc.microservice.standalone.web.UndertowWebServerConfiguration;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author shresa This resource provider describes the standalone DPA deployment stack using Resteasy webserver, remote
 *         hornetq connectores, and pg pooled connection based on jdbc
 */
public class DevResourceProvider extends ResourceProvider {
    private static final String APP_PORT_ENV_NAME = "MS_DEV_APP_PORT";
    protected final Map<String, Class<? extends MicroService>> deployedMicroServiceClassesByServiceURI =
            new ConcurrentHashMap<>();
    protected Map<String, AbstractSchemaBootstrap> schemaBootstrapMap;
    protected WebAPIResolver devApiResolver = new DefaultWebApiResolver();

    protected final Map<String, Map<String, Map<String, String>>> overridenResourceProperties = new HashMap<>();

    public DevResourceProvider() throws IOException, SQLException {
        this(Collections.<String, AbstractSchemaBootstrap>emptyMap());
    }

    public DevResourceProvider(Map<String, AbstractSchemaBootstrap> schemaBootstrapMap)
            throws IOException, SQLException {
        this(schemaBootstrapMap, new DevModeConfigurationImpl());
    }

    public DevResourceProvider(
            Map<String, AbstractSchemaBootstrap> schemaBootstrapMap,
            ConfigurationAPI configurationImpl) throws IOException, SQLException {
        this(schemaBootstrapMap, configurationImpl, new ServiceRegistryImpl(configurationImpl));
    }

    public DevResourceProvider(
            Map<String, AbstractSchemaBootstrap> schemaBootstrapMap,
            ConfigurationAPI configurationImpl,
            ServiceRegistryApi registryAPI) {
        super(configurationImpl, registryAPI);
        init(schemaBootstrapMap, configurationImpl, registryAPI);
    }

    private void init(
            Map<String, AbstractSchemaBootstrap> schemaBootstrapMap,
            ConfigurationAPI configurationImpl,
            ServiceRegistryApi registryAPI) {
        this.schemaBootstrapMap = new HashMap<>(schemaBootstrapMap);
        String portSTR = System.getenv(APP_PORT_ENV_NAME);
        int appPort = 8080;
        if (portSTR != null) {
            appPort = Integer.valueOf(portSTR);
        }

        // Registering web server configuration
        getServiceRegistryApi().registerWebServer(
                "default",
                new UndertowWebServerConfiguration(appPort));

        // Registering messaging configuration
        getServiceRegistryApi().registerMessaging(
                MessagingProviderConfiguration.DEFAULT_MESSAGING_SYSTEM_NAME,
                new DevMessagingProviderConfiguration());

        // Registering messaging stats configuration in registry
        getServiceRegistryApi().registerExternalResource(
                MessagingStatsResourceDescriptor.MESSAGING_STATS_DESCRIPTOR_NAME,
                new HashMap<>());

        // Registering messaging default blobstore
        getServiceRegistryApi().registerBlobStore(
                DevMessagingServer.DEV_MESSAGING_BLOBSTORE_NAME,
                new DevBlobStoreConfiguration(DevMessagingServer.DEV_MESSAGING_BLOBSTORE_NAME));

        new MicroServiceRunner().run(this, new DevModeManagerMicroService());
    }

    @Override
    public void preRunServiceHook(Context context) {
        try {
            DevModeHelper.registerServiceDependencies(
                    context,
                    -1,
                    getSchemaBootstrapMap(),
                    this,
                    getOverridenResourceProperties());
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Failed setting up dev-mode", e);
        }
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
    public String getNodeAddress() {
        return "localhost";
    }

    @NoJavadoc
    public void scaleService(String serviceURI) {
        Class<? extends MicroService> msClass = deployedMicroServiceClassesByServiceURI.get(serviceURI);
        if (msClass == null) {
            throw new IllegalArgumentException("Unsupported service uri " + serviceURI);
        }

        try {
            new MicroServiceRunner().run(this, msClass.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(
                    "Failed initializing service " + serviceURI + " of class " + msClass.getCanonicalName());
        }
    }

    @Override
    public WebAPIResolver getWebAPIResolver() {
        return devApiResolver;
    }

    public void addDeployedMicroServiceClassByServiceURI(String uri, Class<? extends MicroService> microServiceClass) {
        deployedMicroServiceClassesByServiceURI.put(uri, microServiceClass);
    }

    public Map<String, AbstractSchemaBootstrap> getSchemaBootstrapMap() {
        return new HashMap<>(schemaBootstrapMap);
    }
}
