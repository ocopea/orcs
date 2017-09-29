// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.commands;

import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.crb.CrbWebApi;
import com.emc.ocopea.crb.Info;
import com.emc.ocopea.site.RegisterCrbToSiteCommandArgs;
import com.emc.ocopea.site.Site;
import com.emc.ocopea.site.SiteRepository;
import com.emc.ocopea.site.copy.CopyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.InternalServerErrorException;

/**
 * Created by liebea on 1/3/16.
 * Drink responsibly
 */
public class RegisterCrbToSiteCommand extends SiteCommand<RegisterCrbToSiteCommandArgs, Void> {
    private final Logger log = LoggerFactory.getLogger(RegisterCrbToSiteCommand.class);
    private final SiteRepository siteRepository;
    private final WebAPIResolver webAPIResolver;

    public RegisterCrbToSiteCommand(SiteRepository siteRepository, WebAPIResolver webAPIResolver) {
        this.siteRepository = siteRepository;
        this.webAPIResolver = webAPIResolver;
    }

    @Override
    protected Void run(RegisterCrbToSiteCommandArgs args) {

        // Validate input
        final String crbUrn = validateEmptyField("crbUrn", args.getCrbUrn());
        final String crbUrl = validateEmptyField("crbUrl", args.getCrbUrl());
        log.info("Trying to add CRB using URN {}; URL {}", crbUrn, crbUrl);

        // Connecting to the remote endpoint
        CrbWebApi remoteCrbConnection = webAPIResolver.getWebAPI(crbUrl, CrbWebApi.class);
        if (remoteCrbConnection == null) {
            throw new InternalServerErrorException("Failed locating crb with url " + crbUrl);
        }

        // Getting Crb Info
        Info crbInfo = remoteCrbConnection.getInfo();

        Site site = siteRepository.load();
        site.addCopyRepository(new CopyRepository(
                crbUrn,
                crbUrl,
                crbInfo.getName(),
                crbInfo.getRepoType(),
                crbInfo.getVersion()));

        siteRepository.persist(site);
        return null;
    }

}
