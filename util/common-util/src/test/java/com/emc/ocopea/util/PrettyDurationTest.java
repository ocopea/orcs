// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by L Braine on 06/11/2015.
 */
public class PrettyDurationTest {

    @Test
    public void testPrettyDuration() throws Exception {
        assertEquals("0m 1s 0ms", PrettyDuration.prettyDuration(1000));
    }

}
