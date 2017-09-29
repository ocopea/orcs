// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.

package com.emc.ocopea.site;

/**
 * Created by liebea on 3/13/17.
 * Drink responsibly
 */
public class RemoveCrbFromSiteCommandArgs implements SiteCommandArgs {
    private final String crbUrn;

    private RemoveCrbFromSiteCommandArgs() {
        this(null);
    }

    public RemoveCrbFromSiteCommandArgs(String urn) {
        crbUrn = urn;
    }

    public String getCrbUrn() {
        return crbUrn;
    }

    @Override
    public String toString() {
        return "RemoveCrbFromSiteCommandArgs{" +
                "crbUrn='" + crbUrn + '\'' +
                '}';
    }
}
