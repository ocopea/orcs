// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.commands;

import com.emc.ocopea.site.RemoveDsbFromSiteCommandArgs;
import com.emc.ocopea.site.Site;
import com.emc.ocopea.site.SiteRepository;

/**
 * Created by liebea on 1/15/17.
 * Drink responsibly
 */
public class RemoveDsbFromSiteCommand extends SiteCommand<RemoveDsbFromSiteCommandArgs, Void> {

    private final SiteRepository siteRepository;

    public RemoveDsbFromSiteCommand(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @Override
    protected Void run(RemoveDsbFromSiteCommandArgs args) {

        final String dsbUrn = validateEmptyField("dsbUrn", args.getDsbUrn());

        // Loading site from repository
        final Site site = siteRepository.load();

        // Removing the DSB
        site.removeDsb(dsbUrn);

        // Persisting changes to site
        siteRepository.persist(site);

        return null;
    }
}
