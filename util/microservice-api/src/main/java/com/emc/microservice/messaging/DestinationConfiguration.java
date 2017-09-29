// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.messaging;

import com.emc.microservice.resource.ResourceConfiguration;
import com.emc.microservice.resource.ResourceConfigurationProperty;
import com.emc.microservice.resource.ResourceConfigurationPropertyType;

import java.util.Arrays;
import java.util.List;

/**
 * Created with love by liebea on 6/1/2014.
 */
public class DestinationConfiguration extends ResourceConfiguration {

    private static final String CONFIGURATION_NAME = "Messaging Destination";

    private static final ResourceConfigurationProperty PROPERTY_DESTINATION_QUEUE_URI_NAME =
            new ResourceConfigurationProperty(
                    "destinationQueueURI",
                    ResourceConfigurationPropertyType.STRING,
                    "Destination Queue URI",
                    false,
                    false);

    private static final ResourceConfigurationProperty PROPERTY_BLOB_NAMESPACE =
            new ResourceConfigurationProperty(
                    "blobstoreNameSpace",
                    ResourceConfigurationPropertyType.STRING,
                    "Blobstore namespace to use",
                    false,
                    false);

    private static final ResourceConfigurationProperty PROPERTY_BLOB_KEY_HEADER_NAME =
            new ResourceConfigurationProperty(
                    "blobstoreKeyHeaderName",
                    ResourceConfigurationPropertyType.STRING,
                    "Header from message whose value to use as blob key",
                    false,
                    false);

    private static final ResourceConfigurationProperty PROPERTY_LOG_IN_DEBUG =
            new ResourceConfigurationProperty(
                    "logInDebug",
                    ResourceConfigurationPropertyType.BOOLEAN,
                    "Log message body content while reading if logging is in debug",
                    true,
                    false);

    private static final List<ResourceConfigurationProperty> PROPERTIES = Arrays.asList(
            PROPERTY_DESTINATION_QUEUE_URI_NAME, PROPERTY_BLOB_NAMESPACE,
            PROPERTY_BLOB_KEY_HEADER_NAME, PROPERTY_LOG_IN_DEBUG
    );

    public DestinationConfiguration() {
        super(CONFIGURATION_NAME, PROPERTIES);
    }

    public DestinationConfiguration(String destinationQueueURI, boolean logInDebug) {
        this(destinationQueueURI, null, null, logInDebug);
    }

    public DestinationConfiguration(String destinationQueueURI,
                                    String blobStoreNameSpace,
                                    String blobstoreKeyHeaderName,
                                    boolean logInDebug) {
        this();
        setPropertyValues(propArrayToMap(new String[]{
                PROPERTY_DESTINATION_QUEUE_URI_NAME.getName(), destinationQueueURI,
                PROPERTY_BLOB_NAMESPACE.getName(), blobStoreNameSpace,
                PROPERTY_BLOB_KEY_HEADER_NAME.getName(), blobstoreKeyHeaderName,
                PROPERTY_LOG_IN_DEBUG.getName(), Boolean.toString(logInDebug)
        }));
    }

    public String getBlobNamespace() {
        return getProperty(PROPERTY_BLOB_NAMESPACE.getName());
    }

    public String getBlobKeyHeaderName() {
        return getProperty(PROPERTY_BLOB_KEY_HEADER_NAME.getName());
    }

    public String getDestinationQueueURI() {
        return getProperty(PROPERTY_DESTINATION_QUEUE_URI_NAME.getName());
    }

    public boolean isLogContentWhenInDebug() {
        return Boolean.valueOf(getProperty(PROPERTY_LOG_IN_DEBUG.getName()));
    }
}
