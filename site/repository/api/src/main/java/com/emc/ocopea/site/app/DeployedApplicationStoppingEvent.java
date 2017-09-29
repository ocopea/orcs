// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 10/18/16.
 * Drink responsibly
 */
public class DeployedApplicationStoppingEvent extends DeployedApplicationEvent {
    private DeployedApplicationStoppingEvent() {
        super(null, 0L, null, null);
    }

    protected DeployedApplicationStoppingEvent(UUID id, long version, Date timestamp, String message) {
        super(id, version, timestamp, message);
    }
}

