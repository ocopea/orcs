// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * This class represents a queue in the system and responsible for loading messages from the persister and calling
 * consumers for a single queue. the queue holds a single thread in order to
 * run the message loop and delegates consumers to a thread pool provided externally.
 * The queue supports in-memory message buffering
 */
class Queue {
    private static final Logger log = LoggerFactory.getLogger(Queue.class);
    private final String name;
    private AtomicBoolean continueInMessagesLoop = new AtomicBoolean(false);
    private BlockingQueue<QueueConsumer> consumers = new LinkedBlockingDeque<>();
    private final Object inMemoryBufferSyncLock = new Object();
    private QueueState state = QueueState.paused;
    private QueueState desiredState = QueueState.paused;

    private enum QueueState {
        started,
        paused,
    }

    /**
     * This flag is turned on when the in-memory buffer is full. messages could not be handled in memory and
     * persisted to the persister. when the flag is on, messages will be processed until the buffer is cleared before
     * loading another batch from the persister, we default to true in case fast senders send messages before message
     * loop has started
     */
    private AtomicBoolean skipBufferForNewMessages = new AtomicBoolean(true);

    /**
     * Thread pool for the actual consumer jobs
     */
    private ExecutorService jobsExecutor;

    /**
     * Stores the messages in memory, used to pre-load from db, and if available pass message directly from sender to
     * the queue while persisting only for the sake of failure
     */
    private BlockingQueue<Message> messagesBuffer;

    /**
     * This set holds the messages currently being processed. it is used to avoid fetching in-progress messages from
     * the data-store while loading batches of jobs from the persister
     */
    private Set<String> messagesInProcess = new ConcurrentSkipListSet<>();

    /**
     * Message persister is used to store messages in case of a failure, restart
     */
    private final MessagePersister messagePersister;

    /**
     * When a queue consumer is requesting to remove itself it first enters this list until messages has been
     * processed and then the message loop cleans it up when available
     */
    private final Set<String> consumersToRemove = new ConcurrentSkipListSet<>();

    /**
     * In memory buffer size (amount of messages) of the queue provided by the user
     */
    private final int memoryBufferSize;

    /**
     * In case of failure on the consumer side, the shpanMessaging system will retry, this is the time in seconds to
     * sleep between retries
     */
    private final int secondsToSleepBetweenMessageRetries;

    /**
     * Number of consumer failures to allow and retry before ditching the message
     */
    private final int failedConsumerRetries;

    Queue(
            String name,
            ExecutorService jobsExecutor,
            MessagePersister messagePersister,
            int memoryBufferSize,
            int secondsToSleepBetweenMessageRetries,
            int failedConsumerRetries) {

        this.name = name;
        this.jobsExecutor = jobsExecutor;
        this.messagePersister = messagePersister;
        this.memoryBufferSize = Math.max(2, memoryBufferSize);
        this.secondsToSleepBetweenMessageRetries = secondsToSleepBetweenMessageRetries;
        this.failedConsumerRetries = failedConsumerRetries;
        this.messagesBuffer = new ArrayBlockingQueue<>(this.memoryBufferSize, true);
    }

    /**
     * Adding a consumer to the queue. for each consumer added the concurrency of the messages produced by the queue
     * increases
     *
     * @param name unique name of the consumer across the queue
     * @param consumer consumer function for consuming messages from the queue
     */
    void addConsumer(String name, Consumer<Message> consumer) {
        consumers.add(new QueueConsumer(name, consumer));
    }

    /**
     * Remove a consumer. consumers that are currently processing a message will be removed once done processing.
     *
     * @param name consumer unique name as provided while adding the message
     */
    void removeConsumer(String name) {
        consumersToRemove.add(name);
    }

    String getName() {
        return name;
    }

    /**
     * Start producing messages to consumers. start should be called once after the queue has been created and also
     * when resuming the queue after paused
     */
    synchronized void start() {
        this.desiredState = QueueState.started;
        evaluateState();
    }

    private synchronized void doStart() {
        continueInMessagesLoop.set(true);
        log.info("Queue " + this.name + " has been started");
        setState(QueueState.started);
        if (this.messagesBuffer != null) {
            try {
                this.messagesBuffer.clear();
            } catch (Exception ex) {
                // Whatever
            }
        }
        this.messagesBuffer = new ArrayBlockingQueue<>(this.memoryBufferSize, true);
        // Always loading from store when starting
        loadMessagesFromDB();

        new Thread(this::messageLoop, name + " message broker").start();
    }

