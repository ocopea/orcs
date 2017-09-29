// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.configuration;

import com.emc.ocopea.util.JsonUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by liebea on 7/20/17.
 * Drink responsibly
 */
public class StaticResourceConfigurationImpl extends StaticConfigurationImpl {

    public StaticResourceConfigurationImpl(String resourcePath) {
        super(readRootNode(resourcePath));
    }

    private static StaticConfigurationNode readRootNode(String resourcePath) {
        try (final InputStream resourceAsStream = StaticResourceConfigurationImpl.class.getClassLoader()
                .getResourceAsStream(resourcePath)) {
            if (resourceAsStream == null) {
                throw new IllegalStateException("Could not find resource " + resourcePath + " with configuration");
            }

            return JsonUtil.fromJson(StaticConfigurationNode.class, resourceAsStream);

        } catch (IOException e) {
            throw new IllegalStateException("Failed reading resource " + resourcePath, e);
        }
    }
}
