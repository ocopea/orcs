// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging;

import java.util.Map;

/**
 * Created with love by liebea on 6/22/2014.
 * Interface used to send messages between different services
 */
public interface RuntimeMessageSender {

    /***
     * Stream message to remote service
     * @param messageWriter message writer to use when steaming the message
     * @param messageHeaders message headers to pass to the remote service
     * @param messageGroup Optional. Supply if using message grouping
     */
    void streamMessage(MessageWriter messageWriter, Map<String, String> messageHeaders, String messageGroup);
}
