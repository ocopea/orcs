// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging.jms;

import com.emc.microservice.Context;
import com.emc.microservice.messaging.InputQueueConfiguration;
import com.emc.microservice.messaging.ManagedMessageListener;
import com.emc.microservice.messaging.QueueConfiguration;
import com.emc.microservice.messaging.QueueReceiverImpl;
import org.slf4j.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import java.util.Collections;

/**
 * Created with true love.
 * User: liebea
 * Date: 12/1/13
 * Time: 7:33 PM
 */
public class JMSQueueReceiver extends QueueReceiverImpl<QueueConfiguration> implements ExceptionListener {
    private Connection connection = null;
    private final Destination destination;
    private final ConnectionFactory connectionFactory;
    private final Logger logger;

    public JMSQueueReceiver(
            ConnectionFactory connectionFactory,
            Destination destination,
            InputQueueConfiguration inputQueueConfiguration,
            QueueConfiguration queueConfiguration,
            ManagedMessageListener messageListener,
            Context context,
            String consumerName) {
        super(
                inputQueueConfiguration,
                queueConfiguration,
                Collections.<String, QueueConfiguration>emptyMap(),
                messageListener,
                context,
                consumerName);
        this.destination = destination;
        this.connectionFactory = connectionFactory;
        this.logger = context.createSubLogger(JMSQueueReceiver.class);
    }

    @Override
    public void init() {
        // Handling reconnect.
        try {
            if (connection == null) {

                // Creating a jms connection
                connection = connectionFactory.createConnection();

                // Handling connection exceptions
                connection.setExceptionListener(this);

                // Opening a ling living jms session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Creating the consumer and setting the message listener
                //todo: should we even support message selectors?
                String messagesSelector = getInputQueueConfiguration().getProperty("selector");
                MessageConsumer queueReceiver = messagesSelector == null ?
                        session.createConsumer(destination) : session.createConsumer(destination, messagesSelector);
                queueReceiver.setMessageListener(message -> {
                    try {
                        getMessageListener().onMessage(
                                new JMSMessage(
                                        getContext().getSerializationManager(),
                                        message,
                                        blobStoreAPI,
                                        getInputQueueConfiguration().isLogContentWhenInDebug() &&
                                                logger.isDebugEnabled(),
                                        getQueueConfiguration().isGzip()),
                                getContext());
                    } catch (Exception e) {
                        // TODO Decide how we want the health check manager logic to work
                        getContext().getHealthCheckManager().flagAsUnhealthy(
                                "Error processing message " + e.getMessage());
                        throw e;
                    }
                });
            }
        } catch (JMSException e) {
            throw new IllegalStateException("Failed Initializing JMS Connection ", e);
        }

    }

    @Override
    public void start() {

        // Handling first connection if not initialized
        if (connection == null) {
            throw new java.lang.IllegalStateException("Message receiver needs to be initialized before started");
        }

        // Start listening

        try {
            connection.start();
        } catch (JMSException jmsEx) {
            onException(jmsEx);
        }
        logger.debug(
                "Listening on queue {}, ({})",
                getInputQueueConfiguration().getInputQueueURI(),
                getInputQueueConfiguration().toString());

    }

    /**
     * Temporarily Suspends the connection connections
     */
    @Override
    public void pause() {
        try {
            if (connection != null) {
                connection.stop();
                logger.debug("Paused Listeners on queue {}, ({})",
                        getInputQueueConfiguration().getInputQueueURI(), getInputQueueConfiguration().toString());
            }
        } catch (JMSException e) {
            logger.error("Error pausing a jms connection for " +
                    getInputQueueConfiguration().getInputQueueURI() +
                    " (" + getInputQueueConfiguration().toString() + ")", e);
        }
    }

    /**
     * Note - this closes the connection and not the session and producer,
     * this is on purpose! please read the javadoc of the
     * connection close method: once closing the connection there is no need to close it's sessions and producers.
     */
    @Override
    public void cleanUp() {
        if (connection != null) {
            try {
                connection.close();
                logger.debug(
                        "Closing Message listener on queue {}, ({})",
                        getInputQueueConfiguration().getInputQueueURI(),
                        getInputQueueConfiguration().toString());
            } catch (Exception ex) {
                logger.error(
                        "Error closing a jms connection for " + getInputQueueConfiguration().getInputQueueURI() + " (" +
                                getInputQueueConfiguration().toString() + ")",
                        ex);
            } finally {
                connection = null;
            }

        }
    }

    @Override
    public void onException(JMSException exception) {
        logger.warn("connection exception for " + getInputQueueConfiguration().getInputQueueURI() + " (" +
                getInputQueueConfiguration().toString() + ")" + " - will attempt to restart", exception);
        boolean success = false;
        long timeToSleep = 100L;
        while (!success) {
            try {
                Thread.sleep(timeToSleep); //give things time to relax
                timeToSleep = 1000L; // Next times we'll wait longer

                // attempt to close existing connection - at the very least, this prevents HornetQ closing it for
                // us and shouting at us in the logs
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Exception t) {
                        // don't care one squidgy bit
                        logger.debug(
                                "Could not close connection for {} (). Message: {}",
                                new Object[]{getInputQueueConfiguration().getInputQueueURI(),
                                             getInputQueueConfiguration().toString(),
                                             t.getMessage()},
                                t);
                    }
                }

                // Indicating to create new connection
                connection = null;
                init();
                start();
                success = true;
            } catch (Exception e) {
                logger.error(
                        "error restarting connection for " + getInputQueueConfiguration().getInputQueueURI() + " (" +
                                getInputQueueConfiguration().toString() + ")" + " retrying connection in " +
                                timeToSleep + " millis",
                        e);
            }
        }
    }
}
