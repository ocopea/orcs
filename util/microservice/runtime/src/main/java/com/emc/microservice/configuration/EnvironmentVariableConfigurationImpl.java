// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.configuration;

import com.emc.ocopea.util.JsonUtil;

/**
 * Created by liebea on 7/20/17.
 * Drink responsibly
 */
public class EnvironmentVariableConfigurationImpl extends StaticConfigurationImpl {

    public EnvironmentVariableConfigurationImpl(String envVarName) {
        super(readRootNode(envVarName));
    }

    private static StaticConfigurationNode readRootNode(String envVarName) {

        final String confJson = System.getenv(envVarName);
        if (confJson == null) {
            throw new IllegalStateException("Could not find configuration json in env var " + envVarName);
        }

        return JsonUtil.fromJson(StaticConfigurationNode.class, confJson);

    }
}
