// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.dependency;

import com.emc.microservice.Context;
import com.emc.microservice.messaging.MessageSender;
import com.emc.microservice.resource.AbstractManagedResource;

import javax.ws.rs.client.WebTarget;
import java.util.Objects;

/**
 * Created by liebea on 9/21/2014. Enjoy it
 */
public class ManagedDependencyImpl
        extends AbstractManagedResource<ServiceDependencyDescriptor, ServiceDependencyConfiguration>
        implements ManagedDependency {
    private final MessageSender messageSender;
    private final String dependentServiceURI;
    private final Context context;

    protected ManagedDependencyImpl(
            ServiceDependencyDescriptor descriptor,
            ServiceDependencyConfiguration configuration,
            MessageSender messageSender,
            String dependentServiceURI,
            Context context) {
        super(descriptor, configuration);
        this.messageSender = messageSender;
        this.context = Objects.requireNonNull(context, "Resource provider must be supplied");
        this.dependentServiceURI = Objects.requireNonNull(dependentServiceURI, "Service URI must be supplied");
    }

    @Override
    public MessageSender getMessageSender() {
        return messageSender;
    }

    @Override
    public <T> T getWebAPI(Class<T> resourceWebAPI) {
        return context
                .getServiceDiscoveryManager()
                .discoverServiceConnection(dependentServiceURI)
                .resolve(resourceWebAPI);
    }

    @Override
    public WebTarget getWebTarget() {
        return context.getServiceDiscoveryManager().discoverServiceConnection(dependentServiceURI).getWebTarget();
    }
}
