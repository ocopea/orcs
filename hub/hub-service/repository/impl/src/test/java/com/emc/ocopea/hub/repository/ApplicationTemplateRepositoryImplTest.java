// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import com.emc.dpa.dbjunit.UnitTestNativeQueryServiceImpl;
import com.emc.microservice.bootstrap.SchemaBootstrapRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

/**
 * Created by liebea on 7/24/16.
 * Drink responsibly
 */

public class ApplicationTemplateRepositoryImplTest {

    private ApplicationTemplateRepository applicationTemplateRepository;
    private final UUID USER_ID = UUID.randomUUID();

    @Before
    public void before() throws IOException, SQLException {
        HubRepositorySchema schemaBootstrap = new HubRepositorySchema();
        DataSource h2InMemoryTestDataSource =
                UnitTestNativeQueryServiceImpl.createH2InMemoryTestDataSource("hub_db", true);
        SchemaBootstrapRunner.runBootstrap(
                h2InMemoryTestDataSource,
                schemaBootstrap,
                schemaBootstrap.getSchemaName(),
                null);

        applicationTemplateRepository = new ApplicationTemplateRepositoryImpl(h2InMemoryTestDataSource);
    }

    @Test
    public void testCreate() throws DuplicateResourceException {
        UUID id = UUID.randomUUID();

        // Creating new Template
        final DBApplicationTemplate newAppTemplate = createAndVerify(id, "my template");

        // Testing retrieval of template by name
        final DBApplicationTemplate byName = applicationTemplateRepository.findByName(newAppTemplate.getName());
        Assert.assertNotNull(byName);
        Assert.assertEquals(newAppTemplate.toString(), byName.toString());

        // Testing list of apps
        final Collection<DBApplicationTemplate> list = applicationTemplateRepository.list();
        Assert.assertEquals("Must be exactly one template", 1, list.size());
        final DBApplicationTemplate fromList = list.iterator().next();
        Assert.assertNotNull(fromList);
        Assert.assertEquals(newAppTemplate.toString(), fromList.toString());

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

    @Test
    public void testDuplicateId() throws DuplicateResourceException {
        UUID id = UUID.randomUUID();

        // Creating a new template
        createAndVerify(id, "my template");

        // Create new template with the exact same id and different name

        createAndExpectDuplicateException(id, "your template");
    }

    @Test
    public void testDuplicateNameAndId() throws DuplicateResourceException {
        UUID id = UUID.randomUUID();

        // Creating a new template
        createAndVerify(id, "my template");

        // Create new template with the exact same name and same id
        createAndExpectDuplicateException(id, "my template");
    }

    private void createAndExpectDuplicateException(UUID id, String name) {
        boolean duplicateExceptionThrown = false;
        try {
            createAndVerify(id, name);
            Assert.fail("Duplicate template must not be created");
        } catch (DuplicateResourceException dre) {
            duplicateExceptionThrown = true;
        } finally {
            Assert.assertTrue("DuplicateResourceException should have been thrown", duplicateExceptionThrown);
        }
    }

    private DBApplicationTemplate createAndVerify(UUID id, String name) throws DuplicateResourceException {
        final DBApplicationTemplate newAppTemplate = createSampleTemplate(id, name);
        applicationTemplateRepository.createApplicationTemplate(
                newAppTemplate);

        // Verify template created according to data sent
        verifyTemplateCreated(id, newAppTemplate);
        return newAppTemplate;
    }

    private void verifyTemplateCreated(UUID id, DBApplicationTemplate newAppTemplate) {
        final DBApplicationTemplate byId = applicationTemplateRepository.getById(id);
        Assert.assertNotNull(byId);
        try {
            Field modified = DBApplicationTemplate.class.getDeclaredField("dateModified");
            modified.setAccessible(true);
            modified.set(byId, modified.get(byId));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        Assert.assertEquals(newAppTemplate.toString(), byId.toString());
    }

    private DBApplicationTemplate createSampleTemplate(UUID id, String name) {
        Collection<DBApplicationServiceTemplate> appServiceTemplates = Collections.singletonList(
                new DBApplicationServiceTemplate(
                        "appsvc1",
                        "cf",
                        "appimg1",
                        "java",
                        "1",
                        Collections.emptyMap(),
                        Collections.emptyMap(),
                        Collections.singletonList(new DBAppServiceExternalDependency(
                                "dsb1",
                                "bb",
                                Collections.singletonList(
                                        new DBAppServiceExternalDependency
                                                .DBAppServiceExternalDependencyProtocol(
                                                "postgres",
                                                "9.0",
                                                null,
                                                null)),
                                "aaa")),
                        new HashSet<>(Arrays.asList(1, 8080)),
                        8080,
                        "/login"));

        return new DBApplicationTemplate(
                id,
                name,
                new Date(),
                new Date(),
                "1",
                "blabla",
                appServiceTemplates,
                "appsvc1",
                USER_ID);
    }

}
