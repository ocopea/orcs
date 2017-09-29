// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.demo.dsb.h2;

import com.emc.dpa.dev.H2DatasourceConfiguration;
import com.emc.microservice.Context;
import com.emc.microservice.ContextThreadLocal;
import com.emc.microservice.datasource.DatasourceConfiguration;
import com.emc.microservice.datasource.MicroServiceDataSource;
import com.emc.microservice.registry.ServiceRegistryApi;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.microservice.resource.ResourceProviderManager;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.crb.CrbWebDataApi;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.dsb.CopyServiceInstance;
import com.emc.ocopea.dsb.DsbRestoreCopyInfo;
import com.emc.ocopea.util.database.BasicNativeQueryService;
import com.emc.ocopea.util.database.NativeQueryService;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.commons.io.input.CountingInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by liebea on 1/5/16.
 * Drink responsibly
 */
public class H2DSBSingleton implements ServiceLifecycle {
    private static final Logger log = LoggerFactory.getLogger(H2DSBSingleton.class);
    private static final long EMPTY_H2_SIZE = 100L;
    private final Map<String, H2Instance> serviceInstancesMap = new HashMap<>();
    private ResourceProvider resourceProvider;
    private String copyRepositoryURN = null;
    private WebAPIResolver webAPIResolver;

    @Override
    public void init(Context context) {
        log.info("Initialized H2 Singleton");
        //very platform specific, but it is what it is...
        resourceProvider = ResourceProviderManager.getResourceProvider();
        this.webAPIResolver = context.getWebAPIResolver();

    }

    @Override
    public void shutDown() {
        resourceProvider = null;
        log.info("Shutting down h2 singleton");

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    public String getCopyRepositoryURN() {
        return copyRepositoryURN;
    }

    public void setCopyRepositoryURN(String copyRepositoryURN) {
        this.copyRepositoryURN = copyRepositoryURN;
    }

    public boolean exists(String name) {
        return serviceInstancesMap.containsKey(name);
    }

    public void create(final String name, Map<String, String> dsbSettings) {
        checkExists(name);
        createDB(name, dsbSettings, false);
    }

    private void checkExists(String name) {
        if (exists(name)) {
            throw new IllegalArgumentException("H2 instance by name " + name + " already exists");
        }
    }

    public void deleteDB(final String instanceId) {
        remove(get(instanceId));
    }

    private void createDB(final String instanceId, Map<String, String> dsbSettings, boolean readonly) {
        H2DatasourceConfiguration dsConfig = new H2DatasourceConfiguration(instanceId, "shoko");

        // Creating physical database
        //noinspection unchecked
        Objects.requireNonNull(
                resourceProvider.getDataSource(dsConfig),
                "failed instantiating underlying datasource for " + instanceId);

        // Registering in service registry
        resourceProvider.getServiceRegistryApi().registerDataSource(instanceId, dsConfig);

        H2Instance instance = new H2Instance(instanceId, null, new Date(), readonly, dsbSettings);
        instance.setSize(EMPTY_H2_SIZE);
        add(instance);
    }

    private void createDB(
            final String name,
            final InputStream copyStream,
            Map<String, String> dsbSettings,
            boolean readonly) {
        H2DatasourceConfiguration dsConfig = new H2DatasourceConfiguration(name, "shoko");

        // Creating physical database
        //noinspection unchecked
        final MicroServiceDataSource dataSource = resourceProvider.getDataSource(dsConfig);

        long size = EMPTY_H2_SIZE;
        // Building from copy
        if (copyStream != null) {
            log.info("Importing h2 copy stream to {}", name);

            // Read blob to db
            try {
                CountingInputStream cis = new CountingInputStream(copyStream);
                new DBCopyParser(dataSource).parse(cis);
                size = cis.getByteCount();
            } catch (IOException e) {
                throw new IllegalStateException("Failed importing copy stream for h2 service " + name, e);
            }
        }

        // Registering in service registry
        resourceProvider.getServiceRegistryApi().registerDataSource(name, dsConfig);

        H2Instance instance = new H2Instance(name, null, new Date(), readonly, dsbSettings);
        instance.setSize(size);
        add(instance);
    }

    private void remove(H2Instance instance) {
        serviceInstancesMap.remove(instance.getName());
    }

    private void add(H2Instance instance) {
        serviceInstancesMap.put(instance.getName(), instance);
    }

    public Collection<H2Instance> list() {
        return new ArrayList<>(serviceInstancesMap.values());
    }

    public H2Instance get(String name) {
        return serviceInstancesMap.get(name);
    }

    @NoJavadoc
    // TODO add javadoc
    public void createFromCopy(String serviceId, DsbRestoreCopyInfo copyInfo, Map<String, String> dsbSettings) {
        checkExists(serviceId);

        //todo: this needs to test protocol and parse accordingly
        final String copyRepoUrl = copyInfo.getCopyRepoCredentials().get("url");
        if (copyRepoUrl == null) {
            throw new BadRequestException("Failed parsing copy repo credentials, missing \"url\"");
        }

        Response response;
        try {
            response = webAPIResolver.getWebAPI(copyRepoUrl, CrbWebDataApi.class).retrieveCopy(copyInfo.getCopyId());
        } catch (Exception ex) {
            throw new InternalServerErrorException("Failed doanloading copy from copy repo " + copyRepoUrl, ex);
        }
        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new InternalServerErrorException("failed in reading copyrepo: " + response.readEntity(String.class));
        }
        try {
            InputStream copyStream;
            try {
                copyStream = response.readEntity(InputStream.class);
            } catch (Exception ex) {
                throw new InternalServerErrorException("Failed reading stream form artifact", ex);
            }
            createDB(serviceId, copyStream, dsbSettings, false);
        } finally {
            response.close();
        }
    }

