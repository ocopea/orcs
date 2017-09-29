// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

/**
 * Created by liebea on 1/15/17.
 * Drink responsibly
 */
public class SiteCopyRepoInfoDTO {
    private final String urn;
    private final String url;
    private final String name;
    private final String type;
    private final String version;

    private SiteCopyRepoInfoDTO() {
        this(null, null, null, null, null);
    }

    public SiteCopyRepoInfoDTO(String urn, String url, String name, String type, String version) {
        this.urn = urn;
        this.url = url;
        this.name = name;
        this.type = type;
        this.version = version;
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

    public String getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "SiteCopyRepoInfoDTO{" +
                "urn='" + urn + '\'' +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
