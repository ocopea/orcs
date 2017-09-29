// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.output;

/**
 * Created by liebea on 10/4/2014. Enjoy it
 */
public abstract class OutputDescriptor {

    public static enum MicroServiceOutputType {
        messaging,
        service
    }

    private final MicroServiceOutputType outputType;
    private final Class format;
    private final String description;
    private OutputBlobstoreDetails outputBlobstoreDetails = null;

    protected OutputDescriptor(MicroServiceOutputType outputType, Class format, String description) {
        this.outputType = outputType;
        this.format = format;
        this.description = description;
    }

    public MicroServiceOutputType getOutputType() {
        return outputType;
    }

    public Class getFormat() {
        return format;
    }

    public String getDescription() {
        return description;
    }

    public OutputBlobstoreDetails getOutputBlobstoreDetails() {
        return outputBlobstoreDetails;
    }

    public static class OutputBlobstoreDetails {
        private final String blobstoreName;

        public OutputBlobstoreDetails(String blobstoreName) {
            this.blobstoreName = blobstoreName;
        }

        public String getBlobstoreName() {
            return blobstoreName;
        }
    }

    public OutputDescriptor viaBlobStore(OutputBlobstoreDetails outputBlobstoreDetails) {
        this.outputBlobstoreDetails = outputBlobstoreDetails;
        return this;
    }

}
