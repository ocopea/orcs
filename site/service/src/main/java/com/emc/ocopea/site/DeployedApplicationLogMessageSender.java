// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * This class is responsible for publishing custom messages to the site webSocket.
 * It ensures the caller will not block and add it to the task queue if there is enough space
 */
class DeployedApplicationLogMessageSender {
    private static final Logger log = LoggerFactory.getLogger(DeployedApplicationLogMessageSender.class);
    private static final int MESSAGE_BUFFER_LIMIT = 500;

    private final UUID appInstanceId;
    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final ArrayBlockingQueue<SiteLogMessageDTO> pendingMessages =
            new ArrayBlockingQueue<>(MESSAGE_BUFFER_LIMIT);

    DeployedApplicationLogMessageSender(UUID appInstanceId, Consumer<SiteLogMessageDTO> consumer) {
        this.appInstanceId = appInstanceId;
        new Thread(() -> {
            while (!stop.get()) {
                try {
                    final SiteLogMessageDTO nextMessage = pendingMessages.poll(10, TimeUnit.SECONDS);
                    if (nextMessage != null) {
                        try {
                            consumer.accept(nextMessage);
                        } catch (Exception ex) {
                            log.warn("Failed publishing log message for appInstanceId " + appInstanceId + "; " +
                                    "message\n " + nextMessage.toString(), ex);
                        }
                    }
                } catch (InterruptedException e) {
                    // Whatever
                    log.trace("interrupting consumer", e);
                }
            }
        }).start();
    }

    void sendMessage(SiteLogMessageDTO message) {
        if (!pendingMessages.offer(message)) {
            log.warn("failed to send message=" + message + " because appInstanceId=" +
                    appInstanceId + " log message queue is full");
        }
    }

    void stop() {
        this.stop.set(true);
    }
}
