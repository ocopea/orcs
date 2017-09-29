// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.commands;

import com.emc.ocopea.site.RemoveCrbFromSiteCommandArgs;
import com.emc.ocopea.site.Site;
import com.emc.ocopea.site.SiteRepository;

/**
 * Created by liebea on 1/15/17.
 * Drink responsibly
 */
public class RemoveCrbFromSiteCommand extends SiteCommand<RemoveCrbFromSiteCommandArgs, Void> {

    private final SiteRepository siteRepository;

    public RemoveCrbFromSiteCommand(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @Override
    protected Void run(RemoveCrbFromSiteCommandArgs args) {

        final String crbUrn = validateEmptyField("crbUrn", args.getCrbUrn());

        // Loading site from repository
        final Site site = siteRepository.load();

        // Removing the DSB
        site.removeCrb(crbUrn);

        // Persisting changes to site
        siteRepository.persist(site);

        return null;
    }
}
