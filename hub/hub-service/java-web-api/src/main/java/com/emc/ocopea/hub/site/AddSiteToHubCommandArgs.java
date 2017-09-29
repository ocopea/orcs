// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.site;

/**
 * Created by liebea on 12/13/15.
 * Drink responsibly
 */
public class AddSiteToHubCommandArgs {
    private final String urn;
    private final String url;

    private AddSiteToHubCommandArgs() {
        this(null, null);
    }

    public AddSiteToHubCommandArgs(String urn, String url) {
        this.urn = urn;
        this.url = url;
    }

    public String getUrn() {
        return urn;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "AddSiteToHubCommandArgs{" +
                "urn='" + urn + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
