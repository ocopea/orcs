// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging.error;

import com.emc.microservice.Context;
import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.microservice.messaging.InputQueueConfiguration;
import com.emc.microservice.messaging.ManagedMessageListener;
import com.emc.microservice.messaging.QueueReceiverImpl;
import com.emc.microservice.serialization.SerializationManager;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by liebea on 9/28/2014. Enjoy it
 */
public class RabbitMQMessageReceiver extends QueueReceiverImpl<RabbitMQQueueConfiguration> {

    private final Logger log;
    private Connection connection = null;
    private Channel channel = null;
    private QueueingConsumer queueingConsumer;

    private volatile ConsumerRunnable consumer;
    private final ExecutorService executor;
    private final RabbitMQMessagingProviderConfiguration messagingProviderConfiguration;

    private static final Object staticMessageGroupingSyncObject = new Object();

    public RabbitMQMessageReceiver(
            RabbitMQMessagingProviderConfiguration messagingProviderConfiguration,
            RabbitMQQueueConfiguration queueConfiguration,
            Map<String, RabbitMQQueueConfiguration> deadLetterQueueConfigurations,
            InputQueueConfiguration inputQueueConfiguration,
            ManagedMessageListener messageListener,
            Context context,
            String consumerName) {

        super(
                inputQueueConfiguration,
                queueConfiguration,
                deadLetterQueueConfigurations,
                messageListener,
                context,
                consumerName);

        this.messagingProviderConfiguration = messagingProviderConfiguration;
        this.log = context.createSubLogger(RabbitMQMessageReceiver.class);
        executor = Executors.newSingleThreadExecutor();
    }

    public RabbitMQQueueConfiguration getQueueConfiguration() {
        return (RabbitMQQueueConfiguration) super.getQueueConfiguration();
    }

