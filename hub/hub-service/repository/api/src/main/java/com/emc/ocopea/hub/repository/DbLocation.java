// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Created by liebea on 7/5/16.
 * Drink responsibly
 */
public class DbLocation {
    private final double latitude;
    private final double longitude;
    private final String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Map<String, String> properties;

    private DbLocation() {
        this(0D,0D,null,null);
    }

    public DbLocation(double latitude, double longitude, String name, Map<String, String> properties) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.properties = properties;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "DBLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", name='" + name + '\'' +
                ", properties=" + properties +
                '}';
    }
}
