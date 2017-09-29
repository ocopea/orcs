// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging.jms;

import com.emc.microservice.LoggingHelper;
import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.microservice.blobstore.BlobStoreLink;
import com.emc.microservice.blobstore.BlobWriter;
import com.emc.microservice.messaging.LoggingOutputStream;
import com.emc.microservice.messaging.MessageWriter;
import com.emc.microservice.messaging.RuntimeMessageSender;
import com.emc.microservice.serialization.SerializationManager;
import com.emc.microservice.serialization.SerializationWriter;
import com.emc.ocopea.util.hash.MurmurHash;
import org.slf4j.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

/**
 * Created with love by liebea on 6/22/2014.
 */
public class JMSMessageSender implements RuntimeMessageSender {

    public static final String JMS_HORNETQ_INPUT_STREAM = "JMS_HQ_InputStream";

    public static final String JMS_SPECIFIC_MESSAGE_GROUP_HEADER = "JMSXGroupID";

    private final BlobStoreAPI blobStoreAPI;
    private final String blobStoreNamespace;
    private final String blobHeaderKeyName;

    private final SerializationManager serializationManager;
    private final ConnectionFactory connectionFactory;
    private final Destination destination;
    private final String destinationName;
    private final Logger log;
    private final boolean logInDebug;
    private final boolean gzip;

    public JMSMessageSender(
            ConnectionFactory connectionFactory,
            Destination destination,
            String destinationName,
            SerializationManager serializationManager,
            Logger parentLogger,
            BlobStoreAPI blobStoreAPI,
            String blobStoreNamespace,
            String blobHeaderKeyName,
            boolean gzip,
            boolean logContentWhenInDebug) {
        this.connectionFactory = connectionFactory;
        this.destination = destination;
        this.destinationName = destinationName;
        this.serializationManager = serializationManager;
        this.blobStoreAPI = blobStoreAPI;
        this.blobStoreNamespace =
                blobStoreNamespace == null || blobStoreNamespace.isEmpty() ? "shawarma-belafa" : blobStoreNamespace;
        this.blobHeaderKeyName = blobHeaderKeyName;
        this.log = LoggingHelper.createSubLogger(parentLogger, JMSMessageSender.class);
        this.gzip = gzip;
        this.logInDebug = logContentWhenInDebug;
    }

    @Override
    public void streamMessage(MessageWriter messageWriter, Map<String, String> messageHeaders, String messageGroup) {
        try {
            Connection conn = null;
            Session session = null;
            MessageProducer producer = null;

            try {
                conn = connectionFactory.createConnection();
                session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Message message = session.createBytesMessage();
                message.setObjectProperty(
                        JMS_HORNETQ_INPUT_STREAM,
                        getInputStream(messageWriter, this.gzip, this.logInDebug, messageHeaders));

                if (messageGroup != null) {
                    // we hash for a more uniform distribution
                    String messageGroupHash = Long.toHexString(MurmurHash.hash64(messageGroup));
                    if (log.isDebugEnabled()) {
                        log.debug("Setting string property {}:{}", JMS_SPECIFIC_MESSAGE_GROUP_HEADER, messageGroupHash);
                    }
                    message.setStringProperty(JMS_SPECIFIC_MESSAGE_GROUP_HEADER, messageGroupHash);
                }

                for (Map.Entry<String, String> currEntry : messageHeaders.entrySet()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Setting string property {}:{}", currEntry.getKey(), currEntry.getValue());
                    }
                    message.setStringProperty(currEntry.getKey(), currEntry.getValue());
                }

                producer = session.createProducer(destination);
                producer.setDisableMessageID(true);
                producer.setDisableMessageTimestamp(true);
                producer.send(message);

            } finally {

                closeAllJMSStuffNicely(conn, session, producer);
            }

        } catch (JMSException | IOException err) {
            log.error("Unable to send message to " + destinationName, err);
            throw new IllegalStateException("Unable to send to " + destinationName, err);
        }
    }

    private void closeAllJMSStuffNicely(Connection conn, Session session, MessageProducer producer)
            throws JMSException {
        try {
            if (producer != null) {
                producer.close();
            }
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } finally {
                if (conn != null) {
                    conn.close();
                }
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

    private InputStream getInputStream(
            final MessageWriter messageWriter,
            final boolean gzip,
            final boolean logInDebug,
            Map<String, String> messageHeaders) throws IOException {
        final boolean printToLog = logInDebug && log.isDebugEnabled();

        if (blobStoreAPI != null) {
            String key = getBlobKey(messageHeaders);
            blobStoreAPI.create(blobStoreNamespace, key, messageHeaders, new BlobWriter() {
                @Override
                public void write(OutputStream out) {
                    if (gzip) {
                        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(out)) {
                            JMSMessageSender.this.write(gzipOutputStream, printToLog, messageWriter);
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    } else {
                        try {
                            JMSMessageSender.this.write(out, printToLog, messageWriter);
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                }
            });

            BlobStoreLink blobStoreLink = new BlobStoreLink(blobStoreNamespace, key);

            SerializationWriter<BlobStoreLink> writer = serializationManager.getWriter(BlobStoreLink.class);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            writer.writeObject(blobStoreLink, outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } else {

            PipedInputStream in = new PipedInputStream();
            final PipedOutputStream out = new PipedOutputStream(in);
            new Thread(
                    () -> {
                        try {
                            boolean isDebug = logInDebug && log.isDebugEnabled();

                            if (gzip) {
                                try (OutputStream gzout = isDebug ?
                                        new LoggingOutputStream(new GZIPOutputStream(out), log) :
                                        new GZIPOutputStream(out)) {
                                    messageWriter.writeMessage(gzout);

                                    if (gzout instanceof GZIPOutputStream) {
                                        ((GZIPOutputStream) gzout).finish();
                                    } else {
                                        ((LoggingOutputStream) gzout).finish();
                                    }

                                }
                            } else {
                                if (isDebug) {
                                    try (LoggingOutputStream los = new LoggingOutputStream(out, log)) {
                                        messageWriter.writeMessage(los);
                                    }
                                } else {
                                    messageWriter.writeMessage(out);
                                }
                            }
                        } catch (Exception err) {
                            // todo: Ashish has a task to decide about error handling in this thread DPA-33849
                            log.error("Unable to stream message data, failed while writing to stream", err);
                            // todo: Still, hi Ashish, I've added throw runtime here. since this will hopefully
                            // be caught by
                            // jms and will throw it back at sender (send operation failed) better than leaving as is
                            // I guess
                            throw new IllegalStateException(
                                    "Unable to stream message data, failed while writing to stream",
                                    err);
                        } finally {
                            try {
                                out.flush();
                                out.close();
                            } catch (IOException e) {
                                throw new IllegalStateException(
                                        "Unable to close stream, failed while writing to stream",
                                        e);
                            }
                        }
                    },
                    destinationName + " Pipe  " + System.currentTimeMillis() // help identify the thread when debugging
            ).start();
            return in;
        }
    }

    private void write(OutputStream gzipOutputStream, boolean printToLog, MessageWriter messageWriter)
            throws IOException {
        if (printToLog) {
            try (LoggingOutputStream los = new LoggingOutputStream(gzipOutputStream, log)) {
                messageWriter.writeMessage(los);
            }
        } else {
            messageWriter.writeMessage(gzipOutputStream);
        }
    }

}
