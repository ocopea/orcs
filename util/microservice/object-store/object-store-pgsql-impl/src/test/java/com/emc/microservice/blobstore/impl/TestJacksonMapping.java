// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2014 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.blobstore.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

/**
 *
 */
@Ignore
public class TestJacksonMapping {

    @Test
    public void testJacksonProperties() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        Properties properties = new Properties();
        properties.put("host.cpu.clock", "20Mhz");
        properties.put("host.hdd", "Seagate");
        objectMapper.writeValue(System.out, properties);

        properties = objectMapper.readValue(
                "{\"host\" : {\"cpu\" : {\"clock\":\"20Mhz\"},\"hdd\":\"Seagate\"}}",
                Properties.class);

        System.out.println(properties.getProperty("host.cpu"));
    }
}