    private void loadMessagesFromDB() {
        synchronized (inMemoryBufferSyncLock) {
            this.messagesBuffer.addAll(
                    messagePersister.loadNextMessages(
                            this.name,
                            memoryBufferSize,
                            messagesInProcess));

            if (this.messagesBuffer.size() == memoryBufferSize) {
                skipBufferForNewMessages.set(true);
            } else {
                skipBufferForNewMessages.set(false);
            }
        }
    }

    /**
     * Pause a queue, the queue will not produce messages however will allow sending messages
     */
    synchronized void pause() {
        this.desiredState = QueueState.paused;
        evaluateState();
    }

    private void messageLoop() {
        // Keep receiving messages as long as we are started
        while (continueInMessagesLoop.get()) {
            QueueConsumer nextConsumer = null;
            try {

                // In case there are no consumers wait for a minute and then check again if paused
                nextConsumer = consumers.poll(60, TimeUnit.SECONDS);
                if (nextConsumer == null) {
                    continue;
                }

                // Handling consumer removal
                if (consumersToRemove.remove(nextConsumer.name)) {
                    continue;
                }

                log.trace("{}: buffer size {}", this.name, messagesBuffer.size());
                // In case there are no messages to consume we check weather we need to reload from db
                if (skipBufferForNewMessages.get() && messagesBuffer.size() == 0) {
                    loadMessagesFromDB();
                }

                // getting the next message from the queue
                Message nextMessage = messagesBuffer.poll(1, TimeUnit.SECONDS);

                // In case there are no messages to consume we start another iteration of the loop
                if (nextMessage == null) {
                    // Putting consumer back in it's pool before the next round of the loop
                    consumers.add(nextConsumer);
                    continue;
                }

                // We check again for the started flag since we could not be up to date in case we waited on the
                // messagesBuffer while polling and it was empty
                if (!continueInMessagesLoop.get()) {
                    consumers.add(nextConsumer);
                } else {

                    // Marking message as "in-progress" so it won't be loaded from the database more than once
                    messagesInProcess.add(nextMessage.getId());

                    // Submitting the job on the jobs thread pool
                    // Creating final local variable to avoid java limitations :)
                    final QueueConsumer finalNextConsumer = nextConsumer;
                    jobsExecutor.submit(() -> {
                        boolean consumedSuccessfully = false;
                        try {
                            finalNextConsumer.consumer.accept(nextMessage);
                            consumedSuccessfully = true;
                        } catch (Exception ex) {

                            // We retry
                            consumedSuccessfully = retryConsumer(
                                    nextMessage,
                                    finalNextConsumer,
                                    ex,
                                    failedConsumerRetries);
                        } finally {
                            // Putting consumer back in it's pool
                            consumers.add(finalNextConsumer);

                            // In case we are out of the message loop we don't delete the messages - it means there
                            // has been a restart, so we'll retry
                            if (continueInMessagesLoop.get() || consumedSuccessfully) {
                                messagePersister.deleteMessage(this.name, nextMessage.getId());
                            }
                            messagesInProcess.remove(nextMessage.getId());
                        }
                    });
                }
            } catch (Exception e) {
                log.warn("Exception in message loop for queue " + this.name, e);
                if (nextConsumer != null) {
                    consumers.add(nextConsumer);
                }
            }
        }

        // Allowing in-progress messages to drain. since we didn't implement timeout at this stage, this can
        // theoretically take forever
        waitForInProgressMessagesToFinish();

        log.info("Queue " + this.name + " has been paused");
        setState(QueueState.paused);
        evaluateState();

    }

    private void waitForInProgressMessagesToFinish() {
        final long timeStartedWaiting = System.currentTimeMillis();
        long timeSinceLoggedDrainingMessages = System.currentTimeMillis();
        if (!messagesInProcess.isEmpty()) {
            log.info(
                    "Queue {} pausing allowing {} in progress messages to be processed",
                    this.name,
                    messagesInProcess.size());

            while (!messagesInProcess.isEmpty()) {
                sleepNoEx(500L);
                // Adding a log message every minute...
                final long now = System.currentTimeMillis();
                if ((now - timeSinceLoggedDrainingMessages) / 1000 > 60) {
                    timeSinceLoggedDrainingMessages = now;
                    int minutesWaiting = Math.min(1, (int) ((now - timeStartedWaiting) / 1000) / 60);
                    log.info(
                            "Queue {} is still pausing after {} minutes allowing {} " +
                                    "in progress messages to be processed",
                            this.name,
                            minutesWaiting,
                            messagesInProcess.size());
                }
            }
        }
    }

