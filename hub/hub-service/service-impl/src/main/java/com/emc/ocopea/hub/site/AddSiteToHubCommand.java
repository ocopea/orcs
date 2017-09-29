// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.site;

import com.emc.ocopea.hub.commands.HubCommand;

import java.net.URI;

/**
 * Created by liebea on 5/17/16.
 * Registering a site on this hub
 */
public class AddSiteToHubCommand extends HubCommand<AddSiteToHubCommandArgs, URI> {
    private final SiteManagerService siteManagerService;

    public AddSiteToHubCommand(SiteManagerService siteManagerService) {
        this.siteManagerService = siteManagerService;
    }

    @Override
    protected URI run(AddSiteToHubCommandArgs addSiteToHubCommandArgs) {
        validateEmptyField("urn", addSiteToHubCommandArgs.getUrn());
        validateEmptyField("url", addSiteToHubCommandArgs.getUrl());
        final Site site = siteManagerService.addSite(
                addSiteToHubCommandArgs.getUrn(),
                addSiteToHubCommandArgs.getUrl()
        );
        return URI.create(site.getId().toString());
    }
}
