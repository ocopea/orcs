// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by liebea on 9/17/2014. Enjoy it
 */
public class MapMetricState implements MetricState {
    private final Map<String, String> identifiersMap;
    private String identifier = null;

    public MapMetricState(Map<String, String> identifiersMap) {
        this.identifiersMap = identifiersMap;
    }

    @Override
    public String getUniqueIdentifier() {
        if (identifier == null) {
            List<String> sortedKeyList = new ArrayList<>(identifiersMap.keySet());
            Collections.sort(sortedKeyList);
            identifier = "";
            for (String currKey : sortedKeyList) {
                identifier += "|" + identifiersMap.get(currKey);
            }
        }
        return identifier;
    }

    @Override
    public Map<String, String> getStateValues() {
        return identifiersMap;
    }
}
