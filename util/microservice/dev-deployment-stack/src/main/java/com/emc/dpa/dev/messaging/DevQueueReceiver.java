// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.dpa.dev.messaging;

import com.emc.microservice.Context;
import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.microservice.messaging.InputQueueConfiguration;
import com.emc.microservice.messaging.ManagedMessageListener;
import com.emc.microservice.messaging.QueueConfiguration;
import com.emc.microservice.messaging.QueueReceiverImpl;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by liebea on 1/18/15.
 * Drink responsibly
 */
public class DevQueueReceiver extends QueueReceiverImpl<QueueConfiguration> {
    private final Logger logger;
    private final DevMessagingServer server;
    private final ExecutorService executor;
    private final Integer topicId;
    private volatile ConsumerRunnable consumer;

    public DevQueueReceiver(
            InputQueueConfiguration inputQueueConfiguration,
            QueueConfiguration devQueueConfiguration,
            ManagedMessageListener messageListener,
            Context context,
            DevMessagingServer server,
            Integer topicId,
            BlobStoreAPI messagingBlobStore,
            String consumerName) {
        super(
                inputQueueConfiguration,
                devQueueConfiguration,
                Collections.emptyMap(),
                messageListener,
                context,
                consumerName,
                messagingBlobStore);
        this.server = server;
        this.logger = context.createSubLogger(DevQueueReceiver.class);
        //noinspection unchecked
        this.executor = Executors.newSingleThreadExecutor();
        this.topicId = topicId;
    }

    @Override
    public void init() {
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
        try {
            this.executor.shutdown();
        } catch (Exception ignored) {
            // ok... whatever
        }
    }

    private final class ConsumerRunnable implements Runnable {
        private boolean quit = false;

        void quit() {
            quit = true;
        }

        @Override
        public void run() {
            while (!quit) {
                try {
                    DevMessageDescriptor messageDesc =
                            server.receive(getInputQueueConfiguration().getInputQueueURI(), topicId);
                    getMessageListener().onMessage(new DevMessage(
                            messageDesc,
                            getContext().getSerializationManager(),
                            blobStoreAPI,
                            getQueueConfiguration().isGzip()), getContext());
                } catch (InterruptedException e) {
                    logger.info("Queue Interrupted");
                    logger.debug("Interrupt", e);
                } catch (Exception e) {
                    // TODO Decide how we want the health check manager logic to work
                    getContext().getHealthCheckManager().flagAsUnhealthy("Error processing message " + e.getMessage());
                    throw e;
                }
            }

        }
    }

}
