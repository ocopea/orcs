// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging.error;

import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.microservice.blobstore.BlobStoreLink;
import com.emc.microservice.messaging.DefaultMessageReader;
import com.emc.microservice.messaging.LoggingInputStream;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.messaging.MessageReader;
import com.emc.microservice.messaging.MessagingConstants;
import com.emc.microservice.messaging.MessagingSerializationHelper;
import com.emc.microservice.serialization.SerializationManager;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.QueueingConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by liebea on 9/28/2014. Enjoy it
 */
public class RabbitMQMessage implements Message {

    private final Logger log = LoggerFactory.getLogger(RabbitMQMessage.class);
    private final SerializationManager serializationManager;
    private final QueueingConsumer.Delivery delivery;
    private final BlobStoreAPI blobstoreApi;
    private final boolean gzip;
    private final boolean logMessageWhileReading;
    private boolean failed;
    private final List<ErrorMessageHeader> errorMessageHeaders;

    public RabbitMQMessage(
            SerializationManager serializationManager, QueueingConsumer.Delivery delivery,
            BlobStoreAPI blobstoreApi, boolean gzip,
            boolean logMessageWhileReading) {
        this.serializationManager = serializationManager;
        this.delivery = delivery;
        this.blobstoreApi = blobstoreApi;
        this.gzip = gzip;
        this.logMessageWhileReading = logMessageWhileReading;
        this.errorMessageHeaders = new ArrayList<>();
        this.failed = getMessageHeader(MessagingConstants.FAILED_HEADER) == null ?
                false :
                Boolean.parseBoolean(getMessageHeader(MessagingConstants.FAILED_HEADER));
    }

    @Override
    public String getMessageHeader(String headerName) {
        return getMessageHeaders().get(headerName);
    }

