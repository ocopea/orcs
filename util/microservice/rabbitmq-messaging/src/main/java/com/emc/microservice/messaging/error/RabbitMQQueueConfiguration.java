// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging.error;

import com.emc.microservice.messaging.QueueConfiguration;
import com.emc.microservice.resource.ResourceConfigurationProperty;
import com.emc.microservice.resource.ResourceConfigurationPropertyType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created with true love by liebea on 10/12/2014.
 */
public class RabbitMQQueueConfiguration extends QueueConfiguration {

    private static final String CONFIGURATION_NAME = "RabbitMQ Queue";

    private static final ResourceConfigurationProperty PROPERTY_QUEUE_NAME = new ResourceConfigurationProperty(
            "queueName",
            ResourceConfigurationPropertyType.STRING,
            "physical queue name of the destination object",
            false,
            false);
    private static final ResourceConfigurationProperty PROPERTY_EXCHANGE_NAME = new ResourceConfigurationProperty(
            "exchangeName",
            ResourceConfigurationPropertyType.STRING,
            "Exchange name",
            false,
            false);
    private static final ResourceConfigurationProperty PROPERTY_DESTINATION_TYPE = new ResourceConfigurationProperty(
            "destinationType",
            ResourceConfigurationPropertyType.ENUM,
            "destination object type",
            true,
            false);
    private static final ResourceConfigurationProperty PROPERTY_BLOBSTORE_NAME = new ResourceConfigurationProperty(
            "blobstoreName",
            ResourceConfigurationPropertyType.STRING,
            "Blobstore to use for large messages",
            false,
            false);
    private static final ResourceConfigurationProperty PROPERTY_GZIP = new ResourceConfigurationProperty("gzip",
            ResourceConfigurationPropertyType.BOOLEAN,
            "Gzip body when writing",
            true,
            false);
    private static final ResourceConfigurationProperty PROPERTY_MESSAGE_GROUPING = new ResourceConfigurationProperty(
            "messageGrouping",
            ResourceConfigurationPropertyType.BOOLEAN,
            "Message Grouping",
            false,
            false);
    private static final ResourceConfigurationProperty PROPERTY_ACTIVATED = new ResourceConfigurationProperty(
            "activated",
            ResourceConfigurationPropertyType.BOOLEAN,
            "Activated",
            false,
            false);
    private static final ResourceConfigurationProperty PROPERTY_MESSAGE_TTL = new ResourceConfigurationProperty(
            "x-message-ttl",
            ResourceConfigurationPropertyType.LONG,
            "Message Time to Live",
            false,
            false);
    private static final ResourceConfigurationProperty PROPERTY_DEAD_LETTER_EXCHANGE =
            new ResourceConfigurationProperty("x-dead-letter-exchange",
                    ResourceConfigurationPropertyType.STRING,
                    "Dead Letter Exchange",
                    false,
                    false);
    private static final ResourceConfigurationProperty PROPERTY_DEAD_LETTER_ROUTING_KEY =
            new ResourceConfigurationProperty("x-dead-letter-routing-key",
                    ResourceConfigurationPropertyType.STRING,
                    "Dead Letter Routing Key",
                    false,
                    false);
    private static final ResourceConfigurationProperty PROPERTY_MAX_LENGTH = new ResourceConfigurationProperty(
            "x-max-length",
            ResourceConfigurationPropertyType.LONG,
            "Queue Max Length",
            false,
            false);

    private static final List<ResourceConfigurationProperty> PROPERTIES = Arrays.asList(
            PROPERTY_QUEUE_NAME, PROPERTY_EXCHANGE_NAME, PROPERTY_DESTINATION_TYPE,
            PROPERTY_BLOBSTORE_NAME,
            PROPERTY_GZIP, PROPERTY_MESSAGE_GROUPING, PROPERTY_ACTIVATED,
            PROPERTY_MESSAGE_TTL, PROPERTY_DEAD_LETTER_EXCHANGE, PROPERTY_DEAD_LETTER_ROUTING_KEY, PROPERTY_MAX_LENGTH
    );

    public RabbitMQQueueConfiguration() {
        super(CONFIGURATION_NAME, PROPERTIES);
    }

    public String getExchangeName() {
        return getProperty(PROPERTY_EXCHANGE_NAME.getName());
    }

    @Override
    public String getBlobstoreName() {
        return getProperty(PROPERTY_BLOBSTORE_NAME.getName());
    }

    public String getQueueName() {
        return getProperty(PROPERTY_QUEUE_NAME.getName());
    }

    public Long getMessageTTL() {
        String prop = getProperty(PROPERTY_MESSAGE_TTL.getName());
        return prop == null ? null : Long.valueOf(prop);
    }

    public String getDeadLetterExchange() {
        return getProperty(PROPERTY_DEAD_LETTER_EXCHANGE.getName());
    }

    public String getDeadLetterRoutingKey() {
        return getProperty(PROPERTY_DEAD_LETTER_ROUTING_KEY.getName());
    }

    public Long getMaxLength() {
        String prop = getProperty(PROPERTY_MAX_LENGTH.getName());
        return prop == null ? null : Long.valueOf(prop);
    }

    @Override
    public MessageDestinationType getMessageDestinationType() {
        return MessageDestinationType.valueOf(getProperty(PROPERTY_DESTINATION_TYPE.getName()));
    }

    @Override
    public boolean isGzip() {
        return Boolean.valueOf(getProperty(PROPERTY_GZIP.getName()));
    }

    @Override
    protected void customValidate(Map<String, String> propertyValues) {

        // NOTE: Don't rely on the getter methods here - they refer to the super getProperty methods which throw
        // NPE's as a collection is not yet init'd

        String queueName = propertyValues.get(PROPERTY_QUEUE_NAME.getName());
        String exchangeName = propertyValues.get(PROPERTY_EXCHANGE_NAME.getName());
        String messageGroupingStr = propertyValues.get(PROPERTY_MESSAGE_GROUPING.getName());
        boolean messageGrouping = messageGroupingStr == null ? false : Boolean.valueOf(messageGroupingStr);

        if (queueName == null && exchangeName == null) {
            throw new IllegalArgumentException("At least one of the properties " +
                    "destination name or exchange name must be specified");
        }

        if (messageGrouping && (exchangeName == null)) {
            throw new IllegalArgumentException(
                    "If message grouping is specified, an exchange name must also be specified. " +
                            "This exchange must be pre-declared on the RabbitMQ Server of " +
                            "exchange type x-consistent-hash");
        }

        if (messageGrouping && (queueName != null)) {
            throw new IllegalArgumentException(
                    "If message grouping is specified, a queue name cannot be specified. " +
                            "Queue names will be looked up");
        }
    }

    public boolean isMessageGrouping() {
        String prop = getProperty(PROPERTY_MESSAGE_GROUPING.getName());
        if (prop == null) {
            return false;
        }
        return Boolean.valueOf(prop);
    }

    public boolean isActivated() {
        String prop = getProperty(PROPERTY_ACTIVATED.getName());
        if (prop == null) {
            return true;
        }
        return Boolean.valueOf(prop);
    }
}
