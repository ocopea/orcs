// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.blobstore;

import java.util.Map;

/**
 * Created with true love by liebea on 11/10/2014.
 */
public interface BlobStoreAPI extends BlobStore {

    /**
     * Store Blob object into blob store, serialize using default micro-service serialization registry
     *
     * @param namespace Blob object namespace
     * @param key       Blob object key
     * @param headers   Blob object headers/metadata
     * @param format    Object format for serialization
     * @param object    Object to serialize and store
     * @throws ObjectKeyFormatException    in case if namespace or key format is illegal
     * @throws DuplicateObjectKeyException in case blob with the same namespace and key already exists
     * @throws IllegalStoreStateException  in case of exception while communicating with underlying DataSource
     * @see com.emc.microservice.MicroServiceInitializationHelper withCustomSerialization method
     * for overriding the default serialization
     * @see IllegalStoreStateException
     * @see DuplicateObjectKeyException
     * @see ObjectKeyFormatException
     */
    <T> void create(String namespace, String key, Map<String, String> headers, Class<T> format, T object)
            throws ObjectKeyFormatException, DuplicateObjectKeyException, IllegalStoreStateException;

    /**
     * Updates Blob object into blob store, serialize using default micro-service serialization registry
     *
     * @param namespace Blob object namespace
     * @param key       Blob object key
     * @param headers   Blob object headers/metadata
     * @param format    Object format for serialization
     * @param object    Object to serialize and store
     * @throws ObjectKeyFormatException   in case if namespace or key format is illegal
     * @throws IllegalStoreStateException in case of exception while communicating with underlying DataSource
     * @see com.emc.microservice.MicroServiceInitializationHelper withCustomSerialization method
     * for overriding the default serialization
     * @see IllegalStoreStateException
     * @see ObjectKeyFormatException
     */
    <T> void update(String namespace, String key, Map<String, String> headers, Class<T> format, T object)
            throws ObjectKeyFormatException, IllegalStoreStateException;

    /***
     * Read an object from blobstore serializing using micro-service serialization registry
     * @see com.emc.microservice.MicroServiceInitializationHelper withCustomSerialization method
     * for overriding the default serialization
     * @param namespace Blob object namespace
     * @param key Blob object key
     * @param format object format class as registered by MicroService either default of custom
     * @param <T> format format generic type
     * @return instance of the object read
     * @throws ObjectKeyFormatException in case if namespace or key format is illegal
     * @throws IllegalStoreStateException  in case of exception while communicating with underlying DataSource
     * @see IllegalStoreStateException
     * @see ObjectKeyFormatException
     */
    <T> T readBlob(String namespace, String key, Class<T> format)
            throws ObjectKeyFormatException, IllegalStoreStateException;


    /**
     * Move current Blob object from one namespace to another
     *
     * @param oldNamespace Blob object current namespace
     * @param key          Blob object key
     * @param newNamespace Blob object new namespace
     * @throws ObjectKeyFormatException    in case if namespace or key format is illegal
     * @throws DuplicateObjectKeyException in case blob with the same namespace and key already exists
     * @throws IllegalStoreStateException  in case of exception while communicating with underlying DataSource
     * @see IllegalStoreStateException
     * @see DuplicateObjectKeyException
     * @see ObjectKeyFormatException
     */
    void moveNameSpace(String oldNamespace, String key, String newNamespace)
            throws ObjectKeyFormatException, DuplicateObjectKeyException, IllegalStoreStateException;
}
