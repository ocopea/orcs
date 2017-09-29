// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.artifact;

import com.emc.microservice.webclient.WebAPIResolver;

import java.util.Collection;

/**
 * Created by liebea on 1/15/17.
 * Drink responsibly
 */
public class CustomRestArtifactRegistryImpl implements ArtifactRegistryApi {
    private final WebAPIResolver webAPIResolver;
    private final String url;

    public CustomRestArtifactRegistryImpl(WebAPIResolver webAPIResolver, String url) {
        this.webAPIResolver = webAPIResolver;
        this.url = url;
    }

    @Override
    public Collection<String> listVersions(String artifactId) {
        return webAPIResolver.getWebAPI(url, ArtifactRegistryWebApi.class).listVersions(artifactId);
    }
}
