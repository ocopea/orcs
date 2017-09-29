// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.configuration;

import java.util.Map;

/**
 * Created by liebea on 7/20/17.
 * Drink responsibly
 */
public class StaticConfigurationNode {
    private final Object data;
    private final Map<String, StaticConfigurationNode> children;

    private StaticConfigurationNode() {
        this(null, null);
    }

    public StaticConfigurationNode(
            Object data,
            Map<String, StaticConfigurationNode> children) {
        this.data = data;
        this.children = children;
    }

    public Object getData() {
        return data;
    }

    public Map<String, StaticConfigurationNode> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "StaticFileConfigurationNode{" +
                "data='" + data + '\'' +
                ", children=" + children +
                '}';
    }
}
