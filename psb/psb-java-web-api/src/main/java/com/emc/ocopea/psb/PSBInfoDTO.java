// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.psb;

/**
 * Created by liebea on 7/21/15.
 * Drink responsibly
 */
public class PSBInfoDTO {
    private final String name;
    private final String version;
    private final String type;
    private final String description;
    private final int appServiceIdMaxLength;

    private PSBInfoDTO() {
        this(null, null, null, null, 0);
    }

    public PSBInfoDTO(String name, String version, String type, String description, int appServiceIdMaxLength) {
        this.name = name;
        this.version = version;
        this.type = type;
        this.description = description;
        this.appServiceIdMaxLength = appServiceIdMaxLength;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public int getAppServiceIdMaxLength() {
        return appServiceIdMaxLength;
    }

    @Override
    public String toString() {
        return "PSBInfoDTO{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", appServiceIdMaxLength=" + appServiceIdMaxLength +
                '}';
    }
}
