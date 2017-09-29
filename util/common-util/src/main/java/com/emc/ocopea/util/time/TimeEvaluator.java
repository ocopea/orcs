// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: TimeEvaluator.java 89620 2014-08-19 23:56:49Z nivenb $
 *
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */
package com.emc.ocopea.util.time;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Evaluate a 'Time' object - e.g. User or system defined times such as '9am today', 'Every 2 hours' etc
 * <p>
 * Code originally ported from CxmlTime
 */
public class TimeEvaluator {

    /*
     * Days/weeks etc in the future are increased by this amount to differentiate from actual days/weeks etc (ie to
     * differentiate a 'day of month' value from a 'days in the future' value)
     */
    public static final int FUTURE_PLACEHOLDER = 1000;
    private static Logger log = LoggerFactory.getLogger(TimeEvaluator.class);
    private final IVariableTime time;

    private final TimeZone reportRequestorTimeZone;

    public TimeEvaluator(IVariableTime time, TimeZone reportRequestorTimeZone) {
        this.time = time;
        this.reportRequestorTimeZone = reportRequestorTimeZone;
    }

    /**
     * Ported from CxmlTime
     * <p>
     * Evaluate the current CxmlTime using the reference date. Converts the time to real number of seconds past epoch.
     *
     * @param reference Reference date used for calculating time.
     * @param usePastDow If true uses past day of week when evaluating day of week. For example, if the reference date
     *     is on a Wednesday and day of week to evaluate says Monday, the method returns 2 days ago if usePastDow is
     *     true and 5 days in future if it is false.
     *
     * @return time Evaluated time as number of seconds since epoch.
     */
    public int evaluate(Date reference, boolean usePastDow) {
        Calendar referenceCal = Calendar.getInstance();
        referenceCal.setTimeZone(reportRequestorTimeZone);
        referenceCal.setTime(reference);

        Integer year = time.getYear();

        if (year != null) {
            if (year >= -499 && year <= -1) {
                // a year in the past
                referenceCal.add(Calendar.YEAR, year);
                log.debug("year time in the past " + referenceCal.getTime() + ". year value was " + year);
            } else if (year > 1000 && year < 1900) {
                // future values
                referenceCal.add(Calendar.YEAR, (year - 1000));
                log.debug("year time in the future " + referenceCal.getTime() + ". year value was " + year);
            } else if (year >= 1900) {
                // an actual year
                referenceCal.set(Calendar.YEAR, year);
            } else {
                log.warn("Invalid year value " + year);
            }
        }

        Integer month = time.getMonth();

        if (month != null) {
            if (month >= 0) {
                if (month > FUTURE_PLACEHOLDER) {
                    // a month in the future
                    referenceCal.add(Calendar.MONTH, (month - FUTURE_PLACEHOLDER));
                    log.debug("month time in the future " + referenceCal.getTime() + ". month value was " + month);
                } else {
                    // an actual month
                    referenceCal.set(Calendar.MONTH, month - 1); // months indexed at 0 in java
                }
            } else {
                // a month in the past
                referenceCal.add(Calendar.MONTH, month);
                log.debug("month time in the past " + referenceCal.getTime() + ". month value was " + month);
            }
        }

        Integer dayofmonth = time.getDayOfMonth();

        if (dayofmonth != null) {
            log.debug("dayofmonth");
            if (dayofmonth >= 0) {
                if (dayofmonth > FUTURE_PLACEHOLDER) {
                    // day in the future
                    referenceCal.add(Calendar.DAY_OF_MONTH, (dayofmonth - FUTURE_PLACEHOLDER));
                    log.debug("dayofmonth time in the future " + referenceCal.getTime() + ". dayofmonth value was " +
                            dayofmonth);
                } else {
                    if (dayofmonth == 31) {
                        // an actual day of the month
                        int actualDayofmonth = referenceCal.getActualMaximum(Calendar.DAY_OF_MONTH);
                        referenceCal.set(Calendar.DAY_OF_MONTH, actualDayofmonth);
                        log.debug("dayofmonth time " + referenceCal.getTime() + ". dayofmonth value was " + dayofmonth);
                    } else {
                        // an actual day of the month
                        referenceCal.set(Calendar.DAY_OF_MONTH, dayofmonth);
                    }
                }
            } else {
                // day in the past
                referenceCal.add(Calendar.DAY_OF_MONTH, dayofmonth);
                log.debug("dayofmonth time in the past " + referenceCal.getTime());
            }
        }

        Integer dayofweek = time.getDayOfWeek();

        if (dayofweek != null) {
            if (dayofweek >= 0) {

                if (dayofweek > FUTURE_PLACEHOLDER) {
                    int dowNow = referenceCal.get(Calendar.DAY_OF_WEEK);
                    int correctionFactor = dowNow - (dayofweek - FUTURE_PLACEHOLDER);

                    if (correctionFactor == 0) {
                        referenceCal.add(Calendar.DATE, 7);
                    } else if (correctionFactor > 0) {
                        referenceCal.add(Calendar.DATE, 1 * (7 - correctionFactor));
                    } else {
                        referenceCal.add(Calendar.DATE, -1 * correctionFactor);
                    }
                    log.debug("dayofweek time in the future " + referenceCal.getTime() + ". dayofweek value was " +
                            dayofweek);
                } else {
                    int refDow = referenceCal.get(Calendar.DAY_OF_WEEK);

                    if (usePastDow) {
                        if (dayofweek > refDow) {
                            referenceCal.add(Calendar.WEEK_OF_YEAR, -1);
                            referenceCal.getTime();
                        }
                    } else {
                        if (dayofweek < refDow) {
                            referenceCal.add(Calendar.WEEK_OF_YEAR, 1);
                            referenceCal.getTime();
                        }
                    }

                    referenceCal.set(Calendar.DAY_OF_WEEK, dayofweek);
                }
            } else {
                int dowNow = referenceCal.get(Calendar.DAY_OF_WEEK);
                int correctionFactor = dowNow + dayofweek;

                if (correctionFactor > 0) {
                    referenceCal.add(Calendar.DATE, -1 * correctionFactor);
                } else {
                    referenceCal.add(Calendar.DATE, -1 * (7 + correctionFactor));
                }
                log.debug(
                        "dayofweek time in the past " + referenceCal.getTime() + ". dayofweek value was " + dayofweek);
            }
        }

        Integer hour = time.getHour();

        if (hour != null) {
            if (hour >= 0) {
                if (hour > FUTURE_PLACEHOLDER) {
                    // an hour in the future
                    referenceCal.add(Calendar.HOUR_OF_DAY, (hour - FUTURE_PLACEHOLDER));
                    log.debug("hour time in the future " + referenceCal.getTime() + ". hour value was " + hour);
                } else {
                    referenceCal.set(Calendar.HOUR_OF_DAY, hour);
                }
            } else {
                referenceCal.add(Calendar.HOUR_OF_DAY, hour);
                log.debug("hour time in the past " + referenceCal.getTime() + ". hour value was " + hour);
            }
        }

        Integer minute = time.getMinute();

        if (minute != null) {
            if (minute >= 0) {
                if (minute > FUTURE_PLACEHOLDER) {
                    // an minute in the future
                    referenceCal.add(Calendar.MINUTE, (minute - FUTURE_PLACEHOLDER));
                    log.debug("minute time in the future " + referenceCal.getTime() + ". minute value was " + minute);
                } else {
                    referenceCal.set(Calendar.MINUTE, minute);
                }
            } else {
                referenceCal.add(Calendar.MINUTE, minute);
                log.debug("minute time in the past " + referenceCal.getTime() + ". minute value was " + minute);
            }
        }

        return (int) (referenceCal.getTimeInMillis() / 1000);
    }

