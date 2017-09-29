// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.services.rest;

/**
 * Created by liebea on 1/12/15.
 * Drink responsibly
 */
public class ParamConfig {
    private final String name;
    private final String description;
    private final String value;

    // Required by  jackson
    private ParamConfig() {
        this(null, null, null);
    }

    public ParamConfig(String name, String description, String value) {
        this.name = name;
        this.description = description;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getValue() {
        return value;
    }
}