    @Override
    public void init() {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setUsername(messagingProviderConfiguration.getUserName());
            connectionFactory.setPassword(messagingProviderConfiguration.getPassword());
            connectionFactory.setVirtualHost(messagingProviderConfiguration.getVirtualHost());
            connectionFactory.setHost(messagingProviderConfiguration.getHost());
            connectionFactory.setPort(messagingProviderConfiguration.getPort());

            this.connection = connectionFactory.newConnection();

            //todo: register for connection errors. here? not sure..

            if (getQueueConfiguration().isMessageGrouping()) {
                // NOTE: When using message grouping, we'll allow our consumers to start in isolation
                // Whilst there will still be race conditions and subsequent re-tries concerning other
                // deployed instances, we can minimise the effort and noise by having our
                // threads-per-instance setup in isolation
                synchronized (staticMessageGroupingSyncObject) {
                    initForMessageGrouping();
                }
            } else {
                initForStandardSetup();
            }

        } catch (IOException e) {
            throw new IllegalStateException("Failed opening a rabbitMQ connection", e);
        }
    }

    private void initForStandardSetup() throws IOException {

        channel = this.connection.createChannel();

        queueingConsumer = new QueueingConsumer(channel);

        boolean autoAck = false;

        // This assumes that the queue has already been created by the bootstrap
        channel.basicConsume(getQueueConfiguration().getQueueName(), autoAck, queueingConsumer);
    }

    /**
     * Initiate for when messageGrouping is specified. Message grouping requires that an exchange be pre-declared on the
     * RabbitMQ Server, with a type of x-consistent-hash. It also requires multiple queues to be pre-declared and
     * pre-bound to that exchange.
     */
    private void initForMessageGrouping() throws IOException {

        // NOTE: There is a delay between the time that a consumer is attached to a queue,
        // to the time that this is reflected via the RabbitMQ management API
        // We already cater for race conditions further below -
        // and sometimes the delay is more than 1 second so isn't perfect - but sleeping here creates less noise.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            //ignore
        }

        String exchangeName = getQueueConfiguration().getExchangeName();
        String virtualHost = messagingProviderConfiguration.getVirtualHost();

        List<String> availableQueueNames = findAllQueuesOfExchangeThatHaveNoConsumers(exchangeName, virtualHost);
        if (availableQueueNames.isEmpty()) {
            log.warn("Could not find any available queues to attempt to listen on");
            throw new IllegalStateException("Could not find any available queues to attempt to listen on");
        }

        log.debug("Found {} available queues", availableQueueNames.size());

        // Let's shuffle the list to minimize race conditions with other instances that may be deployed at the same time
        // We cater for race conditions with re-tries on other queues but let's try to avoid this as much as we can
        Collections.shuffle(availableQueueNames);

        boolean autoAck = false;
        // we only allow a single consumer on each queue
        boolean exclusiveConsumer = true;
        boolean noLocal = false;
        Map<String, Object> arguments = Collections.emptyMap();
        boolean success = false;
        for (String queueName : availableQueueNames) {
            try {

                // NOTE: A failure in basicConsume closes the whole channel as opposed to just failing and/or closing
                // the consumer. So we must re-establish the channel each time
                ensureChannelIsClosed();
                channel = this.connection.createChannel();
                queueingConsumer = new QueueingConsumer(channel);

                String consumerTag = queueName;
                channel.basicConsume(
                        queueName,
                        autoAck,
                        consumerTag,
                        noLocal,
                        exclusiveConsumer,
                        arguments,
                        queueingConsumer);
                success = true;
                log.info("Listening on queue {}", queueName);
                break;
            } catch (Exception e) {
                // Of course, we can easily encounter race conditions here where queues have since become unavailable.
                // Let's simply recurse and keep trying
                if (isMessageGroupingQueueConnectionExceptionExpected(e)) {
                    log.debug(
                            "Could not connect to queue {} as it is already in use. " +
                                    "Will try remaining available queues.",
                            queueName);
                } else {
                    log.warn(
                            "Unexpected error when attempting to connect to queue {}. " +
                                    "Will still try remaining available queues.",
                            queueName,
                            e);
                }
            }
        }

        if (!success) {
            log.warn(
                    "Could not create a consumer on any queues. " +
                            "It is likely that all declared queues are already in use.");
            throw new IllegalStateException("Could not create a consumer on any queues");
        }
    }

    private void ensureChannelIsClosed() {
        if (channel != null && channel.isOpen()) {
            try {
                channel.close();
            } catch (AlreadyClosedException ace) {
                // ignore
            } catch (Exception e) {
                log.warn("Could not close channel", e);
            }
        }
    }

    private boolean isMessageGroupingQueueConnectionExceptionExpected(Exception e) {
        // NOTE: I can't find any RabbitMQ error codes
        ShutdownSignalException shutdownSignalException = null;
        if (e instanceof ShutdownSignalException) {
            shutdownSignalException = (ShutdownSignalException) e;
        } else if (e.getCause() != null && e.getCause() instanceof ShutdownSignalException) {
            shutdownSignalException = (ShutdownSignalException) e.getCause();
        }

        if (shutdownSignalException != null) {
            // NOTE: Looking at the text is the only way to determine this. The getReason classId/methodId/methodName
            // etc simply refer to the 'channel close' method
            if (shutdownSignalException.getMessage() != null &&
                    shutdownSignalException.getMessage().contains("in exclusive use")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Setups up a RabbitMQ queue.
     * <p>
     * If exchange name is empty, it will declare the queue. This is a direct queue and behaves like queue in JMS.
     * If exchange name is not empty and queue name is empty, it will create a temporary queue and bind it to the
     * exchange. This behaviour is like topic in JMS.
     * If both exchange and queue is present, it expects the setup to have been done in bootstrap or by another
     * setup function. This method does nothing.
     */
    private AMQP.Queue.DeclareOk setupQueue(String exchange, String queue, Channel channel) throws IOException {
        if (exchange == null || exchange.isEmpty()) {
            // equivalent to JMS queue
            return channel.queueDeclare(queue, true, false, false, null);
        }

        if (queue == null || queue.isEmpty()) {
            // equivalent to JMS topic
            channel.exchangeDeclare(exchange, "fanout");
            final AMQP.Queue.DeclareOk queueStatus = channel.queueDeclare();
            // temporary queue with empty routing key
            channel.queueBind(queueStatus.getQueue(), exchange, "");
            return queueStatus;
        }

        // TODO handle predefined exchange and queue
        throw new UnsupportedOperationException("This hasn't been implemented yet");
    }

    /**
     * Find all queues that are bound to a given exchange, and that have no active consumers listening to them
     */
    private List<String> findAllQueuesOfExchangeThatHaveNoConsumers(String exchangeName, String virtualHost)
            throws IOException {
        return Collections.emptyList();
    }

    @Override
    public void start() {
        if (consumer != null) {
            consumer.quit();
        }
        consumer = new ConsumerRunnable();
        executor.execute(consumer);
    }

    @Override
    public void pause() {
        consumer.quit();
    }

    @Override
    public void cleanUp() {
        pause();
        try {

            try {
                channel.close();
            } finally {
                connection.close();
            }
            executor.shutdown();
            this.consumer = null;
        } catch (Exception ignored) {
            log.warn("Error while cleanup", ignored);
        }
    }

    final class ConsumerRunnable implements Runnable {
        private boolean quit = false;

        void quit() {
            quit = true;
        }

        @Override
        public void run() {
            ManagedMessageListener listener = getMessageListener();
            Context context = getContext();
            SerializationManager serializationManager = context.getSerializationManager();
            boolean shouldGzip = getQueueConfiguration().isGzip();
            boolean shouldLogMessageContent =
                    getInputQueueConfiguration().isLogContentWhenInDebug() &&
                            log.isDebugEnabled();

            List<String> deadLetterQueues = getInputQueueConfiguration().getDeadLetterQueues();
            Map<String, RabbitMQQueueConfiguration> deadLetterQueueConfigurations = getDeadLetterQueueConfigurations();

            while (!quit) {
                handleDelivery(
                        queueingConsumer,
                        channel,
                        blobStoreAPI,
                        log,
                        listener,
                        context,
                        serializationManager,
                        shouldGzip,
                        shouldLogMessageContent,
                        deadLetterQueues,
                        deadLetterQueueConfigurations);
            }
        }
    }

    void handleDelivery(
            QueueingConsumer queueingConsumer,
            Channel channel,
            BlobStoreAPI blobStoreAPI,
            Logger log,
            ManagedMessageListener listener,
            Context context,
            SerializationManager serializationManager,
            boolean shouldGzip,
            boolean shouldLogMessageContent,
            List<String> deadLetterQueues,
            Map<String, RabbitMQQueueConfiguration> deadLetterQueueConfigurations) {
        try {
            QueueingConsumer.Delivery delivery = null;
            RabbitMQMessage message = null;
            try {
                delivery = queueingConsumer.nextDelivery();
                message = new RabbitMQMessage(serializationManager,
                        delivery,
                        blobStoreAPI,
                        shouldGzip,
                        shouldLogMessageContent);

                // If the message has failed, send it to onErrorMessage
                if (message.isFailed()) {
                    listener.onErrorMessage(message, context);
                    return;
                }

                listener.onMessage(message, context);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            } catch (NullPointerException e) {
                if (delivery == null) {
                    log.warn("Unable to process message. Will wait for redelivery.", e);
                    return;
                }

                log.warn("Unable to process message. Message will be rejected.", e);
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
                // Add error to headers and forward message to onErrorMessage
                message.addErrorMessageHeader(
                        context.getMicroServiceBaseURI(),
                        System.currentTimeMillis(),
                        500,
                        e.getMessage());
                message.setFailed(true);
                listener.onErrorMessage(message, context);
            } catch (Exception e) {

                if (message == null) {
                    log.warn("Unable to process message. Will wait for redelivery.", e);
                    return;
                }

                // Add error to headers
                message.addErrorMessageHeader(
                        context.getMicroServiceBaseURI(),
                        System.currentTimeMillis(),
                        500,
                        e.getMessage());

                // database exceptions and message sender exceptions
                if (delivery == null) {
                    log.warn("Unable to process message. Will wait for redelivery.", e);
                    return;
                }

                List<RabbitMQMessage.XDeath> deaths = message.getXDeaths();

                // Let's immediately re-queue if this message wasn't already redelivered and dead lettered
                if (!delivery.getEnvelope().isRedeliver() && deaths.isEmpty()) {
                    log.warn("Unable to process message. Message will be immediately re-queued.", e);
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                    return;
                }

                // No dead letter queues specified so send a negative acknowledgement and let rabbitmq deal with it
                if (deadLetterQueues.isEmpty()) {
                    log.warn("Unable to process message. Message will be rejected.", e);
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);

                    // Forward message to onErrorMessage
                    message.setFailed(true);
                    listener.onErrorMessage(message, context);
                    return;
                }

                // Message was not yet dead lettered so place on first dlq and acknowledge
                if (deaths.isEmpty()) {
                    RabbitMQQueueConfiguration queue = deadLetterQueueConfigurations.get(deadLetterQueues.get(0));
                    String exchange = queue.getExchangeName();
                    String routingKey = queue.getQueueName();

                    log.warn("Unable to process message. Message will be routed to dead letter queue '" + routingKey +
                            "' bound to exchange '" + exchange + "'.", e);
                    channel.basicPublish(exchange, routingKey, message.getProperties(), delivery.getBody());
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    return;
                }

                // Message was dead lettered at least once so place it on the next dlq and acknowledge
                // x-death is always sorted most recent first
                String lastDeadLetterQueue = deaths.get(0).getQueue();
                Iterator<String> deadLetterQueuesIter = deadLetterQueues.iterator();
                while (deadLetterQueuesIter.hasNext()) {
                    RabbitMQQueueConfiguration conf = deadLetterQueueConfigurations.get(deadLetterQueuesIter.next());
                    if (lastDeadLetterQueue.equals(conf.getQueueName())) {
                        // Great, lets grab the next conf in the list
                        if (deadLetterQueuesIter.hasNext()) {
                            conf = deadLetterQueueConfigurations.get(deadLetterQueuesIter.next());
                            String exchange = conf.getExchangeName();
                            String routingKey = conf.getQueueName();

                            log.warn("Unable to process message. Message will be routed to dead letter queue '" +
                                    routingKey + "' bound to exchange '" + exchange + "'.", e);
                            channel.basicPublish(exchange, routingKey, message.getProperties(), delivery.getBody());
                            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                            return;
                        }
                    }
                }

                // No dead letter queues left so send a negative acknowledgement and let rabbitmq deal with it
                log.warn(
                        "Unable to process message and no retry dead letter queues are left. Message will be rejected.",
                        e);
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);

                // Forward message to onErrorMessage
                message.setFailed(true);
                listener.onErrorMessage(message, context);

                // Lets have the health check manager do it's thing if we completely failed to process this message
                // TODO Decide how we want the health check manager logic to work
                context.getHealthCheckManager().flagAsUnhealthy("Error processing message " + e.getMessage());
            }
        } catch (IOException e) {
            log.error("Error processing RabbitMQ message.", e);
        }
    }
}
