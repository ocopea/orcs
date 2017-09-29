// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Created by liebea on 11/19/14.
 * Drink responsibly
 */
public class JacksonSerializationReader<T> implements SerializationReader<T> {
    static final ObjectMapper defaultMapper = new ObjectMapper();
    private final Class<T> clazz;
    private final ObjectMapper mapper;

    public JacksonSerializationReader(Class<T> clazz) {
        this(clazz, defaultMapper);
    }

    public JacksonSerializationReader(Class<T> clazz, ObjectMapper mapper) {
        this.clazz = clazz;
        this.mapper = mapper;
    }

    @Override
    public T readObject(InputStream inputStream) {
        // For now we assume object mapper (yey)
        try {
            return mapper.readValue(inputStream, clazz);
        } catch (IOException e) {
            throw new IllegalStateException("Failed parsing class " + clazz.getCanonicalName(), e);
        }
    }

    @Override
    public T readObject(Reader reader) {
        // For now we assume object mapper (yey)
        try {
            return mapper.readValue(reader, clazz);
        } catch (IOException e) {
            throw new IllegalStateException("Failed parsing class " + clazz.getCanonicalName(), e);
        }
    }
}
