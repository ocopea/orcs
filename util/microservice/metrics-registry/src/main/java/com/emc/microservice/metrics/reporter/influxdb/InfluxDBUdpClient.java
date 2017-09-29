// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2015 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.metrics.reporter.influxdb;

import java.io.IOException;
import java.net.InetAddress;

/**
 * @author nivenb
 */
public class InfluxDBUdpClient {

    private final InetAddress address;
    private final int port;

    public InfluxDBUdpClient(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    InfluxDBUdpClientConnection connect() throws IOException {
        return new InfluxDBUdpClientConnection(address, port);
    }

}
