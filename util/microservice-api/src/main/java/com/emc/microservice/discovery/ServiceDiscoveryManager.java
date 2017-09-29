// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.discovery;

/**
 * Created by liebea on 5/2/16.
 * Drink responsibly
 */
public interface ServiceDiscoveryManager {
    DiscoveredService discoverService(String serviceURN);

    WebAPIConnection discoverServiceConnection(String serviceURN);
}
