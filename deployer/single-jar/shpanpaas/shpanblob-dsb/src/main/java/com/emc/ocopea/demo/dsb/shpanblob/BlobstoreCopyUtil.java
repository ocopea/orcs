// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.demo.dsb.shpanblob;

import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.microservice.blobstore.BlobStoreLink;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.commons.io.input.CountingInputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

/**
 * Created by liebea on 12/28/15.
 * Drink responsibly
 */
public abstract class BlobstoreCopyUtil {
    @NoJavadoc
    // TODO add javadoc
    public static long doBlobstoreCopy(
            OutputStream out,
            String serviceType,
            String serviceName,
            String copyPluginName,
            String copyId,
            BlobStoreAPI blobStoreAPI) {
        final long[] copySize = {0L};

        try (final JsonGenerator generator = new JsonFactory().createGenerator(out)) {
            generator.writeStartObject();
            generator.writeStringField("copyId", copyId);
            generator.writeStringField("serviceType", serviceType);
            generator.writeStringField("serviceName", serviceName);
            generator.writeStringField("copyPluginName", copyPluginName);

            Collection<BlobStoreLink> blobKeys = blobStoreAPI.list();

            generator.writeArrayFieldStart("data");
            try {
                for (final BlobStoreLink currKey : blobKeys) {
                    generator.writeStartObject();
                    try {
                        generator.writeStringField("namespace", currKey.getNamespace());
                        generator.writeStringField("key", currKey.getKey());
                        blobStoreAPI.readBlob(currKey.getNamespace(), currKey.getKey(), in -> {
                            CountingInputStream countingInputStream = new CountingInputStream(in);
                            try {
                                generator.writeFieldName("blob");
                                generator.writeBinary(countingInputStream, -1);
                            } catch (IOException e) {
                                throw new IllegalStateException(e);
                            }
                            //todo:copy headers
                            copySize[0] += countingInputStream.getByteCount();
                        });
                    } finally {
                        generator.writeEndObject();
                    }
                }
            } finally {
                generator.writeEndArray();
            }
            generator.writeNumberField("copySize", copySize[0]);
            generator.writeEndObject();
            return copySize[0];
        } catch (IOException e) {
            throw new IllegalStateException("Failed generating copy for " + serviceType + '/' + serviceName, e);
        }
    }
}
