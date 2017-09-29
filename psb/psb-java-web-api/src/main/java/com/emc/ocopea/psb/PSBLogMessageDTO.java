// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.psb;

/*
* This DTO is used as the standard format for log messages on the PSB websocket
*/
public class PSBLogMessageDTO {
    private final String message;
    private final Long timestamp;
    private final MessageType messageType;
    private final String serviceId;

    public PSBLogMessageDTO(
            String message,
            Long timestamp,
            MessageType messageType,
            String serviceId) {
        this.message = message;
        this.timestamp = timestamp;
        this.messageType = messageType;
        this.serviceId = serviceId;
    }

    public PSBLogMessageDTO() {
        this(null, null, null, null);
    }

    public String getMessage() {
        return message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getServiceId() {
        return serviceId;
    }

    public enum MessageType {
        out, err
    }

    @Override
    public String toString() {
        return "PSBLogMessageDTO{" +
                "message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", messageType=" + messageType +
                ", serviceId='" + serviceId + '\'' +
                '}';
    }
}
