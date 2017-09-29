// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.time;
/*
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by L Braine on 07/11/2015.
 */
public class VariableTimeTest {

    @Test
    public void testVariableTime() {

        /**
         *
         * @param minute
         * @param hour
         * @param dayOfWeek
         * @param dayOfMonth
         * @param month
         * @param year
         */
        final VariableTime vt = new VariableTime(20, 4, 3, 4, 12, 1972);
        assertTrue(vt.getMinute() == 20);
        assertTrue(vt.getHour() == 4);
        assertTrue(vt.getDayOfWeek() == 3);
        assertTrue(vt.getDayOfMonth() == 4);
        assertTrue(vt.getMonth() == 12);
        assertTrue(vt.getYear() == 1972);
    }
}
