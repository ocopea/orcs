// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.restapi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by liebea on 7/6/2014. Enjoy it
 */
public class RestResourceManager {
    private final Map<Class, ManagedResourceDescriptor> resourceDescriptorMap;
    private final Map<Class, ManagedResourceDescriptor> providerDescriptorMap;
    private final Set<Class> webSocketClasses;

    public RestResourceManager(
            Collection<ManagedResourceDescriptor> resourceDescriptorList,
            Collection<ManagedResourceDescriptor> providerDescriptorList,
            Set<Class> webSocketClasses) {
        resourceDescriptorMap = asMap(resourceDescriptorList);
        providerDescriptorMap = asMap(providerDescriptorList);
        this.webSocketClasses = webSocketClasses;
    }

    private Map<Class, ManagedResourceDescriptor> asMap(Collection<ManagedResourceDescriptor> resourceDescriptorList) {
        Map<Class, ManagedResourceDescriptor> result = new HashMap<>(resourceDescriptorList.size());
        for (ManagedResourceDescriptor currResource : resourceDescriptorList) {
            result.put(currResource.getResourceClass(), currResource);
        }
        return result;
    }

    public Map<Class, ManagedResourceDescriptor> getResourceDescriptorMap() {
        return resourceDescriptorMap;
    }

    public Map<Class, ManagedResourceDescriptor> getProviderDescriptorMap() {
        return providerDescriptorMap;
    }

    public Set<Class> getWebSocketClasses() {
        return webSocketClasses;
    }

    public void addManagedResource(Class resourceClass, ManagedResourceDescriptor descriptor) {
        resourceDescriptorMap.put(resourceClass, descriptor);
    }

    public void addManagedProvider(Class providerClass, ManagedResourceDescriptor descriptor) {
        providerDescriptorMap.put(providerClass, descriptor);
    }
}