// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.messaging;

import com.emc.microservice.resource.ResourceConfiguration;
import com.emc.microservice.resource.ResourceConfigurationProperty;
import com.emc.microservice.resource.ResourceConfigurationPropertyType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created with love by liebea on 5/28/2014.
 */
public class InputQueueConfiguration extends ResourceConfiguration {
    private static final String CONFIGURATION_NAME = "Input Queue";

    private static final ResourceConfigurationProperty PROPERTY_INPUT_QUEUE_URI_NAME =
            new ResourceConfigurationProperty(
                    "inputQueueURI",
                    ResourceConfigurationPropertyType.STRING,
                    "Queue name",
                    false,
                    false);

    private static final ResourceConfigurationProperty PROPERTY_NUMBER_OF_LISTENERS =
            new ResourceConfigurationProperty(
                    "numberOfListeners",
                    ResourceConfigurationPropertyType.INT,
                    "Default Message Listeners number",
                    true,
                    false);

    private static final ResourceConfigurationProperty PROPERTY_LOG_IN_DEBUG =
            new ResourceConfigurationProperty(
                    "logInDebug",
                    ResourceConfigurationPropertyType.BOOLEAN,
                    "Log message body content when logging is in debug",
                    true,
                    false);

    private static final ResourceConfigurationProperty PROPERTY_DEAD_LETTER_QUEUES =
            new ResourceConfigurationProperty(
                    "deadLetterQueues",
                    ResourceConfigurationPropertyType.STRING,
                    "Dead Letter Queues",
                    false,
                    false);

    private static final List<ResourceConfigurationProperty> PROPERTIES = Arrays.asList(
            PROPERTY_INPUT_QUEUE_URI_NAME,
            PROPERTY_NUMBER_OF_LISTENERS,
            PROPERTY_LOG_IN_DEBUG,
            PROPERTY_DEAD_LETTER_QUEUES
    );

    public InputQueueConfiguration() {
        super(CONFIGURATION_NAME, PROPERTIES);
    }

    public InputQueueConfiguration(String queueURI,
                                   int numberOfListeners,
                                   boolean logInDebug,
                                   List<String> deadLetterQueues) {
        this();

        List<String> properties = new ArrayList<>();

        if (queueURI != null) {
            properties.add(PROPERTY_INPUT_QUEUE_URI_NAME.getName());
            properties.add(queueURI);
        }

        properties.add(PROPERTY_NUMBER_OF_LISTENERS.getName());
        properties.add(Integer.toString(numberOfListeners));

        properties.add(PROPERTY_LOG_IN_DEBUG.getName());
        properties.add(Boolean.toString(logInDebug));

        if (deadLetterQueues != null) {
            properties.add(PROPERTY_DEAD_LETTER_QUEUES.getName());
            properties.add(flattenDeadLetterQueues(deadLetterQueues));
        }

        setPropertyValues(propArrayToMap(properties.toArray(new String[properties.size()])));
    }

    private String flattenDeadLetterQueues(List<String> deadLetterQueues) {
        StringBuilder flatDeadLetterQueues = new StringBuilder();
        Iterator<String> iter = deadLetterQueues.iterator();
        while (iter.hasNext()) {
            flatDeadLetterQueues.append(iter.next());
            if (iter.hasNext()) {
                flatDeadLetterQueues.append("|");
            }
        }
        return flatDeadLetterQueues.toString();
    }

    private List<String> unflattenDeadLetterQueues(String flatDeadLetterQueues) {
        List<String> deadLetterQueues = new ArrayList<>();
        for (String deadLetterQueue : flatDeadLetterQueues.split("\\|")) {
            deadLetterQueues.add(deadLetterQueue);
        }
        return deadLetterQueues;
    }

    public String getInputQueueURI() {
        return getProperty(PROPERTY_INPUT_QUEUE_URI_NAME.getName());
    }

    public int getNumberOfListeners() {
        return Integer.parseInt(getProperty(PROPERTY_NUMBER_OF_LISTENERS.getName()));
    }

    public boolean isLogContentWhenInDebug() {
        return Boolean.valueOf(getProperty(PROPERTY_LOG_IN_DEBUG.getName()));
    }

    public List<String> getDeadLetterQueues() {
        String deadLetterQueues = getProperty(PROPERTY_DEAD_LETTER_QUEUES.getName());
        if (deadLetterQueues == null || deadLetterQueues.isEmpty()) {
            return Collections.emptyList();
        }
        return unflattenDeadLetterQueues(deadLetterQueues);
    }
}
