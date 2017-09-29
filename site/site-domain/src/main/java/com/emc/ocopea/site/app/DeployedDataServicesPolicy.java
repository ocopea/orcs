// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import java.util.Date;
import java.util.Map;

/**
 * Created by liebea on 10/19/16.
 * Drink responsibly
 */
public class DeployedDataServicesPolicy {
    private final String type;
    private final String name;
    private final Map<String, String> settings;
    private DeployedDataServicesPolicyState state = DeployedDataServicesPolicyState.pending;
    private Date stateChangeDate = null;
    private String stateMessage = null;

    public DeployedDataServicesPolicy(String type, String name, Map<String, String> settings) {
        this.type = type;
        this.name = name;
        this.settings = settings;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public Date getStateChangeDate() {
        return stateChangeDate;
    }

    public String getStateMessage() {
        return stateMessage;
    }

    public DeployedDataServicesPolicyState getState() {
        return state;
    }

    public void setState(DeployedDataServicesPolicyState state, String message, Date timestamp) {
        this.state = state;
        this.stateChangeDate = timestamp;
        this.stateMessage = message;
    }

    @Override
    public String toString() {
        return "DeployedDataServicesPolicy{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", settings=" + settings +
                ", state=" + state +
                ", stateChangeDate=" + stateChangeDate +
                ", stateMessage='" + stateMessage + '\'' +
                '}';
    }
}
