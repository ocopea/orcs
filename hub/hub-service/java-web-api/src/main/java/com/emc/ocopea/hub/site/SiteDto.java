// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.site;

import com.emc.ocopea.site.SiteLocationDTO;

import java.util.UUID;

/**
 * Created by liebea on 12/13/15.
 * Drink responsibly
 */
public class SiteDto {
    private final UUID id;
    private final String urn;
    private final String url;
    private final String name;
    private final String version;
    private final SiteLocationDTO location;
    private final String publicDns;

    private SiteDto() {
        this(null, null, null, null, null, null, null);
    }

    public SiteDto(
            UUID id,
            String urn,
            String url,
            String name,
            String version,
            SiteLocationDTO location,
            String publicDns) {
        this.id = id;
        this.urn = urn;
        this.url = url;
        this.name = name;
        this.version = version;
        this.location = location;
        this.publicDns = publicDns;
    }

    public UUID getId() {
        return id;
    }

    public String getUrn() {
        return urn;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
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

    @Override
    public String toString() {
        return "SiteDto{" +
                "id=" + id +
                ", urn='" + urn + '\'' +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", location=" + location +
                ", publicDns='" + publicDns + '\'' +
                '}';
    }
}
