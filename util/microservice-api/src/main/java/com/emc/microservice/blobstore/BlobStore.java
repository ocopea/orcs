// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2015-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.blobstore;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

/**
 * Created by liebea on 6/7/15.
 * Drink responsibly
 */
public interface BlobStore {
    /**
     * Store Blob object into blob store
     *
     * @param namespace Blob object namespace
     * @param key       Blob object key
     * @param headers   Blob object headers/metadata
     * @param blob      Blob {@link java.io.InputStream}
     * @throws ObjectKeyFormatException    in case if namespace or key format is illegal
     * @throws DuplicateObjectKeyException in case blob with the same namespace and key already exists
     * @throws IllegalStoreStateException  in case of exception while communicating with underlying DataSource
     * @see IllegalStoreStateException
     * @see DuplicateObjectKeyException
     * @see ObjectKeyFormatException
     */
    void create(String namespace, String key, Map<String, String> headers, InputStream blob)
            throws ObjectKeyFormatException, DuplicateObjectKeyException, IllegalStoreStateException;

    /**
     * Store Blob object into blob store
     *
     * @param namespace  Blob object namespace
     * @param key        Blob object key
     * @param headers    Blob object headers/metadata
     * @param blobWriter BlobWriter {@link BlobWriter} an instance that writes blob directly
     * @throws ObjectKeyFormatException    in case if namespace or key format is illegal
     * @throws DuplicateObjectKeyException in case blob with the same namespace and key already exists
     * @throws IllegalStoreStateException  in case of exception while communicating with underlying DataSource
     * @see IllegalStoreStateException
     * @see DuplicateObjectKeyException
     * @see ObjectKeyFormatException
     */
    void create(String namespace, String key, Map<String, String> headers, BlobWriter blobWriter)
            throws ObjectKeyFormatException, DuplicateObjectKeyException, IllegalStoreStateException;

    /**
     * Update Blob body or Blob headers
     *
     * @param namespace Blob object namespace
     * @param key       Blob object key
     * @param headers   Blob headers/metadata
     * @param blob      Blob {@link java.io.InputStream}
     * @throws ObjectKeyFormatException   in case if namespace or key format is illegal
     * @throws IllegalStoreStateException in case of exception while communicating with underlying DataSource
     * @see IllegalStoreStateException
     * @see ObjectKeyFormatException
     */
    void update(String namespace, String key, Map<String, String> headers, InputStream blob)
            throws ObjectKeyFormatException, IllegalStoreStateException;

    /**
     * Update Blob body or Blob headers
     *
     * @param namespace  Blob object namespace
     * @param key        Blob object key
     * @param headers    Blob headers/metadata
     * @param blobWriter BlobWriter {@link BlobWriter} an instance that writes blob directly
     * @throws ObjectKeyFormatException   in case if namespace or key format is illegal
     * @throws IllegalStoreStateException in case of exception while communicating with underlying DataSource
     * @see IllegalStoreStateException
     * @see ObjectKeyFormatException
     */
    void update(String namespace, String key, Map<String, String> headers, BlobWriter blobWriter)
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

    /**
     * Read something from blobstore
     *
     * @param namespace Blob object namespace
     * @param key       Blob object key
     * @param out       {@link java.io.OutputStream} to where we read blob object
     * @throws ObjectKeyFormatException   in case if namespace or key format is illegal
     * @throws IllegalStoreStateException in case of exception while communicating with underlying DataSource
     * @see IllegalStoreStateException
     * @see ObjectKeyFormatException
     */
    void readBlob(String namespace, String key, OutputStream out)
            throws ObjectKeyFormatException, IllegalStoreStateException;

    /**
     * Read something from blobstore
     *
     * @param namespace Blob object namespace
     * @param key       Blob object key
     * @param reader    Reader to stream blob out of the blobstore
     * @throws ObjectKeyFormatException   in case if namespace or key format is illegal
     * @throws IllegalStoreStateException in case of exception while communicating with underlying DataSource
     * @see IllegalStoreStateException
     * @see ObjectKeyFormatException
     */
    void readBlob(String namespace, String key, BlobReader reader)
            throws ObjectKeyFormatException, IllegalStoreStateException;

    /**
     * @param namespace Blob object namespace
     * @param key       Blob object key
     * @return Blob object headers
     * @throws ObjectKeyFormatException   in case if namespace or key format is illegal
     * @throws IllegalStoreStateException in case of exception while communicating with underlying DataSource
     * @see IllegalStoreStateException
     * @see ObjectKeyFormatException
     */
    Map<String, String> readHeaders(String namespace, String key)
            throws ObjectKeyFormatException, IllegalStoreStateException;

    /**
     * @param namespace Blob object namespace
     * @param key       Blob object key
     * @throws ObjectKeyFormatException   in case if namespace or key format is illegal
     * @throws IllegalStoreStateException in case of exception while communicating with underlying DataSource
     * @see IllegalStoreStateException
     * @see ObjectKeyFormatException
     */
    void delete(String namespace, String key)
            throws ObjectKeyFormatException, IllegalStoreStateException;

    /**
     * @param expirySeconds - expiration time of blob store records
     */
    void delete(int expirySeconds) throws IllegalStoreStateException;

    /**
     * Checks is an object exists for given namespace and key
     *
     * @param namespace Blob object namespace
     * @param key       Blob object key
     * @return true if object exists, false otherwise
     * @throws IllegalStoreStateException in case of exception while communicating with underlying DataSource
     */
    boolean isExists(String namespace, String key) throws IllegalStoreStateException;

    Collection<BlobStoreLink> list();

}
