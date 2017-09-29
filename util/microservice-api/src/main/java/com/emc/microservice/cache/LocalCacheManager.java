// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2015-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by liebea on 3/19/15.
 * Basic implementation of local cache for services to use
 */
public class LocalCacheManager {
    private final Map<String, Map> cachesByName = new HashMap<>();

    /***
     * Get (Or create) local cache
     * @param name cache name - global for service jvm
     * @return local cache map
     */
    public synchronized <K, V> Map<K, V> getLocalCache(String name) {

        @SuppressWarnings("unchecked")
        Map<K, V> map = (Map<K, V>) cachesByName.get(name);

        if (map == null) {
            map = new ConcurrentHashMap<>();
            cachesByName.put(name, map);
        }
        return map;
    }
}
