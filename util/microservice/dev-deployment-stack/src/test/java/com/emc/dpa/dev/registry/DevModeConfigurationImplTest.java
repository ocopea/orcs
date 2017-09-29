// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.dpa.dev.registry;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by liebea on 7/19/15.
 * Drink responsibly
 */
public class DevModeConfigurationImplTest {

    @Test
    public void testDirectories() {
        DevModeConfigurationImpl c = new DevModeConfigurationImpl();
        c.mkdir("aaa");
        c.mkdir("bbb");
        c.mkdir("aaa/bbb");
        c.mkdir("aaa/bbb2");
        c.mkdir("aaa/bbb/ccc");

        Assert.assertTrue(c.isDirectory("aaa"));
        Assert.assertTrue(c.isDirectory("bbb"));
        Assert.assertTrue(c.isDirectory("aaa/bbb"));
        Assert.assertTrue(c.isDirectory("aaa/bbb2"));
        Assert.assertTrue(c.isDirectory("aaa/bbb/ccc"));
        Assert.assertTrue(c.exists("aaa"));
        Assert.assertTrue(c.exists("bbb"));
        Assert.assertTrue(c.exists("aaa/bbb"));
        Assert.assertTrue(c.exists("aaa/bbb2"));
        Assert.assertTrue(c.exists("aaa/bbb/ccc"));

        Assert.assertFalse(c.exists("aaabbbccc"));
        Assert.assertFalse(c.isDirectory("aaabbbccc"));
        Assert.assertFalse(c.exists("aaa/bbb/ccc/ddd"));
        Assert.assertFalse(c.isDirectory("aaa/bbb/ccc/ddd"));

        Assert.assertEquals(2, c.list("aaa").size());
        Assert.assertEquals(1, c.list("aaa/bbb").size());
        Assert.assertEquals(0, c.list("aaa/bbb/ccc").size());

    }

    @Test
    public void testData() {
        DevModeConfigurationImpl c = new DevModeConfigurationImpl();
        c.mkdir("aaa");
        c.mkdir("bbb");
        c.mkdir("aaa/bbb");
        c.mkdir("aaa/bbb2");
        c.mkdir("aaa/bbb/ccc");

        c.writeData("aaa/data1", "fun data1");
        c.writeData("aaa/bbb/ccc/ddd/data2", "fun data2");

        Assert.assertEquals(3, c.list("aaa").size());
        Assert.assertEquals(1, c.list("aaa/bbb/ccc/ddd").size());
        Assert.assertEquals("fun data2", c.readData("aaa/bbb/ccc/ddd/data2"));
        Assert.assertTrue(c.exists("aaa/bbb/ccc/ddd/data2"));
        Assert.assertFalse(c.isDirectory("aaa/bbb/ccc/ddd/data2"));

    }

}
