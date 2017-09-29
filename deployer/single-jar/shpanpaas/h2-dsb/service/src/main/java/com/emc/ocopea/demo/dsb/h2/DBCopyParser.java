// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.demo.dsb.h2;

import com.emc.microservice.messaging.LoggingInputStream;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.h2.tools.RunScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created with true love by liebea on 4/16/14.
 */
public class DBCopyParser {
    private static final Logger log = LoggerFactory.getLogger(DBCopyParser.class);
    private final DataSource dataSource;

    public DBCopyParser(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @NoJavadoc
    // TODO add javadoc
    public String parse(InputStream inputStream) throws IOException {
        if (log.isDebugEnabled()) {
            inputStream = new LoggingInputStream(inputStream);
        }

        JsonFactory factory = new JsonFactory();
        try (JsonParser parser = factory.createParser(inputStream)) {
            JsonToken token = parser.nextToken();
            if (JsonToken.START_OBJECT != token) {
                throw new IllegalArgumentException("expecting start object token");
            }

            // Parsing the content
            return parseDBCopy(parser);
        }
    }

    private String parseDBCopy(final JsonParser parser) throws IOException {
        String copyId = null;
        String serviceType = null;
        String serviceName = null;
        String copyPluginName = null;

        boolean doneParsing = false;
        while (parser.nextToken() != JsonToken.END_OBJECT && !doneParsing) {
            String fieldName = parser.getCurrentName();
            parser.nextToken();
            switch (fieldName) {
                case "copyId":
                    copyId = parser.getValueAsString();
                    break;
                case "serviceType":
                    serviceType = parser.getValueAsString();
                    break;
                case "serviceName":
                    serviceName = parser.getValueAsString();
                    break;
                case "copyPluginName":
                    copyPluginName = parser.getValueAsString();
                    break;
                case "data":
                    try (Connection c = dataSource.getConnection()) {
                        PipedInputStream in = new PipedInputStream();
                        final PipedOutputStream out = new PipedOutputStream(in);
                        MyRunnable myRunnable = new MyRunnable(parser, out);
                        Thread thread = new Thread(
                                myRunnable
                        );
                        thread.start();
                        RunScript.execute(c, new InputStreamReader(in));
                        thread.join();
                        if (myRunnable.finishedWithError != null) {
                            throw new IllegalStateException("Failed finishing copy stream for service " + serviceType +
                                    "/" + serviceName, myRunnable.finishedWithError);
                        }

                    } catch (SQLException | InterruptedException e) {
                        throw new IllegalStateException("Failed importing copy stream for service " + serviceType + "/"
                                + serviceName, e);
                    }
                    break;
                default:
                    // ignoring fields we don't care about
            }
        }
        return copyId;
    }

    private static class MyRunnable implements Runnable {
        private final JsonParser parser;
        private final PipedOutputStream out;
        private Exception finishedWithError = null;

        public MyRunnable(JsonParser parser, PipedOutputStream out) {
            this.parser = parser;
            this.out = out;
        }

        public void run() {
            try {
                parser.readBinaryValue(out);
                out.close();
            } catch (IOException e) {
                finishedWithError = e;
            }
        }
    }
}
