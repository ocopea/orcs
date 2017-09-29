// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id$
 *
 * This computer code is copyright 2015 EMC Corporation
 * All rights reserved
 */
package com.emc.microservice.metrics.reporter.influxdb;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nivenb on 20/11/2015.
 */
public class InfluxDBJvmTagParser {

    /**
     * Tag/Values are separated by '='. Each tag/value pair is delimited with a comma
     * e.g.1 mytag=myvalue
     * e.g.2 mytag=myvalue,mytag2=myvalue2
     * Real world example: application.instance.id.on.my.paas=1
     */
    public static Map<String, String> parse(String jvmTags) throws IllegalArgumentException {

        if (jvmTags == null || jvmTags.isEmpty()) {
            return Collections.EMPTY_MAP;
        }

        String[] tagValuePairs = jvmTags.split(",");

        Map<String, String> result = new HashMap<>();
        for (String tagValuePair : tagValuePairs) {

            String[] tagValueSplit = tagValuePair.split("=");
            if (tagValueSplit.length != 2) {
                throw new IllegalArgumentException("jvmTags is in an invalid format. " +
                        "Multiple Tag/Value pairs must be delimited by ','. " +
                        "A tag/value pair must be separated with an '='. jvmTag input was '" + jvmTags + "'");
            }

            String tagName = tagValueSplit[0];
            if (tagName.isEmpty()) {
                throw new IllegalArgumentException("A tag name is empty. jvmTag input was '" + jvmTags + "'");
            }

            String value = tagValueSplit[1];
            if (value.isEmpty()) {
                throw new IllegalArgumentException("The tag value for tag name '" + tagName +
                        "' is empty. Full jvmTag input was '" + jvmTags + "'");
            }

            result.put(tagName, value);
        }

        return result;
    }
}
