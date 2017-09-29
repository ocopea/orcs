// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 10/18/16.
 * Drink responsibly
 */
public class AppServiceStateChangeEvent extends DeployedApplicationEvent {
    private final String name;
    private final DeployedAppServiceState state;

    private AppServiceStateChangeEvent() {
        this(null, 0L, null, null, null, null);
    }

    AppServiceStateChangeEvent(UUID id, long version, Date timestamp, String name, DeployedAppServiceState state,
                               String message) {
        super(id, version, timestamp, message);
        this.name = name;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public DeployedAppServiceState getState() {
        return state;
    }

    @Override
    public String toString() {
        return "AppServiceStateChangeEvent{" +
                "name='" + name + '\'' +
                ", state=" + state +
                '}';
    }
}
