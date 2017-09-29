// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.demo.dsb.shpanblob;

import com.emc.dpa.dev.DevBlobStoreConfiguration;
import com.emc.microservice.Context;
import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.microservice.blobstore.BlobStoreConfiguration;
import com.emc.microservice.registry.ServiceRegistryApi;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.microservice.resource.ResourceProviderManager;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.crb.CrbWebDataApi;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.dsb.CopyServiceInstance;
import com.emc.ocopea.dsb.DsbRestoreCopyInfo;
import org.apache.commons.io.input.CountingInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
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
public class ShpanBlobDSBSingleton implements ServiceLifecycle {
    private static final Logger log = LoggerFactory.getLogger(ShpanBlobDSBSingleton.class);
    private static final long EMPTY_BLOBSTORE_SIZE = 50L;
    private final Map<String, ShpanBlobInstance> serviceInstancesMap = new HashMap<>();
    private ResourceProvider resourceProvider;
    private WebAPIResolver webAPIResolver;

    @Override
    public void init(Context context) {
        log.info("Initialized shpanblob Singleton");
        //very platform specific, but it is what it is...
        resourceProvider = ResourceProviderManager.getResourceProvider();
        // should be part of ms lib
        webAPIResolver = context.getWebAPIResolver();
    }

    @Override
    public void shutDown() {
        resourceProvider = null;
        log.info("Shutting down shpanblob singleton");

    }

    public void create(String name, Map<String, String> dsbSettings) {
        create(name, false, dsbSettings);
    }

    public void create(final String name, boolean readonly, Map<String, String> dsbSettings) {
        checkExists(name);
        createDB(name, readonly, dsbSettings);
    }

    public boolean exists(String name) {
        return serviceInstancesMap.containsKey(name);
    }

    private void checkExists(String name) {
        if (exists(name)) {
            throw new IllegalArgumentException("ShpanBlob instance by name " + name + " already exists");
        }
    }

    private void createDB(final String name, boolean readonly, Map<String, String> dsbSettings) {
        DevBlobStoreConfiguration blobStoreConfig = new DevBlobStoreConfiguration(name);
        // Creating physical database
        //noinspection unchecked
        Objects.requireNonNull(
                resourceProvider.getBlobStore(blobStoreConfig, null),
                "failed instantiating underlying blobstore for " + name);

        // Registering in service registry
        resourceProvider.getServiceRegistryApi().registerBlobStore(name, blobStoreConfig);

        ShpanBlobInstance instance = new ShpanBlobInstance(name, null, new Date(), readonly, dsbSettings);
        instance.setSize(EMPTY_BLOBSTORE_SIZE);
        add(instance);
    }

    private void createDB(
            final String name,
            final InputStream copyStream,
            boolean readonly,
            Map<String, String> dsbSettings) {
        DevBlobStoreConfiguration blobStoreConfig = new DevBlobStoreConfiguration(name);

        // Creating physical database
        //noinspection unchecked
        final BlobStoreAPI blobStoreAPI = resourceProvider.getBlobStore(blobStoreConfig, null);
        long size = EMPTY_BLOBSTORE_SIZE;

        // Building from copy
        if (copyStream != null) {

            CountingInputStream cis = new CountingInputStream(copyStream);
            log.info("Importing copy stream to blobstore {}", name);
            try {
                new BlobStoreCopyParser(blobStoreAPI).parse(cis);
                size = cis.getByteCount();
            } catch (IOException e) {
                throw new IllegalStateException("Failed restoring blobstore " + name, e);
            }
        }

        // Registering in service registry
        resourceProvider.getServiceRegistryApi().registerBlobStore(name, blobStoreConfig);

        ShpanBlobInstance instance = new ShpanBlobInstance(name, null, new Date(), readonly, dsbSettings);
        instance.setSize(size);
        add(instance);
    }

    private void add(ShpanBlobInstance instance) {
        serviceInstancesMap.put(instance.getName(), instance);
    }

