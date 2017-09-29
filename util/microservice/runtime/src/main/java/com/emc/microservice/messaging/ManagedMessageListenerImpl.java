// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id$
 * 
 * This computer code is copyright 2014 - 2015 EMC Corporation
 * All rights reserved
 */
package com.emc.microservice.messaging;

import com.emc.microservice.Context;
import com.emc.microservice.ContextImpl;
import com.emc.microservice.ContextThreadLocal;
import com.emc.microservice.metrics.CounterMetric;
import com.emc.microservice.metrics.MapMetricState;
import com.emc.microservice.metrics.MetricsRegistry;
import com.emc.microservice.metrics.StopWatch;
import com.emc.microservice.metrics.TimerMetric;
import org.slf4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.emc.microservice.messaging.MessagingConstants.ERROR_HEADER;

/**
 * Created with love by liebea on 5/28/2014.
 * Moved from ManagedMessageListener class in MS API repo by nivenb
 */
public class ManagedMessageListenerImpl implements ManagedMessageListener {
    private final MessageListener messageListener;
    private final TimerMetric messageProcessingTimer;
    private final CounterMetric activeJobs;
    private final CounterMetric errorCounter;
    private final QueueExecutionState queueExecutionState;
    private final String[] messageHeadersForLogging;
    private final int threadOrdinal;
    private final Logger logger;

    private static final String METRIC_NAME_MESSAGE_PROCESSING_TIMER = "messageProcessingTimer";

    private static final String METRIC_NAME_ACTIVE_JOBS = "activeJobs";

    private static final String METRIC_NAME_ERROR_COUNTER = "errorCounter";

    private static final String METRIC_TAG_QUEUE_NAME = "queueName";

    public ManagedMessageListenerImpl(
            Context context,
            MessageListener messageListener,
            ManagedInputQueue managedQueue,
            int threadOrdinal) {
        //todo: context should be a thread local thingie not stored as members ..
        this.messageListener = messageListener;
        final MetricsRegistry metricsRegistry = context.getMetricsRegistry();
        Map<String, String> metricTags = new HashMap<>();
        metricTags.put(METRIC_TAG_QUEUE_NAME, managedQueue.getQueueName());
        this.messageProcessingTimer = metricsRegistry.getTimerMetric(
                METRIC_NAME_MESSAGE_PROCESSING_TIMER,
                metricTags,
                messageListener.getClass());
        this.activeJobs = metricsRegistry.getCounter(METRIC_NAME_ACTIVE_JOBS, metricTags, messageListener.getClass());
        this.errorCounter =
                metricsRegistry.getCounter(METRIC_NAME_ERROR_COUNTER, metricTags, messageListener.getClass());
        this.threadOrdinal = threadOrdinal;
        this.queueExecutionState = new QueueExecutionState(new Date());
        String[] tmpMessageHeadersForLogging = managedQueue.getDescriptor().getMessageHeadersForLogging();
        if (tmpMessageHeadersForLogging != null && tmpMessageHeadersForLogging.length == 0) {
            tmpMessageHeadersForLogging = null;
        }
        this.messageHeadersForLogging = tmpMessageHeadersForLogging;
        logger = context.createSubLogger(ManagedMessageListenerImpl.class);
    }

    @Override
    public void onMessage(Message message, Context context) {
        handleMessage(message, context, false);
    }

    @Override
    public void onErrorMessage(Message message, Context context) {
        logger.error("Error message received {}", message.getMessageHeader(ERROR_HEADER));

        handleMessage(message, context, true);
    }

