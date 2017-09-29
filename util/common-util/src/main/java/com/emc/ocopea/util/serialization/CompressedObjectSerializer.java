// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: CompressedObjectSerializer.java 89731 2014-08-21 18:23:44Z shresa $
 *
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */
package com.emc.ocopea.util.serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author nivenb Extracted from dpa report commands and made generic
 */
public class CompressedObjectSerializer<T> {

    private static Logger log = LoggerFactory.getLogger(CompressedObjectSerializer.class);

    /**
     * Serialize to the provided output file. It is asserted that the output file exists
     */
    public void serializeToFile(T object, File outputFile) throws IOException {

        try (OutputStream outputStream = new FileOutputStream(outputFile)) {

            serializeToStream(object, outputStream, outputFile.getAbsolutePath());

        }
    }

    /**
     * Serialize to the provided output stream. It is up to the calling code to close the stream
     */
    public void serializeToStream(T object, OutputStream outputStream, String zipEntryName) throws IOException {

        ZipOutputStream zipOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            zipOutputStream = new ZipOutputStream(outputStream);
            zipOutputStream.putNextEntry(new ZipEntry(zipEntryName));
            objectOutputStream = new ObjectOutputStream(zipOutputStream);
            objectOutputStream.writeObject(object);
            zipOutputStream.closeEntry();
        } finally {

            // cleanup
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException ioe) {
                    // ignore
                    log.debug("Could not close object objectOutputStream: " + ioe.getMessage());
                }
            }

            if (zipOutputStream != null) {
                try {
                    zipOutputStream.close();
                } catch (IOException ioe) {
                    // ignore
                    log.debug("Could not close object zipOutputStream: " + ioe.getMessage());
                }
            }
        }

    }

}
