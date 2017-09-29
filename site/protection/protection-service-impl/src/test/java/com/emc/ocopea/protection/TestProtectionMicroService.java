// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import com.emc.microservice.testing.MicroServiceTestHelper;
import com.emc.ocopea.crb.CopyMetaData;
import com.emc.ocopea.crb.CopyInstance;
import com.emc.ocopea.crb.CrbWebApi;
import com.emc.ocopea.crb.Info;
import com.emc.ocopea.crb.RepositoryInfo;
import com.emc.ocopea.crb.RepositoryInstance;
import com.emc.ocopea.crb.RepositoryStats;
import com.emc.ocopea.dsb.CopyServiceInstance;
import com.emc.ocopea.dsb.CopyServiceInstanceResponse;
import com.emc.ocopea.dsb.CreateServiceInstance;
import com.emc.ocopea.dsb.DsbInfo;
import com.emc.ocopea.dsb.DsbWebApi;
import com.emc.ocopea.dsb.ServiceInstance;
import com.emc.ocopea.dsb.ServiceInstanceDetails;
import com.emc.ocopea.site.AddCrToSiteCommandArgs;
import com.emc.ocopea.site.AddCustomArtifactRegistryToSiteCommandArgs;
import com.emc.ocopea.site.AddDockerArtifactRegistryToSiteCommandArgs;
import com.emc.ocopea.site.AddMavenArtifactRegistryToSiteCommandArgs;
import com.emc.ocopea.site.AppInstanceCopyStatisticsDTO;
import com.emc.ocopea.site.AppInstanceInfoDTO;
import com.emc.ocopea.site.AppInstanceStatisticsDTO;
import com.emc.ocopea.site.CreateAppCopyCommandArgs;
import com.emc.ocopea.site.DeployApplicationOnSiteCommandArgs;
import com.emc.ocopea.site.RegisterCrbToSiteCommandArgs;
import com.emc.ocopea.site.RegisterDsbToSiteCommandArgs;
import com.emc.ocopea.site.RegisterPsbToSiteCommandArgs;
import com.emc.ocopea.site.RemoveArtifactRegistryFromSiteCommandArgs;
import com.emc.ocopea.site.RemoveCrbFromSiteCommandArgs;
import com.emc.ocopea.site.RemoveDsbFromSiteCommandArgs;
import com.emc.ocopea.site.ServiceLogsWebSocketDTO;
import com.emc.ocopea.site.SiteArtifactRegistryInfoDTO;
import com.emc.ocopea.site.SiteCopyRepoInfoDTO;
import com.emc.ocopea.site.SiteInfoDto;
import com.emc.ocopea.site.SitePsbDetailedInfoDto;
import com.emc.ocopea.site.SitePsbInfoDto;
import com.emc.ocopea.site.SiteWebApi;
import com.emc.ocopea.site.StopAppOnSiteCommandArgs;
import com.emc.ocopea.site.SupportedServiceDto;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TestProtectionMicroService {

    private MicroServiceTestHelper testHelper;

    @Before
    public void init() throws IOException, SQLException {
        testHelper = new MicroServiceTestHelper(new ProtectionMicroService(),
                "protection-db" + System.nanoTime());
        testHelper.createOrUpgrdaeSchema(new ProtectionRepositorySchema("protection"));
        testHelper.startServiceInTestMode();
        testHelper.mockDependentServiceRestResource("site", SiteWebApi.class, new SiteMock());
        testHelper.mockDependentServiceRestResourceByUrl("http://crb1", CrbWebApi.class, new CopyRepoMock());
    }

    @After
    public void tearDown() {
        testHelper.stopTestMode();
    }

    @Test
    public void testCreatingSingleCopies() {

        // Mocking DSB Web API
        testHelper.mockDependentServiceRestResourceByUrl("http://dsb1", DsbWebApi.class, new MockDSBAPI());

        final UUID appInstanceId = UUID.randomUUID();

        UUID copyId = createCopy(appInstanceId);
        verifyCopiesState(copyId, ApplicationCopyState.created);
        verifyCopiesCount(appInstanceId, 1);

        copyId = createCopy(appInstanceId);
        verifyCopiesState(copyId, ApplicationCopyState.created);
        verifyCopiesCount(appInstanceId, 2);
    }

    @Test
    public void testCopyFailed() {

        // Mocking DSB Web API
        final MockDSBAPI mockDSB = new MockDSBAPI();
        mockDSB.setFailTakingCopy(true);

        testHelper.mockDependentServiceRestResourceByUrl("http://dsb1", DsbWebApi.class, mockDSB);

        final UUID appInstanceId = UUID.randomUUID();

        final UUID copyId = createCopy(appInstanceId);
        verifyCopiesState(copyId, ApplicationCopyState.failed);
        verifyCopiesCount(appInstanceId, 1);

    }

    @Test
    public void testProtectApplication() {
        testHelper.mockDependentServiceRestResourceByUrl("http://dsb1", DsbWebApi.class, new MockDSBAPI());
        UUID appInstanceId = UUID.randomUUID();
        getProtectionAPI().protectApplication(createProtectionApplicationInstanceInfoDTO(appInstanceId));

        // mock scheduler runs every 100ms regardless of the interval specified, so we expect several copies to be
        // created within 100-200ms
        int retries = 0;
        while (getProtectionAPI().listAppInstanceCopies(appInstanceId).size() < 3 && retries < 10) {
            retries += 1;
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // don't care
            }
        }
        Assert.assertTrue(getProtectionAPI().listAppInstanceCopies(appInstanceId).size() >= 3);
    }

    @Test
    public void testListAppInstanceCopies() {
        testHelper.mockDependentServiceRestResourceByUrl("http://dsb1", DsbWebApi.class, new MockDSBAPI());
        UUID appInstanceId = UUID.randomUUID();
        Set<UUID> copies = new HashSet<>(3);
        for (int i = 0; i < 3; i++) {
            copies.add(createCopy(appInstanceId));
        }
        Collection<ProtectionAppCopyDTO> retrievedCopies = getProtectionAPI().listAppInstanceCopies(appInstanceId);
        Assert.assertEquals(copies.size(), retrievedCopies.size());
        retrievedCopies.forEach(retrievedCopy -> Assert.assertTrue(copies.contains(retrievedCopy.getCopyId())));
    }

    @Test
    public void testListAppInstanceCopiesWithTimeInterval() {
        testHelper.mockDependentServiceRestResourceByUrl("http://dsb1", DsbWebApi.class, new MockDSBAPI());
        UUID appInstanceId = UUID.randomUUID();
        UUID copyId = createCopy(appInstanceId);

        ProtectionAppCopyDTO copy = getProtectionAPI().getCopy(copyId);
        long copyTime = copy.getTimeStamp().getTime();
        Assert.assertEquals(
                getProtectionAPI()
                        .listAppInstanceCopies(appInstanceId, copyTime - 10L, copyTime + 10L)
                        .size(),
                1);

        // irrelevant interval should not return copies
        Assert.assertTrue(getProtectionAPI().listAppInstanceCopies(appInstanceId, 0L, 0L).isEmpty());

        // interval should be inclusive of intervalStart
        Assert.assertEquals(
                getProtectionAPI()
                        .listAppInstanceCopies(appInstanceId, copyTime, copyTime + 10L)
                        .size(),
                1);

        // interval should be exclusive of intervalEnd
        Assert.assertEquals(
                getProtectionAPI()
                        .listAppInstanceCopies(appInstanceId, copyTime - 10L, copyTime)
                        .size(),
                0);

        // interval in the future should not return copies
        Assert.assertEquals(
                getProtectionAPI()
                        .listAppInstanceCopies(appInstanceId, copyTime + 10L, copyTime + 20L)
                        .size(),
                0);

        try {
            getProtectionAPI().listAppInstanceCopies(appInstanceId, copyTime + 10L, copyTime - 10L);
            Assert.fail("Should've thrown 400 Bad Request because intervalStart > intervalEnd");
        } catch (BadRequestException ex) {
            // test passed
        }
    }

    @Test(expected = NotFoundException.class)
    public void testGetNonExistentCopy() {
            getProtectionAPI().getCopy(UUID.randomUUID());
    }

    private void verifyCopiesState(UUID copyId, ApplicationCopyState expected) {
        Assert.assertEquals(expected, getProtectionAPI().getCopy(copyId).getState());
    }

    private void verifyCopiesCount(UUID appInstanceId, int expected) {
        final Collection<ProtectionAppCopyDTO> copies =
                getProtectionAPI().listAppInstanceCopies(appInstanceId);

        Assert.assertEquals(expected, copies.size());
    }

    private UUID createCopy(UUID appInstanceId) {
        return getProtectionAPI().createAppCopy(createProtectionApplicationInstanceInfoDTO(appInstanceId));
    }

    private ProtectApplicationInstanceInfoDTO createProtectionApplicationInstanceInfoDTO(UUID appInstanceId) {
        return new ProtectApplicationInstanceInfoDTO(
                Collections.singleton(
                        new ProtectApplicationInstanceInfoDTO.ProtectDataServiceBinding(
                                "dsbName",
                                "dsb1",
                                "http://dsb1",
                                "popo",
                                "serviceId1",
                                "shuki",
                                Collections.emptyMap())),
                1,
                appInstanceId.toString(),
                Collections.singletonList(
                        new ProtectApplicationInstanceInfoDTO.ProtectAppConfiguration(
                                "psb1",
                                "appSvc1",
                                "appImg1",
                                "java",
                                "1.0"
                        )));
    }

    private ProtectionWebResource getProtectionAPI() {
        return testHelper.getServiceResource(ProtectionWebResource.class);
    }

    private class MockDSBAPI implements DsbWebApi {

        private boolean failTakingCopy = false;

        public boolean isFailTakingCopy() {
            return failTakingCopy;
        }

        public void setFailTakingCopy(boolean failTakingCopy) {
            this.failTakingCopy = failTakingCopy;
        }

        @Override
        public DsbInfo getDSBInfo() {
            return null;
        }

        @Override
        public Response getDSBIcon() {
            return Response.noContent().build();
        }

        @Override
        public List<ServiceInstance> getServiceInstances() {
            return null;
        }

        @Override
        public CopyServiceInstanceResponse copyServiceInstance(@PathParam("instanceId") String instanceId,
                                                               CopyServiceInstance copyDetails) {
            if (failTakingCopy) {
                throw new InternalServerErrorException("Ha ha ha");
            }
            return new CopyServiceInstanceResponse(0, "yey", copyDetails.getCopyId());
        }

        @Override
        public ServiceInstance createServiceInstance(CreateServiceInstance serviceSettings) {
            return null;
        }

        @Override
        public ServiceInstance deleteServiceInstance(@PathParam("instanceId") String instanceId) {
            return null;
        }

        @Override
        public ServiceInstanceDetails getServiceInstance(@PathParam("instanceId") String instanceId) {
            return null;
        }
    }

    private static class SiteMock implements SiteWebApi {

        @Override
        public SiteInfoDto getSiteInfo() {
            return null;
        }

        @Override
        public Collection<AppInstanceInfoDTO> listAppInstanceInfo() {
            return null;
        }

        @Override
        public Collection<SitePsbInfoDto> listPsbs() {
            return Collections.emptyList();
        }

        @Override
        public Collection<SitePsbDetailedInfoDto> listPsbsDetailed() {
            return Collections.emptyList();
        }

        @Override
        public Collection<SiteArtifactRegistryInfoDTO> listArtifactRegistries() {
            return null;
        }

        @Override
        public Collection<String> listArtifactVersions(@PathParam("artifactRegistryName") String artifactRegistryName,
                                                       @PathParam("artifactId") String artifactId) {
            return null;
        }

        @Override
        public Collection<SiteCopyRepoInfoDTO> listCopyRepositories() {
            return Collections.singleton(new SiteCopyRepoInfoDTO("crb1", "http://crb1", "crbName", "shpanTest", "1.0"));
        }

        @Override
        public AppInstanceInfoDTO getAppInstanceInfo(@PathParam("appInstanceId") UUID appInstanceId) {
            return null;
        }

        @Override
        public List<ServiceLogsWebSocketDTO> getAppInstanceLogsWebSockets(@PathParam("appInstanceId") UUID appInstanceId) {
            return Collections.emptyList();
        }

        @Override
        public Collection<AppInstanceCopyStatisticsDTO> getCopyHistory(
                @PathParam("appInstanceId") UUID appInstanceId,
                @QueryParam("intervalStart") Long intervalStart,
                @QueryParam("intervalEnd") Long intervalEnd) {
            return null;
        }

        @Override
        public AppInstanceStatisticsDTO getAppInstanceStatistics(@PathParam("appInstanceId") UUID appInstanceId) {
            return null;
        }

        @Override
        public AppInstanceCopyStatisticsDTO getCopyMetadata(@PathParam("copyId") UUID copyId) {
            return null;
        }

        @Override
        public Response downloadCopy(@PathParam("copyRepoURN") String copyRepoURN, @PathParam("copyId") UUID copyId) {
            return null;
        }

        @Override
        public void addCr(AddCrToSiteCommandArgs addCrToSiteCommandArgs) {
        }

        @Override
        public void registerCrb(RegisterCrbToSiteCommandArgs registerCrbToSiteCommandArgs) {
        }

        @Override
        public void registerPsb(RegisterPsbToSiteCommandArgs registerPsbToSiteCommandArgs) {
        }

        @Override
        public SupportedServiceDto registerDsb(RegisterDsbToSiteCommandArgs args) {
            return null;
        }

        @Override
        public void addCustomRestArtifactRegistry(
                AddCustomArtifactRegistryToSiteCommandArgs addCustomArtifactRegistryToSiteCommandArgs) {}

        @Override
        public void addMavenArtifactRegistry(
                AddMavenArtifactRegistryToSiteCommandArgs addMavenArtifactRegistryToSiteCommandArgs) {}

        @Override
        public void addDockerArtifactRegistry(AddDockerArtifactRegistryToSiteCommandArgs addDockerArtifactRegistryToSiteCommandArgs) {
        }

        @Override
        public void removeArtifactRegistry(RemoveArtifactRegistryFromSiteCommandArgs
                removeArtifactRegistryFromSiteCommandArgs) {}

        @Override
        public void removeDsb(RemoveDsbFromSiteCommandArgs removeDsbFromSiteCommandArgs) {}

        @Override
        public void removeCrb(RemoveCrbFromSiteCommandArgs removeCrbFromSiteCommandArgs) {
        }

        @Override
        public void deployApplicationOnSite(DeployApplicationOnSiteCommandArgs deployAppManifest) {}

        @Override
        public void stageCopy(InputStream inputStream, @HeaderParam("crbUrn") String crbUrn,
                                  @HeaderParam("dsb") String dsb, @HeaderParam("copyTimestamp") Long copyTimestamp,
                                  @HeaderParam("facility") String facility, @HeaderParam("meta") String meta,
                                  @HeaderParam("copyId") UUID copyId) {}

        @Override
        public UUID createAppCopy(CreateAppCopyCommandArgs createAppCopyCommandArgs) {
            return null;
        }

        @Override
        public void stopApp(StopAppOnSiteCommandArgs stopAppOnSiteCommandArgs) {}
    }

    private static class CopyRepoMock implements CrbWebApi {

        @Override
        public Info getInfo() {
            return new Info("crbName", "1.0", "shpanRest", "foo");
        }

        @Override
        public String deleteCopy(@PathParam("copyId") String copyId) {
            return null;
        }

        @Override
        public String deleteRepository(@PathParam("repoId") String repoId) {
            return null;
        }

        @Override
        public List<CopyInstance> getCopyInstances(
                @QueryParam("repoIdInQuery") String repoIdInQuery,
                @QueryParam("pageNumber") Integer pageNumber,
                @QueryParam("pageSize") Integer pageSize) {
            return null;
        }

        @Override
        public CopyMetaData getCopyMetaData(@PathParam("copyId") String copyId) {
            return null;
        }

        @Override
        public RepositoryInfo getRepositoryInfo(@PathParam("repoId") String repoId) {
            return null;
        }

        @Override
        public List<RepositoryInstance> listRepositoryInstances() {
            return Collections.singletonList(new RepositoryInstance("reop-id", "foo://repo.url"));
        }

        @Override
        public RepositoryStats getRepositoryStats(@PathParam("repoId") String repoId) {
            return null;
        }

        @Override
        public CopyInstance storeCopyMetaData(@PathParam("copyId") String copyId, CopyMetaData copyMeta) {
            return null;
        }

        @Override
        public RepositoryInstance storeRepositoryInfo(
                @PathParam("repoId") String repoId, RepositoryInfo repoInfo) {
            return null;
        }

        @Override
        public RepositoryInstance updateRepositoryInfo(
                @PathParam("repoId") String repoId, RepositoryInfo repoInfo) {
            return null;
        }
    }
}
