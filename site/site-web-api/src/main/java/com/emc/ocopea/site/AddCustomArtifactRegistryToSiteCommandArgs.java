// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

/**
 * Created by liebea on 1/15/17.
 * Drink responsibly
 */
public class AddCustomArtifactRegistryToSiteCommandArgs implements SiteCommandArgs {
    private final String name;
    private final String url;

    private AddCustomArtifactRegistryToSiteCommandArgs() {
        this(null, null);
    }

    public AddCustomArtifactRegistryToSiteCommandArgs(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "AddCustomArtifactRegistryToSiteCommandArgs{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
