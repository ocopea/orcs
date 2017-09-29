// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import com.emc.dpa.dbjunit.UnitTestNativeQueryServiceImpl;
import com.emc.microservice.bootstrap.SchemaBootstrapRunner;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

public class HubConfigRepositoryImplTest {

    @Test
    public void testConfig() throws SQLException, IOException {
        final UnitTestNativeQueryServiceImpl nqs = new UnitTestNativeQueryServiceImpl("hub-config", true);
        SchemaBootstrapRunner.runBootstrap(nqs.getDataSource(), new HubRepositorySchema("hub"), "hub", null);
        final HubConfigRepositoryImpl configRepository = new HubConfigRepositoryImpl(nqs);
        configRepository.storeKey("a", "hello");

        Assert.assertEquals("hello", configRepository.readKey("a"));
        Assert.assertEquals(null, configRepository.readKey("b"));
    }

}
