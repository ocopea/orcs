// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.artifact;

import com.emc.microservice.webclient.WebAPIResolver;

import java.util.Map;

/**
 * Created by liebea on 1/15/17.
 * Drink responsibly
 */
public interface ArtifactRegistryFactory {
    ArtifactRegistryApi create(
            SiteArtifactRegistry.ArtifactRegistryType type,
            Map<String, String> parameters,
            WebAPIResolver resolver);
}
