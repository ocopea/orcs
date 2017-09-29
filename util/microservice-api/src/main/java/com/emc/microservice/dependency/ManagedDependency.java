// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.dependency;

import com.emc.microservice.discovery.WebAPIConnection;
import com.emc.microservice.messaging.MessageSender;
import com.emc.microservice.resource.ManagedResource;

/**
 * Created by liebea on 9/21/2014. Enjoy it
 */
public interface ManagedDependency extends
        ManagedResource<ServiceDependencyDescriptor, ServiceDependencyConfiguration>, WebAPIConnection {

    MessageSender getMessageSender();

    <T> T getWebAPI(Class<T> resourceWebAPI);

    @Override
    default <T> T resolve(Class<T> resourceClass) {
        return getWebAPI(resourceClass);
    }
}
