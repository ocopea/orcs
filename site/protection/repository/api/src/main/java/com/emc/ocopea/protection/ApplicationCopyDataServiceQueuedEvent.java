// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 10/26/16.
 * Drink responsibly
 */
public class ApplicationCopyDataServiceQueuedEvent extends ApplicationCopyEvent {
    private final String dsbUrn;
    private final String dsbUrl;
    private final String serviceId;

    private ApplicationCopyDataServiceQueuedEvent() {
        this(null, null, 0, null, null, null, null, null);
    }

    public ApplicationCopyDataServiceQueuedEvent(
            UUID id,
            UUID appInstanceId,
            long version,
            Date timeStamp,
            String message,
            String dsbUrn,
            String dsbUrl,
            String serviceId) {

        super(id, appInstanceId, version, timeStamp, message);
        this.dsbUrn = dsbUrn;
        this.dsbUrl = dsbUrl;
        this.serviceId = serviceId;
    }

    public String getDsbUrn() {
        return dsbUrn;
    }

    public String getDsbUrl() {
        return dsbUrl;
    }

    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String toString() {
        return "ApplicationCopyDataServiceQueuedEvent{" +
                "dsbUrn='" + dsbUrn + '\'' +
                ", dsbUrl='" + dsbUrl + '\'' +
                ", serviceId='" + serviceId + '\'' +
                '}';
    }
}
