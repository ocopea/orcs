// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.messaging;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Represents a message in the system
 */
public interface Message {

    /**
     * Returns a unique message id
     */
    String getId();

    /**
     * Returns an immutable message headers or a detached copy
     */
    Map<String, String> getHeaders();

    /**
     * Allowing consumers to read the message body
     */
    void readMessage(Consumer<InputStream> messageConsumer);
}
