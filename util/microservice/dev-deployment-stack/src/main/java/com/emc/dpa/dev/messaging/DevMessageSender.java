// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.dpa.dev.messaging;

import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.microservice.blobstore.BlobStoreLink;
import com.emc.microservice.messaging.MessageWriter;
import com.emc.microservice.messaging.RuntimeMessageSender;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

/**
 * Created by liebea on 1/18/15.
 * Drink responsibly
 */
public class DevMessageSender implements RuntimeMessageSender {
    private final DevMessagingServer server;
    private final String queueName;
    private final BlobStoreAPI blobStoreAPI;
    private final String blobStoreNamespace;
    private final String blobHeaderKeyName;
    private final boolean gzip;

    public DevMessageSender(
            DevMessagingServer server,
            String queueName,
            BlobStoreAPI blobStoreAPI,
            String blobNamespace,
            String blobKeyHeaderName,
            boolean gzip) {
        this.server = server;
        this.queueName = queueName;
        this.blobStoreAPI = blobStoreAPI;
        this.blobStoreNamespace = blobNamespace == null ? DevMessage.DEV_MESSAGING_NAMESPACE : blobNamespace;
        this.blobHeaderKeyName = blobKeyHeaderName;
        this.gzip = gzip;
    }

    @Override
    public void streamMessage(
            final MessageWriter messageWriter,
            Map<String, String> messageHeaders,
            String messageGroup) {
        String key = getKey(messageHeaders);
        blobStoreAPI.create(blobStoreNamespace, key, messageHeaders, out -> {
            if (gzip) {
                try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(out)) {
                    messageWriter.writeMessage(gzipOutputStream);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            } else {
                messageWriter.writeMessage(out);
            }
        });
        server.sendMessage(
                queueName,
                new DevMessageDescriptor(messageHeaders, new BlobStoreLink(blobStoreNamespace, key)));
    }

    private String getKey(Map<String, String> messageHeaders) {
        if (blobHeaderKeyName != null) {
            String key = messageHeaders.get(blobHeaderKeyName);
            if (key != null) {
                return key;
            }
        }
        return UUID.randomUUID().toString();
    }
}
