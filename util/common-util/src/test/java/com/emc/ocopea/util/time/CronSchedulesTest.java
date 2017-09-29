// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id:$
 *
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */
package com.emc.ocopea.util.time;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class CronSchedulesTest {

    private static final String CRON_EXP_MATCHER =
            "^\\s*($|#|\\w+\\s*=|(\\?|\\*|(?:[0-5]?\\d)(?:(?:-|\\/|\\,)(?:[0-5]?\\d))?(?:,(?:[0-5]?\\d)(?:(?:-|\\/|\\,)(?:[0-5]?\\d))?)*)\\s+(\\?|\\*|(?:[0-5]?\\d)(?:(?:-|\\/|\\,)(?:[0-5]?\\d))?(?:,(?:[0-5]?\\d)(?:(?:-|\\/|\\,)(?:[0-5]?\\d))?)*)\\s+(\\?|\\*|(?:[01]?\\d|2[0-3])(?:(?:-|\\/|\\,)(?:[01]?\\d|2[0-3]))?(?:,(?:[01]?\\d|2[0-3])(?:(?:-|\\/|\\,)(?:[01]?\\d|2[0-3]))?)*)\\s+(\\?|\\*|(?:0?[1-9]|[12]\\d|3[01])(?:(?:-|\\/|\\,)(?:0?[1-9]|[12]\\d|3[01]))?(?:,(?:0?[1-9]|[12]\\d|3[01])(?:(?:-|\\/|\\,)(?:0?[1-9]|[12]\\d|3[01]))?)*)\\s+(\\?|\\*|(?:[1-9]|1[012])(?:(?:-|\\/|\\,)(?:[1-9]|1[012]))?(?:L|W)?(?:,(?:[1-9]|1[012])(?:(?:-|\\/|\\,)(?:[1-9]|1[012]))?(?:L|W)?)*|\\?|\\*|(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(?:(?:-)(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))?(?:,(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(?:(?:-)(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))?)*)\\s+(\\?|\\*|(?:[0-6])(?:(?:-|\\/|\\,|#)(?:[0-6]))?(?:L)?(?:,(?:[0-6])(?:(?:-|\\/|\\,|#)(?:[0-6]))?(?:L)?)*|\\?|\\*|(?:MON|TUE|WED|THU|FRI|SAT|SUN)(?:(?:-)(?:MON|TUE|WED|THU|FRI|SAT|SUN))?(?:,(?:MON|TUE|WED|THU|FRI|SAT|SUN)(?:(?:-)(?:MON|TUE|WED|THU|FRI|SAT|SUN))?)*)(|\\s)+(\\?|\\*|(?:|\\d{4})(?:(?:-|\\/|\\,)(?:|\\d{4}))?(?:,(?:|\\d{4})(?:(?:-|\\/|\\,)(?:|\\d{4}))?)*))$";

    private static void testCommon(String cron) {
        assertFalse(cron.trim().isEmpty()); // regexp incorrectly allows black strings
        assertTrue(cron.matches(CRON_EXP_MATCHER));

        String[] parts = cron.split(" ");

        int seconds = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int hours = Integer.parseInt(parts[2]);

        assertTrue(seconds >= 0 && seconds < 60);
        assertTrue(minutes >= 0 && minutes < 60);
        assertTrue(hours >= 0 && hours < 24);

        assertEquals("*", parts[3]);
        assertEquals("*", parts[4]);
        assertEquals("?", parts[5]);
        assertEquals("*", parts[6]);
    }

    private static void testBounds(String cron, List<Integer> expectedHours) {
        String[] parts = cron.split(" ");
        int hours = Integer.parseInt(parts[2]);
        assertTrue("Generated time out of bounds: " + hours, expectedHours.contains(hours));
    }

    @Test
    public void testGenerateDefaultDailySchedule() throws Exception {
        String cron = CronSchedules.generateDailySchedule();

        testCommon(cron);

        String otherCron = CronSchedules.generateDailySchedule();
        assertNotEquals(otherCron, cron);
    }

    @Test
    public void testGenerateDailyScheduleInBounds() throws Exception {
        int startHour = 10;
        int endHour = 12;
        List<Integer> expectedHours = Arrays.asList(10, 11);
        String cron = CronSchedules.generateDailySchedule(startHour, endHour);

        testCommon(cron);
        testBounds(cron, expectedHours);

        String otherCron = CronSchedules.generateDailySchedule();
        assertNotEquals(otherCron, cron);
    }

    @Test
    public void testGenerateDailyScheduleInDifferentDaysBounds() throws Exception {
        int startHour = 22;
        int endHour = 5;
        List<Integer> expectedHours = Arrays.asList(22, 23, 0, 1, 2, 3, 4);
        String cron = CronSchedules.generateDailySchedule(startHour, endHour);

        testCommon(cron);
        testBounds(cron, expectedHours);

        String otherCron = CronSchedules.generateDailySchedule();
        assertNotEquals(otherCron, cron);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartHourEqualsEndHour() throws Exception {
        CronSchedules.generateDailySchedule(5, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncorrectStartHour() throws Exception {
        CronSchedules.generateDailySchedule(25, 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncorrectEndHour() throws Exception {
        CronSchedules.generateDailySchedule(8, -1);
    }
}
