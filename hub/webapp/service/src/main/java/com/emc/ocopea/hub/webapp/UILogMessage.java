// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.webapp;

import java.util.Set;

/*
* This DTO is used as the standard format for log messages on the PSB websocket
*/
public class UILogMessage {
    private final String message;
    private final Long timestamp;
    private final MessageType messageType;
    private final Set<String> tags;

    public UILogMessage(
            String message,
            Long timestamp,
            MessageType messageType,
            Set<String> tags) {
        this.message = message;
        this.timestamp = timestamp;
        this.messageType = messageType;
        this.tags = tags;
    }

    public UILogMessage() {
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

    public Set<String> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        return "SiteLogMessageDTO{" +
                "message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", messageType=" + messageType +
                ", tags=" + tags +
                '}';
    }

    public enum MessageType {
        out, err
    }
}
