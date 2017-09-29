// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author nivenb
 */
public class NaturalOrderStringComparatorTest {

    private static final Logger log = LoggerFactory.getLogger(NaturalOrderStringComparatorTest.class);

    @Test
    public void test() {
        String[] stringsSortedAsTheyShouldBe = new String[]{"1-2",
                                                            "1-02",
                                                            "1-20",
                                                            "10-20",
                                                            "fred",
                                                            "jane",
                                                            "pic01",
                                                            "pic2",
                                                            "pic02",
                                                            "pic02a",
                                                            "pic3",
                                                            "pic4",
                                                            "pic 4 else",
                                                            "pic 5",
                                                            "pic05",
                                                            "pic 5",
                                                            "pic 5 something",
                                                            "pic 6",
                                                            "pic 7",
                                                            "pic100",
                                                            "pic100a",
                                                            "pic120",
                                                            "pic121",
                                                            "pic02000",
                                                            "tom",
                                                            "x2-g8",
                                                            "x2-y7",
                                                            "x2-y08",
                                                            "x8-y8"};

        List toExpect = Arrays.asList(stringsSortedAsTheyShouldBe);
        log.debug("toExpect={}", toExpect);

        List copiedShuffledAndSorted = Arrays.asList(stringsSortedAsTheyShouldBe);
        Collections.shuffle(copiedShuffledAndSorted);
        Collections.sort(copiedShuffledAndSorted, new NaturalOrderStringComparator());
        log.debug("copiedShuffledAndSorted={}", copiedShuffledAndSorted);

        Assert.assertEquals(toExpect, copiedShuffledAndSorted);
    }

}
