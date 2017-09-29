// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import com.emc.dpa.dbjunit.UnitTestNativeQueryServiceImpl;
import com.emc.microservice.bootstrap.SchemaBootstrapRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

/**
 * Created by liebea on 7/24/16.
 * Drink responsibly
 */

public class SavedImageRepositoryImplTest {

    private SavedImageRepository savedImageRepository;

    @Before
    public void before() throws IOException, SQLException {
        HubRepositorySchema schemaBootstrap = new HubRepositorySchema();
        DataSource h2InMemoryTestDataSource = UnitTestNativeQueryServiceImpl.createH2InMemoryTestDataSource
                ("hub_db", true);
        SchemaBootstrapRunner.runBootstrap(h2InMemoryTestDataSource, schemaBootstrap, schemaBootstrap.getSchemaName(), null);
        savedImageRepository = new SavedImageRepositoryImpl(h2InMemoryTestDataSource);
    }

    @Test
    public void testCreate() throws DuplicateResourceException {
        UUID id = UUID.randomUUID();

        // Creating new Image
        final DBSavedImage newImage = createAndVerify(id, "my Image");

        // Testing list of images
        final Collection<DBSavedImage> list = savedImageRepository.list();
        Assert.assertEquals("Must be exactly one template", 1, list.size());
        final DBSavedImage fromList = list.iterator().next();
        Assert.assertNotNull(fromList);
        assertEquals(newImage, fromList);

    }

    @Test
    public void testDuplicateName() throws DuplicateResourceException {
        UUID id = UUID.randomUUID();

        // Creating a new template
        createAndVerify(id, "my template");

        // Create new template with the exact same name/different id
        UUID id2 = UUID.randomUUID();

        createAndExpectDuplicateException(id2, "my template");
    }

    private void createAndExpectDuplicateException(UUID id, String name) {
        boolean duplicateExceptionThrown = false;
        try {
            createAndVerify(id, name);
            Assert.fail("Duplicate image must not be created");
        }catch (DuplicateResourceException dre){
            duplicateExceptionThrown = true;
        }finally {
            Assert.assertTrue("DuplicateResourceException should have been thrown", duplicateExceptionThrown);
        }
    }

    private DBSavedImage createAndVerify(UUID id, String name) throws DuplicateResourceException {
        final DBSavedImage newImage = createSavedImage(id, name);
        savedImageRepository.add(
                newImage);

        // Verify template created according to data sent
        verifyTemplateCreated(id, newImage);
        return newImage;
    }

    private void verifyTemplateCreated(UUID id, DBSavedImage savedImage) {
        final DBSavedImage byId = savedImageRepository.get(id);
        Assert.assertNotNull(byId);
        assertEquals(savedImage, byId);
    }

    private void assertEquals(DBSavedImage savedImage, DBSavedImage byId) {
        Assert.assertEquals(savedImage.getName(), byId.getName());
        Assert.assertEquals(savedImage.getDescription(), byId.getDescription());
        Assert.assertEquals(savedImage.getCreatorUserId(), byId.getCreatorUserId());
        Assert.assertEquals(savedImage.getAppCopyId(), byId.getAppCopyId());
        Assert.assertEquals(savedImage.getTags(), byId.getTags());
    }

    private DBSavedImage createSavedImage(UUID id, String name) {
        return new DBSavedImage(id,
                UUID.randomUUID(),
                name,
                "blabla",
                UUID.randomUUID(),
                new HashSet<>(Arrays.asList("test", "customer")),
                new Date(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                DBSavedImage.DBSavedImageState.created);
    }
}
