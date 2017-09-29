// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import com.emc.microservice.Context;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.messaging.MessageListener;
import com.emc.microservice.singleton.ServiceLifecycle;

/**
 * Created by liebea on 10/26/16.
 * Drink responsibly
 */
public class ApplicationCopyEventsMessageBroker implements MessageListener, ServiceLifecycle {

    private DataServiceCopyCreator dataServiceCopyCreator;
    private AppServiceCopyCreator appServiceCopyCreator;

    @Override
    public void onMessage(Message message, Context context) {
        final ApplicationCopyEvent applicationCopyEvent = message.readObject(ApplicationCopyEvent.class);
        dataServiceCopyCreator.processEvent(applicationCopyEvent);
        appServiceCopyCreator.processEvent(applicationCopyEvent);
    }

    @Override
    public void onErrorMessage(Message message, Context context) {
    }

    @Override
    public void init(Context context) {
        final ApplicationCopyLoader applicationCopyLoader =
                new ApplicationCopyLoader(
                        context
                                .getDynamicJavaServicesManager()
                                .getManagedResourceByName(ApplicationCopyEventRepository.class.getSimpleName())
                                .getInstance());
        final AppCopyPersisterService appCopyPersisterService =
                new AppCopyPersisterService(context.getDestinationManager()
                        .getManagedResourceByName("pending-application-copy-events").getMessageSender());
        dataServiceCopyCreator =
                new DataServiceCopyCreator(
                        context.getWebAPIResolver(),
                        applicationCopyLoader,
                        appCopyPersisterService,
                        new CopyRepositoryNegotiatorImpl(
                                context.getServiceDiscoveryManager(),
                                context.getWebAPIResolver()));
        appServiceCopyCreator = new AppServiceCopyCreator(appCopyPersisterService, applicationCopyLoader);

    }

    @Override
    public void shutDown() {
    }
}
