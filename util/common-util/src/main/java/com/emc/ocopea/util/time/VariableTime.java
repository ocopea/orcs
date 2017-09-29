// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */
package com.emc.ocopea.util.time;

/**
 * @author nivenb
 */
public class VariableTime implements IVariableTime {

    private final Integer minute;

    private final Integer hour;

    private final Integer dayOfWeek;

    private final Integer dayOfMonth;

    private final Integer month;

    private final Integer year;

    public VariableTime(
            Integer minute,
            Integer hour,
            Integer dayOfWeek,
            Integer dayOfMonth,
            Integer month,
            Integer year) {
        this.minute = minute;
        this.hour = hour;
        this.dayOfWeek = dayOfWeek;
        this.dayOfMonth = dayOfMonth;
        this.month = month;
        this.year = year;
    }

    public Integer getDayOfMonth() {
        return dayOfMonth;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public Integer getYear() {
        return year;
    }

    public Integer getMonth() {
        return month;
    }

    public Integer getHour() {
        return hour;
    }

    public Integer getMinute() {
        return minute;
    }
}
