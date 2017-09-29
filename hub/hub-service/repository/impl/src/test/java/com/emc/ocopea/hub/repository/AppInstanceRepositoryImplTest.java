// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import com.emc.dpa.dbjunit.UnitTestNativeQueryServiceImpl;
import com.emc.microservice.bootstrap.SchemaBootstrapRunner;
import com.emc.microservice.datasource.MicroServiceDataSource;
import com.emc.microservice.testing.MockTestingResourceProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 8/14/16.
 * Drink responsibly
 */
public class AppInstanceRepositoryImplTest {

    private AppInstanceRepository appInstanceRepository;

    @Before
    public void before() throws IOException, SQLException {
        HubRepositorySchema schemaBootstrap = new HubRepositorySchema();
        MicroServiceDataSource h2InMemoryTestDataSource = MockTestingResourceProvider.wrapDataSource(
                UnitTestNativeQueryServiceImpl.createH2InMemoryTestDataSource("hub_db", true));

        SchemaBootstrapRunner.runBootstrap(h2InMemoryTestDataSource, schemaBootstrap, schemaBootstrap.getSchemaName(), null);
        appInstanceRepository = new AppInstanceRepositoryImpl(h2InMemoryTestDataSource);
    }

    @Test
    public void testCreate() throws DuplicateResourceException {
        UUID id = UUID.randomUUID();

        // Creating new AppInstance
        DBAppInstanceConfig config = createAndVerify(id, "my instance");

        // Testing retrieval of template by name
        final DBAppInstanceConfig byName = appInstanceRepository.findConfig(config.getName());
        Assert.assertNotNull(byName);
        Assert.assertEquals(config.toString(), byName.toString());

        // Testing list of apps
        final Collection<DBAppInstanceConfig> list = appInstanceRepository.listConfig();
        Assert.assertEquals("Must be exactly one instance", 1, list.size());
        final DBAppInstanceConfig fromList = list.iterator().next();
        Assert.assertNotNull(fromList);
        Assert.assertEquals(config.toString(), fromList.toString());

    }

    @Test
    public void testDuplicateName() throws DuplicateResourceException {
        UUID id = UUID.randomUUID();

        // Creating a new template
        createAndVerify(id, "appInstance1");

        // Create new template with the exact same name/different id
        UUID id2 = UUID.randomUUID();

        createAndExpectDuplicateException(id2, "appInstance1");
    }

    @Test
    public void testUpdateState() throws DuplicateResourceException {
        UUID id = UUID.randomUUID();

        // Creating a new template
        createAndVerify(id, "cute app instance");

        appInstanceRepository.updateState(id, "error");
        Assert.assertEquals("error", appInstanceRepository.getState(id).getState());

        appInstanceRepository.updateState(id, "running");
        Assert.assertEquals("running", appInstanceRepository.getState(id).getState());

        final URI url = URI.create("http://shpandrak.com");
        appInstanceRepository.updateStateAndUrl(id, "funny", url);
        Assert.assertEquals("funny", appInstanceRepository.getState(id).getState());
        Assert.assertEquals(url, appInstanceRepository.getState(id).getUrl());

    }




    private void createAndExpectDuplicateException(UUID id, String name) {
        boolean duplicateExceptionThrown = false;
        try {
            createAndVerify(id, name);
            Assert.fail("Duplicate app Instance not be created");
        }catch (DuplicateResourceException dre){
            duplicateExceptionThrown = true;
        }finally {
            Assert.assertTrue("DuplicateResourceException should have been thrown", duplicateExceptionThrown);
        }

    }

    private DBAppInstanceConfig createAndVerify(UUID id, String name) throws DuplicateResourceException {
        final DBAppInstanceConfig config = new DBAppInstanceConfig(id, name, UUID.randomUUID(), "prod", UUID.randomUUID(), null, null, new Date(), new Date(), UUID.randomUUID());
        final DBAppInstanceState state = new DBAppInstanceState(id, "deploying", new Date(), null);

        appInstanceRepository.add(config, state);

        final DBAppInstanceConfig newConfig = appInstanceRepository.getConfig(id);
        final DBAppInstanceState newState = appInstanceRepository.getState(id);

        Assert.assertEquals(config.toString(), newConfig.toString());
        Assert.assertEquals(state.toString(), newState.toString());
        return config;

    }



}
