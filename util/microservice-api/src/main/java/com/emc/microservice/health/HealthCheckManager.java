// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.health;

/**
 * Created by liebea on 7/31/2014. Enjoy it
 */
public interface HealthCheckManager {
    void flagAsUnhealthy(String reason);

    void addCheck(HealthCheck check);
}