    private void sleepNoEx(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void setState(QueueState state) {
        this.state = state;
    }

    private synchronized void evaluateState() {
        if (desiredState != state) {
            if (desiredState == QueueState.started) {
                doStart();
            } else {
                doPause();
            }
        }
    }

    private synchronized void doPause() {
        this.continueInMessagesLoop.set(false);
        skipBufferForNewMessages.set(true);
    }

    private boolean retryConsumer(
            Message nextMessage,
            QueueConsumer finalNextConsumer,
            Exception ex,
            int attemptsLeft) {

        if (attemptsLeft == 0) {
            // ok, gone now..
            //todo: allow configuring DLQ?
            log.error("bye bye message with id " + nextMessage.getId() + " on queue " + this.name, ex);
            return false;
        }

        try {
            // ShpanMessaging retry strategy is simplistic. if the consumer messes things we'll
            // sleep for a few seconds and try again for small amount of attempts.
            // if the consumer tricks us twice we throw away the message to the dark corners of the world
            log.warn("Consumer " + finalNextConsumer.name + " failed to process it's message, " +
                    "we'll try again once after " + secondsToSleepBetweenMessageRetries +
                    " seconds and lets see how it goes", ex);
            Thread.sleep(Math.max(100, secondsToSleepBetweenMessageRetries * 1000L));
            if (!continueInMessagesLoop.get()) {
                return false;
            } else {
                finalNextConsumer.consumer.accept(nextMessage);
                return true;
            }
        } catch (Exception exx) {

            // Another round
            return retryConsumer(nextMessage, finalNextConsumer, exx, attemptsLeft - 1);
        }
    }

    /**
     * Send a message to consumers of the queue
     *
     * @param messageBody input stream representing the message body
     * @param headers headers for the message
     */
    void sendMessage(InputStream messageBody, Map<String, String> headers) {
        Objects.requireNonNull(messageBody, "messageBody must not be null");
        Objects.requireNonNull(headers, "headers must not be null");

        // Persisting message in the persister
        final Message message = persistMessage(messageBody, headers);

        // If buffer allows trying to push the message in-memory directly to the in-memory buffer.
        consumeSentMessageIntoBuffer(message);
    }

    /**
     * Send a message to consumers of the queue
     *
     * @param messageBody input stream representing the message body
     * @param headers headers for the message
     */
    void sendMessage(Consumer<OutputStream> writer, Map<String, String> headers) {
        Objects.requireNonNull(writer, "writer must not be null");
        Objects.requireNonNull(headers, "headers must not be null");

        // Persisting message in the persister
        final Message message = persistMessage(writer, headers);

        // If buffer allows trying to push the message in-memory directly to the in-memory buffer.
        consumeSentMessageIntoBuffer(message);
    }

    private void consumeSentMessageIntoBuffer(Message message) {
        try {
            synchronized (inMemoryBufferSyncLock) {

                // Once the queue has reached full capacity we allow it to clean before reloading from the database
                if (!skipBufferForNewMessages.get()) {

                    // First we try to push the message in-memory
                    if (!messagesBuffer.offer(message)) {

                        // In case the in-memory part of the queue is full we flag it to stop filling the in-memory
                        // part until empty and then reload from db
                        skipBufferForNewMessages.set(true);
                    }
                }
            }
        } catch (Exception ex) {
            // The block above is not supposed to fail, however, in case something unexpected and undocumented happens
            // here we should flag the queue that it's buffer is not in-sync with the persister
            // We should not however rethrow the exception to the user since the message has already been persisted
            // and will be sent to consumers when the queue will re-sync with persister
            skipBufferForNewMessages.set(true);

            // We are however logging this   since we want to know..
            log.warn("Unexpected error occurred internally in the ShpanMessaging library, messages are being " +
                    "delivered correctly however in-memory buffers may be inefficient. if this bothers you - please " +
                    "go to the shpan-store closed to your home location for personal treat", ex);
        }
    }

    private Message persistMessage(InputStream messageBody, Map<String, String> headers) {
        try {
            return messagePersister.persistMessage(this.name, messageBody, headers);
        } catch (Exception pex) {
            throw new PersistentMessagingException(
                    "Error occurred while trying to persist message on queue " + name + ": " + pex.getMessage(),
                    pex);
        }
    }

    private Message persistMessage(Consumer<OutputStream> writer, Map<String, String> headers) {
        try {
            return messagePersister.persistMessage(this.name, writer, headers);
        } catch (Exception pex) {
            throw new PersistentMessagingException(
                    "Error occurred while trying to persist message on queue " + name + ": " + pex.getMessage(),
                    pex);
        }
    }

    private class QueueConsumer {
        private final String name;
        private final Consumer<Message> consumer;

        private QueueConsumer(String name, Consumer<Message> consumer) {
            this.name = name;
            this.consumer = consumer;
        }
    }
}