    public void addErrorMessageHeader(String microserviceURI, long timestamp, int errorCode, String errorMessage) {
        errorMessageHeaders.add(new ErrorMessageHeader(microserviceURI, timestamp, errorCode, errorMessage));
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public boolean isFailed() {
        return failed;
    }

    @Override
    public Map<String, String> getMessageHeaders() {
        return extractHeaders(MessagingSerializationHelper.HEADER_FILTER);
    }

    @Override
    public Map<String, String> getMessageContext() {
        return extractHeaders(MessagingSerializationHelper.CONTEXT_FILTER);
    }

    @Override
    public String getContextValue(String key) {
        return getMessageContext().get(key);
    }

    @Override
    public void readMessage(final MessageReader messageReader) {

        if (blobstoreApi != null) {
            //read the key and get the blob from blobstore
            BlobStoreLink blobStoreLink = MessagingSerializationHelper.readObject(
                    serializationManager,
                    BlobStoreLink.class,
                    delivery.getBody());
            if (blobstoreApi.isExists(blobStoreLink.getNamespace(), blobStoreLink.getKey())) {
                blobstoreApi.readBlob(blobStoreLink.getNamespace(), blobStoreLink.getKey(), in -> {
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

            byte[] body = delivery.getBody();

            try (InputStream stream = getInputStream(body)) {
                messageReader.read(stream);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

    }

    private InputStream getInputStream(byte[] body) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(body);

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
        return delivery;
    }

    @Override
    public <T> T readObject(Class<T> format) {
        DefaultMessageReader<T> messageReader =
                new DefaultMessageReader<>(serializationManager.getReader(format), format);
        readMessage(messageReader);
        return messageReader.getResult();
    }

    public List<XDeath> getXDeaths() {
        List<XDeath> deaths = (List<XDeath>) extractHeader(RABBITMQ_XDEATH_HEADER_FILTER);
        if (deaths == null) {
            return Collections.emptyList();
        }
        return deaths;
    }

    @NoJavadoc
    public static String getNamespace(String blobstoreNamespace, String exchange, String queue) {
        if (blobstoreNamespace != null && !blobstoreNamespace.isEmpty()) {
            return blobstoreNamespace;
        }
        if (exchange == null) {
            exchange = "";
        }
        if (queue == null) {
            queue = "";
        }
        return "RabbitMQ-" + exchange + '-' + queue;
    }

    private Map<String, String> extractHeaders(MessagingSerializationHelper.Filter filter) {

        Map<String, Object> headers = new HashMap<>();

        // Add delivery headers
        if (delivery != null && delivery.getProperties() != null && delivery.getProperties().getHeaders() != null) {
            headers.putAll(delivery.getProperties().getHeaders());
        }

        Map<String, String> stringHeaders = new HashMap<>(headers.size());
        for (Map.Entry<String, Object> currEntry : headers.entrySet()) {
            String key = currEntry.getKey();
            if (!filter.accept(key)) {
                continue;
            }
            Object value = currEntry.getValue();
            if (value == null) {
                stringHeaders.put(filter.transform(key), null);
                continue;
            }
            stringHeaders.put(filter.transform(key), value.toString());
        }

        // Add error headers
        String errorMessageHeader = getErrorMessageHeader();
        if (errorMessageHeader != null) {
            stringHeaders.put(MessagingConstants.ERROR_HEADER, errorMessageHeader);

            // Add retry header indicating whether or not this message should retried after the errors
            stringHeaders.put(MessagingConstants.FAILED_HEADER, Boolean.toString(failed));
        }

        return stringHeaders;
    }

    private String getErrorMessageHeader() {
        if (errorMessageHeaders.isEmpty()) {
            return null;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(errorMessageHeaders);
        } catch (JsonProcessingException e) {
            log.error("Unable to add error headers", e);
            return null;
        }

    }

    private static final MessagingSerializationHelper.Filter RABBITMQ_XDEATH_HEADER_FILTER =
            new MessagingSerializationHelper.Filter() {
                @Override
                public boolean accept(String key) {
                    return "x-death".equals(key);
                }

                @Override
                public String transform(String key) {
                    return key;
                }

                @Override
                public Object transformValue(Object value) {
                    List<Map<String, Object>> deathsHeader = (List<Map<String, Object>>) value;
                    List<XDeath> deaths = new ArrayList<>(deathsHeader.size());
                    for (Map<String, Object> deathHeader : deathsHeader) {
                        long count = deathHeader.get("count") == null ? 0 : (Long) deathHeader.get("count");
                        String reason =
                                deathHeader.get("reason") == null ? null : deathHeader.get("reason").toString();
                        String queue = deathHeader.get("queue") == null ? null : deathHeader.get("queue").toString();
                        Date time = deathHeader.get("time") == null ? null : (Date) deathHeader.get("time");
                        String exchange =
                                deathHeader.get("exchange") == null ? null : deathHeader.get("exchange").toString();
                        List<Object> routingKeysHeader = (List<Object>) deathHeader.get("routing-keys");
                        List<String> routingKeys;
                        if (routingKeysHeader != null) {
                            routingKeys = new ArrayList<>(routingKeysHeader.size());
                            for (Object routingKeyHeader : routingKeysHeader) {
                                routingKeys.add(routingKeyHeader.toString());
                            }
                        } else {
                            routingKeys = Collections.emptyList();
                        }
                        deaths.add(new XDeath(count, reason, queue, time, exchange, routingKeys));
                    }
                    return deaths;
                }
            };

    private Object extractHeader(MessagingSerializationHelper.Filter filter) {
        Map<String, Object> headers = delivery.getProperties().getHeaders();
        if (headers == null) {
            return null;
        }

        for (Map.Entry<String, Object> currEntry : headers.entrySet()) {
            String key = currEntry.getKey();
            if (!filter.accept(key)) {
                continue;
            }
            Object value = currEntry.getValue();
            if (value == null) {
                return null;
            }

            return filter.transformValue(value);
        }

        return null;
    }

    @NoJavadoc
    public AMQP.BasicProperties getProperties() {
        AMQP.BasicProperties.Builder propertiesBuilder = delivery.getProperties().builder();

        // Build a map of headers that contains the headers in the delivery plus the error headers.
        HashMap<String, Object> headers = new HashMap<>();
        if (delivery.getProperties().getHeaders() != null) {
            headers.putAll(delivery.getProperties().getHeaders());
        }
        String errorMessageHeader = getErrorMessageHeader();
        if (errorMessageHeader != null) {
            headers.put(MessagingConstants.ERROR_HEADER, errorMessageHeader);
            headers.put(MessagingConstants.FAILED_HEADER, Boolean.toString(failed));
        }

        return propertiesBuilder.headers(headers).build();
    }

    static class XDeath {
        private final long count;
        private final String reason;
        private final String queue;
        private final Date time;
        private final String exchange;
        private final List<String> routingKeys;

        public XDeath(long count, String reason, String queue, Date time, String exchange, List<String> routingKeys) {
            this.count = count;
            this.reason = reason;
            this.queue = queue;
            this.time = time;
            this.exchange = exchange;
            this.routingKeys = routingKeys;
        }

        public long getCount() {
            return count;
        }

        public String getReason() {
            return reason;
        }

        public String getQueue() {
            return queue;
        }

        public Date getTime() {
            return time;
        }

        public String getExchange() {
            return exchange;
        }

        public List<String> getRoutingKeys() {
            return routingKeys;
        }
    }
}
