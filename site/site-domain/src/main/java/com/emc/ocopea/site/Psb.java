// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

/**
 * Created by liebea on 1/3/17.
 * Drink responsibly
 */
public class Psb {
    private final String urn;
    private final String url;
    private final String type;
    private final String name;
    private final String version;
    private final int maxAppServiceIdLength;

    public Psb(String urn, String url, String type, String name, String version, int maxAppServiceIdLength) {
        this.urn = urn;
        this.url = url;
        this.type = type;
        this.name = name;
        this.version = version;
        this.maxAppServiceIdLength = maxAppServiceIdLength;
    }

    public String getUrn() {
        return urn;
    }

    public String getUrl() {
        return url;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public int getMaxAppServiceIdLength() {
        return maxAppServiceIdLength;
    }
}
