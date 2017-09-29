// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import com.emc.microservice.Context;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.messaging.MessageListener;
import com.emc.microservice.messaging.MessageSender;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.ocopea.site.app.DeployedApplication;
import com.emc.ocopea.site.app.DeployedApplicationCreatedEvent;
import com.emc.ocopea.site.app.DeployedApplicationEvent;
import com.emc.ocopea.site.app.DeployedApplicationEventRepository;
import com.emc.ocopea.site.app.DeployedApplicationLoader;
import com.emc.ocopea.util.MapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Created by liebea on 4/25/17.
 * Drink responsibly
 */
public class PendingDeployedApplicationEventsProcessor implements MessageListener, ServiceLifecycle {
    private static Logger log = LoggerFactory.getLogger(PendingDeployedApplicationEventsProcessor.class);
    private DeployedApplicationLoader deployedApplicationLoader;
    private DeployedApplicationEventRepository deployedApplicationEventRepository;
    private MessageSender pendingEventsMessageSender;

    @Override
    public void init(Context context) {
        deployedApplicationEventRepository =
                context.getDynamicJavaServicesManager().getManagedResourceByName(
                        DeployedApplicationEventRepository.class.getSimpleName()).getInstance();

        this.deployedApplicationLoader = new DeployedApplicationLoader(deployedApplicationEventRepository);
        this.pendingEventsMessageSender = context.getDestinationManager().getManagedResourceByName(
                "pending-deployed-application-events").getMessageSender();
    }

    @Override
    public void shutDown() {
    }

    @Override
    public void onMessage(Message message, Context context) {
        // Parsing the event
        final DeployedApplicationEvent deployedApplicationEvent = message.readObject(DeployedApplicationEvent.class);

        // Loading the app
        DeployedApplication deployedApplication =
                deployedApplicationLoader.load(deployedApplicationEvent.getAppInstanceId());

        if (deployedApplication == null) {
            log.warn(
                    "Received and event to process for an application that does not exist with id {}, event {}",
                    deployedApplicationEvent.getAppInstanceId(),
                    deployedApplicationEvent);
        } else {

            final Collection<DeployedApplicationEvent> downStreamEvents;

            // The created event is special in that it is always being persisted externally so no need to persist again
            if (!(deployedApplicationEvent instanceof DeployedApplicationCreatedEvent)) {
                downStreamEvents = deployedApplication.rollEvent(deployedApplicationEvent);
                deployedApplicationEventRepository.store(deployedApplicationEvent);
            } else {
                downStreamEvents = new DeployedApplication().rollEvent(deployedApplicationEvent);
            }

            // Sending all down-stream events to pending queue
            downStreamEvents.forEach(event -> pendingEventsMessageSender.sendMessage(
                    DeployedApplicationEvent.class,
                    event,
                    MapBuilder.<String, String>newHashMap()
                            .with("appInstanceId", deployedApplication.getId().toString())
                            .with("appName", deployedApplication.getName())
                            .with("appTemplate", deployedApplication.getAppTemplateName())
                            .with("eventType", event.getClass().getSimpleName())
                            .build()));
        }

    }

    @Override
    public void onErrorMessage(Message message, Context context) {
    }
}
