// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2015-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice;

import java.util.Locale;
import java.util.Objects;

/**
 * Created by ashish on 29/06/15.
 */
public class MicroserviceIdentifier {
    private final String shortName;

    public MicroserviceIdentifier(String name) {
        shortName = validate(name);
    }

    public String getShortName() {
        return shortName;
    }

    @Override
    public int hashCode() {
        return shortName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MicroserviceIdentifier microserviceIdentifier = (MicroserviceIdentifier) o;
        return Objects.equals(shortName, microserviceIdentifier.shortName);

    }

    @Override
    public String toString() {
        return "Microservice Identifier: " + shortName;
    }

    /***
     * Get the default input queue name for a service
     */
    public String getDefaultInputQueueName() {
        return shortName + ".queues.input";
    }

    /***
     * Get the default dependent callback queue name
     * @param dependentServiceShortName dependentService shortName
     * @return dependency queue name
     */
    public String getDependencyCallbackQueueName(String dependentServiceShortName) {
        return shortName + ".queues.dependency-callback." + dependentServiceShortName;
    }

    public String getRestURI() {
        return shortName + "-api";
    }

    private String validate(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Microservice identifier name can't be empty.");
        }

        // FIXME - matches current rule; should change to only allow letters, _ and -
        if (name.indexOf(' ') >= 0) {
            throw new IllegalArgumentException("Microservice identifier name can't contain spaces.");
        }

        return name.toLowerCase(Locale.ENGLISH); // make sure we use consistent rule
    }
}
