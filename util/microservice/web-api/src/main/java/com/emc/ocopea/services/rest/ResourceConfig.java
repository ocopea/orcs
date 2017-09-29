// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.services.rest;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.Map;

/**
 * Created by liebea on 1/12/15.
 * Drink responsibly
 */
public class ResourceConfig {
    private final String name;

    @JsonUnwrapped
    private final Map<String, String> properties;

    // Required by  jackson
    private ResourceConfig() {
        this(null, null);
    }

    public ResourceConfig(String name, Map<String, String> properties) {
        this.name = name;
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    @JsonUnwrapped
    public Map<String, String> getProperties() {
        return properties;
    }
}
