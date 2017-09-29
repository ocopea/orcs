// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2015 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.metrics.reporter.influxdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author nivenb
 */
public class InfluxDBUdpClientConnection implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(InfluxDBUdpClientConnection.class);

    private final InetSocketAddress socketAddress;
    private final DatagramSocket socket;

    InfluxDBUdpClientConnection(InetAddress address, int port) throws IOException {
        socketAddress = new InetSocketAddress(address, port);
        if (socketAddress.isUnresolved()) {
            throw new IOException("Unable to resolve the address of the server.");
        }
        socket = new DatagramSocket();
    }

    /**
     * See the InfluxDB Line Protocol definition at: https://influxdb.com/docs/v0.9/write_protocols/line.html
     */
    void send(String metric, Map<String, String> tags, String value, long timestampInMillis) throws IOException {

        StringBuilder sb = new StringBuilder();
        metric = escapeMetricNameTagNameOrMeasurementName(metric);
        sb.append(metric);
        if (tags != null) {
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                sb.append(",");
                sb.append(escapeMetricNameTagNameOrMeasurementName(entry.getKey()));
                sb.append("=");
                sb.append(escapeValue(entry.getValue()));
            }
        }

        long timeStampInNanos = TimeUnit.MILLISECONDS.toNanos(timestampInMillis);
        sb.append(" value=");
        sb.append(value);
        sb.append(" ");
        sb.append(timeStampInNanos);

        String toSend = sb.toString();
        byte[] data = toSend.getBytes("UTF-8");
        log.debug("Sending to InfluxDB: {}", toSend);
        socket.send(new DatagramPacket(data, data.length, socketAddress));
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    /**
     * InfluxDB requires spaces and commas in metric/tag/measurement names to be escaped with a backslash.
     * See https://influxdb.com/docs/v0.9/write_protocols/line.html However...
     * We don't want spaces or commas in our names as this means we'll also have to escape when querying.
     * Instead, we'll replace spaces with '_' and commas with ";' and log a WARN message for engineers
     */
    private String escapeMetricNameTagNameOrMeasurementName(String name) {
        if (name.contains(" ")) {
            log.warn("Replacing spaces in metric/tag/measurement name '{}' with an underscore. " +
                    "Please correct this name in future.", name);
            name = name.replace(" ", "_");
        }

        if (name.contains(",")) {
            log.warn("Replacing commas in metric/tag/measurement name '{}' with ';'. " +
                    "Please correct this name in future.", name);
            name = name.replace(",", ";");
        }

        return name;
    }

    /**
     * See https://influxdb.com/docs/v0.9/write_protocols/line.html
     * '... tag values must escape any spaces or commas using a backslash (\). For example: \ and \,.
     * All tag values are stored as strings and should not be surrounded in quotes. '
     */
    private String escapeValue(String value) {

        value = value.replace(" ", "\\ ");
        value = value.replace(",", "\\,");
        return value;
    }
}
