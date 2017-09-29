// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.psb;

public class PSBLogsWebSocketDTO {

    private final String address;
    private final String serialization;

    /***
     * Describes a logging WebSocket
     * @param address  address of the websocket
     * @param serialization  which serialization the websocket uses.
     *     Possible values for serialization are: "json", "protobuf-cf-log"
     *     "protobuf-cf-log" implies that each frame on the socket is encoded as a LogMessage as defined in:
     *     https://github.com/cloudfoundry/dropsonde-protocol/blob/master/events/log.proto
     */
    public PSBLogsWebSocketDTO(String address, String serialization) {
        this.address = address;
        this.serialization = serialization;
    }

    private PSBLogsWebSocketDTO() {
        this(null, null);
    }

    public String getAddress() {
        return address;
    }

    public String getSerialization() {
        return serialization;
    }

    @Override
    public String toString() {
        return "PSBLogsWebSocketDTO{" +
                "address='" + address + '\'' +
                ", serialization='" + serialization + '\'' +
                '}';
    }
}
