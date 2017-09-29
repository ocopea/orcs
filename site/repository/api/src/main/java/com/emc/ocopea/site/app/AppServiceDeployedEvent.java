// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 10/18/16.
 * Drink responsibly
 */
public class AppServiceDeployedEvent extends DeployedApplicationEvent {
    private final String name;
    private final String urlIfPublic;

    private AppServiceDeployedEvent() {
        this(null, 0L, null, null, null, null);
    }

    AppServiceDeployedEvent(UUID id, long version, Date timestamp, String name, String message, String urlIfPublic) {
        super(id, version, timestamp, message);
        this.name = name;
        this.urlIfPublic = urlIfPublic;
    }

    public String getName() {
        return name;
    }

    public String getUrlIfPublic() {
        return urlIfPublic;
    }

    @Override
    public String toString() {
        return "AppServiceActiveEvent{" +
                "name='" + name + '\'' +
                ", urlIfPublic='" + urlIfPublic + '\'' +
                '}';
    }
}
