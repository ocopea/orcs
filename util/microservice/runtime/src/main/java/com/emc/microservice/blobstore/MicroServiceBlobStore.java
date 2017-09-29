// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.blobstore;

import com.emc.microservice.serialization.SerializationManager;
import com.emc.microservice.serialization.SerializationWriter;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

/**
 * Created by liebea on 6/7/15.
 * Drink responsibly.
 */
public class MicroServiceBlobStore implements BlobStoreAPI {

    private final SerializationManager serializationManager;
    private final BlobStore blobStore;

    public MicroServiceBlobStore(BlobStore blobStore, SerializationManager serializationManager) {
        this.blobStore = blobStore;
        this.serializationManager = serializationManager;
    }

    @Override
    public void create(
            String namespace,
            String key, Map<String, String> headers,
            InputStream blob)
            throws ObjectKeyFormatException, DuplicateObjectKeyException, IllegalStoreStateException {

        blobStore.create(namespace, key, headers, blob);
    }

    @Override
    public <T> void create(
            String namespace,
            String key, Map<String, String> headers,
            Class<T> format, final T object)
            throws ObjectKeyFormatException, DuplicateObjectKeyException, IllegalStoreStateException {

        final SerializationWriter<T> writer = getWriter(format);
        blobStore.create(namespace, key, headers, out -> writer.writeObject(object, out));
    }

    @Override
    public void create(
            String namespace,
            String key, Map<String, String> headers,
            BlobWriter blobWriter)
            throws ObjectKeyFormatException, DuplicateObjectKeyException, IllegalStoreStateException {

        blobStore.create(namespace, key, headers, blobWriter);
    }

    private <T> SerializationWriter<T> getWriter(Class<T> format) {
        final SerializationWriter<T> writer = serializationManager.getWriter(format);
        if (writer == null) {
            throw new IllegalStoreStateException("Could not find serializer for class " + format.getCanonicalName());
        }
        return writer;
    }

    @Override
    public <T> void update(
            String namespace,
            String key, Map<String, String> headers,
            Class<T> format,
            T object) throws ObjectKeyFormatException, IllegalStoreStateException {

        final SerializationWriter<T> writer = getWriter(format);
        blobStore.update(namespace, key, headers, out -> writer.writeObject(object, out));

    }

    @Override
    public void update(
            String namespace,
            String key, Map<String, String> headers,
            InputStream blob)
            throws ObjectKeyFormatException, IllegalStoreStateException {

        blobStore.update(namespace, key, headers, blob);
    }

    @Override
    public void update(
            String namespace,
            String key,
            Map<String, String> headers,
            BlobWriter blobWriter) throws ObjectKeyFormatException, IllegalStoreStateException {

        blobStore.update(namespace, key, headers, blobWriter);
    }

    @Override
    public void readBlob(
            String namespace,
            String key,
            OutputStream out) throws ObjectKeyFormatException, IllegalStoreStateException {

        blobStore.readBlob(namespace, key, out);
    }

    @Override
    public void readBlob(
            String namespace,
            String key,
            BlobReader reader) throws ObjectKeyFormatException, IllegalStoreStateException {

        blobStore.readBlob(namespace, key, reader);
    }

    @Override
    public <T> T readBlob(
            String namespace,
            String key,
            Class<T> format) throws ObjectKeyFormatException, IllegalStoreStateException {

        DefaultBlobReader<T> defaultBlobReader = new DefaultBlobReader<>(serializationManager, format);
        readBlob(namespace, key, defaultBlobReader);
        return defaultBlobReader.getObject();
    }

    @Override
    public void moveNameSpace(
            String oldNamespace,
            String key,
            String newNamespace)
            throws ObjectKeyFormatException, DuplicateObjectKeyException, IllegalStoreStateException {

        blobStore.moveNameSpace(oldNamespace, key, newNamespace);
    }

    @Override
    public Map<String, String> readHeaders(String namespace, String key)
            throws ObjectKeyFormatException, IllegalStoreStateException {

        return blobStore.readHeaders(namespace, key);
    }

    @Override
    public void delete(String namespace, String key) throws ObjectKeyFormatException, IllegalStoreStateException {
        blobStore.delete(namespace, key);
    }

    @Override
    public void delete(int expirySeconds) throws IllegalStoreStateException {
        blobStore.delete(expirySeconds);
    }

    @Override
    public boolean isExists(String namespace, String key) throws IllegalStoreStateException {
        return blobStore.isExists(namespace, key);
    }

    @Override
    public Collection<BlobStoreLink> list() {
        return blobStore.list();
    }
}
