// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2016 EMC Corporation
 * All rights reserved
 */
package com.emc.microservice.metrics.reporter;

import com.emc.microservice.metrics.DPAMetricDescriptor;

/**
 * Created by nivenb on 21/01/2016.
 */
public interface IDpaMetricDescriptorLookup {

    DPAMetricDescriptor getMetricDescriptor(String metricName);
}
