// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

/**
 * Created by liebea on 3/13/17.
 * Drink responsibly
 */
public class RemoveArtifactRegistryFromSiteCommandArgs implements SiteCommandArgs {
    private final String artifactRegistryName;

    private RemoveArtifactRegistryFromSiteCommandArgs() {
        this(null);
    }

    public RemoveArtifactRegistryFromSiteCommandArgs(String name) {
        artifactRegistryName = name;
    }

    public String getArtifactRegistryName() {
        return artifactRegistryName;
    }

    @Override
    public String toString() {
        return "RemoveArtifactRegistryFromSiteCommandArgs{" +
                "artifactRegistryName='" + artifactRegistryName + '\'' +
                '}';
    }
}
