// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.registry;

public interface RegistryClient {

    void registerService(String urn, String url);
}
