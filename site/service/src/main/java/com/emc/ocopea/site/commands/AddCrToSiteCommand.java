// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.commands;

import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.crb.CrbWebApi;
import com.emc.ocopea.crb.RepositoryInfo;
import com.emc.ocopea.crb.SupportedProtocol;
import com.emc.ocopea.crb.SupportedProtocolCopyLocation;
import com.emc.ocopea.crb.SupportedProtocolCredentials;
import com.emc.ocopea.crb.SupportedProtocolEndpoint;
import com.emc.ocopea.site.AddCrToSiteCommandArgs;
import com.emc.ocopea.site.Site;
import com.emc.ocopea.site.SiteRepository;
import com.emc.ocopea.site.copy.CopyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.InternalServerErrorException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liebea on 1/3/16.
 * Drink responsibly
 */
public class AddCrToSiteCommand extends SiteCommand<AddCrToSiteCommandArgs, Void> {
    private final Logger log = LoggerFactory.getLogger(AddCrToSiteCommand.class);
    private final SiteRepository siteRepository;
    private final WebAPIResolver webAPIResolver;

    public AddCrToSiteCommand(
            SiteRepository siteRepository,
            WebAPIResolver webAPIResolver) {
        this.siteRepository = siteRepository;
        this.webAPIResolver = webAPIResolver;
    }

    @Override
    protected Void run(AddCrToSiteCommandArgs args) {
        final String crbUrn = validateEmptyField("crbUrn", args.getCrbUrn());
        final String crName = validateEmptyField("crName", args.getCrName());
        final Map<String, String> crProperties = validateEmptyField("crProperties", args.getCrProperties());
        final UUID repoId = UUID.randomUUID();

        log.info("Trying to add cr {} to crb {} (using id {})", crName, crbUrn, repoId);

        final Site site = siteRepository.load();
        final CopyRepository crb = site.getCopyRepositoryByUrn(crbUrn);
        if (crb == null) {
            throw new InternalServerErrorException(
                    "CRB with urn " + crbUrn + " is not registered on on site " + site.getName());
        }

        // Connecting to the remote endpoint
        final CopyRepository crbInstance = site.getCopyRepositoryByUrn(crbUrn);
        if (crbInstance == null) {
            throw new InternalServerErrorException("Failed locating crb with urn " + crbUrn);
        }

        // Adding CR to CRB (Temp implementation specific to fs-crb)
        String addr = validateEmptyField("crProperties.addr", crProperties.get("addr"));
        String user = validateEmptyField("crProperties.user", crProperties.get("user"));
        String password = validateEmptyField("crProperties.password", crProperties.get("password"));

        webAPIResolver.getWebAPI(crbInstance.getUrl(), CrbWebApi.class).storeRepositoryInfo(
                repoId.toString(),
                new RepositoryInfo(
                        repoId.toString(),
                        args.getCrName(),
                        Collections.singletonList(new SupportedProtocol(
                                "nfs",
                                "4.1",
                                new SupportedProtocolEndpoint(addr, Collections.emptyMap()),
                                new SupportedProtocolCredentials(user, password, Collections.emptyMap()),
                                new SupportedProtocolCopyLocation(addr, Collections.emptyMap()))),
                        Collections.emptyMap()));

        return null;
    }
}
