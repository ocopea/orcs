// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.blobstore;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Created by estera on 26/11/2014.
 * Describes a link to an object in blobstore
 */
public class BlobStoreLink {

    private final String namespace;
    private final String key;
    private final Map<String, String> headers;

    /**
     * Constructor for  jackson (current version, when we upgrade we can ditch)
     */
    private BlobStoreLink() {
        this(null, null, null);
    }

    /**
     * Constructor
     *
     * @param key       blob key
     * @param namespace blob namespace
     */
    public BlobStoreLink(String namespace, String key) {
        this(namespace, key, Collections.<String, String>emptyMap());
    }

    /**
     * Constructor
     *
     * @param key       blob key
     * @param namespace blob namespace
     * @param headers   blob headers
     */
    public BlobStoreLink(String namespace, String key, Map<String, String> headers) {
        this.namespace = namespace;
        this.key = key;
        this.headers = headers;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getKey() {
        return key;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BlobStoreLink that = (BlobStoreLink) o;
        if (!Objects.equals(key, that.key)) {
            return false;
        }
        return Objects.equals(namespace, that.namespace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, namespace);
    }

    @Override
    public String toString() {
        return "BlobStoreLink{" +
                "key='" + key + '\'' +
                ", namespace='" + namespace + '\'' +
                '}';
    }
}
