// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging.jms;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MessageEOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Created with IntelliJ IDEA.
 * Date: 11/27/13
 * Time: 4:49 PM
 * <p>
 * this class was taken from axis2
 */
public class JMSBytesMessageInputStream extends InputStream {

    private final BytesMessage message;

    JMSBytesMessageInputStream(BytesMessage message) {
        Objects.requireNonNull(message, "Received an empty message.");
        this.message = message;
    }

    @Override
    public int read() throws IOException {
        try {
            return message.readByte() & 0xFF;
        } catch (MessageEOFException ex) {
            return -1;
        } catch (JMSException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (off == 0) {
            try {
                return message.readBytes(b, len);
            } catch (JMSException ex) {
                throw new IOException(ex);
            }
        } else {
            byte[] b2 = new byte[len];
            int c = read(b2);
            if (c > 0) {
                System.arraycopy(b2, 0, b, off, c);
            }
            return c;
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        try {
            return message.readBytes(b);
        } catch (JMSException ex) {
            throw new IOException(ex);
        }
    }
}
