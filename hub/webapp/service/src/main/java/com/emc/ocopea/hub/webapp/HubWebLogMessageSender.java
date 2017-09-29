// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.webapp;

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
class HubWebLogMessageSender {
    private static final Logger log = LoggerFactory.getLogger(HubWebLogMessageSender.class);
    private static final int MESSAGE_BUFFER_LIMIT = 500;

    private final UUID appInstanceId;
    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final ArrayBlockingQueue<UILogMessage> pendingMessages =
            new ArrayBlockingQueue<>(MESSAGE_BUFFER_LIMIT);

    HubWebLogMessageSender(UUID appInstanceId, Consumer<UILogMessage> consumer) {
        this.appInstanceId = appInstanceId;
        new Thread(() -> {
            while (!stop.get()) {
                try {
                    final UILogMessage nextMessage = pendingMessages.poll(10, TimeUnit.SECONDS);
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

    void sendMessage(UILogMessage message) {
        if (!pendingMessages.offer(message)) {
            log.warn("failed to send message=" + message + " because appInstanceId=" +
                    appInstanceId + " log message queue is full");
        }
    }

    void stop() {
        this.stop.set(true);
    }
}
