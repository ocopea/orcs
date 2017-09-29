// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.blobstore;

import java.io.OutputStream;

/**
 * * A type that may be used as a resource method
 * in a {@link BlobStoreAPI} when the application wishes to stream the output.
 */
public interface BlobWriter {

    void write(OutputStream out);
}
