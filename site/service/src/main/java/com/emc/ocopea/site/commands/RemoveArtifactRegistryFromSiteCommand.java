// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.commands;

import com.emc.ocopea.site.RemoveArtifactRegistryFromSiteCommandArgs;
import com.emc.ocopea.site.Site;
import com.emc.ocopea.site.SiteRepository;

/**
 * Created by liebea on 1/15/17.
 * Drink responsibly
 */
public class RemoveArtifactRegistryFromSiteCommand
        extends SiteCommand<RemoveArtifactRegistryFromSiteCommandArgs, Void> {

    private final SiteRepository siteRepository;

    public RemoveArtifactRegistryFromSiteCommand(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @Override
    protected Void run(RemoveArtifactRegistryFromSiteCommandArgs args) {
        final String artifactRegistryName = validateEmptyField("ArtifactRegistryName", args.getArtifactRegistryName());

        // Loading site from repository
        final Site site = siteRepository.load();

        // Removing the artifact registry
        site.removeArtifactRegistry(artifactRegistryName);

        // Persisting changes to site
        siteRepository.persist(site);

        return null;
    }
}
