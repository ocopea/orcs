// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import com.emc.dpa.dbjunit.UnitTestNativeQueryServiceImpl;
import com.emc.microservice.bootstrap.SchemaBootstrapRunner;
import com.emc.microservice.serialization.JacksonSerializationReader;
import com.emc.microservice.serialization.JacksonSerializationWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 7/24/16.
 * Drink responsibly
 */

public class ConnectedSiteRepositoryImplTest {

    private ConnectedSiteRepository connectedSiteRepository;

    @Before
    public void before() throws IOException, SQLException {
        HubRepositorySchema schemaBootstrap = new HubRepositorySchema();
        DataSource h2InMemoryTestDataSource = UnitTestNativeQueryServiceImpl.createH2InMemoryTestDataSource
                ("hub_db", true);
        SchemaBootstrapRunner.runBootstrap(h2InMemoryTestDataSource, schemaBootstrap, schemaBootstrap.getSchemaName(), null);
        connectedSiteRepository = new ConnectedSiteRepositoryImpl(
                h2InMemoryTestDataSource,
                new JacksonSerializationReader<>(DbConnectedSite.class),
                new JacksonSerializationWriter<>());
    }

    @Test
    public void testCreate() throws DuplicateResourceException {
        UUID id = UUID.randomUUID();

        // Creating new
        final DbConnectedSite newConnectedSite = createAndVerify(id, "site1");

        // Testing retrieval of template by name
        final DbConnectedSite byURN = connectedSiteRepository.findByURN(newConnectedSite.getName());
        Assert.assertNotNull(byURN);
        Assert.assertEquals(newConnectedSite.toString(), byURN.toString());

        // Testing list of apps
        final Collection<DbConnectedSite> list = connectedSiteRepository.list();
        Assert.assertEquals("Must be exactly one connected site", 1, list.size());
        final DbConnectedSite fromList = list.iterator().next();
        Assert.assertNotNull(fromList);
        Assert.assertEquals(newConnectedSite.toString(), fromList.toString());

    }

    @Test
    public void testDuplicateName() throws DuplicateResourceException {
        UUID id = UUID.randomUUID();

        // Creating a new template
        createAndVerify(id, "site1");

        // Create new template with the exact same name/different id
        UUID id2 = UUID.randomUUID();

        createAndExpectDuplicateException(id2, "site1");
    }

    @Test
    public void testDuplicateId() throws DuplicateResourceException {
        UUID id = UUID.randomUUID();

        // Creating a new template
        createAndVerify(id, "site1");

        // Create new template with the exact same id and different name

        createAndExpectDuplicateException(id, "site2");
    }

    @Test
    public void testDuplicateNameAndId() throws DuplicateResourceException {
        UUID id = UUID.randomUUID();

        // Creating a new template
        createAndVerify(id, "site1");

        // Create new template with the exact same name and same id
        createAndExpectDuplicateException(id, "site1");
    }

    private void createAndExpectDuplicateException(UUID id, String name) {
        boolean duplicateExceptionThrown = false;
        try {
            createAndVerify(id, name);
            Assert.fail("Duplicate site must not be created");
        }catch (DuplicateResourceException dre){
            duplicateExceptionThrown = true;
        }finally {
            Assert.assertTrue("DuplicateResourceException should have been thrown", duplicateExceptionThrown);
        }
    }



    private DbConnectedSite createAndVerify(UUID id, String urn) throws DuplicateResourceException {
        final DbConnectedSite newAppTemplate = createSampleSite(id, urn);
        connectedSiteRepository.addConnectedSite(
                newAppTemplate);

        // Verify template created according to data sent
        verifyConnectedSiteCreated(id, newAppTemplate);
        return newAppTemplate;
    }


    private void verifyConnectedSiteCreated(UUID id, DbConnectedSite newConnectedSite) {
        final DbConnectedSite byId = connectedSiteRepository.getById(id);
        Assert.assertNotNull(byId);
        Assert.assertEquals(newConnectedSite.toString(), byId.toString());
    }



    private DbConnectedSite createSampleSite(UUID id, String urn) {
        return new DbConnectedSite(
                id,
                urn,
                new Date(),
                "http://site1.com/site-api",
                "site1",
                "1.0",
                new DbLocation(32.1792126,34.9005128, "Israel", Collections.emptyMap()),
                "http://site1.com");
    }

}
