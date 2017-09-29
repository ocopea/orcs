// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.jmsl.dev.server;

import com.emc.dpa.dev.DevResourceProvider;
import com.emc.microservice.bootstrap.AbstractSchemaBootstrap;
import com.emc.microservice.configuration.ConfigurationMicroservice;
import com.emc.microservice.configuration.bootstrap.ConfigurationSchemaBootstrap;
import com.emc.microservice.runner.MicroServiceRunner;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liebea on 5/6/16.
 * Drink responsibly
 */
public class RemoteDevServerMain {

    @NoJavadoc
    public static void main(String[] args) throws Exception {
        Map<String, AbstractSchemaBootstrap> schemaBootstrapMap = new HashMap<>();
        schemaBootstrapMap.put("config-db", new ConfigurationSchemaBootstrap());

        DevResourceProvider resourceProvider = new DevResourceProvider(schemaBootstrapMap);
        new MicroServiceRunner().run(
                resourceProvider,
                new ConfigurationMicroservice(),
                new RemoteDevServerMicroService());
    }
}
