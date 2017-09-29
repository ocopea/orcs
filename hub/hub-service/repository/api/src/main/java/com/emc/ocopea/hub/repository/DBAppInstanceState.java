// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.net.URI;
import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 12/13/15.
 * Drink responsibly
 */
public class DBAppInstanceState {
    @JsonIgnore
    private final UUID appInstanceId;
    @JsonIgnore
    private final String state;
    @JsonIgnore
    private final Date dateModified;
    @JsonIgnore
    private final URI url;

    public DBAppInstanceState(UUID appInstanceId, String state, Date dateModified, URI url) {
        this.appInstanceId = appInstanceId;
        this.state = state;
        this.dateModified = dateModified;
        this.url = url;
    }

    public UUID getAppInstanceId() {
        return appInstanceId;
    }

    public String getState() {
        return state;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public URI getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "DBAppInstanceState{" +
                "appInstanceId=" + appInstanceId +
                ", state='" + state + '\'' +
                ", dateModified=" + dateModified +
                ", url='" + url + '\'' +
                '}';
    }
}
