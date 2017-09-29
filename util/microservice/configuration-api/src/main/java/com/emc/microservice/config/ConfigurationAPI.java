// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.config;

import java.util.Collection;

/**
 * Created by liebea on 7/16/15.
 * Describes minimal implementation required to supply micro service configuration for a ResourceProvider stack
 */
public interface ConfigurationAPI {
    /***
     * Returns list of keys contained by the path
     * @param path path to a directory
     * @return List of keys contained in directory
     */
    Collection<String> list(String path);

    /***
     * Reads data for a specific path
     * @param path path separated by "/" delimiter
     */
    String readData(String path);

    /***
     * Checks whether a specific path is a directory or a data node
     * @param path path separated by "/" delimiter
     */
    boolean isDirectory(String path);

    /***
     * Verify if a certain path exists
     * @param path path separated by "/" delimiter
     * @return true if exists otherwise false
     */
    boolean exists(String path);

    /***
     * Writes data node. in case directory does not exist creating it according to path
     * In case node already exists overwriting it
     * @param path path separated by "/" delimiter
     * @param data data to store
     */
    void writeData(String path, String data);

    /***
     * Creates a directory according to path. Creates any missing parent.
     * @param path directory path separated by "/" delimiter
     */
    void mkdir(String path);
}
