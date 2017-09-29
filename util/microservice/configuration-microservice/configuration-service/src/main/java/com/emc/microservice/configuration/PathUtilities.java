// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.configuration;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Created by shresa on 04/08/15.
 */
public class PathUtilities {
    private static Pattern VALID_PATH_NAME = Pattern.compile("[a-z,A-Z,0-9]+[a-z,A-Z,0-9,\\-\\.]*");

    static boolean isValid(String path) {
        Objects.requireNonNull(path, "Path can't be null");
        // Check if it is the root path
        if ("/".equals(path)) {
            return true;
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        for (String part : split(path)) {
            if (!isPathNameValid(part)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPathNameValid(String name) {
        return VALID_PATH_NAME.matcher(name).matches();
    }

    static String[] split(String path) {
        return path.split("/");
    }
}
