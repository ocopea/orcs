// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.resource;

/**
 * Created by liebea on 4/8/15.
 * Drink responsibly
 */
public class ResourceProviderManager {
    private static ResourceProvider resourceProvider = null;

    public static ResourceProvider getResourceProvider() {
        return resourceProvider;
    }

    public static void setResourceProvider(ResourceProvider resourceProvider) {
        ResourceProviderManager.resourceProvider = resourceProvider;
    }
}
