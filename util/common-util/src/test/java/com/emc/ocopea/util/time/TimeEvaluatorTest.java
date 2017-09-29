// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.time;
/*
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import org.junit.Test;

import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertTrue;

/**
 * TODO: I suspect there are more tests needed here - but as I don't understand the use case for this
 * class, I defer
 */
public class TimeEvaluatorTest {

    @Test
    public void testEvaluate() throws Exception {

        final VariableTime vt = new VariableTime(30, 4, 3, 4, 12, 1972);
        TimeEvaluator timeEvaluator = new TimeEvaluator(vt, TimeZone.getTimeZone("EST"));
        Date now = new Date();

        int result1 = timeEvaluator.evaluate(now, true);
        int result2 = timeEvaluator.evaluate(now, false);

        assertTrue(result1 > 0);
        assertTrue(result2 > 0);
    }

    @Test
    public void testGetTime() throws Exception {

        /**
         *  parameter order for VariableTime :
         *  minute,  hour, dayOfWeek, dayOfMonth, month, year
         */
        final VariableTime vt = new VariableTime(30, 4, 3, 4, 12, 1972);
        TimeEvaluator timeEvaluator = new TimeEvaluator(vt, TimeZone.getTimeZone("EST"));
        Date now = new Date();
        long result = timeEvaluator.getTime(now, 10000);
        System.out.println("result = " + result);
        assertTrue(result != 0);
    }
}