    private void createCopyAndStream(
            OutputStream out,
            final String copyId,
            final String serviceName,
            final String copyPluginName) {
        NativeQueryService nqs = getNativeQueryService(serviceName);
        log.info("Copying to {} datasource with id {}", serviceName, copyId);
        final long[] copySize = {0L};

        try (final JsonGenerator generator = new JsonFactory().createGenerator(out)) {
            // Slow down dude..
            Thread.sleep(5000L);
            generator.writeStartObject();
            generator.writeStringField("copyId", copyId);
            generator.writeStringField("serviceType", ServiceRegistryApi.SERVICE_TYPE_DATASOURCE);
            generator.writeStringField("serviceName", serviceName);
            generator.writeStringField("copyPluginName", copyPluginName);

            nqs.getStream("SCRIPT DROP", null, inputStream -> {
                CountingInputStream countingInputStream = new CountingInputStream(inputStream);
                generator.writeFieldName("data");
                generator.writeBinary(countingInputStream, -1);
                copySize[0] = countingInputStream.getByteCount();
            });
            generator.writeNumberField("copySize", copySize[0]);
            generator.writeEndObject();
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Failed generating database copy for " + serviceName, e);
        }

        serviceInstancesMap.get(serviceName).setSize(copySize[0]);

    }

    @NoJavadoc
    // TODO add javadoc
    public void copy(final String serviceName, CopyServiceInstance copyDetails) {

        try {
            final String restCRUrl = copyDetails.getCopyRepoCredentials().get("url");
            if (restCRUrl == null) {
                throw new BadRequestException("missing url in rest CR credentials");
            }
            final String restCopyRepoId = copyDetails.getCopyRepoCredentials().get("repoId");
            if (restCopyRepoId == null) {
                throw new BadRequestException("Missing repoId in rest copy repo credentials");
            }

            CrbWebDataApi copyRepoAPI = webAPIResolver.getWebAPI(restCRUrl, CrbWebDataApi.class);

            PipedInputStream in = new PipedInputStream();
            final PipedOutputStream out = new PipedOutputStream(in);

            final Context context = ContextThreadLocal.getContext();
            Thread thread = new Thread(
                    () -> {
                        ContextThreadLocal.setContext(context);
                        createCopyAndStream(out, copyDetails.getCopyId(), serviceName, copyDetails.getCopyType());
                    }
            );
            thread.start();

            //todo:amit: shouldn't be two fields - desired time v.s. actual time?
            Long copyTime = copyDetails.getCopyTime();

            // TODO(maloni): need to assert success
            String response = copyRepoAPI.createCopyInRepo(restCopyRepoId, copyDetails.getCopyId(), in);
            //String response = copyRepoAPI.createCopyInRepo(in, "h2-dsb", copyTime.getTime(),
            //        copyDetails.getCopyType(), "", copyDetails.getCopyId());

            thread.join();
        } catch (Exception ex) {
            //todo:amit:proper error handling
            throw new IllegalStateException(ex);
        }
    }

    private NativeQueryService getNativeQueryService(String serviceName) {
        DatasourceConfiguration dataSourceConfiguration =
                Objects.requireNonNull(
                        resourceProvider.getServiceRegistryApi()
                                .getDataSourceConfiguration(H2DatasourceConfiguration.class, serviceName),
                        "Failed locating datasource " + serviceName + " in service registry");

        @SuppressWarnings("unchecked")
        MicroServiceDataSource dataSource = resourceProvider.getDataSource(dataSourceConfiguration);

        return new BasicNativeQueryService(dataSource);
    }
}
