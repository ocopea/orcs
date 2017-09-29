// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import java.util.Map;

/**
 * Created by liebea on 1/15/17.
 * Drink responsibly
 */
public class SiteArtifactRegistryInfoDTO {
    private final String name;
    private final String type;
    private final Map<String, String> parameters;

    private SiteArtifactRegistryInfoDTO() {
        this(null, null, null);
    }

    public SiteArtifactRegistryInfoDTO(String name, String type, Map<String, String> parameters) {
        this.name = name;
        this.type = type;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "SiteArtifactRegistryInfoDTO{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
