// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import com.emc.microservice.Context;
import com.emc.microservice.dependency.ManagedDependency;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.messaging.MessageListener;
import com.emc.microservice.schedule.ManagedScheduler;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.ocopea.protection.ProtectionWebAPI;
import com.emc.ocopea.site.app.DeployedApplicationEvent;
import com.emc.ocopea.site.app.DeployedApplicationEventConsumer;
import com.emc.ocopea.site.app.DeployedApplicationEventRepository;
import com.emc.ocopea.site.app.DeployedApplicationLoader;
import com.emc.ocopea.site.app.DeployedApplicationPersisterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeployedApplicationMessageBroker implements MessageListener, ServiceLifecycle {
    private static final Logger log = LoggerFactory.getLogger(DeployedApplicationMessageBroker.class);
    private DeployedApplicationEventConsumer consumer;
    private LoggingWebSocketsManager loggingWebSocketsManager;
    private DeployedApplicationEventLogMessageTranslator deployedApplicationEventLogMessageTranslator;

    @Override
    public void init(Context context) {
        final ManagedScheduler scheduler =
                context.getSchedulerManager().getManagedResourceByName("default");
        deployedApplicationEventLogMessageTranslator = new DeployedApplicationEventLogMessageTranslator();
        this.loggingWebSocketsManager = context.getSingletonManager()
                .getManagedResourceByName(LoggingWebSocketsManager.class.getSimpleName()).getInstance();

        DeployedApplicationEventRepository deployedApplicationEventRepository = context.getDynamicJavaServicesManager()
                .getManagedResourceByName(DeployedApplicationEventRepository.class.getSimpleName()).getInstance();

        ManagedDependency protectionPolicyDependency =
                context.getDependencyManager().getManagedResourceByName("protection");
        consumer = new DeployedApplicationEventConsumer(
                new DeployedApplicationLoader(deployedApplicationEventRepository),
                policyType -> protectionPolicyDependency.getWebAPI(ProtectionWebAPI.class),
                context.getWebAPIResolver(),
                scheduler,
                context.getServiceDiscoveryManager(),
                context.getSingletonManager().getManagedResourceByName("site-singleton").getInstance(),
                new DeployedApplicationPersisterService(
                        context.getDestinationManager()
                                .getManagedResourceByName(
                                        "pending-deployed-application-events").getMessageSender()));

    }

    @Override
    public void shutDown() {
    }

    @Override
    public void onMessage(Message message, Context context) {
        final DeployedApplicationEvent deployedApplicationEvent = message.readObject(DeployedApplicationEvent.class);
        log.debug("Received event {}", deployedApplicationEvent);
        consumer.accept(deployedApplicationEvent);
        logMessage(deployedApplicationEvent);

    }

    private void logMessage(DeployedApplicationEvent deployedApplicationEvent) {
        try {
            final SiteLogMessageDTO logMessage =
                    deployedApplicationEventLogMessageTranslator.apply(deployedApplicationEvent);
            if (logMessage != null) {
                loggingWebSocketsManager.publish(deployedApplicationEvent.getAppInstanceId(), logMessage);
            }
        } catch (Exception ex) {
            log.warn("Failed publishing log message for " + deployedApplicationEvent.toString(), ex);
        }
    }

    @Override
    public void onErrorMessage(Message message, Context context) {
    }

}
