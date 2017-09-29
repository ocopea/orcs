// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.metrics;

import java.util.Collections;
import java.util.Map;

/**
 * Created by liebea on 9/17/2014. Enjoy it
 */
public interface MetricState {
    String getUniqueIdentifier();

    Map<String, String> getStateValues();

    MetricState EMPTY = new MetricState() {

        @Override
        public String getUniqueIdentifier() {
            return "__EMPTY METRIC STATE__";
        }

        @Override
        public Map<String, String> getStateValues() {
            return Collections.emptyMap();
        }

    };
}
