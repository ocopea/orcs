// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

public class RegisterPsbToSiteCommandArgs implements SiteCommandArgs {
    private final String psbUrn;
    private final String psbUrl;

    private RegisterPsbToSiteCommandArgs() {
        this(null, null);
    }

    public RegisterPsbToSiteCommandArgs(String psbUrn, String psbUrl) {
        this.psbUrn = psbUrn;
        this.psbUrl = psbUrl;
    }

    public String getPsbUrn() {
        return psbUrn;
    }

    public String getPsbUrl() {
        return psbUrl;
    }

    @Override
    public String toString() {
        return "RegisterPsbToSiteCommandArgs{" +
                "psbUrn='" + psbUrn + '\'' +
                ", psbUrl='" + psbUrl + '\'' +
                '}';
    }
}
