// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import com.emc.microservice.Context;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.messaging.MessageListener;
import com.emc.microservice.messaging.MessageSender;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.ocopea.util.MapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Created by liebea on 4/30/17.
 * Drink responsibly
 */
public class PendingAppCopyEventsProcessor implements MessageListener, ServiceLifecycle {
    private static Logger log = LoggerFactory.getLogger(PendingAppCopyEventsProcessor.class);
    private ApplicationCopyLoader applicationCopyLoader;
    private ApplicationCopyEventRepository applicationCopyEventRepository;
    private MessageSender pendingEventsMessageSender;

    @Override
    public void init(Context context) {
        applicationCopyEventRepository =
                context.getDynamicJavaServicesManager().getManagedResourceByName(
                        ApplicationCopyEventRepository.class.getSimpleName()).getInstance();

        this.applicationCopyLoader = new ApplicationCopyLoader(applicationCopyEventRepository);
        this.pendingEventsMessageSender = context.getDestinationManager().getManagedResourceByName(
                "pending-application-copy-events").getMessageSender();
    }

    @Override
    public void shutDown() {
    }

    @Override
    public void onMessage(Message message, Context context) {
        // Parsing the event
        final ApplicationCopyEvent appCopyEvent = message.readObject(ApplicationCopyEvent.class);

        // Loading the copy
        ApplicationCopy applicationCopy =
                applicationCopyLoader.load(appCopyEvent.getAppCopyId());

        if (applicationCopy == null) {
            log.warn(
                    "Received and event to process for a copy that does not exist with id {}, and app instanace " +
                            "{} event {}",
                    appCopyEvent.getAppCopyId(),
                    appCopyEvent.getAppInstanceId(),
                    appCopyEvent);
        } else {

            final Collection<ApplicationCopyEvent> downStreamEvents;

            // The created event is special in that it is always being persisted externally so no need to persist again
            if (!(appCopyEvent instanceof ApplicationCopyScheduledEvent)) {
                downStreamEvents = applicationCopy.rollEvent(appCopyEvent);
                applicationCopyEventRepository.store(appCopyEvent);
            } else {
                downStreamEvents = new ApplicationCopy().rollEvent(appCopyEvent);
            }

            // Sending all down-stream events to pending queue
            downStreamEvents.forEach(event ->
                    pendingEventsMessageSender.sendMessage(
                            ApplicationCopyEvent.class,
                            event,
                            MapBuilder.<String, String>newHashMap()
                                    .with("appInstanceId", event.getAppInstanceId().toString())
                                    .with("appCopyId", event.getAppCopyId().toString())
                                    .with("eventType", event.getClass().getSimpleName())
                                    .build()));
        }
    }

    @Override
    public void onErrorMessage(Message message, Context context) {
    }

}
