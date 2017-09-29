// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging;

import com.emc.microservice.serialization.SerializationManager;
import com.emc.microservice.serialization.SerializationWriter;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.util.io.StreamUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with true love by liebea on 10/20/2014.
 * Helper class to make serializing objects fun. well.. at least not a nightmare
 */
public abstract class MessagingSerializationHelper {
    private static final String CONTEXT_HEADER_PREFIX = "__contextHeader_";

    private static class DefaultMessageReader<T> implements MessageReader {
        private Class<T> clazz;
        private T retVal;

        private DefaultMessageReader(Class<T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public void read(InputStream in) {
            retVal = readObject(clazz, in);
        }
    }

    /***
     * Print pretty json
     */
    public static String printJson(Object obj) throws IOException {
        try (StringWriter stringWriter = new StringWriter()) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writerWithDefaultPrettyPrinter();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.writeValue(stringWriter, obj);
            return stringWriter.toString();
        }
    }

    /***
     * Print json message prtty print
     */
    public static String printJsonMessage(Message message) throws IOException {

        final JsonFactory jsonFactory = new JsonFactory();
        final StringWriter stringWriter = new StringWriter();

        message.readMessage(in -> {
            try (JsonParser parser = jsonFactory.createParser(in)) {
                ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
                TreeNode o = objectMapper.readTree(parser);
                objectMapper.writerWithDefaultPrettyPrinter();
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                objectMapper.writeValue(stringWriter, o);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }

        });

        return stringWriter.toString();
    }

    public static MessageWriter getInputStreamMessageWriter(InputStream inputStream) {
        return getInputStreamMessageWriter(inputStream, 1024);
    }

    public static MessageWriter getInputStreamMessageWriter(InputStream inputStream, int bufferSize) {
        return new InputStreamMessageWriter(inputStream, bufferSize);
    }

    private static class InputStreamMessageWriter implements MessageWriter {
        private final InputStream inputStream;
        private final int bufferSize;

        public InputStreamMessageWriter(InputStream inputStream, int bufferSize) {
            this.inputStream = inputStream;
            this.bufferSize = bufferSize;
        }

        @Override
        public void writeMessage(OutputStream outputStream) {
            StreamUtil.copyLarge(inputStream, outputStream, new byte[bufferSize]);
        }
    }

    /***
     * Stream an object
     */
    public static <T> void streamObjectMessage(
            Class<T> format,
            final T objectForSerialization,
            Map<String, String> messageHeaders,
            Map<String, String> messageContext,
            MessageSender messageSender,
            SerializationManager serializationManager) {
        final SerializationWriter<T> writer = serializationManager.getWriter(format);
        messageSender.streamMessage(
                outputStream -> writer.writeObject(
                        objectForSerialization,
                        outputStream),
                messageHeaders,
                messageContext);
    }

    /***
     * Add Context as headers with a prefix that can later be filtered out
     */
    public static Map<String, String> appendContextToHeadersUsingPrefix(
            Map<String, String> messageHeaders,
            Map<String, String> messageContext) {
        if (messageContext == null || messageContext.isEmpty()) {
            return messageHeaders;
        }

        Map<String, String> headersToSend;
        if (messageHeaders != null && !messageHeaders.isEmpty()) {
            headersToSend = new HashMap<>(messageHeaders.size() + messageContext.size());
            headersToSend.putAll(messageHeaders);
        } else {
            headersToSend = new HashMap<>(messageContext.size());
        }

        for (Map.Entry<String, String> currContextEntry : messageContext.entrySet()) {
            headersToSend.put(getUnderlyingKey(currContextEntry.getKey()), currContextEntry.getValue());
        }
        return headersToSend;
    }

    static String getMessageContextKey(String key) {
        if (key.length() <= CONTEXT_HEADER_PREFIX.length()) {
            throw new IllegalArgumentException("Invalid context key");
        }
        return key.substring(CONTEXT_HEADER_PREFIX.length());
    }

    public static String getUnderlyingKey(String key) {
        return CONTEXT_HEADER_PREFIX + key;
    }

    public static boolean isContextKey(String key) {
        return key.startsWith(CONTEXT_HEADER_PREFIX);
    }

    public interface Filter {
        boolean accept(String key);

        String transform(String key);

        Object transformValue(Object value);
    }

    public static final Filter HEADER_FILTER = new Filter() {

        @Override
        public boolean accept(String key) {
            return !MessagingSerializationHelper.isContextKey(key);
        }

        @Override
        public String transform(String key) {
            return key;
        }

        @Override
        public Object transformValue(Object value) {
            return value;
        }
    };

    public static final Filter CONTEXT_FILTER = new Filter() {

        @Override
        public boolean accept(String key) {
            return MessagingSerializationHelper.isContextKey(key);
        }

        @Override
        public String transform(String key) {
            return MessagingSerializationHelper.getMessageContextKey(key);
        }

        @Override
        public Object transformValue(Object value) {
            return value;
        }
    };

    public static byte[] readAsBytes(SerializationManager serializationManager, Object object) {
        return readAsBytes(serializationManager, object, object.getClass());
    }

    @NoJavadoc
    public static byte[] readAsBytes(SerializationManager serializationManager, Object object, Class format) {
        try {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                //noinspection unchecked
                serializationManager.getWriter(format).writeObject(object, out);
                return out.toByteArray();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed reading object as bytes", e);
        }
    }

    /**
     * Read object from message
     */
    public static <T> T readObject(final Class<T> clazz, final Message message) {
        DefaultMessageReader<T> messageReader = new DefaultMessageReader<>(clazz);
        message.readMessage(messageReader);
        return messageReader.retVal;
    }

    @NoJavadoc
    public static <T> T readObject(SerializationManager serializationManager, Class<T> format, byte[] bytes) {
        try {
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
                return readObject(serializationManager, format, inputStream);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed reading object from bytes", e);
        }
    }

    private static <T> T readObject(Class<T> clazz, InputStream inputStream) {
        // For now we assume object mapper (yey)
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(inputStream, clazz);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> T readObject(
            SerializationManager serializationManager,
            Class<T> format,
            InputStream inputStream) {
        return serializationManager.getReader(format).readObject(inputStream);
    }

    public static Map<String, String> extractContext(Map<String, String> messageHeaders) {
        return extractHeaders(messageHeaders, CONTEXT_FILTER);
    }

    public static Map<String, String> extractHeaders(Map<String, String> messageHeaders) {
        return extractHeaders(messageHeaders, HEADER_FILTER);
    }

    private static Map<String, String> extractHeaders(
            Map<String, String> messageHeaders,
            MessagingSerializationHelper.Filter filter) {

        Map<String, String> stringHeaders = new HashMap<>(messageHeaders.size());
        for (Map.Entry<String, String> currEntry : messageHeaders.entrySet()) {
            String key = currEntry.getKey();
            if (filter.accept(key)) {
                Object value = currEntry.getValue();
                if (value == null) {
                    stringHeaders.put(filter.transform(key), null);
                } else {
                    stringHeaders.put(filter.transform(key), value.toString());
                }
            }
        }

        return stringHeaders;
    }

}
