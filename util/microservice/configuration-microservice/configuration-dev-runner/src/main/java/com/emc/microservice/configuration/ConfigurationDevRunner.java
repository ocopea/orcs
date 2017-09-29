// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.configuration;

import com.emc.dpa.dev.DevResourceProvider;
import com.emc.microservice.MicroServiceController;
import com.emc.microservice.bootstrap.AbstractSchemaBootstrap;
import com.emc.microservice.configuration.bootstrap.ConfigurationSchemaBootstrap;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.microservice.runner.MicroServiceRunner;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shresa on 04/08/15
 */
public class ConfigurationDevRunner {
    private static Map<String, MicroServiceController> services;

    public static void main(String[] args) {
        run();
    }

    @NoJavadoc
    public static ResourceProvider run() {
        try {
            Map<String, AbstractSchemaBootstrap> schemaMap = new HashMap<>();
            ConfigurationSchemaBootstrap schemaBootstrap = new ConfigurationSchemaBootstrap();
            schemaMap.put(ConfigurationMicroservice.CONFIG_DB, schemaBootstrap);

            ResourceProvider resourceProvider = new DevResourceProvider(schemaMap);
            services = new MicroServiceRunner().run(resourceProvider, new ConfigurationMicroservice());
            return resourceProvider;
        } catch (IOException | SQLException err) {
            throw new IllegalStateException("Unable to start configuration microservice.", err);
        }
    }

    public static void stop() {
        services.values().forEach(MicroServiceController::stop);
    }
}
