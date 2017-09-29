// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.messaging;

import com.emc.microservice.messaging.QueueConfiguration;
import com.emc.microservice.resource.ResourceConfigurationProperty;
import com.emc.microservice.resource.ResourceConfigurationPropertyType;

import java.util.Arrays;
import java.util.List;

/**
 * Created by liebea on 1/18/15.
 * Drink responsibly
 */
public class PersistentQueueConfiguration extends QueueConfiguration {
    private static final ResourceConfigurationProperty PROPERTY_DESTINATION_TYPE = new ResourceConfigurationProperty(
            "destinationType",
            ResourceConfigurationPropertyType.ENUM,
            "destination object type",
            true,
            false);
    private static final ResourceConfigurationProperty PROPERTY_QUEUE_NAME = new ResourceConfigurationProperty(
            "queueName",
            ResourceConfigurationPropertyType.STRING,
            "QueueName",
            true,
            false);
    private static final ResourceConfigurationProperty PROPERTY_MEMORY_BUFFER_MAX_MESSAGES =
            new ResourceConfigurationProperty(
                    "memoryBufferMaxMessages",
                    ResourceConfigurationPropertyType.INT,
                    "Messages buffer size",
                    true,
                    false);

    private static final ResourceConfigurationProperty PROPERTY_SECONDS_TO_SLEEP_BETWEEN_RETRIES =
            new ResourceConfigurationProperty(
                    "secondsToSleepBetweenMessageRetries",
                    ResourceConfigurationPropertyType.INT,
                    "Seconds to sleep between message retries",
                    true,
                    false);

    private static final ResourceConfigurationProperty PROPERTY_MAX_RETRIES = new ResourceConfigurationProperty(
            "maxRetries",
            ResourceConfigurationPropertyType.INT,
            "Maximum amount of retries when message fails to consume",
            true,
            false);

    private static final List<ResourceConfigurationProperty> PROPERTIES = Arrays.asList(
            PROPERTY_DESTINATION_TYPE,
            PROPERTY_QUEUE_NAME,
            PROPERTY_MEMORY_BUFFER_MAX_MESSAGES,
            PROPERTY_SECONDS_TO_SLEEP_BETWEEN_RETRIES,
            PROPERTY_MAX_RETRIES);

    public PersistentQueueConfiguration() {
        super("Persistent Queue", PROPERTIES);
    }

    public PersistentQueueConfiguration(
            MessageDestinationType messageDestinationType,
            String queueName,
            int memoryBufferMaxMessages,
            int secondsToSleepBetweenMessageRetries,
            int maxRetries) {
        this();
        setPropertyValues(propArrayToMap(new String[]{
                PROPERTY_DESTINATION_TYPE.getName(),
                messageDestinationType.name(),
                PROPERTY_QUEUE_NAME.getName(),
                queueName,
                PROPERTY_MEMORY_BUFFER_MAX_MESSAGES.getName(),
                Integer.toString(memoryBufferMaxMessages),
                PROPERTY_SECONDS_TO_SLEEP_BETWEEN_RETRIES.getName(),
                Integer.toString(secondsToSleepBetweenMessageRetries),
                PROPERTY_MAX_RETRIES.getName(),
                Integer.toString(maxRetries)
        }));

    }

    @Override
    public MessageDestinationType getMessageDestinationType() {
        return MessageDestinationType.valueOf(getProperty(PROPERTY_DESTINATION_TYPE.getName()));
    }

    @Override
    public boolean isGzip() {
        return false;
    }

    @Override
    public String getBlobstoreName() {
        return null;
    }

    public String getQueueName() {
        return getProperty(PROPERTY_QUEUE_NAME.getName());
    }

    public int getMemoryBufferMaxMessages() {
        return Integer.valueOf(getProperty(PROPERTY_MEMORY_BUFFER_MAX_MESSAGES.getName()));
    }

    public int getSecondsToSleepBetweenRetries() {
        return Integer.valueOf(getProperty(PROPERTY_SECONDS_TO_SLEEP_BETWEEN_RETRIES.getName()));
    }

    public int getMaxRetries() {
        return Integer.valueOf(getProperty(PROPERTY_MAX_RETRIES.getName()));
    }
}
