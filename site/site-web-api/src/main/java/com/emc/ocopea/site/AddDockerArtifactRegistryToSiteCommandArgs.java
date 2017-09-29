// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

/**
 * Created by liebea on 1/15/17.
 * Drink responsibly
 */
public class AddDockerArtifactRegistryToSiteCommandArgs implements SiteCommandArgs {
    private final String name;
    private final String url;
    private final String username;
    private final String password;

    private AddDockerArtifactRegistryToSiteCommandArgs() {
        this(null, null, null, null);
    }

    public AddDockerArtifactRegistryToSiteCommandArgs(String name, String url, String username, String password) {
        this.name = name;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "AddDockerArtifactRegistryToSiteCommandArgs{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password " + (password.isEmpty() ? "exists" : "empty") +
                '}';
    }
}
