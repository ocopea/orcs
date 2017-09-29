// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: PrettyDuration.java 55385 2011-10-14 03:26:08Z nivenb $
 *
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */
package com.emc.ocopea.util;

import java.util.concurrent.TimeUnit;

/**
 * @author nivenb
 */
public class PrettyDuration {

    /**
     * Used for debug logging
     *
     * @param milliseconds time in millis
     *
     * @return pretty duration
     */
    public static String prettyDuration(long milliseconds) {

        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        milliseconds -= TimeUnit.SECONDS.toMillis(seconds);

        StringBuilder sb = new StringBuilder();
        sb.append(minutes);
        sb.append("m ");
        sb.append(seconds);
        sb.append("s ");
        sb.append(milliseconds);
        sb.append("ms");

        return (sb.toString());
    }

}
