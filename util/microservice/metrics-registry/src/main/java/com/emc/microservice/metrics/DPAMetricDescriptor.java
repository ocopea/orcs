// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2015 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.metrics;

import java.util.Map;

/**
 * Created by nivenb on 1/12/2015.
 */
public class DPAMetricDescriptor {

    private final String metricShortName;

    private final Map<String, String> metricTags;

    public DPAMetricDescriptor(String metricShortName, Map<String, String> metricTags) {
        this.metricShortName = metricShortName;
        this.metricTags = metricTags;
    }

    public String getMetricShortName() {
        return metricShortName;
    }

    public Map<String, String> getMetricTags() {
        return metricTags;
    }

}
