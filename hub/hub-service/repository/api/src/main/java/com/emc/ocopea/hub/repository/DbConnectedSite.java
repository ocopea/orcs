// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 12/13/15.
 * Drink responsibly
 */
public class DbConnectedSite {
    @JsonIgnore
    private final UUID id;
    @JsonIgnore
    private final String urn;
    @JsonIgnore
    private final Date dateModified;

    private final String url;
    private final String name;
    private final String version;
    private final DbLocation location;
    private final String publicDns;

    DbConnectedSite withNonSerializedFields(UUID id, String urn, Date dateModified) {
        return new DbConnectedSite(
                id,
                urn,
                dateModified,
                this.url,
                this.name,
                this.version,
                this.location,
                this.publicDns);
    }

    private DbConnectedSite() {
        this(null, null, null, null, null, null, null, null);
    }

    public DbConnectedSite(
            UUID id,
            String urn,
            Date dateModified,
            String url,
            String name,
            String version,
            DbLocation location,
            String publicDns) {
        this.id = id;
        this.urn = urn;
        this.url = url;
        this.dateModified = dateModified;
        this.name = name;
        this.version = version;
        this.location = location;
        this.publicDns = publicDns;
    }

    public UUID getId() {
        return id;
    }

    public String getUrn() {
        return urn;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public DbLocation getLocation() {
        return location;
    }

    public String getPublicDns() {
        return publicDns;
    }

    @Override
    public String toString() {
        return "DbConnectedSite{" +
                "id=" + id +
                ", urn='" + urn + '\'' +
                ", dateModified=" + dateModified +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", location=" + location +
                ", publicDns='" + publicDns + '\'' +
                '}';
    }
}
