// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging.error;

import com.emc.microservice.Context;
import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.microservice.blobstore.BlobStoreLink;
import com.emc.microservice.messaging.DestinationConfiguration;
import com.emc.microservice.messaging.LoggingOutputStream;
import com.emc.microservice.messaging.MessageWriter;
import com.emc.microservice.messaging.MessagingSerializationHelper;
import com.emc.microservice.messaging.RuntimeMessageSender;
import com.emc.microservice.serialization.SerializationManager;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

/**
 * Created by liebea on 9/28/2014. Enjoy it
 */
public class RabbitMQMessageSender implements RuntimeMessageSender {

    private final ConnectionFactory connectionFactory;
    private final String exchangeName;
    private final String destinationName;
    private final String blobStoreNamespace;
    private final String blobHeaderKeyName;
    private final BlobStoreAPI blobStoreAPI;
    private final SerializationManager serializationManager;
    private final Logger log;
    private final boolean gzip;
    private final boolean logInDebug;
    private final boolean messageGrouping;
    private final boolean activated;

    public RabbitMQMessageSender(
            RabbitMQMessagingProviderConfiguration messagingProviderConfiguration,
            RabbitMQQueueConfiguration queueConfiguration,
            DestinationConfiguration destinationConfiguration,
            Context context) {
        this.serializationManager = context.getSerializationManager();
        this.blobStoreAPI = initBlobstoreAPI(context, queueConfiguration);
        this.log = context.createSubLogger(RabbitMQMessageSender.class);
        connectionFactory = new ConnectionFactory();
        connectionFactory.setUsername(messagingProviderConfiguration.getUserName());
        connectionFactory.setPassword(messagingProviderConfiguration.getPassword());
        connectionFactory.setVirtualHost(messagingProviderConfiguration.getVirtualHost());
        connectionFactory.setHost(messagingProviderConfiguration.getHost());
        connectionFactory.setPort(messagingProviderConfiguration.getPort());
        String exchange = queueConfiguration.getExchangeName();
        exchangeName = exchange == null ? "" : exchange;
        destinationName = queueConfiguration.getQueueName() == null ? "" : queueConfiguration.getQueueName();
        blobHeaderKeyName = destinationConfiguration.getBlobKeyHeaderName() != null &&
                !destinationConfiguration.getBlobKeyHeaderName().isEmpty() ?
                destinationConfiguration.getBlobKeyHeaderName() :
                null;

        blobStoreNamespace = RabbitMQMessage.getNamespace(
                destinationConfiguration.getBlobNamespace(),
                exchangeName,
                destinationName);
        gzip = queueConfiguration.isGzip();
        this.logInDebug = destinationConfiguration.isLogContentWhenInDebug();
        this.messageGrouping = queueConfiguration.isMessageGrouping();
        this.activated = queueConfiguration.isActivated();
    }

    private BlobStoreAPI initBlobstoreAPI(Context context, RabbitMQQueueConfiguration destinationConfiguration) {
        String blobstoreName = destinationConfiguration.getBlobstoreName();
        if (blobstoreName == null || blobstoreName.isEmpty()) {
            return null;
        } else {
            try {
                return context.getBlobStoreManager()
                        .getManagedResourceByName(blobstoreName).getBlobStoreAPI();
            } catch (Exception ex) {
                throw new IllegalStateException("Failed loading blobStore " + blobstoreName + " which is required "
                        + "for RabbitMQ messaging configuration " + destinationConfiguration.toString());
            }
        }
    }

    @Override
    public void streamMessage(
            final MessageWriter messageWriter,
            Map<String, String> messageHeaders,
            String messageGroup) {
        //todo: makes sense to cache connection!
        //todo: register for connection error/shutdown

        //don't send the message the queue is not activated
        if (!activated) {
            log.debug("The queue {} is not activated, we are not sending this message!", destinationName);
            return;
        }

        final boolean printToLog = logInDebug && log.isDebugEnabled();
        Connection connection = null;
        Channel channel = null;
        try {
            //todo: makes sense to cache connection!
            connection = connectionFactory.newConnection();
            //todo: makes sense to cache connection/channel?
            channel = connection.createChannel();
            setup(exchangeName, destinationName, channel);

            //if useBlobStore = true --> write to blobstore and send link (uuid)
            if (blobStoreAPI != null) {
                String key = getBlobKey(messageHeaders);
                blobStoreAPI.create(blobStoreNamespace, key, messageHeaders, out -> {
                    try {
                        if (gzip) {
                            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(out)) {
                                RabbitMQMessageSender.this.write(gzipOutputStream, printToLog, messageWriter);
                            }
                        } else {
                            RabbitMQMessageSender.this.write(out, printToLog, messageWriter);
                        }
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                });

                BlobStoreLink blobStoreLink = new BlobStoreLink(blobStoreNamespace, key);

                if (messageGrouping) {
                    channel.basicPublish(
                            exchangeName,
                            messageGroup,
                            new AMQP.BasicProperties.Builder().headers(new HashMap<>(messageHeaders)).build(),
                            MessagingSerializationHelper.readAsBytes(serializationManager, blobStoreLink));
                } else {
                    channel.basicPublish(
                            exchangeName,
                            destinationName,
                            new AMQP.BasicProperties.Builder().headers(new HashMap<>(messageHeaders)).build(),
                            MessagingSerializationHelper.readAsBytes(serializationManager, blobStoreLink));
                }

            } else {
                //todo: real stream to queue!
                try (ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream()) {
                    try (OutputStream outputStream = this.gzip ? new GZIPOutputStream(byteArrayStream)
                            : byteArrayStream) {
                        write(outputStream, printToLog, messageWriter);
                    }

                    if (messageGrouping) {
                        channel.basicPublish(
                                exchangeName,
                                messageGroup,
                                new AMQP.BasicProperties.Builder().headers(new HashMap<>(messageHeaders)).build(),
                                byteArrayStream.toByteArray());
                    } else {
                        channel.basicPublish(
                                exchangeName,
                                destinationName,
                                new AMQP.BasicProperties.Builder().headers(new HashMap<>(messageHeaders)).build(),
                                byteArrayStream.toByteArray());
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error Sending message", e);
        } finally {
            try {
                try {
                    if (channel != null) {
                        channel.close();
                    }
                } finally {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (IOException ex) {
                log.warn("Error closing a rabbitMQ connection", ex);
                //noinspection ThrowFromFinallyBlock
                throw new IllegalStateException("Error closing a rabbitMQ connection", ex);
            }
        }
    }

    private String getBlobKey(Map<String, String> messageHeaders) {
        if (blobHeaderKeyName != null) {
            String key = messageHeaders.get(blobHeaderKeyName);
            if (key != null && !key.isEmpty()) {
                return key;
            }
        }
        return UUID.randomUUID().toString();
    }

    private void write(OutputStream outputStream, boolean printToLog, MessageWriter messageWriter) throws IOException {
        if (printToLog) {
            try (LoggingOutputStream los = new LoggingOutputStream(outputStream, log)) {
                messageWriter.writeMessage(los);
            }
        } else {
            messageWriter.writeMessage(outputStream);
        }
    }

    private void setup(String exchangeName, String destinationName, Channel channel) throws IOException {
        // For message grouping, it is a requirement that the exchange and all queues are pre-declared
        if (!messageGrouping) {
            if (exchangeName == null || exchangeName.isEmpty()) {
                channel.queueDeclare(destinationName, true, false, false, null);
            } else {
                channel.exchangeDeclare(exchangeName, "fanout");
            }
        }
    }
}