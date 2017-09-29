// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.configuration;

import com.emc.ocopea.util.JsonUtil;
import com.emc.ocopea.util.MapBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

/**
 * Created by liebea on 7/20/17.
 * Drink responsibly
 */
public class StaticResourceConfigurationImplTest {
    @Test
    public void testBasic() {

        StaticResourceConfigurationImpl c = new StaticResourceConfigurationImpl("static-conf-test.json");

        final Collection<String> lst = c.list("a");

        Assert.assertEquals(2, lst.size());
        Assert.assertTrue(lst.contains("a/aa"));
        Assert.assertTrue(lst.contains("a/ab"));
        Assert.assertTrue(!c.readData("b").isEmpty());

        Assert.assertEquals(
                JsonUtil.toPrettyJson(
                        MapBuilder.<String, String>newHashMap()
                                .with("koko", "shoko")
                                .with("moka", "latte")
                                .build()),
                c.readData("a/ab/abc"));
    }
}