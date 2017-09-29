// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2016 EMC Corporation All Rights Reserved
 */

package com.emc.microservice.messaging.error;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zydowr on 07/03/2016.
 */
public class RabbitMQQueueInit {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQQueueInit.class);

    private final RabbitMQMessagingProviderConfiguration messagingProviderConfiguration;
    private final RabbitMQQueueConfiguration rabbitMQQueueConfiguration;

    public RabbitMQQueueInit(
            RabbitMQMessagingProviderConfiguration messagingProviderConfiguration,
            RabbitMQQueueConfiguration rabbitMQQueueConfiguration) {
        this.messagingProviderConfiguration = messagingProviderConfiguration;
        this.rabbitMQQueueConfiguration = rabbitMQQueueConfiguration;
    }

    /**
     * Setups up a RabbitMQ queue.
     * <p>
     * <p>
     * If exchange name is empty, it will declare the queue. This is a direct queue and behaves like queue in JMS.
     * <p>
     * If exchange name is not empty and queue name is empty, it will create a temporary queue and bind it to the
     * exchange. This behaviour is like topic in JMS.
     * <p>
     * If both exchange and queue is present, it will create the queue, create the exchange and bind the queue to
     * the exchange.
     */
    public void createQueue() {
        String queueName = rabbitMQQueueConfiguration.getQueueName();
        String exchangeName = rabbitMQQueueConfiguration.getExchangeName();

        logger.info("Setting up queue: queueName={}, exchangeName={}", queueName, exchangeName);

        Connection connection = null;
        try {
            connection = createConnection(messagingProviderConfiguration);
            Channel channel = connection.createChannel();

            if (exchangeName == null || exchangeName.isEmpty()) {
                // equivalent to JMS queue
                channel.queueDeclare(queueName, true, false, false, null);
                return;
            }

            if (queueName == null || queueName.isEmpty()) {
                // equivalent to JMS topic
                channel.exchangeDeclare(exchangeName, "fanout");
                AMQP.Queue.DeclareOk queueStatus = channel.queueDeclare();
                // temporary queue with empty routing key
                channel.queueBind(queueStatus.getQueue(), exchangeName, "");
                return;
            }

            channel.exchangeDeclare(exchangeName, "direct", true, false, false, null);

            Map<String, Object> arguments = new HashMap<>();
            String deadLetterExchange = rabbitMQQueueConfiguration.getDeadLetterExchange();
            if (deadLetterExchange != null) {
                arguments.put("x-dead-letter-exchange", deadLetterExchange);
            }
            String deadLetterRoutingKey = rabbitMQQueueConfiguration.getDeadLetterRoutingKey();
            if (deadLetterRoutingKey != null) {
                arguments.put("x-dead-letter-routing-key", deadLetterRoutingKey);
            }
            Long messageTTL = rabbitMQQueueConfiguration.getMessageTTL();
            if (messageTTL != null) {
                arguments.put("x-message-ttl", messageTTL);
            }
            Long maxLength = rabbitMQQueueConfiguration.getMaxLength();
            if (maxLength != null) {
                arguments.put("x-max-length", maxLength);
            }

            AMQP.Queue.DeclareOk queueStatus = channel.queueDeclare(queueName, true, false, false, arguments);
            channel.queueBind(queueStatus.getQueue(), exchangeName, queueName);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException e) {
                    logger.error("Could not close RabbitMQ connection!", e);
                }
            }
        }
    }

    private static Connection createConnection(RabbitMQMessagingProviderConfiguration messagingProviderConfiguration)
            throws IOException {
        String username = messagingProviderConfiguration.getUserName();
        String password = messagingProviderConfiguration.getPassword();
        String virtualHost = messagingProviderConfiguration.getVirtualHost();
        String host = messagingProviderConfiguration.getHost();
        int port = messagingProviderConfiguration.getPort();

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Opening RabbitMQ connection: username={}, vhost={}, host={}, port={}",
                    username,
                    virtualHost,
                    host,
                    port);
        }

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(virtualHost);
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);

        return connectionFactory.newConnection();
    }

}
