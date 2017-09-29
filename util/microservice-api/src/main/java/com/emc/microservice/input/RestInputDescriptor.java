// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.input;

/**
 * Created by liebea on 10/4/2014. Enjoy it
 */
public class RestInputDescriptor extends InputDescriptor {

    private final String resourceURI;

    public RestInputDescriptor(String description, Class format, String resourceURI) {
        super(MicroServiceInputType.rest, description, format);
        this.resourceURI = resourceURI;
    }

    public String getResourceURI() {
        return resourceURI;
    }
}
