// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.jmsl.dev.messaging;

import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.microservice.messaging.DefaultMessageReader;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.messaging.MessageReader;
import com.emc.microservice.messaging.MessagingSerializationHelper;
import com.emc.microservice.serialization.SerializationManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by liebea on 1/18/15.
 * Drink responsibly
 */
public class DevMessage implements Message {
    private final DevMessageDescriptor descriptor;
    private final SerializationManager serializationManager;
    private final BlobStoreAPI blobStoreAPI;
    private final boolean gzip;
    static final String DEV_MESSAGING_NAMESPACE = "dev-messaging";

    public DevMessage(
            DevMessageDescriptor descriptor,
            SerializationManager serializationManager,
            BlobStoreAPI blobStoreAPI,
            boolean gzip) {
        this.descriptor = descriptor;
        this.serializationManager = serializationManager;
        this.blobStoreAPI = blobStoreAPI;
        this.gzip = gzip;
    }

    @Override
    public String getMessageHeader(String headerName) {
        return getMessageHeaders().get(headerName);
    }

    @Override
    public String getContextValue(String key) {
        return getMessageContext().get(key);
    }

    @Override
    public Map<String, String> getMessageHeaders() {
        return extractHeaders(descriptor.getHeaders(), MessagingSerializationHelper.HEADER_FILTER);
    }

    @Override
    public Map<String, String> getMessageContext() {
        return extractHeaders(descriptor.getHeaders(), MessagingSerializationHelper.CONTEXT_FILTER);
    }

    private Map<String, String> extractHeaders(
            Map<String, String> messageHeaders,
            MessagingSerializationHelper.Filter filter) {

        Map<String, String> stringHeaders = new HashMap<>(messageHeaders.size());
        for (Map.Entry<String, String> currEntry : messageHeaders.entrySet()) {
            String key = currEntry.getKey();
            if (filter.accept(key)) {
                Object value = currEntry.getValue();
                if (value == null) {
                    stringHeaders.put(filter.transform(key), null);
                } else {
                    stringHeaders.put(filter.transform(key), value.toString());
                }
            }
        }

        return stringHeaders;
    }

    @Override
    public void readMessage(final MessageReader messageReader) {
        //read the key and get the blob from blobstore
        if (blobStoreAPI.isExists(descriptor.getBlobKey().getNamespace(), descriptor.getBlobKey().getKey())) {
            blobStoreAPI.readBlob(
                    descriptor.getBlobKey().getNamespace(),
                    descriptor.getBlobKey().getKey(),
                    in -> {
                        if (gzip) {
                            try (GZIPInputStream gzipInputStream = new GZIPInputStream(in)) {
                                messageReader.read(gzipInputStream);
                            } catch (IOException e) {
                                throw new IllegalStateException(e);
                            }
                        } else {
                            messageReader.read(in);
                        }
                    });
        } else {
            throw new IllegalStateException(
                    "Failed reading message with id (id doesn't exist)" + descriptor.getBlobKey());
        }
    }

    @Override
    public <T> T readObject(Class<T> format) {
        DefaultMessageReader<T> messageReader =
                new DefaultMessageReader<>(serializationManager.getReader(format), format);
        readMessage(messageReader);
        return messageReader.getResult();
    }

    @Override
    public Object getUnderlyingMessageObject() {
        return this;
    }
}
