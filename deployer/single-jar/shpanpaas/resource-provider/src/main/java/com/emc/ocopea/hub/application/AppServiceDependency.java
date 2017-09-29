// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

/**
 * Created by liebea on 7/22/15.
 * Drink responsibly
 */
public class AppServiceDependency {
    private final String type;
    private final String name;
    private final String description;

    public AppServiceDependency(String type, String name, String description) {
        this.type = type;
        this.name = name;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
