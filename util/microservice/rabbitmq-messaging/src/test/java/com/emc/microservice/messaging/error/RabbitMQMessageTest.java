// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging.error;

import com.emc.microservice.messaging.LoggingInputStream;
import com.emc.microservice.serialization.SerializationManagerImpl;
import com.rabbitmq.client.QueueingConsumer;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author ashish
 */
public class RabbitMQMessageTest {

    private final byte[] data;

    public RabbitMQMessageTest() throws Exception {
        data = "{\"a\":1}".getBytes("UTF-8");
    }

    @Test
    public void testThatWeGetGZipStreamForCompressedData() throws Exception {
        try (ByteArrayOutputStream s = new ByteArrayOutputStream()) {
            try (GZIPOutputStream out = new GZIPOutputStream(s)) {
                out.write(data);
                out.finish();
                QueueingConsumer.Delivery delivery = new QueueingConsumer.Delivery(null, null, s.toByteArray());
                RabbitMQMessage mqMsg =
                        new RabbitMQMessage(new SerializationManagerImpl(), delivery, null, true, false);

                mqMsg.readMessage(in -> assertTrue(in instanceof GZIPInputStream));
            }
        }
    }

    @Test
    public void testThatWeGetNormalStreamForUncompressedData() throws Exception {
        QueueingConsumer.Delivery delivery = new QueueingConsumer.Delivery(null, null, data);
        RabbitMQMessage mqMsg = new RabbitMQMessage(new SerializationManagerImpl(), delivery, null, false, false);

        mqMsg.readMessage(in -> assertFalse(in instanceof GZIPInputStream));
    }

    @Test
    public void testThatWeGetLoggingStreamForUncompressedData() throws Exception {
        QueueingConsumer.Delivery delivery = new QueueingConsumer.Delivery(null, null, data);
        RabbitMQMessage mqMsg = new RabbitMQMessage(new SerializationManagerImpl(), delivery, null, false, true);

        mqMsg.readMessage(in -> assertTrue(in instanceof LoggingInputStream));
    }

    @Test
    public void testThatWeGetLoggingStreamForCompressedData() throws Exception {

        try (ByteArrayOutputStream s = new ByteArrayOutputStream()) {
            try (GZIPOutputStream out = new GZIPOutputStream(s)) {
                out.write(data);
                out.finish();
                QueueingConsumer.Delivery delivery = new QueueingConsumer.Delivery(null, null, s.toByteArray());
                RabbitMQMessage mqMsg = new RabbitMQMessage(new SerializationManagerImpl(), delivery, null, true, true);

                mqMsg.readMessage(in -> assertTrue(in instanceof LoggingInputStream));
            }
        }
    }
}
