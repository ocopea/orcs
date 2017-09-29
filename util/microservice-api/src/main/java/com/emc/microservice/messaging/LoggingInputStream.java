// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */


package com.emc.microservice.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author shresa
 */
public class LoggingInputStream extends InputStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingInputStream.class);
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final InputStream in;

    public LoggingInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public int read() throws IOException {
        int v = in.read();
        out.write(v);
        return v;
    }

    @Override
    public synchronized void reset() throws IOException {
        out.reset();
        in.reset();
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }

    @Override
    public void close() throws IOException {
        out.close();
        LOGGER.debug(out.toString("UTF-8"));
        in.close();
    }

}
