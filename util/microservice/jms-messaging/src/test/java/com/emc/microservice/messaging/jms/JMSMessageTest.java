// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging.jms;

import com.emc.microservice.messaging.LoggingInputStream;
import com.emc.microservice.serialization.SerializationManagerImpl;
import org.junit.Test;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import java.io.ByteArrayOutputStream;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author ashish
 */
public class JMSMessageTest {

    @Test
    public void testThatWeGetGZipStream() throws Exception {
        try (ByteArrayOutputStream bstream = new ByteArrayOutputStream()) {
            try (GZIPOutputStream out = new GZIPOutputStream(bstream)) {
                out.write("{\"a\":1}".getBytes("UTF-8"));
                out.finish();
                JMSMessage jmsMsg = new JMSMessage(
                        new SerializationManagerImpl(),
                        new TestMessage(bstream.toByteArray()), null, false, true);

                jmsMsg.readMessage(in -> assertTrue(in instanceof GZIPInputStream));
            }
        }
    }

    @Test
    public void testThatWeGetLoggingStream() throws Exception {
        try (ByteArrayOutputStream bstream = new ByteArrayOutputStream()) {
            try (GZIPOutputStream out = new GZIPOutputStream(bstream)) {
                out.write("{\"a\":1}".getBytes("UTF-8"));
                out.finish();
                JMSMessage jmsMsg = new JMSMessage(
                        new SerializationManagerImpl(),
                        new TestMessage(bstream.toByteArray()), null, true, true);

                jmsMsg.readMessage(in -> assertTrue(in instanceof LoggingInputStream));
            }
        }
    }

    @Test
    public void testThatWeGetNormalStream() throws Exception {
        Message msg = new TestMessage("{\"a\":1}".getBytes("UTF-8"));
        JMSMessage jmsMsg = new JMSMessage(new SerializationManagerImpl(), msg, null, true, false);
        jmsMsg.readMessage(in -> assertFalse(in instanceof GZIPInputStream));
    }

    private static class TestMessage implements BytesMessage {

        private final byte[] data;
        private int counter = 0;

        TestMessage(byte[] data) {
            this.data = data;
            this.counter = 0;
        }

        @Override
        public long getBodyLength() throws JMSException {
            return data.length;
        }

        @Override
        public boolean readBoolean() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public byte readByte() throws JMSException {
            return data[counter++];
        }

        @Override
        public int readUnsignedByte() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public short readShort() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int readUnsignedShort() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public char readChar() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int readInt() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long readLong() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float readFloat() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public double readDouble() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String readUTF() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int readBytes(byte[] bytes) throws JMSException {
            return readBytes(bytes, bytes.length);
        }

        @Override
        public int readBytes(byte[] bytes, int i) throws JMSException {
            System.arraycopy(data, counter, bytes, 0, i);
            counter += i;
            return i;
        }

        @Override
        public void writeBoolean(boolean bln) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void writeByte(byte b) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void writeShort(short s) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void writeChar(char c) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void writeInt(int i) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void writeLong(long l) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void writeFloat(float f) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void writeDouble(double d) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void writeUTF(String string) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void writeBytes(byte[] bytes) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void writeBytes(byte[] bytes, int i, int i1) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void writeObject(Object o) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void reset() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getJMSMessageID() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setJMSMessageID(String string) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long getJMSTimestamp() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setJMSTimestamp(long l) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setJMSCorrelationIDAsBytes(byte[] bytes) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setJMSCorrelationID(String string) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getJMSCorrelationID() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Destination getJMSReplyTo() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setJMSReplyTo(Destination dstntn) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Destination getJMSDestination() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setJMSDestination(Destination dstntn) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getJMSDeliveryMode() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setJMSDeliveryMode(int i) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean getJMSRedelivered() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setJMSRedelivered(boolean bln) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getJMSType() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setJMSType(String string) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long getJMSExpiration() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setJMSExpiration(long l) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getJMSPriority() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setJMSPriority(int i) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void clearProperties() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean propertyExists(String string) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean getBooleanProperty(String string) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public byte getByteProperty(String string) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public short getShortProperty(String string) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getIntProperty(String string) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long getLongProperty(String string) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float getFloatProperty(String string) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public double getDoubleProperty(String string) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getStringProperty(String string) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object getObjectProperty(String string) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Enumeration getPropertyNames() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setBooleanProperty(String string, boolean bln) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setByteProperty(String string, byte b) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setShortProperty(String string, short s) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setIntProperty(String string, int i) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setLongProperty(String string, long l) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setFloatProperty(String string, float f) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setDoubleProperty(String string, double d) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setStringProperty(String string, String string1) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setObjectProperty(String string, Object o) throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void acknowledge() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void clearBody() throws JMSException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
