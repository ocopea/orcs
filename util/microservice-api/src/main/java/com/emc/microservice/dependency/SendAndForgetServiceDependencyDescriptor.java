// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.dependency;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with true love by liebea on 10/13/2014.
 * This descriptor class describes dependency on another service of type Send and forget
 */
public class SendAndForgetServiceDependencyDescriptor extends ServiceDependencyDescriptor {

    private final List<String> messageRoutingTable = new ArrayList<>();

    public SendAndForgetServiceDependencyDescriptor(
            String dependentServiceShortName,
            boolean verifyDependencyAsHealthCheck,
            Class format) {
        super(
                ServiceDependencyType.SEND_AND_FORGET,
                dependentServiceShortName,
                verifyDependencyAsHealthCheck,
                format,
                null);

        this.messageRoutingTable.add(dependentServiceShortName);
    }

    @Override
    public SendAndForgetServiceDependencyDescriptor appendCustomRouting(String additionalServiceShortName) {
        this.messageRoutingTable.add(additionalServiceShortName);
        return this;
    }

    @Override
    public List<String> getMessageRoutingTable() {
        return this.messageRoutingTable;
    }
}
