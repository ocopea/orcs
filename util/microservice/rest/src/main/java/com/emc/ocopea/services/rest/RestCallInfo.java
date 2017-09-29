// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.services.rest;

import java.util.Date;

/**
 * Created by liebea on 4/26/17.
 * Drink responsibly
 */
public class RestCallInfo {
    private final Date startTime;
    private final String url;
    private final String method;

    public RestCallInfo(Date startTime, String url, String method) {
        this.startTime = startTime;
        this.url = url;
        this.method = method;
    }

    public Date getStartTime() {
        return startTime;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return "RestCallInfo{" +
                "startTime=" + startTime +
                ", url='" + url + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
}
