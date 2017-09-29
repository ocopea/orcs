// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.health;

import com.emc.microservice.Context;

/**
 * Created by liebea on 7/31/2014. Enjoy it
 */
public interface HealthCheck {

    String getName();

    HealthCheckResult check(Context context);
}
