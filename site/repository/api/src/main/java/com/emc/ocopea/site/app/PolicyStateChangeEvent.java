// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 10/18/16.
 * Drink responsibly
 */
public class PolicyStateChangeEvent extends DeployedApplicationEvent {
    private final String name;
    private final String type;
    private final DeployedDataServicesPolicyState state;

    private PolicyStateChangeEvent() {
        this(null, 0L, null, null, null, null, null);
    }

    PolicyStateChangeEvent(UUID id, long version, Date timestamp, String message, String name, String type,
                           DeployedDataServicesPolicyState state) {
        super(id, version, timestamp, message);
        this.name = name;
        this.type = type;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public DeployedDataServicesPolicyState getState() {
        return state;
    }

    @Override
    public String toString() {
        return "PolicyStateChangeEvent{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", state=" + state +
                '}';
    }
}
