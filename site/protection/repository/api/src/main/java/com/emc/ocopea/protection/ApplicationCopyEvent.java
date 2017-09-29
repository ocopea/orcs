// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 10/26/16.
 * Drink responsibly
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes(
        {
                @JsonSubTypes.Type(ApplicationCopyScheduledEvent.class),
                @JsonSubTypes.Type(ApplicationCopyCreatedSuccessfullyEvent.class),
                @JsonSubTypes.Type(ApplicationCopyDataServiceFailedEvent.class),
                @JsonSubTypes.Type(ApplicationCopyDataServiceCreatedSuccessfullyEvent.class),
                @JsonSubTypes.Type(ApplicationCopyDataServiceQueuedEvent.class),
                @JsonSubTypes.Type(ApplicationCopyDataServiceRunningEvent.class),
                @JsonSubTypes.Type(ApplicationCopyErrorEvent.class),
                @JsonSubTypes.Type(ApplicationCopyAppServiceCreatedSuccessfullyEvent.class),
                @JsonSubTypes.Type(ApplicationCopyAppServiceQueuedEvent.class),
                @JsonSubTypes.Type(ApplicationCopyAppServiceRunningEvent.class),
                @JsonSubTypes.Type(ApplicationCopyAppServiceFailedEvent.class)

        })
public abstract class ApplicationCopyEvent {
    private final UUID appCopyId;
    private final UUID appInstanceId;
    private final long version;
    private final Date timeStamp;
    private final String message;

    protected ApplicationCopyEvent() {
        this(null, null, 0, null, null);
    }

    protected ApplicationCopyEvent(UUID appCopyId, UUID appInstanceId, long version, Date timeStamp, String message) {
        this.appCopyId = appCopyId;
        this.appInstanceId = appInstanceId;
        this.version = version;
        this.timeStamp = timeStamp;
        this.message = message;
    }

    public UUID getAppCopyId() {
        return appCopyId;
    }

    public UUID getAppInstanceId() {
        return appInstanceId;
    }

    public long getVersion() {
        return version;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ApplicationCopyEvent{" +
                "appCopyId=" + appCopyId +
                ", appInstanceId=" + appInstanceId +
                ", version=" + version +
                ", timeStamp=" + timeStamp +
                ", message='" + message + '\'' +
                '}';
    }
}
