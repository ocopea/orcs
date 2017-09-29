// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 10/26/16.
 * Drink responsibly
 */
public class ApplicationCopyErrorEvent extends ApplicationCopyEvent {

    private ApplicationCopyErrorEvent() {
    }

    protected ApplicationCopyErrorEvent(UUID id, UUID appInstanceId, long version, Date timeStamp, String message) {
        super(id, appInstanceId, version, timeStamp, message);
    }
}
