// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.site;

import com.emc.microservice.discovery.WebAPIConnection;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.site.SiteLocationDTO;

import javax.ws.rs.client.WebTarget;
import java.util.UUID;

/**
 * Created by liebea on 11/29/15.
 * Drink responsibly
 */
public class Site {
    private final UUID id;
    private final String name;
    private final String urn;
    private final String url;
    private final String version;
    private final SiteLocationDTO location;
    private final String publicDns;
    private final WebAPIResolver webAPIResolver;

    public Site(
            UUID id,
            String name,
            String urn,
            String url,
            String version,
            SiteLocationDTO location,
            String publicDns,
            WebAPIResolver webAPIResolver) {
        this.id = id;
        this.name = name;
        this.urn = urn;
        this.url = url;
        this.version = version;
        this.location = location;
        this.publicDns = publicDns;
        this.webAPIResolver = webAPIResolver;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrn() {
        return urn;
    }

    public String getUrl() {
        return url;
    }

    public String getVersion() {
        return version;
    }

    public SiteLocationDTO getLocation() {
        return location;
    }

    public String getPublicDns() {
        return publicDns;
    }

    public WebAPIConnection getWebAPIConnection() {
        return new WebAPIConnection() {
            @Override
            public <T> T resolve(Class<T> clazz) {
                return webAPIResolver.getWebAPI(url, clazz);
            }

            @Override
            public WebTarget getWebTarget() {
                return webAPIResolver.getWebTarget(url);
            }
        };
    }

    @Override
    public String toString() {
        return "Site{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", urn='" + urn + '\'' +
                ", url='" + url + '\'' +
                ", version='" + version + '\'' +
                ", location=" + location +
                ", publicDns='" + publicDns + '\'' +
                '}';
    }
}


