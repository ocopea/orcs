// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.copy;

import com.emc.microservice.Context;
import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.microservice.serialization.SerializationManager;
import com.emc.ocopea.crb.CopyInstance;
import com.emc.ocopea.crb.CopyMetaData;
import com.emc.ocopea.crb.CrbWebApi;
import com.emc.ocopea.crb.Info;
import com.emc.ocopea.crb.RepositoryInfo;
import com.emc.ocopea.crb.RepositoryInstance;
import com.emc.ocopea.crb.RepositoryStats;
import com.emc.ocopea.crb.SupportedProtocol;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ShpanCopyRepositoryResource implements CrbWebApi {

    static final String META_NAMESPACE = "meta";
    static final String DATA_NAMESPACE = "data";
    static final String REPOSITORY_NAMESPACE = "repo";

    static final String SHPAN_PROTOCOL = "shpanRest";
    static final String SIZE_INFO_KEY = "size";
    static final String SHPAN_VERSION = "1.0";

    private Info crbInfo;

    private BlobStoreAPI copyStore;

    @javax.ws.rs.core.Context
    private UriInfo uriInfo;

    @NoJavadoc
    @javax.ws.rs.core.Context
    public void setApplication(Application application) {
        Context context = ((MicroServiceApplication) application).getMicroServiceContext();
        MicroService serviceDescriptor = context.getServiceDescriptor();
        crbInfo = new Info(
                "Postgres Object",
                Integer.toString(serviceDescriptor.getVersion()),
                "foo-build",
                "referenceImplementation");
        copyStore = context.getBlobStoreManager().getManagedResourceByName("copy-store").getBlobStoreAPI();
        SerializationManager serializationManager = context.getSerializationManager();
        if (!serializationManager.getSupportedWriters().contains(ShpanRepositoryInfo.class)) {
            serializationManager.registerJackson(ShpanRepositoryInfo.class);
        }
        if (!serializationManager.getSupportedWriters().contains(CopyMetaData.class)) {
            serializationManager.registerJackson(CopyMetaData.class);
        }
    }

    @Override
    public String deleteCopy(@PathParam("copyId") String copyId) {
        if (copyStore.isExists(META_NAMESPACE, copyId)) {
            CopyMetaData copyMetaData = copyStore.readBlob(META_NAMESPACE, copyId, CopyMetaData.class);
            if (copyMetaData.getProtocol().equals(SHPAN_PROTOCOL) &&
                    copyMetaData.getProtocolVersion().equals(SHPAN_VERSION)) {
                String repoId = copyMetaData.getRepoId();
                ShpanRepositoryInfo repositoryInfo = copyStore.readBlob(
                        REPOSITORY_NAMESPACE,
                        repoId,
                        ShpanRepositoryInfo.class);
                repositoryInfo.addByteCount(Long.parseLong(copyMetaData.getCopyAdditionalInfo().get(SIZE_INFO_KEY)));
                repositoryInfo.stats.setNumberOfCopiesStored(repositoryInfo.stats.getNumberOfCopiesStored() - 1);
                copyStore.update(
                        REPOSITORY_NAMESPACE,
                        repoId,
                        Collections.emptyMap(),
                        ShpanRepositoryInfo.class,
                        repositoryInfo);
            }
            copyStore.delete(META_NAMESPACE, copyId);
        }
        if (copyStore.isExists(DATA_NAMESPACE, copyId)) {
            copyStore.delete(DATA_NAMESPACE, copyId);
        } else {
            throw new NotFoundException("copy " + copyId + "does not exists");
        }
        //TODO: what should we return?
        return copyId;
    }

    @Override
    public String deleteRepository(@PathParam("repoId") String repoId) {
        if (copyStore.isExists(REPOSITORY_NAMESPACE, repoId)) {
            copyStore.delete(REPOSITORY_NAMESPACE, repoId);
        } else {
            throw new NotFoundException("repository " + repoId + "does not exists");
        }
        //TODO: what should we return?
        return repoId;

    }

    @Override
    public List<CopyInstance> getCopyInstances(
            @QueryParam("repoIdInQuery") String repoIdInQuery,
            @QueryParam("pageNumber") Integer pageNumber,
            @QueryParam("pageSize") Integer pageSize) {
        if (copyStore.isExists(REPOSITORY_NAMESPACE, repoIdInQuery)) {
            List<String> allCopies = copyStore
                    .readBlob(REPOSITORY_NAMESPACE, repoIdInQuery, ShpanRepositoryInfo.class)
                    .copies;
            if (pageSize != null) {
                int fromIndex = pageSize * pageNumber;
                if (fromIndex > allCopies.size()) {
                    fromIndex = allCopies.size();
                }
                int toIndex = fromIndex + pageSize;
                if (toIndex > allCopies.size()) {
                    toIndex = allCopies.size();
                }
                allCopies = allCopies.subList(fromIndex, toIndex);
            }

            return allCopies
                    .stream()
                    .map(s -> copyStore.readBlob(META_NAMESPACE, s, CopyInstance.class))
                    .collect(Collectors.toList());
        } else {
            throw new NotFoundException("repository " + repoIdInQuery + " does not exist");
        }
    }

    @Override
    public RepositoryInfo getRepositoryInfo(@PathParam("repoId") String repoId) {
        if (copyStore.isExists(REPOSITORY_NAMESPACE, repoId)) {
            return copyStore.readBlob(REPOSITORY_NAMESPACE, repoId, ShpanRepositoryInfo.class).info;
        } else {
            throw new NotFoundException("repository " + repoId + " does not exist");
        }
    }

    @Override
    public List<RepositoryInstance> listRepositoryInstances() {
        List<RepositoryInstance> repos = copyStore
                .list()
                .stream()
                .filter(link -> link.getNamespace().equals(REPOSITORY_NAMESPACE))
                .map(link ->
                        copyStore.readBlob(REPOSITORY_NAMESPACE, link.getKey(), ShpanRepositoryInfo.class).instance)
                .collect(Collectors.toList());

        // In shpan repo we are not smart, there should always be at least one default repo
        if (repos.isEmpty()) {
            storeRepositoryInfo(
                    "default-shpan-repo",
                    new RepositoryInfo(
                            "default-shpan-repo",
                            "Default",
                            Collections.singletonList(new SupportedProtocol("shpanRest", "1.0")),
                            Collections.emptyMap()
                    ));

            repos = listRepositoryInstances();
        }
        return repos;
    }

    @Override
    public RepositoryStats getRepositoryStats(@PathParam("repoId") String repoId) {
        if (copyStore.isExists(REPOSITORY_NAMESPACE, repoId)) {
            return copyStore.readBlob(REPOSITORY_NAMESPACE, repoId, ShpanRepositoryInfo.class).stats;
        } else {
            throw new NotFoundException("repository " + repoId + " does not exist");
        }
    }

    @Override
    public CopyInstance storeCopyMetaData(@PathParam("copyId") String copyId, CopyMetaData copyMeta) {
        copyStore.create(
                META_NAMESPACE,
                copyId,
                Collections.emptyMap(),
                CopyMetaData.class,
                copyMeta);

        return new CopyInstance(copyMeta.getCopyDataURL());

    }

    @Override
    public RepositoryInstance storeRepositoryInfo(
            @PathParam("repoId") String repoId, RepositoryInfo repoInfo) {
        if (copyStore.isExists(REPOSITORY_NAMESPACE, repoId)) {
            throw new ClientErrorException("repository " + repoId + " already exists", Response.Status.CONFLICT);
        }
        ShpanRepositoryInfo repo = new ShpanRepositoryInfo();
        repo.stats = new RepositoryStats(Long.MAX_VALUE, 0L, Long.MAX_VALUE, 0);
        // Assuming this method and getRepositoryInfo use the same path
        repo.instance = new RepositoryInstance(repoId, uriInfo.getAbsolutePath().toString());
        repo.copies = new ArrayList<>();
        repo.info = repoInfo;
        copyStore.create(
                REPOSITORY_NAMESPACE,
                repoId,
                Collections.emptyMap(),
                ShpanRepositoryInfo.class,
                repo);
        return repo.instance;
    }

    @Override
    public RepositoryInstance updateRepositoryInfo(
            @PathParam("repoId") String repoId, RepositoryInfo repoInfo) {
        if (!copyStore.isExists(REPOSITORY_NAMESPACE, repoId)) {
            throw new NotFoundException("repository " + repoId + " does not exist");
        }
        ShpanRepositoryInfo repo = copyStore.readBlob(REPOSITORY_NAMESPACE, repoId, ShpanRepositoryInfo.class);
        repo.info = repoInfo;
        copyStore.update(
                REPOSITORY_NAMESPACE,
                repoId,
                Collections.emptyMap(),
                ShpanRepositoryInfo.class,
                repo);
        return repo.instance;

    }

    @Override
    public CopyMetaData getCopyMetaData(@PathParam("copyId") String copyId) {
        if (copyStore.isExists(META_NAMESPACE, copyId)) {
            return copyStore.readBlob(META_NAMESPACE, copyId, CopyMetaData.class);
        } else {
            throw new NotFoundException("copy " + copyId + " does not exist");
        }
    }

    @Override
    public Info getInfo() {
        return crbInfo;
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    static class ShpanRepositoryInfo {
        List<String> copies;
        RepositoryInfo info;
        RepositoryInstance instance;
        RepositoryStats stats;

        void addByteCount(long byteCount) {
            stats.setUsedCapacityInBytes(stats.getUsedCapacityInBytes() + byteCount);
            stats.setAvailableCapacityInBytes(stats.getAvailableCapacityInBytes() - byteCount);
        }
    }
}
