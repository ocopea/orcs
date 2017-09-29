// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.messaging;

import com.emc.microservice.serialization.SerializationReader;

import java.io.InputStream;

/**
 * Created by liebea on 12/29/14.
 * Drink responsibly
 */
public class DefaultMessageReader<T> implements MessageReader {
    private final SerializationReader<T> reader;
    private T result;

    // TODO - find if we can get rid of this constructor
    public DefaultMessageReader(SerializationReader<T> reader, Class<T> clazz) {
        this(reader);
    }

    public DefaultMessageReader(SerializationReader<T> reader) {
        this.reader = reader;
    }

    public T getResult() {
        return result;
    }

    @Override
    public void read(InputStream in) {
        this.result = reader.readObject(in);
    }
}
