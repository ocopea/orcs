// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import com.emc.microservice.Context;
import com.emc.microservice.blobstore.IllegalStoreStateException;
import com.emc.microservice.serialization.SerializationReader;
import com.emc.microservice.serialization.SerializationWriter;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.dsb.DsbPlan;
import com.emc.ocopea.dsb.DsbSupportedCopyProtocol;
import com.emc.ocopea.dsb.DsbSupportedProtocol;
import com.emc.ocopea.site.artifact.ArtifactRegistryApi;
import com.emc.ocopea.site.artifact.ArtifactRegistryFactory;
import com.emc.ocopea.site.artifact.ArtifactRegistryFactoryImpl;
import com.emc.ocopea.site.artifact.SiteArtifactRegistry;
import com.emc.ocopea.site.copy.CopyRepository;
import com.emc.ocopea.site.dsb.Dsb;
import com.emc.ocopea.util.PostgresUtil;
import com.emc.ocopea.util.database.BasicNativeQueryService;
import com.emc.ocopea.util.database.NativeQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by liebea on 1/3/17.
 * Drink responsibly
 */
public class SiteRepositoryImpl implements SiteRepository, ServiceLifecycle {
    private static final Logger log = LoggerFactory.getLogger(SiteRepositoryImpl.class);
    private ArtifactRegistryFactory artifactRegistryFactory;
    private WebAPIResolver webAPIResolver;
    private String siteName;
    private DbSite dbSite;
    private NativeQueryService nqs;

    private SerializationWriter<DbSite> writer;
    private SerializationReader<DbSite> reader;

    private static final String SQL_READ_SITE_CONFIG = "select * from siteConfig";
    private static final String SQL_INSERT_SITE_CONFIG = "insert into siteConfig values (?)";
    private static final String SQL_UPDATE_SITE_CONFIG = "update siteConfig set data=?";

    // Required for instantiating
    public SiteRepositoryImpl() {
    }

    // Constructor for using class as a library (or tests)
    public SiteRepositoryImpl(
            String siteName,
            DataSource dataSource,
            SerializationWriter<DbSite> writer,
            SerializationReader<DbSite> reader,
            ArtifactRegistryFactory artifactRegistryFactory,
            WebAPIResolver resolver) {
        initialize(siteName, dataSource, writer, reader, artifactRegistryFactory, resolver);
    }

    private void initialize(
            String siteName,
            DataSource dataSource,
            SerializationWriter<DbSite> writer,
            SerializationReader<DbSite> reader,
            ArtifactRegistryFactory artifactRegistryFactory,
            WebAPIResolver resolver) {

        log.info("Initializing site {}", siteName);

        this.siteName = siteName;
        this.nqs = new BasicNativeQueryService(dataSource);
        this.writer = writer;
        this.reader = reader;
        this.artifactRegistryFactory = artifactRegistryFactory;
        this.webAPIResolver = resolver;

        // Loading site on startup in case this is not the first time
        dbSite = readSite();

    }

    @Override
    public void init(Context context) {
        initialize(
                context.getParametersBag().getString("site-name"),
                context.getDatasourceManager().getManagedResourceByName("site-db").getDataSource(),
                context.getSerializationManager().getWriter(DbSite.class),
                context.getSerializationManager().getReader(DbSite.class),
                context.getSingletonManager().getManagedResourceByName(
                        ArtifactRegistryFactoryImpl.class.getSimpleName()).getInstance(),
                context.getWebAPIResolver()
        );
    }

    private DbSite readSite() {
        // There should be one entry only for site config, reading it
        final List<DbSite> list = nqs.getList(SQL_READ_SITE_CONFIG, this::convertRow, null);

        // In case there is no data, we create the row ourselves with empty configuration
        if (list.isEmpty()) {
            DbSite emptySiteConfig = new DbSite(Collections.emptyList(), Collections.emptyList(),
                    Collections.emptyList(), Collections.emptyList());
            nqs.executeUpdate(SQL_INSERT_SITE_CONFIG, Collections.singletonList(
                    PostgresUtil.objectToJsonBParameter(emptySiteConfig, nqs)));
            return emptySiteConfig;
        }
        return list.get(0);
    }

    private DbSite convertRow(ResultSet rset, int pos) throws SQLException {
        try (Reader data = rset.getCharacterStream("data")) {
            return reader.readObject(data);
        } catch (IOException e) {
            throw new IllegalStoreStateException("Failed reading data for " +
                    reader.getClass().getGenericInterfaces()[0].getClass().getSimpleName(), e);
        }
    }

    @Override
    public void shutDown() {
        dbSite = null;
    }

    @Override
    public Site load() {

        // Building a site object
        final Site site = new Site(siteName);

        // populating the site from the dbSite object
        populateSite(site);
        return site;

    }

