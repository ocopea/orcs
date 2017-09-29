// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.services.rest;

import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.serialization.SerializationManager;
import com.emc.microservice.serialization.SerializationWriter;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Created by liebea on 7/19/15.
 * Drink responsibly
 */
@Provider
@Consumes({"application/json"})
@Produces({"application/json"})
public class MicroServiceSerializationProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {
    private SerializationManager serializationManager = null;
    private MicroServiceApplication microServiceApplication;

    public MicroServiceSerializationProvider() {
    }

    public MicroServiceSerializationProvider(SerializationManager serializationManager) {
        this.serializationManager = serializationManager;
    }

    @Context
    public void setApplication(Application application) {
        microServiceApplication = ((MicroServiceApplication) application);
    }

    @Override
    public boolean isReadable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
        return getSerializationManager().getSupportedReaders().contains(clazz);
    }

    private SerializationManager getSerializationManager() {
        if (this.serializationManager == null) {
            this.serializationManager = microServiceApplication.getMicroServiceContext().getSerializationManager();
        }
        return serializationManager;
    }

    @Override
    public Object readFrom(
            Class<Object> clazz,
            Type type,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, String> multivaluedMap,
            InputStream inputStream) throws IOException, WebApplicationException {
        return getSerializationManager().getReader(clazz).readObject(inputStream);
    }

    @Override
    public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
        return getSerializationManager().getSupportedWriters().contains(clazz);
    }

    @Override
    public long getSize(Object o, Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(
            Object o,
            Class<?> clazz,
            Type type,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, Object> multivaluedMap,
            OutputStream outputStream) throws IOException, WebApplicationException {
        //noinspection unchecked
        ((SerializationWriter) getSerializationManager().getWriter(clazz)).writeObject(o, outputStream);
    }
}
