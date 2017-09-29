// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.psb;

import java.util.Map;

/**
 * Spaces represent a namespace in PaaS that allows isolation within a single "cluster"
 * In CF it is space, in Kubernetes "namespace".
 */
public class PSBSpaceDTO {
    private final String name;

    // Properties are different in each and every platform. allowing in the api to express that
    private final Map<String, String> properties;

    private PSBSpaceDTO() {
        this(null, null);
    }

    public PSBSpaceDTO(String name, Map<String, String> properties) {
        this.name = name;
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "PSBSpaceDTO{" +
                "name='" + name + '\'' +
                ", properties=" + properties +
                '}';
    }
}
