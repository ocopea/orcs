// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import com.emc.ocopea.util.PostgresUtil;
import com.emc.ocopea.util.database.IntegerNativeQueryConverter;
import com.emc.ocopea.util.database.NativeQueryService;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;

public class HubConfigRepositoryImpl implements HubConfigRepository {
    private final NativeQueryService nqs;

    public HubConfigRepositoryImpl(NativeQueryService nqs) {
        this.nqs = nqs;
    }

    @Override
    public void storeKey(String key, String value) {
        final boolean exists = nqs.getSingleValue(
                "SELECT COUNT(*) FROM hubConfig WHERE key=?",
                new IntegerNativeQueryConverter(),
                Collections.singletonList(key)) > 0;

        if (exists) {
            final int updatedRowsCount = nqs.executeUpdate(
                    "UPDATE hubConfig SET data=? WHERE key=?",
                    Arrays.asList(PostgresUtil.jsonStringToJsonBParameter(value, nqs), key));
            if (updatedRowsCount != 1) {
                throw new IllegalStateException("Failed updating key " + key + ": No updated rows to existing key");
            }
        } else {
            nqs.executeUpdate(
                    "INSERT INTO hubConfig (key,data) VALUES (?,?)",
                    Arrays.asList(key, PostgresUtil.jsonStringToJsonBParameter(value, nqs)));
        }
    }

    @Override
    public String readKey(String key) {
        return nqs.getSingleValue(
                "SELECT data FROM hubConfig WHERE key=?",
                ResultSet::getString,
                Collections.singletonList(key));
    }
}
