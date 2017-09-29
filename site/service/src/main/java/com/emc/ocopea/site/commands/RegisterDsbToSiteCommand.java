// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.commands;

import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.dsb.DsbInfo;
import com.emc.ocopea.dsb.DsbWebApi;
import com.emc.ocopea.site.RegisterDsbToSiteCommandArgs;
import com.emc.ocopea.site.Site;
import com.emc.ocopea.site.SiteRepository;
import com.emc.ocopea.site.dsb.Dsb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.InternalServerErrorException;

/**
 * Created by liebea on 1/3/16.
 * Drink responsibly
 */
public class RegisterDsbToSiteCommand extends SiteCommand<RegisterDsbToSiteCommandArgs, Dsb> {
    private final Logger log = LoggerFactory.getLogger(RegisterDsbToSiteCommand.class);
    private final SiteRepository siteRepository;
    private final WebAPIResolver webAPIResolver;

    public RegisterDsbToSiteCommand(SiteRepository siteRepository, WebAPIResolver webAPIResolver) {
        this.siteRepository = siteRepository;
        this.webAPIResolver = webAPIResolver;
    }

    @Override
    protected Dsb run(RegisterDsbToSiteCommandArgs args) {

        // Validate input
        final String dsbUrn = validateEmptyField("dsbUrn", args.getDsbUrn());
        final String dsbUrl = validateEmptyField("dsbUrl", args.getDsbUrl());
        log.info("Trying to add dsb using URN {}; URL {}", dsbUrn, dsbUrl);

        // Getting DSB Info, verifying connectivity
        final DsbInfo dsbInfo = getDsbInfo(dsbUrl);

        // Loading site from repository
        final Site site = siteRepository.load();

        // Adding dsb info to site
        Dsb newDsb = site.addDSB(
                new Dsb(
                        dsbInfo.getName(),
                        dsbUrn,
                        dsbUrl,
                        dsbInfo.getType(),
                        dsbInfo.getDescription(),
                        dsbInfo.getPlans()));

        // persisting the site info
        siteRepository.persist(site);
        return newDsb;

    }

    private DsbInfo getDsbInfo(String dsbUrl) {

        // Connecting to the remote endpoint
        try {
            return webAPIResolver.getWebAPI(dsbUrl, DsbWebApi.class).getDSBInfo();
        } catch (Exception ex) {
            throw new InternalServerErrorException("failed connecting to dsb with url " + dsbUrl, ex);
        }
    }

}
