// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by liebea on 11/19/14.
 * Drink responsibly
 */
public class JacksonSerializationWriter<T> implements SerializationWriter<T> {
    private final ObjectMapper mapper;

    public JacksonSerializationWriter() {
        this(JacksonSerializationReader.defaultMapper);
    }

    public JacksonSerializationWriter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void writeObject(T object, OutputStream outputStream) {
        try {
            mapper.writeValue(outputStream, object);
        } catch (IOException e) {
            throw new IllegalStateException("failed writing object", e);
        }
    }
}
