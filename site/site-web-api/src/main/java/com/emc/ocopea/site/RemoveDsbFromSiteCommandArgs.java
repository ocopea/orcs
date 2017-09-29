// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

/**
 * Created by liebea on 3/13/17.
 * Drink responsibly
 */
public class RemoveDsbFromSiteCommandArgs implements SiteCommandArgs {
    private final String dsbUrn;

    private RemoveDsbFromSiteCommandArgs() {
        this(null);
    }

    public RemoveDsbFromSiteCommandArgs(String urn) {
        dsbUrn = urn;
    }

    public String getDsbUrn() {
        return dsbUrn;
    }

    @Override
    public String toString() {
        return "RemoveDsbFromSiteCommandArgs{" +
                "dsbUrn='" + dsbUrn + '\'' +
                '}';
    }
}
