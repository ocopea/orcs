// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.dependency;

import com.emc.microservice.MicroserviceIdentifier;
import com.emc.microservice.messaging.DestinationConfiguration;
import com.emc.microservice.resource.ResourceDescriptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liebea on 9/21/2014. Enjoy it
 */
public abstract class ServiceDependencyDescriptor implements ResourceDescriptor {
    private final ServiceDependencyType serviceDependencyType;
    private final MicroserviceIdentifier dependentServiceIdentifier;
    private final boolean verifyDependencyAsHealthCheck;
    private final Class format;
    private final Class returnValueFormat;
    private final Map<String, DestinationConfiguration> customDestinationConfiguration = new HashMap<>();

    /***
     * Create a service dependency descriptor
     * @param serviceDependencyType         type of the dependency in service
     * @param dependentServiceShortName     name of dependent service uri use when looking up service in registry
     * @param verifyDependencyAsHealthCheck whether to pause current service if the dependent service is down or paused.
     *                                      in most scenarios we want to pause our current service if dependent service
     *                                      is down however there might be cases where dependency is used only to
     *                                      process small
     * @param format                        message format sent to service, will use default serializer
     *                                      unless overridden
     * @param returnValueFormat             optional - message format returned by service,
     *                                      if service does not return value, provide null
     */
    protected ServiceDependencyDescriptor(ServiceDependencyType serviceDependencyType,
                                          String dependentServiceShortName,
                                          boolean verifyDependencyAsHealthCheck,
                                          Class format,
                                          Class returnValueFormat) {
        this.serviceDependencyType = serviceDependencyType;
        this.dependentServiceIdentifier = new MicroserviceIdentifier(dependentServiceShortName);
        this.verifyDependencyAsHealthCheck = verifyDependencyAsHealthCheck;
        this.format = format;
        this.returnValueFormat = returnValueFormat;
    }

    public MicroserviceIdentifier getDependentServiceIdentifier() {
        return dependentServiceIdentifier;
    }

    public ServiceDependencyType getServiceDependencyType() {
        return serviceDependencyType;
    }

    public boolean isVerifyDependencyAsHealthCheck() {
        return verifyDependencyAsHealthCheck;
    }

    public Class getFormat() {
        return format;
    }

    public Class getReturnValueFormat() {
        return returnValueFormat;
    }

    @Override
    public String getName() {
        return dependentServiceIdentifier.getShortName();
    }

    /**
     * For multi-route messages adding a hop in the chain
     *
     * @param additionalServiceShortName additional service shortName
     * @return ServiceDependencyDescriptor (itself)
     */
    public abstract ServiceDependencyDescriptor appendCustomRouting(String additionalServiceShortName);

    /***
     * set custom destination configuration
     */
    public ServiceDependencyDescriptor setCustomDestinationConfiguration(String serviceShortName,
                                                                         String blobStoreNameSpace,
                                                                         String blobstoreKeyHeaderName,
                                                                         boolean logInDebug) {
        this.customDestinationConfiguration.put(
                serviceShortName,
                new DestinationConfiguration(
                        null,
                        blobStoreNameSpace,
                        blobstoreKeyHeaderName,
                        logInDebug));
        return this;

    }

    public Map<String, DestinationConfiguration> getCustomDestinationConfiguration() {
        return customDestinationConfiguration;
    }

    public abstract List<String> getMessageRoutingTable();

    /***
     * Return last route in the chain
     */
    public String getLastRoute() {
        List<String> messageRoutingTable = getMessageRoutingTable();
        if (messageRoutingTable.isEmpty()) {
            return getDependentServiceIdentifier().getShortName();
        } else {
            return messageRoutingTable.get(messageRoutingTable.size() - 1);
        }
    }
}
