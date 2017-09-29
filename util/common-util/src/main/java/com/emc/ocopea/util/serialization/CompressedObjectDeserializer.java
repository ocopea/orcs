// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: CompressedObjectDeserializer.java 87868 2014-07-07 03:20:28Z nivenb $
 *
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */
package com.emc.ocopea.util.serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.zip.ZipInputStream;

/**
 * @author nivenb Extracted from dpa report commands and made generic
 */
public class CompressedObjectDeserializer<T> {

    private static final Logger log = LoggerFactory.getLogger(CompressedObjectDeserializer.class);

    /**
     * NOTE: Only use this method if the class to deserialize is visible to the apollo classloader (i.e. if it is an
     * Apollo class or something that apollo depends on) If not, use the overloaded method - else you'll get
     * ClassNotFoundException's slapped in your face
     */
    public T deserializeFromFile(File file) throws IOException, ClassNotFoundException {
        return deserializeFromFile(file, new ApolloClassLoaderDeserializationHelper());
    }

    /**
     * Use this overloaded method when the class to deserialize is not visible to the Apollo classloader
     */
    public T deserializeFromFile(File file, DeserializationHelper deserializationHelper)
            throws IOException, ClassNotFoundException {

        FileInputStream fileInputStream = null;
        try {

            fileInputStream = new FileInputStream(file);
            return deserializeFromStream(fileInputStream, deserializationHelper);

        } finally {

            // cleanup
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException ioe) {
                    // ignore
                    log.debug("Could not close object fileInputStream: " + ioe.getMessage());
                }
            }

        }

    }

    /**
     * NOTE: Only use this method if the class to deserialize is visible to the apollo classloader (i.e. if it is an
     * Apollo class or something that apollo depends on) If not, use the overloaded method - else you'll get
     * ClassNotFoundException's slapped in your face
     */
    @SuppressWarnings("unchecked")
    public T deserializeFromStream(InputStream inputStream) throws IOException, ClassNotFoundException {
        return deserializeFromStream(inputStream, new ApolloClassLoaderDeserializationHelper());
    }

    /**
     * Use this overloaded method when the class to deserialize is not visible to the Apollo classloader
     */
    @SuppressWarnings("unchecked")
    public T deserializeFromStream(InputStream inputStream, DeserializationHelper deserializationHelper)
            throws IOException, ClassNotFoundException {

        ZipInputStream zipInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            zipInputStream = new ZipInputStream(inputStream);
            zipInputStream.getNextEntry();
            objectInputStream = new ObjectInputStream(zipInputStream);
            return (T) deserializationHelper.readObject(objectInputStream);
        } finally {

            // cleanup
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException ioe) {
                    // ignore
                    log.debug("Could not close object objectInputStream: " + ioe.getMessage());
                }
            }

            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (IOException ioe) {
                    // ignore
                    log.debug("Could not close object zipInputStream: " + ioe.getMessage());
                }
            }

        }

    }

    private class ApolloClassLoaderDeserializationHelper extends DeserializationHelper {
        @Override
        public Object readObject(ObjectInputStream objectInputStream) throws ClassNotFoundException, IOException {
            return objectInputStream.readObject();
        }
    }
}
