// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Map;

public abstract class JsonUtil {
    private static final ObjectMapper defaultObjectMapper = new ObjectMapper();

    private JsonUtil() {
    }

    public static String toPrettyJson(Object obj) {
        return doPrintJson(obj, defaultObjectMapper.writerWithDefaultPrettyPrinter());
    }

    private static String doPrintJson(Object obj, ObjectWriter writer) {
        try {
            return writer.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed printing json from object of type " + obj.getClass().getCanonicalName(), e);
        }

    }

    public static String toJson(Object obj) {
        return doPrintJson(obj, defaultObjectMapper.writer());
    }

    public static Map<String, String> readMap(Reader reader) {
        //noinspection unchecked
        return fromJson(Map.class, reader);
    }

    public static Map<String, String> readMap(String json) {
        //noinspection unchecked
        return fromJson(Map.class, json);
    }

    /**
     * Write an object to OutputStream
     */
    public static <T> void writeObject(T object, OutputStream outputStream) throws IOException {
        defaultObjectMapper.writeValue(outputStream, object);
    }

    /**
     * Parse json to an object
     */
    public static <T> T fromJson(Class<T> clazz, String json) {
        try {
            return defaultObjectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed parsing json " + json + " to object of type " + clazz.getCanonicalName(), e);
        }
    }

    /**
     * Parse json to an object
     */
    public static <T> T fromJson(Class<T> clazz, Reader reader) {
        try {
            return defaultObjectMapper.readValue(reader, clazz);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed parsing json reader to object of type " + clazz.getCanonicalName(), e);
        }
    }

    /**
     * Parse json to an object
     */
    public static <T> T fromJson(Class<T> clazz, byte[] bytes) {
        try {
            return defaultObjectMapper.readValue(bytes, clazz);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed parsing json reader to object of type " + clazz.getCanonicalName(), e);
        }
    }

    /**
     * Parse json to an object
     */
    public static <T> T fromJson(Class<T> clazz, InputStream inputStream) {
        try {
            return defaultObjectMapper.readValue(inputStream, clazz);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed parsing json reader to object of type " + clazz.getCanonicalName(), e);
        }
    }
}
