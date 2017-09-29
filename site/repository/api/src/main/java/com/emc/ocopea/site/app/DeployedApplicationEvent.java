// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 10/18/16.
 * Drink responsibly
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "eventType")
@JsonSubTypes(
        {
                @JsonSubTypes.Type(AppServiceDeployedEvent.class),
                @JsonSubTypes.Type(AppServiceStateChangeEvent.class),
                @JsonSubTypes.Type(ApplicationDeployedSuccessfullyEvent.class),
                @JsonSubTypes.Type(DataServiceBoundEvent.class),
                @JsonSubTypes.Type(DataServiceStateChangeEvent.class),
                @JsonSubTypes.Type(DeployedApplicationCreatedEvent.class),
                @JsonSubTypes.Type(PolicyStateChangeEvent.class),
                @JsonSubTypes.Type(ApplicationFailedDeployingEvent.class),
                @JsonSubTypes.Type(DeployedApplicationStoppingEvent.class),
                @JsonSubTypes.Type(DeployedApplicationFailedStoppingEvent.class),
                @JsonSubTypes.Type(DeployedApplicationStoppedEvent.class)
        })
public abstract class DeployedApplicationEvent {
    private final UUID appInstanceId;
    private final long version;
    private final Date timestamp;
    private final String message;

    protected DeployedApplicationEvent(UUID appInstanceId, long version, Date timestamp, String message) {
        this.appInstanceId = appInstanceId;
        this.version = version;
        this.timestamp = timestamp;
        this.message = message;
    }

    public UUID getAppInstanceId() {
        return appInstanceId;
    }

    public long getVersion() {
        return version;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "DeployedApplicationEvent{" +
                "id=" + appInstanceId +
                ", version=" + version +
                ", timestamp=" + timestamp +
                ", message='" + message + '\'' +
                '}';
    }
}
