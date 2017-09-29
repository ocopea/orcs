// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.commands;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Created by liebea on 3/12/17.
 * Drink responsibly
 */
public class PsbAppServiceIdGeneratorTest {

    private static final Random random = new Random(System.currentTimeMillis());

    @Test
    public void testItNowBye() {
        Map<String, String> ret = PsbAppServiceIdGenerator.generatePsbAppServiceIdsByAppSvcName(
                "cool app",
                new HashSet<>(
                        Arrays.asList("svc a", "svc b", "svc c")),
                12 + 11
        );

        ret.values().forEach(s -> Assert.assertTrue(s + " too long", s.length() <= 12 + 11));

        Assert.assertEquals("cool-a-svc-a", ret.get("svc a").substring(0, "cool-a-svc-a".length()));
        Assert.assertEquals("cool-a-svc-b", ret.get("svc b").substring(0, "cool-a-svc-b".length()));
        Assert.assertEquals("cool-a-svc-c", ret.get("svc c").substring(0, "cool-a-svc-c".length()));

        ret = PsbAppServiceIdGenerator.generatePsbAppServiceIdsByAppSvcName(
                "cool app",
                new HashSet<>(
                        Arrays.asList("svc long test  name a", "svc long test  name b")),
                12 + 11
        );
        ret.values().forEach(s -> Assert.assertTrue(s + " too long", s.length() <= 12 + 11));
        System.out.println(ret);
        Assert.assertEquals("cool-a-svc-l", ret.get("svc long test  name a").substring(0, "cool-a-svc-l".length()));
        Assert.assertEquals("cool-a-svc-1", ret.get("svc long test  name b").substring(0, "cool-a-svc-1".length()));
    }

    @Test
    public void testRandom() {
        testIt(
                generateRandomString(),
                new HashSet<>(
                        Arrays.asList(generateRandomString(), generateRandomString(), generateRandomString())),
                50);
        testIt(
                generateRandomString(),
                new HashSet<>(
                        Arrays.asList(generateRandomString(), generateRandomString(), generateRandomString())),
                51);
        testIt(
                generateRandomString(),
                new HashSet<>(
                        Collections.singletonList(generateRandomString())),
                50);
    }

    void testIt(String appInstanceName, HashSet<String> appServiceNames, int length) {

        Map<String, String> ret = PsbAppServiceIdGenerator.generatePsbAppServiceIdsByAppSvcName(
                appInstanceName,
                appServiceNames,
                length
        );

        System.out.println(appInstanceName);
        System.out.println(appServiceNames);
        System.out.println(ret);
        ret.values().forEach(s -> Assert.assertTrue(s + " too long", s.length() <= length));
    }

    private String generateRandomString() {
        // Creating 1-30 length random string
        String longRandomString = (UUID.randomUUID().toString() + UUID.randomUUID().toString()).replaceAll("-","");
        int length = random.nextInt(30) + 1;
        return longRandomString.substring(0, length);
    }
}