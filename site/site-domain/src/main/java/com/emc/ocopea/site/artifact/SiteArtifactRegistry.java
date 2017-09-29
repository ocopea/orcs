// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.artifact;

import java.util.Map;

/**
 * Created by liebea on 1/15/17.
 * Drink responsibly
 */
public class SiteArtifactRegistry {
    public SiteArtifactRegistry(String name, ArtifactRegistryType type, Map<String, String> parameters,
                                ArtifactRegistryApi api) {
        this.name = name;
        this.type = type;
        this.parameters = parameters;
        this.api = api;
    }

    public enum ArtifactRegistryType {
        mavenRepository,
        dockerRegistry,
        customRest
    }

    private final String name;
    private final ArtifactRegistryType type;
    private final Map<String, String> parameters;
    private final ArtifactRegistryApi api;

    public String getName() {
        return name;
    }

    public ArtifactRegistryType getType() {
        return type;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public ArtifactRegistryApi getApi() {
        return api;
    }
}
