// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by shresa on 12/05/2014.
 * logging output stream wrapper to print stream as it is written to log
 */
public class LoggingOutputStream extends OutputStream {
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(LoggingOutputStream.class);
    private final Logger logger;
    private final ByteArrayOutputStream bout = new ByteArrayOutputStream();
    private final OutputStream out;

    public LoggingOutputStream(OutputStream out, Logger logger) {
        this.out = out;
        this.logger = logger;
    }

    public LoggingOutputStream(OutputStream out) {
        this(out, DEFAULT_LOGGER);
    }

    @Override
    public void write(int b) throws IOException {
        bout.write(b);
        out.write(b);
    }

    @Override
    public void flush() throws IOException {
        bout.flush();
        out.flush();
    }

    @Override
    public void close() throws IOException {
        bout.close();
        out.close();
        logger.debug(bout.toString("UTF-8"));
    }

    public void finish() throws IOException {
        if (out instanceof GZIPOutputStream) {
            ((GZIPOutputStream) out).finish();
        }
    }
}
