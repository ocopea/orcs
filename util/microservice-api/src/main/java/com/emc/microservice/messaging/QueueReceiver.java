// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2013-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.messaging;

/**
 * Created with true love.
 * User: liebea
 * Date: 12/1/13
 * Time: 7:33 PM
 */
public interface QueueReceiver {

    void init();

    void start();

    /**
     * Temporarily Suspends the connection connections
     */
    void pause();

    /**
     * Note - this closes the connection and not the session and producer,
     * this is on purpose! please read the javadoc of the
     * connection close method: once closing the connection there is no need to close it's sessions and producers.
     */
    void cleanUp();

}
