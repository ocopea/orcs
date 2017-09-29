// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2014 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.blobstore;

import com.emc.microservice.bootstrap.SchemaBootstrap;
import com.emc.microservice.bootstrap.SchemaBootstrapScript;

import java.util.Arrays;

/**
 * Initialize Blob store schema
 */
public class PGBlobstoreSchemaBootstrap extends SchemaBootstrap {

    public PGBlobstoreSchemaBootstrap(String schemaName) {
        super(
                schemaName,
                Arrays.asList(new SchemaBootstrapScript("blobstore_pg_schema.sql",
                        "Build Initial Blobstore Schema",
                        1)),
                1);
    }
}