// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2015-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.serialization;

import java.util.Set;

/**
 * Created by liebea on 4/19/15.
 * Drink responsibly
 */
public interface SerializationManager {

    <T> void register(Class<T> clazz, SerializationReader<T> reader, SerializationWriter<T> writer);

    <T> void registerJackson(Class<T> clazz);

    <T> SerializationReader<T> getReader(Class<T> clazz);

    <T> SerializationWriter<T> getWriter(Class<T> clazz);

    Set<Class> getSupportedReaders();

    Set<Class> getSupportedWriters();
}
