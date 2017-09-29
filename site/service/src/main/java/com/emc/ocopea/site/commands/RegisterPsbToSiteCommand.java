// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.commands;

import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.psb.PSBInfoDTO;
import com.emc.ocopea.psb.PSBWebAPI;
import com.emc.ocopea.site.Psb;
import com.emc.ocopea.site.RegisterPsbToSiteCommandArgs;
import com.emc.ocopea.site.Site;
import com.emc.ocopea.site.SiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.InternalServerErrorException;

/**
 * Registers a PSB to the site
 */
public class RegisterPsbToSiteCommand extends SiteCommand<RegisterPsbToSiteCommandArgs, Void> {
    private static final Logger log = LoggerFactory.getLogger(RegisterPsbToSiteCommand.class);
    private final SiteRepository siteRepository;
    private final WebAPIResolver webAPIResolver;

    public RegisterPsbToSiteCommand(
            SiteRepository siteRepository,
            WebAPIResolver webAPIResolver) {
        this.siteRepository = siteRepository;
        this.webAPIResolver = webAPIResolver;
    }

    @Override
    protected Void run(RegisterPsbToSiteCommandArgs args) {

        // Validate input
        final String psbUrn = validateEmptyField("psbUrn", args.getPsbUrn());
        final String psbUrl = validateEmptyField("psbUrl", args.getPsbUrl());
        log.info("Trying to add PSB using URN {}; URL {}", psbUrn, psbUrl);
        PSBInfoDTO psbInfo = getPsbInfo(psbUrl);

        Site site = siteRepository.load();
        site.addPSB(
                new Psb(
                        psbUrn,
                        psbUrl,
                        psbInfo.getType(),
                        psbInfo.getName(),
                        psbInfo.getVersion(),
                        psbInfo.getAppServiceIdMaxLength()));

        siteRepository.persist(site);
        return null;
    }

    private PSBInfoDTO getPsbInfo(String psbUrl) {
        // Getting PSB Info
        try {
            return webAPIResolver.getWebAPI(psbUrl, PSBWebAPI.class).getPSBInfo();
        } catch (Exception ex) {
            throw new InternalServerErrorException(
                    "Failed accessing psb info via url " + psbUrl + " - " + ex.getMessage(), ex);
        }
    }

}
