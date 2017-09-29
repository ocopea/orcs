// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.copy;

/**
 * Created by liebea on 1/3/16.
 * Drink responsibly
 */
public class CopyRepository {
    private final String urn;
    private final String url;
    private final String name;
    private final String type;
    private final String version;

    public CopyRepository(String urn, String url, String name, String type, String version) {
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
        return "CopyRepository{" +
                "urn='" + urn + '\'' +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
