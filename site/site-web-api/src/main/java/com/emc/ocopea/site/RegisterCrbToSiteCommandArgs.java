// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

public class RegisterCrbToSiteCommandArgs implements SiteCommandArgs {
    private final String crbUrn;
    private final String crbUrl;

    private RegisterCrbToSiteCommandArgs() {
        this(null, null);
    }

    public RegisterCrbToSiteCommandArgs(String crbUrn, String crbUrl) {
        this.crbUrn = crbUrn;
        this.crbUrl = crbUrl;
    }

    public String getCrbUrn() {
        return crbUrn;
    }

    public String getCrbUrl() {
        return crbUrl;
    }

    @Override
    public String toString() {
        return "RegisterCrbToSiteCommandArgs{" +
                "crbUrn='" + crbUrn + '\'' +
                ", crbUrl='" + crbUrl + '\'' +
                '}';
    }
}
