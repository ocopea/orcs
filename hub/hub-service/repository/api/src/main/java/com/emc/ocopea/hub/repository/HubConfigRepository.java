// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

public interface HubConfigRepository {

    void storeKey(String key, String value);

    String readKey(String key);
}
