// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.dependency;

import com.emc.microservice.messaging.MessageListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with true love by liebea on 10/13/2014.
 * This descriptor class describes dependency on another service of type Async Callback, meaning the service interacts
 * With the dependent service by executing it and consuming the result asynchronously. the result could arrive to the
 * same service executing or to another instance of the same service type
 */
public class AsyncCallbackServiceDependencyDescriptor extends ServiceDependencyDescriptor {
    private final Class<? extends MessageListener> serviceResultCallback;
    private final int defaultCallbackConcurrency;
    private final int defaultCallbackTimeOutInSeconds;
    private final List<String> messageRoutingTable = new ArrayList<>();
    private String customDependencyName;

    /**
     * Create an async callback service dependency descriptor
     *
     * @param dependentServiceShortName       uri of dependent service to use when looking up service in registry
     * @param verifyDependencyAsHealthCheck   whether to pause current service if the dependent service is down
     *                                        or paused. in most scenarios we want to pause our current service if
     *                                        dependent service is down however there might be cases where dependency is
     *                                        used only to process small message format sent to service,
     *                                        will use default serializer unless overridden with custom serializer
     * @param returnValueFormat               message format returned by service, if service does not return value,
     *                                        provide null
     * @param serviceResultCallback           callback Message listener class.
     *                                        used to create callbacks handler on startup and when
     *                                        increasing concurrency of callback handlers on runtime.
     *                                        class may implement ServiceLifecycle
     * @param defaultCallbackConcurrency      default concurrency of callback handlers,
     *                                        can be overridden by configuration
     * @param defaultCallbackTimeOutInSeconds default callback timeout in seconds, can be overridden by configuration.
     *                                        when timeout expires ServiceResultCallback onTimeOut method of Callback
     *                                        supplied will be invoked and further result message for this call
     *                                        will be discarded
     */
    public AsyncCallbackServiceDependencyDescriptor(
            String dependentServiceShortName,
            boolean verifyDependencyAsHealthCheck,
            Class format,
            Class returnValueFormat,
            Class<? extends MessageListener> serviceResultCallback,
            int defaultCallbackConcurrency,
            int defaultCallbackTimeOutInSeconds) {

        super(
                ServiceDependencyType.ASYNC_CALL,
                dependentServiceShortName,
                verifyDependencyAsHealthCheck,
                format,
                returnValueFormat);

        this.serviceResultCallback = serviceResultCallback;
        this.defaultCallbackConcurrency = defaultCallbackConcurrency;
        this.defaultCallbackTimeOutInSeconds = defaultCallbackTimeOutInSeconds;
        this.messageRoutingTable.add(dependentServiceShortName);
        this.customDependencyName = null;
    }

    public Class<? extends MessageListener> getServiceResultCallback() {
        return serviceResultCallback;
    }

    public int getDefaultCallbackConcurrency() {
        return defaultCallbackConcurrency;
    }

    public int getDefaultCallbackTimeOutInSeconds() {
        return defaultCallbackTimeOutInSeconds;
    }

    @Override
    public AsyncCallbackServiceDependencyDescriptor appendCustomRouting(String additionalServiceShortName) {
        this.messageRoutingTable.add(additionalServiceShortName);
        return this;
    }

    public AsyncCallbackServiceDependencyDescriptor withCustomDependencyName(String customDependencyName) {
        this.customDependencyName = customDependencyName;
        return this;
    }

    @Override
    public String getName() {
        if (this.customDependencyName == null) {
            return super.getName();
        } else {
            return this.customDependencyName;
        }
    }

    @Override
    public List<String> getMessageRoutingTable() {
        return messageRoutingTable;
    }
}
