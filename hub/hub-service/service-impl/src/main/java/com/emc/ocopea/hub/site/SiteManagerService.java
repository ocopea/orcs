// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.site;

import com.emc.microservice.Context;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.hub.repository.DbConnectedSite;
import com.emc.ocopea.hub.repository.DbLocation;
import com.emc.ocopea.hub.repository.DuplicateResourceException;
import com.emc.ocopea.site.SiteInfoDto;
import com.emc.ocopea.site.SiteLocationDTO;
import com.emc.ocopea.site.SiteWebApi;
import com.emc.ocopea.hub.repository.ConnectedSiteRepository;

import javax.ws.rs.WebApplicationException;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by liebea on 11/29/15.
 * Drink responsibly
 */
public class SiteManagerService implements ServiceLifecycle {

    private ConnectedSiteRepository connectedSiteRepository;
    private WebAPIResolver webAPIResolver;

    @Override
    public void init(Context context) {
        webAPIResolver = context.getWebAPIResolver();
        connectedSiteRepository = context.getDynamicJavaServicesManager()
                .getManagedResourceByName(ConnectedSiteRepository.class.getSimpleName()).getInstance();
    }

    @Override
    public void shutDown() {}

    private Site convert(DbConnectedSite dbConnectedSite) {
        return new Site(
                dbConnectedSite.getId(),
                dbConnectedSite.getName(),
                dbConnectedSite.getUrn(),
                dbConnectedSite.getUrl(),
                dbConnectedSite.getVersion(),
                new SiteLocationDTO(
                        dbConnectedSite.getLocation().getLatitude(),
                        dbConnectedSite.getLocation().getLongitude(),
                        dbConnectedSite.getLocation().getName(),
                        dbConnectedSite.getLocation().getProperties()),
                dbConnectedSite.getPublicDns(),
                webAPIResolver);
    }

    public Collection<Site> list() {
        return connectedSiteRepository.list()
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public Site getSiteById(UUID id) {
        final DbConnectedSite byId = connectedSiteRepository.getById(id);
        return byId == null ? null : convert(byId);
    }

    // todo: add javadoc
    @NoJavadoc
    public Site addSite(String siteUrn, String siteUrl) {

        SiteWebApi siteWebApi = webAPIResolver.getWebAPI(siteUrl, SiteWebApi.class);
        SiteInfoDto info = siteWebApi.getSiteInfo();
        if (info == null) {
            throw new IllegalStateException("Failed connecting to site via URL " + siteUrn);
        }

        String publicDns = info.getPublicDNS();
        if (publicDns == null || publicDns.isEmpty()) {
            publicDns = siteUrl;
        }

        final UUID id = UUID.randomUUID();
        final DbConnectedSite connectedSite = new DbConnectedSite(
                id,
                siteUrn,
                new Date(),
                siteUrl,
                info.getName(),
                info.getVersion(),
                convertToDBLocation(info),
                publicDns);
        try {
            connectedSiteRepository.addConnectedSite(connectedSite);
        } catch (DuplicateResourceException e) {
            throw new WebApplicationException("Site already exist " + connectedSite.getUrn(), e, 409);
        }
        return convert(connectedSite);
    }

    private DbLocation convertToDBLocation(SiteInfoDto info) {
        if (info.getLocation() == null) {
            return null;
        }
        return new DbLocation(
                info.getLocation().getLatitude(),
                info.getLocation().getLongitude(),
                info.getLocation().getName(),
                info.getLocation().getProperties());
    }

}
