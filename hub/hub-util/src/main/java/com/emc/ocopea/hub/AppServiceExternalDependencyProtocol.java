// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub;

import java.util.Map;

/**
 * Created by liebea on 12/13/16.
 * Drink responsibly
 */
public class AppServiceExternalDependencyProtocol {
    private final String protocol;
    private final String version;
    private final Map<String, String> conditions;
    private final Map<String, String> settings;

    public AppServiceExternalDependencyProtocol(
            String protocol,
            String version,
            Map<String, String> conditions,
            Map<String, String> settings) {
        this.protocol = protocol;
        this.version = version;
        this.conditions = conditions;
        this.settings = settings;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getConditions() {
        return conditions;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    @Override
    public String toString() {
        return "AppServiceExternalDependencyProtocol{" +
                "protocol='" + protocol + '\'' +
                ", version='" + version + '\'' +
                ", conditions=" + conditions +
                ", settings=" + settings +
                '}';
    }
}
