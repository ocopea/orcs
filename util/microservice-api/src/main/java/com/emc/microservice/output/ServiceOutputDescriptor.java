// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.output;

/**
 * Created by liebea on 10/4/2014. Enjoy it
 */
public class ServiceOutputDescriptor extends OutputDescriptor {
    private final String serviceURI;

    public ServiceOutputDescriptor(Class format, String description, String serviceURI) {
        super(MicroServiceOutputType.service, format, description);
        this.serviceURI = serviceURI;
    }

    public String getServiceURI() {
        return serviceURI;
    }
}