    private void handleMessage(Message message, Context context, boolean isError) {
        // Setting contextThreadLocal
        ContextThreadLocal.setContext(context);

        // Getting thread for renaming it
        Thread currentThread = Thread.currentThread();
        String origThreadName = currentThread.getName();
        MessageLoggingContext messageLoggingContext = buildMessageLoggingContext(message);

        Map<String, String> messageHeaderValuesForLogging = messageLoggingContext.getMessageHeaderValuesForLogging();
        if (logger.isDebugEnabled()) {
            logger.debug("currentThread {}", currentThread);
            logger.debug("origThreadName {}", origThreadName);
            messageHeaderValuesForLogging.values().forEach(logger::debug);
        }

        // Count number of active messages being processed and time taken to process
        try (CounterMetric autoCounter = activeJobs.inc();
             StopWatch stopWatch = messageProcessingTimer.getStopWatch(
                     new MapMetricState(messageHeaderValuesForLogging))) {

            // Set Execution State
            queueExecutionState.setExecutionState(messageHeaderValuesForLogging);

            // Setting a new human readable thread name
            currentThread.setName(messageLoggingContext.getThreadName());

            if (logger.isDebugEnabled()) {
                logger.debug("Executing message listener");
            }

            if (isError) {
                handleErrorMessage(message, context);
            } else {
                // Call the underlying message listener
                messageListener.onMessage(message, context);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Done executing message listener");
            }
        } catch (Exception ex) {
            // since we are rethrowing the exception please don't
            // log a stack trace here.
            logger.warn("Error while processing message. {}", ex.getMessage());
            errorCounter.inc();
            throw ex;
        } finally {
            try {
                // Clear execution state
                queueExecutionState.clearExecutionState();
            } finally {
                // Setting original thread name back to what it used to be
                currentThread.setName(origThreadName);
            }

        }
    }

    private <T extends Object> void handleErrorMessage(Message message, Context context) {
        final String errorMessage = message.getMessageHeaders().get(ERROR_HEADER);
        final String serviceURI = getLastRoutingServiceURI(message);

        logger.debug("Handling error message initiatorService={}, errorMessage={}", serviceURI, errorMessage);

        if (serviceURI == null || serviceURI.equals(context.getMicroServiceBaseURI())) {
            // Call the underlying message listener
            messageListener.onErrorMessage(message, context);
            return;
        }

        // Forward error message to initiator microservice
        logger.debug("Forwarding error message to {}: error={}", serviceURI, errorMessage);
        MessageSender messageSender =
                context.getDependencyManager().getManagedResourceByName(serviceURI).getMessageSender();
        messageSender.sendMessage(
                (Class<T>) message.getUnderlyingMessageObject().getClass(),
                (T) message.getUnderlyingMessageObject(),
                message.getMessageHeaders(),
                message.getMessageContext());
    }

    private String getLastRoutingServiceURI(Message message) {
        // The routing plan is a comma separated list of microservices where this messages will be routed to
        final String routingPlan = message.getContextValue(ContextImpl.MS_API_ROUTING_PLAN_HEADER);

        if (routingPlan == null || routingPlan.isEmpty()) {
            return null;
        }

        String[] serviceURIs = routingPlan.split(",");

        return serviceURIs[serviceURIs.length - 1];
    }

    public QueueExecutionState getQueueExecutionState() {
        return queueExecutionState;
    }

    private MessageLoggingContext buildMessageLoggingContext(Message message) {
        StringBuilder newThreadName = new StringBuilder();
        newThreadName.append(messageListener.getClass().getSimpleName());
        newThreadName.append(" #");
        newThreadName.append(threadOrdinal);

        Map<String, String> messageHeaderValuesForLogging;

        if (this.messageHeadersForLogging != null) {
            messageHeaderValuesForLogging = new HashMap<>(this.messageHeadersForLogging.length);
            for (String currHeaderForLogging : this.messageHeadersForLogging) {
                String valueForLogging = message.getMessageHeader(currHeaderForLogging);
                if (valueForLogging != null) {
                    newThreadName.append(':');
                    newThreadName.append(valueForLogging);
                }
                messageHeaderValuesForLogging.put(currHeaderForLogging, valueForLogging);
            }
        } else {
            messageHeaderValuesForLogging = message.getMessageHeaders();
        }

        return new MessageLoggingContext(newThreadName.toString(), messageHeaderValuesForLogging);
    }

    private class MessageLoggingContext {
        String threadName;
        Map<String, String> messageHeaderValuesForLogging;

        public MessageLoggingContext(String threadName, Map<String, String> messageHeaderValuesForLogging) {
            this.threadName = threadName;
            this.messageHeaderValuesForLogging = messageHeaderValuesForLogging;
        }

        public String getThreadName() {
            return threadName;
        }

        public Map<String, String> getMessageHeaderValuesForLogging() {
            return messageHeaderValuesForLogging;
        }
    }
}
