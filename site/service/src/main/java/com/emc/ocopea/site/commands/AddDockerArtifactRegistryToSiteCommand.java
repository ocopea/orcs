// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.commands;

import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.site.AddDockerArtifactRegistryToSiteCommandArgs;
import com.emc.ocopea.site.Site;
import com.emc.ocopea.site.SiteRepository;
import com.emc.ocopea.site.artifact.ArtifactRegistryApi;
import com.emc.ocopea.site.artifact.ArtifactRegistryFactory;
import com.emc.ocopea.site.artifact.SiteArtifactRegistry;
import com.emc.ocopea.util.MapBuilder;

import java.util.Map;

public class AddDockerArtifactRegistryToSiteCommand
        extends SiteCommand<AddDockerArtifactRegistryToSiteCommandArgs, Void> {

    private final SiteRepository siteRepository;
    private final ArtifactRegistryFactory artifactRegistryFactory;
    private final WebAPIResolver webAPIResolver;

    public AddDockerArtifactRegistryToSiteCommand(
            SiteRepository siteRepository,
            ArtifactRegistryFactory artifactRegistryFactory,
            WebAPIResolver webAPIResolver) {
        this.siteRepository = siteRepository;
        this.artifactRegistryFactory = artifactRegistryFactory;
        this.webAPIResolver = webAPIResolver;
    }

    @Override
    protected Void run(AddDockerArtifactRegistryToSiteCommandArgs args) {
        validateEmptyField("name", args.getName());
        final String url = validateEmptyField("url", args.getUrl());

        final Map<String, String> parameters = MapBuilder.<String, String>newHashMap()
                .with("url", url)
                .with("username", args.getUsername())
                .with("password", args.getPassword())
                .build();

        ArtifactRegistryApi dockerArtifactRegistry =
                artifactRegistryFactory.create(
                        SiteArtifactRegistry.ArtifactRegistryType.dockerRegistry,
                        parameters,
                        webAPIResolver);

        // Loading site from repository
        final Site site = siteRepository.load();

        site.addArtifactRegistry(
                new SiteArtifactRegistry(
                        args.getName(),
                        SiteArtifactRegistry.ArtifactRegistryType.dockerRegistry,
                        parameters,
                        dockerArtifactRegistry));

        // Persisting changes to site
        siteRepository.persist(site);

        return null;
    }
}
