// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 10/26/16.
 * Drink responsibly
 */
public class ApplicationCopyDataServiceCreatedSuccessfullyEvent extends ApplicationCopyEvent {
    private final String dsbURN;
    private final String serviceId;
    private final UUID dataServiceCopyId;
    private final String copyRepoURN;

    private ApplicationCopyDataServiceCreatedSuccessfullyEvent() {
        this(null, null, 0, null, null, null, null, null, null);
    }

    public ApplicationCopyDataServiceCreatedSuccessfullyEvent(UUID id, UUID appInstanceId, long version,
                                                              Date timeStamp, String message, String dsbURN,
                                                              String serviceId, UUID dataServiceCopyId,
                                                              String copyRepoURN) {
        super(id, appInstanceId, version, timeStamp, message);
        this.dsbURN = dsbURN;
        this.serviceId = serviceId;
        this.dataServiceCopyId = dataServiceCopyId;
        this.copyRepoURN = copyRepoURN;
    }

    public String getDsbURN() {
        return dsbURN;
    }

    public String getServiceId() {
        return serviceId;
    }

    public UUID getDataServiceCopyId() {
        return dataServiceCopyId;
    }

    public String getCopyRepoURN() {
        return copyRepoURN;
    }

    @Override
    public String toString() {
        return "ApplicationCopyDataServiceCreatedSuccessfullyEvent{" +
                "dsbURN='" + dsbURN + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", dataServiceCopyId=" + dataServiceCopyId +
                ", copyRepoURN='" + copyRepoURN + '\'' +
                '}';
    }
}
