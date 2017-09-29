// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

/**
 * Created by liebea on 1/3/16.
 * Drink responsibly
 */
public class RegisterDsbToSiteCommandArgs implements SiteCommandArgs {
    private final String dsbUrn;
    private final String dsbUrl;

    private RegisterDsbToSiteCommandArgs() {
        this(null, null);
    }

    public RegisterDsbToSiteCommandArgs(String dsbUrn, String dsbUrl) {
        this.dsbUrn = dsbUrn;
        this.dsbUrl = dsbUrl;
    }

    public String getDsbUrn() {
        return dsbUrn;
    }

    public String getDsbUrl() {
        return dsbUrl;
    }

    @Override
    public String toString() {
        return "RegisterDsbToSiteCommandArgs{" +
                "dsbUrn='" + dsbUrn + '\'' +
                ", dsbUrl='" + dsbUrl + '\'' +
                '}';
    }
}
