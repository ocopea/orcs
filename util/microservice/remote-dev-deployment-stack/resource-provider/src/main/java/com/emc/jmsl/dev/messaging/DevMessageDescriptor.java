// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.jmsl.dev.messaging;

import com.emc.microservice.blobstore.BlobStoreLink;

import java.util.Collections;
import java.util.Map;

/**
 * Created by liebea on 1/18/15.
 * Drink responsibly
 */
public class DevMessageDescriptor {
    private final Map<String, String> headers;
    private final BlobStoreLink blobKey;

    public DevMessageDescriptor(Map<String, String> headers, BlobStoreLink blobKey) {
        this.headers = headers == null ? Collections.<String, String>emptyMap() : headers;
        this.blobKey = blobKey;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public BlobStoreLink getBlobKey() {
        return blobKey;
    }
}
