// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging.jms;

import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.microservice.blobstore.BlobStoreLink;
import com.emc.microservice.messaging.DefaultMessageReader;
import com.emc.microservice.messaging.LoggingInputStream;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.messaging.MessageReader;
import com.emc.microservice.messaging.MessagingSerializationHelper;
import com.emc.microservice.serialization.SerializationManager;
import com.emc.microservice.serialization.SerializationReader;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created with love by liebea on 6/22/2014.
 */
public class JMSMessage implements Message {
    private final SerializationManager serializationManager;
    private final javax.jms.Message msg;
    private final BlobStoreAPI blobStoreAPI;
    private final boolean gzip;
    private final boolean logMessageWhileReading;

    public JMSMessage(
            SerializationManager serializationManager,
            javax.jms.Message msg,
            BlobStoreAPI blobStoreAPI,
            boolean logMessageWhileReading,
            boolean gzip) {
        this.serializationManager = serializationManager;
        this.msg = msg;
        this.blobStoreAPI = blobStoreAPI;
        this.gzip = gzip;
        this.logMessageWhileReading = logMessageWhileReading;
    }

    @Override
    public String getMessageHeader(String headerName) {
        try {
            return msg.getStringProperty(headerName);
        } catch (JMSException e) {
            throw new IllegalArgumentException("Unable to get message property with name " + headerName, e);
        }
    }

    @Override
    public Map<String, String> getMessageHeaders() {
        return extractHeaders(msg, MessagingSerializationHelper.HEADER_FILTER);
    }

    @Override
    public String getContextValue(String key) {
        try {
            return msg.getStringProperty(MessagingSerializationHelper.getUnderlyingKey(key));
        } catch (JMSException e) {
            throw new IllegalArgumentException("Unable to get message context property with name " + key, e);
        }
    }

    @Override
    public Map<String, String> getMessageContext() {
        return extractHeaders(msg, MessagingSerializationHelper.CONTEXT_FILTER);
    }

    @Override
    public void readMessage(final MessageReader messageReader) {
        if (blobStoreAPI != null) {
            //read the key and get the blob from blobstore
            BlobStoreLink blobStoreLink;
            SerializationReader<BlobStoreLink> reader = serializationManager.getReader(BlobStoreLink.class);
            try (InputStream inputStream = getInputStream(false)) {
                blobStoreLink = reader.readObject(inputStream);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            if (blobStoreAPI.isExists(blobStoreLink.getNamespace(), blobStoreLink.getKey())) {
                blobStoreAPI.readBlob(blobStoreLink.getNamespace(), blobStoreLink.getKey(), in -> {
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
                        "Failed reading message with id (id doesn't exist)" + blobStoreLink.getNamespace() + ':' +
                                blobStoreLink.getKey());
            }
        } else {
            try (InputStream jmsBytesMessageInputStream = getInputStream(gzip)) {
                messageReader.read(jmsBytesMessageInputStream);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private InputStream getInputStream(boolean gzip) throws IOException {
        InputStream inputStream = new JMSBytesMessageInputStream((BytesMessage) msg);

        // Wrapping with GZIP input stream if needed
        if (gzip) {
            inputStream = new GZIPInputStream(inputStream);
        }

        // Wrapping with logging stream if requested
        if (logMessageWhileReading) {
            inputStream = new LoggingInputStream(inputStream);
        }

        return inputStream;
    }

    @Override
    public Object getUnderlyingMessageObject() {
        return msg;
    }

    private Map<String, String> extractHeaders(javax.jms.Message message, MessagingSerializationHelper.Filter filter) {
        try {
            Enumeration propertyNames = message.getPropertyNames();
            if (propertyNames == null) {
                return Collections.emptyMap();
            }

            Map<String, String> headers = new HashMap<>();
            while (propertyNames.hasMoreElements()) {
                String name = (String) propertyNames.nextElement();
                if (filter.accept(name)) {
                    String value = message.getStringProperty(name);
                    headers.put(filter.transform(name), value);
                }
            }

            return headers;
        } catch (JMSException e) {
            throw new IllegalArgumentException("Failed reading message headers for message " + msg, e);
        }
    }

    @Override
    public <T> T readObject(Class<T> format) {
        DefaultMessageReader<T> messageReader =
                new DefaultMessageReader<>(serializationManager.getReader(format), format);
        readMessage(messageReader);
        return messageReader.getResult();
    }
}
