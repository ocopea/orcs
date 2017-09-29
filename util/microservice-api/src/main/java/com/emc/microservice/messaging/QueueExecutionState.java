// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.messaging;

import java.util.Date;
import java.util.Map;

/**
 * Created with love by liebea on 6/18/2014.
 */
public class QueueExecutionState {
    public static enum ReceiverRunningState {
        RUNNING,
        IDLE
    }

    private final Date initializedDate;
    private Long executionStartTime = null;
    private Long executionEndTime = null;
    private Map<String, String> headers = null;

    public QueueExecutionState(Date initializedDate) {
        this.initializedDate = initializedDate;
    }

    public Date getInitializedDate() {
        return initializedDate;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setExecutionState(Map<String, String> headers) {
        this.executionStartTime = System.currentTimeMillis();
        this.executionEndTime = null;
        this.headers = headers;
    }

    public void clearExecutionState() {
        this.executionEndTime = System.currentTimeMillis();
    }

    public Long getExecutionEndTime() {
        return executionEndTime;
    }

    public Long getExecutionStartTime() {
        return executionStartTime;
    }

    public ReceiverRunningState getState() {
        return (executionStartTime != null && executionEndTime == null) ?
                ReceiverRunningState.RUNNING : ReceiverRunningState.IDLE;
    }

}
