// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import java.util.Map;

public class AddCrToSiteCommandArgs implements SiteCommandArgs {
    private final String crbUrn;
    private final String crName;
    private final Map<String, String> crProperties;

    private AddCrToSiteCommandArgs() {
        this(null, null, null);
    }

    public AddCrToSiteCommandArgs(
            String crbUrn,
            String crName,
            Map<String, String> crProperties) {
        this.crbUrn = crbUrn;
        this.crName = crName;
        this.crProperties = crProperties;
    }

    public String getCrbUrn() {
        return crbUrn;
    }

    public String getCrName() {
        return crName;
    }

    public Map<String, String> getCrProperties() {
        return crProperties;
    }

    @Override
    public String toString() {
        return "AddCrToSiteCommandArgs{" +
                "crbUrn='" + crbUrn + '\'' +
                ", crName='" + crName + '\'' +
                ", crProperties=" + crProperties +
                '}';
    }
}
