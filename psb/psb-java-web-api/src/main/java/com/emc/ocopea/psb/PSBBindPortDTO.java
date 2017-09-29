// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.psb;

/**
 * Created by liebea on 6/30/16.
 * Drink responsibly
 */
public class PSBBindPortDTO {
    private final String protocol;
    private final String destination;
    private final int port;

    private PSBBindPortDTO() {
        this(null, null, 0);
    }

    public PSBBindPortDTO(String protocol, String destination, int port) {
        this.protocol = protocol;
        this.destination = destination;
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getDestination() {
        return destination;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "DSBBindPortDTO{" +
                "protocol='" + protocol + '\'' +
                ", destination='" + destination + '\'' +
                ", port=" + port +
                '}';
    }
}
