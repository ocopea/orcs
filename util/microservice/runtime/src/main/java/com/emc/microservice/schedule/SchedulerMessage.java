// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.schedule;

import java.util.Map;

public class SchedulerMessage {
    private final String name;
    private final String listenerIdentifier;
    private final Map<String, String> headers;
    private final String payload;

    private SchedulerMessage() {
        this(null, null, null, null);
    }

    public SchedulerMessage(String name, String listenerIdentifier, Map<String, String> headers, String payload) {
        this.name = name;
        this.listenerIdentifier = listenerIdentifier;
        this.headers = headers;
        this.payload = payload;
    }

    public String getName() {
        return name;
    }

    public String getListenerIdentifier() {
        return listenerIdentifier;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getPayload() {
        return payload;
    }
}
