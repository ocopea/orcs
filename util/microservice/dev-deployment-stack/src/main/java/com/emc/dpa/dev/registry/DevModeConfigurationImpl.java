// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.dpa.dev.registry;

import com.emc.microservice.config.ConfigurationAPI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by liebea on 7/19/15.
 * Drink responsibly
 */
public class DevModeConfigurationImpl implements ConfigurationAPI {

    private final ConcurrentMap<String, Set<String>> directories = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> values = new ConcurrentHashMap<>();

    @Override
    public Collection<String> list(String path) {
        validatePath(path);
        Set<String> childNodes = directories.get(path);
        if (childNodes == null) {
            return Collections.emptyList();
        } else {
            return new ArrayList<>(childNodes);
        }
    }

    @Override
    public String readData(String path) {
        validatePath(path);
        return values.get(path);
    }

    @Override
    public boolean isDirectory(String path) {
        validatePath(path);
        return directories.containsKey(path);
    }

    @Override
    public boolean exists(String path) {
        validatePath(path);
        return values.containsKey(path) || directories.containsKey(path);
    }

    @Override
    public void writeData(String path, String data) {

        validatePath(path);

        String dataDirPath = path.substring(0, path.lastIndexOf("/"));

        // Verifying full path exists and create
        makeDirectory(dataDirPath);

        values.put(path, data);
        directories.get(dataDirPath).add(path);
    }

    @Override
    public void mkdir(String path) {
        validatePath(path);

        makeDirectory(path);

    }

    private void makeDirectory(String path) {
        // Creating full path. entry for every level
        String[] pathParts = path.split("/");
        String fullPath = null;
        String previousPath = null;
        for (String currPart : pathParts) {
            if (fullPath == null) {
                fullPath = currPart;
            } else {
                previousPath = fullPath;
                fullPath += "/" + currPart;
            }
            Set<String> values = directories.get(fullPath);
            if (values == null) {
                directories.put(fullPath, new HashSet<String>());
            }
            if (previousPath != null) {
                directories.get(previousPath).add(fullPath);
            }
        }
    }

    private void validatePath(String path) {
        if (path.startsWith("/")) {
            throw new IllegalArgumentException("Invalid path starting with '/': " + path);
        }
        if (path.endsWith("/")) {
            throw new IllegalArgumentException("Invalid path ends with '/': " + path);
        }
    }
}
