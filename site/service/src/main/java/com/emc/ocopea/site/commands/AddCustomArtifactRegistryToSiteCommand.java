// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.commands;

import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.site.AddCustomArtifactRegistryToSiteCommandArgs;
import com.emc.ocopea.site.Site;
import com.emc.ocopea.site.SiteRepository;
import com.emc.ocopea.site.artifact.ArtifactRegistryApi;
import com.emc.ocopea.site.artifact.ArtifactRegistryFactory;
import com.emc.ocopea.site.artifact.SiteArtifactRegistry;
import com.emc.ocopea.util.MapBuilder;

import java.util.Map;

/**
 * Created by liebea on 1/15/17.
 * Drink responsibly
 */
public class AddCustomArtifactRegistryToSiteCommand
        extends SiteCommand<AddCustomArtifactRegistryToSiteCommandArgs, Void> {

    private final SiteRepository siteRepository;
    private final ArtifactRegistryFactory artifactRegistryFactory;
    private final WebAPIResolver webAPIResolver;

    public AddCustomArtifactRegistryToSiteCommand(
            SiteRepository siteRepository,
            ArtifactRegistryFactory artifactRegistryFactory,
            WebAPIResolver webAPIResolver) {
        this.siteRepository = siteRepository;
        this.artifactRegistryFactory = artifactRegistryFactory;
        this.webAPIResolver = webAPIResolver;
    }

    @Override
    protected Void run(AddCustomArtifactRegistryToSiteCommandArgs args) {

        // Validating input
        final String name = validateEmptyField("name", args.getName());
        final String url = validateEmptyField("url", args.getUrl());

        final Map<String, String> parameters =
                MapBuilder.<String, String>newHashMap()
                        .with("url", url)
                .build();
        ArtifactRegistryApi restBasedArtifactRegistry = artifactRegistryFactory.create(
                SiteArtifactRegistry.ArtifactRegistryType.customRest,
                parameters,
                webAPIResolver);

        // Loading site from repository
        final Site site = siteRepository.load();
        site.addArtifactRegistry(
                new SiteArtifactRegistry(
                        name,
                        SiteArtifactRegistry.ArtifactRegistryType.customRest,
                        parameters,
                        restBasedArtifactRegistry));

        // Persisting changes to site
        siteRepository.persist(site);

        return null;
    }
}
