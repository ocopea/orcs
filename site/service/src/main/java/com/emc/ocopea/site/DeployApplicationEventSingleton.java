// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import com.emc.microservice.Context;
import com.emc.microservice.messaging.MessageSender;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.ocopea.site.app.DeployedApplicationEvent;
import com.emc.ocopea.site.app.DeployedApplicationEventRepository;
import com.emc.ocopea.util.MapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Created by liebea on 11/3/16.
 * This class is responsible for tapping into events from the DeployedApplicationEvent and putting those as
 * items in the services work queue for processing those messages. no logic here this is just connecting
 * the pipes the logic needs using a queue
 */
public class DeployApplicationEventSingleton implements ServiceLifecycle, Consumer<DeployedApplicationEvent> {
    private static final Logger log = LoggerFactory.getLogger(DeployApplicationEventSingleton.class);
    private MessageSender deployedApplicationWorkQueueMessageSender;

    @Override
    public void init(Context context) {

        // Getting the work queue message sender
        deployedApplicationWorkQueueMessageSender = context.getDestinationManager()
                .getManagedResourceByName("deployed-application-events").getMessageSender();

        // Registering for events from the deployedApplicationEventRepo
        DeployedApplicationEventRepository deployedApplicationEventRepository = context.getDynamicJavaServicesManager()
                .getManagedResourceByName(DeployedApplicationEventRepository.class.getSimpleName()).getInstance();
        deployedApplicationEventRepository.subscribe(this);
    }

    @Override
    public void shutDown() {
    }

    @Override
    public void accept(DeployedApplicationEvent event) {
        log.debug("sending event {}", event);
        deployedApplicationWorkQueueMessageSender.sendMessage(
                DeployedApplicationEvent.class,
                event,
                MapBuilder.<String, String>newHashMap()
                        .with("appInstanceId", event.getAppInstanceId().toString())
                        .with("eventType", event.getClass().getSimpleName())
                        .build());
    }

}
