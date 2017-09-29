// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.input;

/**
 * Created by liebea on 10/4/2014. Enjoy it
 */
public abstract class InputDescriptor {

    public static enum MicroServiceInputType {
        messaging,
        rest
    }

    private final MicroServiceInputType inputType;
    private final String description;
    private final Class format;

    protected InputDescriptor(MicroServiceInputType inputType, String description, Class format) {
        this.inputType = inputType;
        this.description = description;
        this.format = format;
    }

    public MicroServiceInputType getInputType() {
        return inputType;
    }

    public String getDescription() {
        return description;
    }

    public Class getFormat() {
        return format;
    }
}
