// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by liebea on 4/26/17.
 * Drink responsibly
 */
public class StaticObjectMetric<T> {
    private final ConcurrentMap<UUID, T> map = new ConcurrentSkipListMap<>();

    public void put(UUID id, T object) {
        map.put(id, object);
    }

    public void remove(UUID id) {
        map.remove(id);
    }

    public Collection<T> list() {
        return new ArrayList<>(map.values());
    }
}