    private void populateSite(Site site) {
        for (DbSite.DbSiteDsb dsb : dbSite.getDsbs()) {
            site.addDSB(
                    new Dsb(
                            dsb.getName(),
                            dsb.getUrn(),
                            dsb.getUrl(),
                            dsb.getType(),
                            dsb.getDescription(),
                            dsb.getPlans()
                                    .stream()
                                    .map(p ->
                                            new DsbPlan(
                                                    p.getId(),
                                                    p.getName(),
                                                    p.getDescription(),
                                                    p.getPrice(),
                                                    p.getProtocols()
                                                            .stream()
                                                            .map(protocol -> new DsbSupportedProtocol(
                                                                    protocol.getProtocolName(),
                                                                    protocol.getProtocolVersion(),
                                                                    protocol.getProperties()))
                                                            .collect(Collectors.toList()),
                                                    p.getCopyProtocols()
                                                            .stream()
                                                            .map(protocol -> new DsbSupportedCopyProtocol(
                                                                    protocol.getProtocolName(),
                                                                    protocol.getProtocolVersion()))
                                                            .collect(Collectors.toList()),
                                                    p.getDsbSettings()
                                            ))
                                    .collect(Collectors.toList())
                    ));
        }

        for (DbSite.DbCrb currCrb : dbSite.getCrbs()) {
            site.addCopyRepository(
                    new CopyRepository(
                            currCrb.getUrn(),
                            currCrb.getUrl(),
                            currCrb.getName(),
                            currCrb.getType(),
                            currCrb.getVersion()));
        }

        for (DbSite.DbSitePsb currPsb : dbSite.getPsbs()) {
            site.addPSB(
                    new Psb(
                            currPsb.getUrn(),
                            currPsb.getUrl(),
                            currPsb.getType(),
                            currPsb.getName(),
                            currPsb.getVersion(),
                            currPsb.getMaxAppServiceIdLength()));
        }
        for (DbSite.DbSiteArtifactRegistry currAR : dbSite.getArtifactRegistries()) {
            final SiteArtifactRegistry.ArtifactRegistryType artifactRegistryType =
                    SiteArtifactRegistry.ArtifactRegistryType.valueOf(currAR.getType());
            ArtifactRegistryApi ar = artifactRegistryFactory.create(
                    artifactRegistryType,
                    currAR.getParameters(),
                    webAPIResolver);
            site.addArtifactRegistry(
                    new SiteArtifactRegistry(currAR.getName(), artifactRegistryType, currAR.getParameters(), ar));
        }
    }

    @Override
    public void persist(Site site) {

        // Converting site domain object into DBSite
        dbSite = buildDBSite(site);

        // updating the data row
        nqs.executeUpdate(
                SQL_UPDATE_SITE_CONFIG,
                Collections.singletonList(PostgresUtil.objectToJsonBParameter(dbSite, nqs)));
    }

    static DbSite buildDBSite(Site site) {
        return new DbSite(
                site.getDsbList()
                        .stream()
                        .map(dsb ->
                                new DbSite.DbSiteDsb(
                                        dsb.getName(),
                                        dsb.getUrn(),
                                        dsb.getUrl(),
                                        dsb.getType(),
                                        dsb.getDescription(),
                                        dsb.getPlans()
                                                .stream()
                                                .map(plan -> new DBSiteDsbPlan(
                                                        plan.getId(),
                                                        plan.getName(),
                                                        plan.getDescription(),
                                                        plan.getPrice(),
                                                        plan.getProtocols()
                                                                .stream()
                                                                .map(p ->
                                                                        new DbSite.DBSiteSupportedDsbProtocol(
                                                                                p.getProtocol(),
                                                                                p.getVersion(),
                                                                                p.getProperties()))
                                                                .collect(Collectors.toList()),
                                                        plan.getCopyProtocols()
                                                                .stream()
                                                                .map(sp -> new DbSite.DBSiteSupportedDsbCopyProtocol(
                                                                        sp.getCopyProtocol(),
                                                                        sp.getCopyProtocolVersion()))
                                                                .collect(Collectors.toList()),
                                                        plan.getDsbSettings()))
                                                .collect(Collectors.toList())))
                        .collect(Collectors.toList()),
                site.getCopyRepositories()
                        .stream()
                        .map(copyRepository ->
                                new DbSite.DbCrb(
                                        copyRepository.getUrn(),
                                        copyRepository.getUrl(),
                                        copyRepository.getName(),
                                        copyRepository.getType(),
                                        copyRepository.getVersion()))
                        .collect(Collectors.toList()),
                site.getPsbs()
                        .stream()
                        .map(psb ->
                                new DbSite.DbSitePsb(
                                        psb.getUrn(),
                                        psb.getUrl(),
                                        psb.getName(),
                                        psb.getType(),
                                        psb.getVersion(),
                                        psb.getMaxAppServiceIdLength()))
                        .collect(Collectors.toList()),
                site.getArtifactRegistires()
                        .stream()
                        .map(ar ->
                                new DbSite.DbSiteArtifactRegistry(
                                        ar.getName(),
                                        ar.getType().name(),
                                        ar.getParameters()))
                        .collect(Collectors.toList()));
    }

    void refreshCache() {
        // re-loading site from db
        dbSite = readSite();

    }

}
