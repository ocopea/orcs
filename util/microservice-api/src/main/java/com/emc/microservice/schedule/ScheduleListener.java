// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.schedule;

import com.emc.microservice.messaging.Message;

public interface ScheduleListener {
    /**
     * The method to be executed when the schedule executes the listener.
     * @return must return true if the schedule should continue running, or false if the schedule should abort
     */
    boolean onTick(Message message);
}
