// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.serialization;

import com.emc.microservice.MicroServiceInitializationHelper;
import com.emc.microservice.blobstore.BlobStoreLink;
import com.emc.microservice.dependency.ServiceDependencyDescriptor;
import com.emc.microservice.input.InputDescriptor;
import com.emc.microservice.output.OutputDescriptor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by liebea on 11/19/14.
 * Drink responsibly
 */
public class SerializationManagerImpl implements SerializationManager {
    private final Map<Class, SerializationReader> serializationReaders = new ConcurrentHashMap<>();
    private final Map<Class, SerializationWriter> serializationWriters = new ConcurrentHashMap<>();

    /***
     * Factory method fo Initializing Serialization manager
     *
     * @param initializationHelper micro service initialization helper
     * @return initialized SerializationManager instance
     */
    public static SerializationManagerImpl initializeSerializationManager(
            MicroServiceInitializationHelper initializationHelper) {

        initializationHelper.withCustomSerialization(
                BlobStoreLink.class,
                new JacksonSerializationReader<>(BlobStoreLink.class),
                new JacksonSerializationWriter<>());
        SerializationManagerImpl serializationManager = new SerializationManagerImpl();

        // By default we used unchanged object mapper, we'll allow overrides soon
        ObjectMapper objectMapper = JacksonSerializationReader.defaultMapper;

        // In case we have input descriptor for service,
        // we parse the format and use either custom or default serializers
        InputDescriptor inputDescriptor = initializationHelper.getInputDescriptor();
        if (inputDescriptor != null) {
            registerFormat(initializationHelper, serializationManager, objectMapper, inputDescriptor.getFormat());
        }

        // In case we have output descriptor for service,
        // we parse the format and use either custom or default serializers
        OutputDescriptor outputDescriptor = initializationHelper.getOutputDescriptor();
        if (outputDescriptor != null) {
            registerFormat(initializationHelper, serializationManager, objectMapper, outputDescriptor.getFormat());
        }

        // In case we have dependencies for service, we parse the format and use either custom or default serializers
        for (ServiceDependencyDescriptor currDependencyDescriptor : initializationHelper.getDependencyDescriptors()) {
            registerFormat(
                    initializationHelper,
                    serializationManager,
                    objectMapper,
                    currDependencyDescriptor.getFormat());

            registerFormat(
                    initializationHelper,
                    serializationManager,
                    objectMapper,
                    currDependencyDescriptor.getReturnValueFormat());
        }

        // Adding non registered custom serializers
        Set<Class> customSerializers = new HashSet<>(
                initializationHelper.getCustomMessageReaders().keySet().size() +
                        initializationHelper.getCustomMessageWriters().keySet().size());
        customSerializers.addAll(initializationHelper.getCustomMessageReaders().keySet());
        customSerializers.addAll(initializationHelper.getCustomMessageWriters().keySet());

        for (Class currCustomSerializerClass : customSerializers) {
            registerFormat(initializationHelper, serializationManager, objectMapper, currCustomSerializerClass);
        }

        // Adding all jackson Serializers
        //noinspection unchecked
        initializationHelper.getClassesForJacksonSerialization().forEach(c ->
                serializationManager.register(
                        c,
                        new JacksonSerializationReader(c),
                        new JacksonSerializationWriter<>())

        );

        return serializationManager;
    }

    private static <T> void registerFormat(
            MicroServiceInitializationHelper initializationHelper,
            SerializationManagerImpl serializationManager,
            ObjectMapper objectMapper,
            Class<T> format) {
        // In case we use hard core input/output stream, we don't have serializers as the service will
        // stream data in and out
        if (format != null && !OutputStream.class.equals(format) && !InputStream.class.equals(format)) {
            SerializationReader reader = initializationHelper.getCustomMessageReaders().get(format);
            SerializationWriter writer = initializationHelper.getCustomMessageWriters().get(format);

            //noinspection unchecked
            serializationManager.registerUsingDefaults(objectMapper, format, reader, writer);
        }
    }

    private <T> void registerUsingDefaults(
            ObjectMapper objectMapper,
            Class<T> format,
            SerializationReader<T> reader,
            SerializationWriter<T> writer) {
        if (reader == null) {
            //noinspection unchecked
            reader = new JacksonSerializationReader(format, objectMapper);
        }

        if (writer == null) {
            //noinspection unchecked
            writer = new JacksonSerializationWriter(objectMapper);
        }

        register(format, reader, writer);
    }

    @Override
    public <T> void register(Class<T> clazz, SerializationReader<T> reader, SerializationWriter<T> writer) {
        this.serializationReaders.put(clazz, reader);
        this.serializationWriters.put(clazz, writer);
    }

    @Override
    public <T> void registerJackson(Class<T> clazz) {
        registerUsingDefaults(JacksonSerializationReader.defaultMapper, clazz, null, null);
    }

    @Override
    public <T> SerializationReader<T> getReader(Class<T> clazz) {
        //noinspection unchecked
        return Objects.requireNonNull(
                serializationReaders.get(Objects.requireNonNull(
                        clazz,
                        "class format for serialization must not be null")),
                "Unsupported format for serialization: " + clazz.getCanonicalName() +
                        " make sure format reader is registered");
    }

    @Override
    public <T> SerializationWriter<T> getWriter(Class<T> clazz) {
        //noinspection unchecked
        return Objects.requireNonNull(
                serializationWriters.get(Objects.requireNonNull(
                        clazz,
                        "class format for serialization must not be null")),
                "Unsupported format for serialization: " + clazz.getCanonicalName() +
                        " make sure format writer is registered");
    }

    @Override
    public Set<Class> getSupportedReaders() {
        return new HashSet<>(serializationReaders.keySet());
    }

    @Override
    public Set<Class> getSupportedWriters() {
        return new HashSet<>(serializationWriters.keySet());
    }
}
