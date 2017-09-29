// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.dependency;

import java.util.Arrays;
import java.util.List;

/**
 * Created with true love by liebea on 10/13/2014.
 * This descriptor class describes dependency on another service of type Send and forget
 */
public class SyncCallServiceDependencyDescriptor extends ServiceDependencyDescriptor {

    public SyncCallServiceDependencyDescriptor(
            String dependentServiceURI,
            boolean verifyDependencyAsHealthCheck
    ) {
        super(ServiceDependencyType.SYNC_CALL, dependentServiceURI, verifyDependencyAsHealthCheck, null, null);
    }

    @Override
    public SyncCallServiceDependencyDescriptor appendCustomRouting(String additionalServiceShortName) {
        return this;
    }

    @Override
    public List<String> getMessageRoutingTable() {
        return Arrays.asList(this.getDependentServiceIdentifier().getShortName());
    }

}
