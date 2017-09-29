// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import com.emc.dpa.dbjunit.UnitTestNativeQueryServiceImpl;
import com.emc.microservice.bootstrap.SchemaBootstrapRunner;
import com.emc.microservice.resource.DefaultWebApiResolver;
import com.emc.microservice.serialization.JacksonSerializationReader;
import com.emc.microservice.serialization.JacksonSerializationWriter;
import com.emc.ocopea.dsb.DsbPlan;
import com.emc.ocopea.dsb.DsbSupportedCopyProtocol;
import com.emc.ocopea.dsb.DsbSupportedProtocol;
import com.emc.ocopea.site.artifact.ArtifactRegistryFactoryImpl;
import com.emc.ocopea.site.artifact.SiteArtifactRegistry;
import com.emc.ocopea.site.copy.CopyRepository;
import com.emc.ocopea.site.dsb.Dsb;
import com.emc.ocopea.site.repository.SiteRepositorySchema;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;

/**
 * Created by liebea on 1/8/17.
 * Drink responsibly
 */
public class SiteRepositoryImplTest {
    private SiteRepositoryImpl siteRepository;

    @Before
    public void before() throws IOException, SQLException {
        SiteRepositorySchema schemaBootstrap = new SiteRepositorySchema();
        DataSource h2InMemoryTestDataSource =
                UnitTestNativeQueryServiceImpl.createH2InMemoryTestDataSource("site_db", true);
        SchemaBootstrapRunner.runBootstrap(h2InMemoryTestDataSource, schemaBootstrap, schemaBootstrap.getSchemaName(),
                null);
        siteRepository = new SiteRepositoryImpl(
                "my-site",
                h2InMemoryTestDataSource,
                new JacksonSerializationWriter<>(),
                new JacksonSerializationReader<>(DbSite.class),
                new ArtifactRegistryFactoryImpl(),
                new DefaultWebApiResolver());
    }

    @Test
    public void testEmptyCreated() {

        // Should create on startup an empty instance
        final Site empty = siteRepository.load();
        Assert.assertTrue(empty.getCopyRepositories().isEmpty());
        Assert.assertTrue(empty.getDsbList().isEmpty());
        Assert.assertTrue(empty.getPsbs().isEmpty());
        Assert.assertEquals("my-site", empty.getName());
    }

    @Test
    public void testAdd() {

        // Should create on startup an empty instance
        persistSite();
        Site newSite = siteRepository.load();
        assertSiteEquals(newSite);

    }

    @Test
    public void testAddWithClearCache() {

        // Should create on startup an empty instance
        persistSite();
        siteRepository.refreshCache();
        Site newSite = siteRepository.load();
        assertSiteEquals(newSite);

    }

    private void assertSiteEquals(Site newSite) {
        Assert.assertEquals(1, newSite.getPsbs().size());
        Assert.assertEquals("psbURN", newSite.getPsbUrnByType("psbType"));

        Assert.assertEquals(1, newSite.getDsbList().size());
        Assert.assertEquals("dsbName", newSite.getDsb("dsbURN").getName());
        Assert.assertEquals("bla bla", newSite.getDsb("dsbURN").getDescription());
        Assert.assertEquals(1, newSite.getDsb("dsbURN").getPlans().size());
        Assert.assertTrue(newSite.getDsb("dsbURN").getPlans().stream().filter(plan -> "default".equals(plan.getId()))
                .count() > 0);

        Assert.assertEquals(1, newSite.getCopyRepositories().size());
        Assert.assertEquals("cr", newSite.getCopyRepositoryByUrn("crURN").getName());
    }

    private void persistSite() {
        final Site site = siteRepository.load();
        site.addCopyRepository(new CopyRepository("crURN", "http://crbURN", "cr", "funCRType", "1"));
        site.addDSB(
                new Dsb(
                        "dsbName",
                        "dsbURN",
                        "dsbUrl",
                        "dsbType",
                        "bla bla",
                        Collections.singletonList(
                                new DsbPlan(
                                        "default",
                                        "default",
                                        "pg",
                                        null,
                                        Collections.singletonList(
                                                new DsbSupportedProtocol("pg", "1", Collections.emptyMap())),
                                        Collections.singletonList(
                                                new DsbSupportedCopyProtocol("pg", "1")),
                                        Collections.emptyMap()
                                        ))
                        ));

        site.addPSB(new Psb("psbURN", "http://psbUrn", "psbType", "psbName", "1", 50));
        site.addArtifactRegistry(new SiteArtifactRegistry(
                "artifactRegistryName",
                SiteArtifactRegistry.ArtifactRegistryType.customRest,
                Collections.emptyMap(),
                artifactId -> Collections.emptyList()));
        siteRepository.persist(site);
    }

}
