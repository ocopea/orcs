// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id$
 *
 * This computer code is copyright 2015 EMC Corporation
 * All rights reserved
 */
package com.emc.microservice.metrics.reporter.influxdb;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

/**
 * Created by nivenb on 20/11/2015.
 */
public class InfluxDBJvmTagParserTest {

    @Test
    public void testNullInput() {
        Assert.assertEquals(Collections.EMPTY_MAP, new InfluxDBJvmTagParser().parse(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoEqualsSign() {
        InfluxDBJvmTagParser.parse("iDontContainAnEqualsSign");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLeadsWithEqualsSign() {
        InfluxDBJvmTagParser.parse("=iLeadWithAnEqualsSign");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultipleEqualsSignsWithNoDelimeter() {
        InfluxDBJvmTagParser.parse("tag1=value1 tag2=value2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultipleEqualsSignsWithIncorrectDelimeter() {
        InfluxDBJvmTagParser.parse("tag1=value1;tag2=value2");
    }

    @Test()
    public void testSingleTag() {
        Map<String, String> result = InfluxDBJvmTagParser.parse("tag1=value1");
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("value1", result.get("tag1"));
    }

    @Test()
    public void testMultipleTags() {
        Map<String, String> result = InfluxDBJvmTagParser.parse("tag1=value1,tag2=value2,tag3=value3");
        Assert.assertEquals(3, result.size());
        Assert.assertEquals("value1", result.get("tag1"));
        Assert.assertEquals("value2", result.get("tag2"));
        Assert.assertEquals("value3", result.get("tag3"));
    }
}
