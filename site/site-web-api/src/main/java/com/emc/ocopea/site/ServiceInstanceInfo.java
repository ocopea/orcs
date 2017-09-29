// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

/**
 * Created by liebea on 7/21/15.
 * Drink responsibly
 */
public class ServiceInstanceInfo {
    private final String name;
    private final String dsb;
    private final String type;
    private final String basedOnCopyId;

    private ServiceInstanceInfo() {
        this(null, null, null, null);
    }

    public ServiceInstanceInfo(String name, String dsb, String type, String basedOnCopyId) {
        this.name = name;
        this.dsb = dsb;
        this.type = type;
        this.basedOnCopyId = basedOnCopyId;
    }

    public String getDsb() {
        return dsb;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getBasedOnCopyId() {
        return basedOnCopyId;
    }
}
