// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.configuration;

import com.emc.microservice.config.ConfigurationAPI;
import com.emc.ocopea.util.JsonUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by liebea on 7/20/17.
 * Drink responsibly
 */
abstract class StaticConfigurationImpl implements ConfigurationAPI {
    private final Map<String, StaticConfigurationNode> configurationCache = new HashMap<>();

    protected StaticConfigurationImpl(StaticConfigurationNode rootConfigurationNode) {
        initCache(null, null, rootConfigurationNode);
    }

    private void initCache(String rootPath, String path, StaticConfigurationNode configurationNode) {
        final String nextPath;
        if (rootPath == null) {
            nextPath = path;
            configurationCache.put(nextPath, configurationNode);
        } else {
            if ("".equals(rootPath)) {
                nextPath = path;
            } else {
                nextPath = rootPath + "/" + path;
            }
            configurationCache.put(nextPath, configurationNode);
        }

        if (configurationNode.getChildren() != null) {
            configurationNode.getChildren().entrySet().forEach(
                    childEntry -> initCache(nextPath, childEntry.getKey(), childEntry.getValue()));
        }
    }

    @Override
    public Collection<String> list(String path) {
        final StaticConfigurationNode node = configurationCache.get(path);
        if (node == null) {
            return Collections.emptyList();
        } else {
            return node.getChildren()
                    .keySet()
                    .stream()
                    .map(k -> path + "/" + k)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public String readData(String path) {
        final StaticConfigurationNode node = configurationCache.get(path);
        if (node == null) {
            return null;
        }
        return JsonUtil.toPrettyJson(node.getData());
    }

    @Override
    public boolean isDirectory(String path) {
        return configurationCache.containsKey(path) && !configurationCache.get(path).getChildren().isEmpty();
    }

    @Override
    public boolean exists(String path) {
        return configurationCache.containsKey(path);
    }

    @Override
    public void writeData(String path, String data) {
        throw new UnsupportedOperationException("This configuration implementation does not support writing data");
    }

    @Override
    public void mkdir(String path) {
        throw new UnsupportedOperationException("This configuration implementation does not support writing data");
    }

}