    public Collection<ShpanBlobInstance> list() {
        return new ArrayList<>(serviceInstancesMap.values());
    }

    public ShpanBlobInstance get(String name) {
        return serviceInstancesMap.get(name);
    }

    @NoJavadoc
    // TODO add javadoc
    public void createFromCopy(String serviceId, DsbRestoreCopyInfo restoreInfo) {
        checkExists(serviceId);
        //todo: this needs to test protocol and parse accordingly
        final String copyRepoUrl = restoreInfo.getCopyRepoCredentials().get("url");
        if (copyRepoUrl == null) {
            throw new BadRequestException("Failed parsing copy repo credentials, missing \"url\"");
        }

        Response response;
        try {
            response = webAPIResolver.getWebAPI(copyRepoUrl, CrbWebDataApi.class).retrieveCopy(restoreInfo.getCopyId());
        } catch (ClientErrorException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalServerErrorException("Failed doanloading copy from copy repo " + copyRepoUrl, ex);
        }
        try {
            InputStream copyStream;
            try {
                copyStream = response.readEntity(InputStream.class);
            } catch (Exception ex) {
                throw new InternalServerErrorException("Failed reading stream form artifact", ex);
            }
            createDB(serviceId, copyStream, false, restoreInfo.getCopyRepoCredentials());
        } finally {
            response.close();
        }

    }

    private void createCopyAndStream(
            OutputStream out,
            final String copyId,
            final String serviceName,
            final String copyPluginName) {

        // Slow down dude..
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            //Whatever Dude.. just sleeping here...
        }

        BlobStoreConfiguration blobStoreConfiguration =
                Objects.requireNonNull(
                        resourceProvider.getServiceRegistryApi().getBlobStoreConfiguration(
                                resourceProvider.getBlobStoreConfigurationClass(), serviceName),
                        "Failed locating datasource " + serviceName + " in service registry");

        log.info("Creating copy for {}/{} with id {}", serviceName, copyId);
        @SuppressWarnings("unchecked")
        BlobStoreAPI blobStoreAPI = resourceProvider.getBlobStore(blobStoreConfiguration, null);

        long copySize = BlobstoreCopyUtil.doBlobstoreCopy(out, ServiceRegistryApi.SERVICE_TYPE_BLOBSTORE,
                serviceName, copyPluginName, copyId, blobStoreAPI);
        if (copySize == 0) {
            copySize = 50;
        }
        serviceInstancesMap.get(serviceName).setSize(copySize);

    }

    @NoJavadoc
    // TODO add javadoc
    public void copy(final String serviceName, CopyServiceInstance copyDetails) {

        try {
            final String restCopyRepoUrl = copyDetails.getCopyRepoCredentials().get("url");
            if (restCopyRepoUrl == null) {
                throw new BadRequestException("Missing url in rest copy repo credentials");
            }
            final String restCopyRepoId = copyDetails.getCopyRepoCredentials().get("repoId");
            if (restCopyRepoId == null) {
                throw new BadRequestException("Missing repoId in rest copy repo credentials");
            }
            CrbWebDataApi copyRepoAPI = webAPIResolver.getWebAPI(restCopyRepoUrl, CrbWebDataApi.class);

            PipedInputStream in = new PipedInputStream();
            final PipedOutputStream out = new PipedOutputStream(in);

            Thread thread = new Thread(
                    () -> createCopyAndStream(out, copyDetails.getCopyId(), serviceName, copyDetails.getCopyType()));
            thread.start();

            //todo:amit: shouldn't be two fields - desired time v.s. actual time?
            Long copyTime1 = copyDetails.getCopyTime();

            copyRepoAPI.createCopyInRepo(restCopyRepoId, copyDetails.getCopyId(), in);
            thread.join();
        } catch (Exception ex) {
            //todo:amit:proper error handling
            throw new IllegalStateException(ex);
        }
    }

    public void delete(String instanceId) {
        serviceInstancesMap.remove(instanceId);
    }

}
