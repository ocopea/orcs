// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.time;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import java.util.Random;

/**
 * Provides cron util methods:
 * <ul>
 *     <li>generate String with cron expression, corresponding to "Once a day" schedule. Exact time to run is
 *     chosen randomly.</li>
 *     <li>the same as previous, but user can specify interval (accurate to an hour) for running time.</li>
 * </ul>
 */
public final class CronSchedules {

    private static final Random random = new Random();

    private CronSchedules() {
    }

    /**
     * Generates String with cron expression, corresponding to "Once a day" schedule. Exact time to run is chosen
     * randomly.
     *
     * @return generated cron expression
     */
    public static String generateDailySchedule() {
        return random.nextInt(60) + " " + random.nextInt(60) + " " + random.nextInt(24) + " * * ? *";
    }

    /**
     * Generates String with cron expression, corresponding to "Once a day" schedule.
     * Exact time to run is chosen randomly in specified interval (accurate to an hour).
     *
     * @param startHour start hour of the interval, inclusive
     * @param endHour end hour of the interval, exclusive
     *
     * @return generated cron expression
     *
     * @throws IllegalArgumentException if startHour or endHour equals or greater than 23 or less than 0
     */
    public static String generateDailySchedule(int startHour, int endHour) {
        if (startHour < 0 || startHour > 23) {
            throw new IllegalArgumentException("Start hour isn't a valid 24-hours value, given " + startHour);
        }
        if (endHour < 0 || endHour > 23) {
            throw new IllegalArgumentException("End hour isn't a valid 24-hours value, given " + endHour);
        }
        if (startHour == endHour) {
            throw new IllegalArgumentException("Start hour equals end hour, they should be different");
        }
        int hours = (random.nextInt((endHour + 24 - startHour) % 24) + startHour) % 24;
        return random.nextInt(60) + " " + random.nextInt(60) + " " + hours + " * * ? *";
    }

}
