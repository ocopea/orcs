// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

/**
 * Created by liebea on 1/10/16.
 * Drink responsibly
 */
public class SiteInfoDto {
    private final String name;
    private final String version;
    private final String publicDNS;
    private final SiteLocationDTO location;

    public SiteInfoDto() {
        this(null, null, null, null);
    }

    public SiteInfoDto(String name, String version, String publicDNS, SiteLocationDTO location) {
        this.name = name;
        this.version = version;
        this.publicDNS = publicDNS;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getPublicDNS() {
        return publicDNS;
    }

    public SiteLocationDTO getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "SiteInfoDto{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", publicDNS='" + publicDNS + '\'' +
                ", location=" + location +
                '}';
    }
}
