// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.demo.dsb.shpanblob;

import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.microservice.messaging.LoggingInputStream;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created with true love by liebea on 4/16/14.
 */
public class BlobStoreCopyParser {
    private static final Logger log = LoggerFactory.getLogger(BlobStoreCopyParser.class);
    private final BlobStoreAPI blobStoreAPI;

    public BlobStoreCopyParser(BlobStoreAPI blobStoreAPI) {
        this.blobStoreAPI = blobStoreAPI;
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
            return parseBlobstoreCopy(parser);
        }
    }

    private String parseBlobstoreCopy(JsonParser parser) throws IOException {
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
                    parser.nextToken();
                    while (parser.getCurrentToken() != JsonToken.END_ARRAY) {
                        processEntry(parser);
                        parser.nextToken();
                    }

                    doneParsing = true;
                    break;
                default:
                    // ignoring fields we don't care about
            }
        }
        return copyId;
    }

    private void processEntry(final JsonParser parser) throws IOException {

        String key = null;
        String namespace = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            parser.nextToken();
            String fieldName = parser.getCurrentName();
            switch (fieldName) {
                case "key":
                    key = parser.getValueAsString();
                    break;
                case "namespace":
                    namespace = parser.getValueAsString();
                    break;
                case "blob":
                    //todo:headers
                    blobStoreAPI.create(namespace, key, null, (out) -> {
                        try {
                            parser.readBinaryValue(out);
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    });
                    break;
                default:
                    // ignoring fields we don't care about
            }

        }
    }

}
