// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 10/26/16.
 * Drink responsibly
 */
public class ApplicationCopyDataServiceFailedEvent extends ApplicationCopyEvent {
    private final String dsbURN;
    private final String serviceId;

    public ApplicationCopyDataServiceFailedEvent() {
        this(null, null, 0, null, null, null, null);
    }

    public ApplicationCopyDataServiceFailedEvent(UUID id, UUID appInstanceId, long version, Date timeStamp,
                                                 String message, String dsbURN, String serviceId) {
        super(id, appInstanceId, version, timeStamp, message);
        this.dsbURN = dsbURN;
        this.serviceId = serviceId;
    }

    public String getDsbURN() {
        return dsbURN;
    }

    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String toString() {
        return "ApplicationCopyDataServiceFailedEvent{" +
                "dsbURN='" + dsbURN + '\'' +
                ", serviceId='" + serviceId + '\'' +
                '}';
    }
}
