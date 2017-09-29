// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.resource;

/**
 * Created with true love by liebea on 10/17/2014.
 * This class describes a resource configuration property description. every micro-service resource configuration
 * read from the registry will implement a set of supported properties to accommodate the specific technology
 * settings required for setting up the resource
 */
public class ResourceConfigurationProperty {
    private final String name;
    private final ResourceConfigurationPropertyType type;
    private final String description;
    private final boolean mandatory;
    private final boolean hideWhenLogging;

    /***
     * Create a new Resource Configuration property descriptor
     * @param name property name
     * @param type property type
     * @param description description of property
     * @param mandatory whether the configuration should be considered as invalid if missing
     * @param hideWhenLogging whether to hide this property when printing configuration to log or user.
     *                        some properties are secure in nature like passwords etc.
     */
    public ResourceConfigurationProperty(String name,
                                         ResourceConfigurationPropertyType type,
                                         String description,
                                         boolean mandatory,
                                         boolean hideWhenLogging) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.mandatory = mandatory;
        this.hideWhenLogging = hideWhenLogging;
    }

    public String getName() {
        return name;
    }

    public ResourceConfigurationPropertyType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public boolean isHideWhenLogging() {
        return hideWhenLogging;
    }
}
