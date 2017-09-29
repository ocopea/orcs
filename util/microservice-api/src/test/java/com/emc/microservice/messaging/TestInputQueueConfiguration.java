// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2016 EMC Corporation All Rights Reserved
 */

package com.emc.microservice.messaging;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by zydowr on 10/02/2016.
 */
public class TestInputQueueConfiguration {

    private final static String QUEUE_URI = "input.queue";
    private final static int NUMBER_OF_LISTENERS = 10;
    private final static boolean LOG_IN_DEBUG = false;
    private final static List<String> DEAD_LETTER_QUEUES = new ArrayList<>(Arrays.asList("DLQ_1", "DLQ_2"));

    @Test
    public void testWithDeadLetterQueues() {
        InputQueueConfiguration inputQueueConfiguration = new InputQueueConfiguration(QUEUE_URI, NUMBER_OF_LISTENERS, LOG_IN_DEBUG, DEAD_LETTER_QUEUES);

        assertEquals(QUEUE_URI, inputQueueConfiguration.getInputQueueURI());
        assertEquals(NUMBER_OF_LISTENERS, inputQueueConfiguration.getNumberOfListeners());
        assertEquals(LOG_IN_DEBUG, inputQueueConfiguration.isLogContentWhenInDebug());

        List<String> deadLetterQueues = inputQueueConfiguration.getDeadLetterQueues();
        assertEquals(2, deadLetterQueues.size());

        String dlq1 = deadLetterQueues.get(0);
        assertEquals("DLQ_1", dlq1);

        String dlq2 = deadLetterQueues.get(1);
        assertEquals("DLQ_2", dlq2);
    }

    @Test
    public void testWithoutDeadLetterQueues() {
        InputQueueConfiguration inputQueueConfiguration = new InputQueueConfiguration(QUEUE_URI, NUMBER_OF_LISTENERS, LOG_IN_DEBUG, null);

        assertEquals(QUEUE_URI, inputQueueConfiguration.getInputQueueURI());
        assertEquals(NUMBER_OF_LISTENERS, inputQueueConfiguration.getNumberOfListeners());
        assertEquals(LOG_IN_DEBUG, inputQueueConfiguration.isLogContentWhenInDebug());

        List<String> deadLetterQueueConfigurations = inputQueueConfiguration.getDeadLetterQueues();
        assertEquals(0, deadLetterQueueConfigurations.size());
    }


    @Test
    public void testWitHDeadLetterQueuesEMpty() {
        List<String> deadLetterQueues = new ArrayList<>();
        InputQueueConfiguration inputQueueConfiguration = new InputQueueConfiguration(QUEUE_URI, NUMBER_OF_LISTENERS, LOG_IN_DEBUG, deadLetterQueues);

        assertEquals(QUEUE_URI, inputQueueConfiguration.getInputQueueURI());
        assertEquals(NUMBER_OF_LISTENERS, inputQueueConfiguration.getNumberOfListeners());
        assertEquals(LOG_IN_DEBUG, inputQueueConfiguration.isLogContentWhenInDebug());

        List<String> deadLetterQueueConfigurations = inputQueueConfiguration.getDeadLetterQueues();
        assertEquals(0, deadLetterQueueConfigurations.size());
    }
}
