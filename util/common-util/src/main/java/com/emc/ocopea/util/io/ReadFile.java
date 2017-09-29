// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: ReadFile.java 54885 2011-09-26 05:26:51Z nivenb $
 *
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */
package com.emc.ocopea.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author nivenb
 */
public class ReadFile {

    /**
     * Read file contents from a resource location (e.g. a file that lives in the /resources dir or a subdirectory of
     * it). An example resourceLocation may simply be myFileName.xml or it may be /mySubDir/myFileName.xml
     *
     * @param resourceLocation string represents the resource name
     *
     * @return the content of the file held by the resource
     *
     * @throws IOException On any missing file error, read error etc
     */
    public static String getFileContentsFromResource(String resourceLocation) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return getFileContentsFromResource(resourceLocation, classLoader);
    }

    /**
     * Read file contents from a resource location (e.g. a file that lives in the /resources dir or a subdirectory of
     * it). An example resourceLocation may simply be myFileName.xml or it may be /mySubDir/myFileName.xml
     *
     * @param resourceLocation string represents the resource name
     * @param classLoader classLoader to use for resource fetching
     *
     * @return content of the file
     *
     * @throws IOException On any missing file error, read error etc
     */
    public static String getFileContentsFromResource(String resourceLocation, ClassLoader classLoader)
            throws IOException {

        InputStream inputStream = classLoader.getResourceAsStream(resourceLocation);
        if (inputStream == null) {
            throw new FileNotFoundException("Could not find resource file at path '" + resourceLocation + "'");
        }

        return getFileContents(inputStream).toString();
    }

    /**
     * @param resourceLocation string represents the resource name
     * @param classLoader class loader to use
     *
     * @return stringbuilder with content of the file
     */
    public static StringBuilder getFileContentsFromResourceAsStringBuilder(
            String resourceLocation,
            ClassLoader classLoader) throws IOException {

        InputStream inputStream = classLoader.getResourceAsStream(resourceLocation);
        if (inputStream == null) {
            throw new FileNotFoundException("Could not find resource file at path '" + resourceLocation + "'");
        }

        return getFileContents(inputStream);
    }

    /**
     * Read file contents from a File in the filesystem
     *
     * @param file file to read
     *
     * @return content of the file as string
     */
    public static String getFileContents(File file) throws IOException {

        FileInputStream fileInputStream = new FileInputStream(file);
        return getFileContents(fileInputStream).toString();
    }

    /**
     * @param inputStream input stream
     *
     * @return content of the file as string builder
     */
    public static StringBuilder getFileContents(InputStream inputStream) throws IOException {

        StringBuilder sb = new StringBuilder();
        final char[] buffer = new char[0x10000];
        Reader in = new InputStreamReader(inputStream, "UTF-8");
        try {
            int read;
            do {
                read = in.read(buffer, 0, buffer.length);
                if (read > 0) {
                    sb.append(buffer, 0, read);
                }
            } while (read >= 0);
        } finally {
            in.close();
        }

        return sb;
    }

    /**
     * @param reader reader
     *
     * @return content of the file as string builder
     */
    public static StringBuilder getFileContents(Reader reader) throws IOException {

        StringBuilder sb = new StringBuilder();
        final char[] buffer = new char[0x10000];
        try {
            int read;
            do {
                read = reader.read(buffer, 0, buffer.length);
                if (read > 0) {
                    sb.append(buffer, 0, read);
                }
            } while (read >= 0);
        } finally {
            reader.close();
        }

        return sb;
    }

}
