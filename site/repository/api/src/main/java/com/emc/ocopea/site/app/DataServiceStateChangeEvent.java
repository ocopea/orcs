// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 10/18/16.
 * Drink responsibly
 */
public class DataServiceStateChangeEvent extends DeployedApplicationEvent {
    private final String bindName;
    private final DeployedDataServiceState state;

    private DataServiceStateChangeEvent() {
        this(null, 0L, null, null, null, null);
    }

    DataServiceStateChangeEvent(UUID id, long version, Date timestamp, String bindName, DeployedDataServiceState state,
                                String message) {
        super(id, version, timestamp, message);
        this.bindName = bindName;
        this.state = state;
    }

    public String getBindName() {
        return bindName;
    }

    public DeployedDataServiceState getState() {
        return state;
    }

    @Override
    public String toString() {
        return "DataServiceStateChangeEvent{" +
                "bindName='" + bindName + '\'' +
                ", state=" + state +
                '}';
    }
}
