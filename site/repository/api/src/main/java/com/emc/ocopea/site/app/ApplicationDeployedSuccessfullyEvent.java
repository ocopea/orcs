// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 10/18/16.
 * Drink responsibly
 */
public class ApplicationDeployedSuccessfullyEvent extends DeployedApplicationEvent {
    private final String entryPointURL;

    public ApplicationDeployedSuccessfullyEvent() {
        this(null, 0L, null, null, null);
    }

    ApplicationDeployedSuccessfullyEvent(UUID id, long version, Date timestamp, String message, String entryPointURL) {
        super(id, version, timestamp, message);
        this.entryPointURL = entryPointURL;
    }

    public String getEntryPointURL() {
        return entryPointURL;
    }

    @Override
    public String toString() {
        return "ApplicationDeployedSuccessfullyEvent{" +
                "entryPointURL='" + entryPointURL + '\'' +
                '}';
    }
}