    /**
     * Ported from CxmlTime
     * <p>
     * This is where the magic happens, converting a time definition into the real number of seconds
     */
    public long getTime(Date stime, long baseTime) {

        if (log.isDebugEnabled()) {
            log.debug("stime=" + stime);
            log.debug("reportRequestorTimeZone=" + reportRequestorTimeZone);
            log.debug("baseTime=" + baseTime);
        }

        Calendar newTime = Calendar.getInstance();
        newTime.setTimeZone(reportRequestorTimeZone);
        newTime.setTimeInMillis(baseTime);

        /*
         * If we have a relative time window where the start and end time are not the same then I would expect it to be
         * as follows:
         * - Start time: 0.000 seconds past the current time minus the relative offset
         * - End time: 59.999 seconds past the current time minus one minute
         *
         * So for example if the window was 'Last Hour' and the current time is 15:43:23 then I would expect the start
         * time to be 14:43:00 and the end time to be 15:42:59.999. This ensures that the data that we show is always
         * using the last complete minute (rather than the current system that uses the next complete minute and as such
         * may have no data yet), and also that we don't get the old problem of the second-by-second changes to reports.
         *
         * If the start and end time are the same then I would leave things exactly as they are (so no rounding at all),
         * as that allows second-by-second updating when the customer asks for 'Now'.
         */
        if (stime == null) {
            newTime.set(Calendar.SECOND, 0);
            newTime.set(Calendar.MILLISECOND, 0);
        } else {
            /* This is an end time then set to top of minute */
            newTime.add(Calendar.MINUTE, -1);
            newTime.set(Calendar.SECOND, 59);
            newTime.set(Calendar.MILLISECOND, 999);
        }

        Integer year = time.getYear();

        if (year != null) {
            if (year >= -499 && year <= -1) {
                // a year in the past
                newTime.add(Calendar.YEAR, year);
                log.debug("year time in the past " + newTime.getTime() + ". year value was " + year);
            } else if (year > 1000 && year < 1900) {
                // future values
                newTime.add(Calendar.YEAR, (year - 1000));
                log.debug("year time in the future " + newTime.getTime() + ". year value was " + year);
            } else if (year >= 1900) {
                // an actual year
                newTime.set(Calendar.YEAR, year);
            } else {
                log.warn("Invalid year value " + year);
            }
        }

        Integer month = time.getMonth();

        if (month != null) {
            if (month >= 0) {
                if (month > FUTURE_PLACEHOLDER) {
                    // a month in the future
                    newTime.add(Calendar.MONTH, (month - FUTURE_PLACEHOLDER));
                    log.debug("month time in the future " + newTime.getTime() + ". month value was " + month);
                } else {
                    // an actual month
                    newTime.set(Calendar.MONTH, month - 1); // months indexed at 0 in java
                }
            } else {
                // a month in the past
                newTime.add(Calendar.MONTH, month);
                log.debug("month time in the past " + newTime.getTime() + ". month value was " + month);
            }
        }

        Integer dayofmonth = time.getDayOfMonth();

        if (dayofmonth != null) {
            log.debug("dayofmonth");
            if (dayofmonth >= 0) {
                if (dayofmonth > FUTURE_PLACEHOLDER) {
                    // day in the future
                    newTime.add(Calendar.DAY_OF_MONTH, (dayofmonth - FUTURE_PLACEHOLDER));
                    log.debug("dayofmonth time in the future " + newTime.getTime() + ". dayofmonth value was " +
                            dayofmonth);
                } else {
                    if (dayofmonth == 31) {
                        // an actual day of the month
                        int actualDayofmonth = newTime.getActualMaximum(Calendar.DAY_OF_MONTH);
                        newTime.set(Calendar.DAY_OF_MONTH, actualDayofmonth);
                        log.debug("dayofmonth time " + newTime.getTime() + ". dayofmonth value was " + dayofmonth);
                    } else {
                        // an actual day of the month
                        newTime.set(Calendar.DAY_OF_MONTH, dayofmonth);
                    }
                }
            } else {
                // day in the past
                newTime.add(Calendar.DAY_OF_MONTH, dayofmonth);
                log.debug("dayofmonth time in the past " + newTime.getTime());
            }
        }

        Integer dayofweek = time.getDayOfWeek();

        if (dayofweek != null) {
            if (dayofweek >= 0) {

                if (dayofweek > FUTURE_PLACEHOLDER) {
                    int dowNow = newTime.get(Calendar.DAY_OF_WEEK);
                    int correctionFactor = dowNow - (dayofweek - FUTURE_PLACEHOLDER);

                    if (correctionFactor == 0) {
                        newTime.add(Calendar.DATE, 7);
                    } else if (correctionFactor > 0) {
                        newTime.add(Calendar.DATE, 1 * (7 - correctionFactor));
                    } else {
                        newTime.add(Calendar.DATE, -1 * correctionFactor);
                    }
                    log.debug(
                            "dayofweek time in the future " + newTime.getTime() + ". dayofweek value was " + dayofweek);
                } else {
                    int today = newTime.get(Calendar.DAY_OF_WEEK);
                    int thisweek = newTime.get(Calendar.WEEK_OF_YEAR);

                    int startday = -1;
                    int startweek = -1;

                    if (stime != null) {
                        Calendar startTime = Calendar.getInstance();
                        startTime.setTimeZone(reportRequestorTimeZone);
                        startTime.setTime(stime);
                        startday = startTime.get(Calendar.DAY_OF_WEEK);
                        startweek = startTime.get(Calendar.WEEK_OF_YEAR);
                    }

                    if ((dayofweek > today) && (startday == -1)) {
                        newTime.add(Calendar.WEEK_OF_YEAR, -1);
                        newTime.getTime();
                    }

                    if ((dayofweek < startday) && (startday != -1)) {
                        if (thisweek == startweek) {
                            newTime.add(Calendar.WEEK_OF_YEAR, 1);
                        }

                        newTime.getTime();
                    }

                    newTime.set(Calendar.DAY_OF_WEEK, dayofweek);
                }
            } else {
                int dowNow = newTime.get(Calendar.DAY_OF_WEEK);
                int correctionFactor = dowNow + dayofweek;

                if (correctionFactor > 0) {
                    newTime.add(Calendar.DATE, -1 * correctionFactor);
                } else {
                    newTime.add(Calendar.DATE, -1 * (7 + correctionFactor));
                }
                log.debug("dayofweek time in the past " + newTime.getTime() + ". dayofweek value was " + dayofweek);
            }
        }

        Integer hour = time.getHour();

        if (hour != null) {
            if (hour >= 0) {
                if (hour > FUTURE_PLACEHOLDER) {
                    // an hour in the future
                    newTime.add(Calendar.HOUR_OF_DAY, (hour - FUTURE_PLACEHOLDER));
                    log.debug("hour time in the future " + newTime.getTime() + ". hour value was " + hour);
                } else {
                    newTime.set(Calendar.HOUR_OF_DAY, hour);
                }
            } else {
                newTime.add(Calendar.HOUR_OF_DAY, hour);
                log.debug("hour time in the past " + newTime.getTime() + ". hour value was " + hour);
            }
        }

        Integer minute = time.getMinute();

        if (minute != null) {
            if (minute >= 0) {
                if (minute > FUTURE_PLACEHOLDER) {
                    // an minute in the future
                    newTime.add(Calendar.MINUTE, (minute - FUTURE_PLACEHOLDER));
                    log.debug("minute time in the future " + newTime.getTime() + ". minute value was " + minute);
                } else {
                    newTime.set(Calendar.MINUTE, minute);
                }
            } else {
                newTime.add(Calendar.MINUTE, minute);
                log.debug("minute time in the past " + newTime.getTime() + ". minute value was " + minute);
            }
        }

        long millis = newTime.getTimeInMillis();

        int result = ((int) (millis / 1000));

        return (result);
    }

}
