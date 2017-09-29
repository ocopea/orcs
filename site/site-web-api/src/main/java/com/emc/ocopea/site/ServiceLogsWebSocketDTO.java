// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import java.util.List;

public class ServiceLogsWebSocketDTO {

    private final String address;
    private final String serialization;
    private final List<String> tags;

    /***
     * Describes a WebSocket that emits service logs
     * @param address address of the websocket
     * @param serialization which serialization the websocket uses. either "json" or "protobuf"
     * @param tags various tags for filtering purposes
     */
    public ServiceLogsWebSocketDTO(String address, String serialization, List<String> tags) {
        this.address = address;
        this.serialization = serialization;
        this.tags = tags;
    }

    private ServiceLogsWebSocketDTO() {
        this(null, null, null);
    }

    public String getAddress() {
        return address;
    }

    public String getSerialization() {
        return serialization;
    }

    public List<String> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        return "ServiceLogsWebSocketDTO{" +
                "address='" + address + '\'' +
                ", serialization='" + serialization + '\'' +
                ", tags=" + tags +
                '}';
    }
}
