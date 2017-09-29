// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import com.emc.microservice.registry.RegistryClientDescriptor;
import com.emc.microservice.testing.MicroServiceTestHelper;
import com.emc.ocopea.crb.CopyInstance;
import com.emc.ocopea.crb.CopyMetaData;
import com.emc.ocopea.crb.CrbWebApi;
import com.emc.ocopea.crb.Info;
import com.emc.ocopea.crb.RepositoryInfo;
import com.emc.ocopea.crb.RepositoryInstance;
import com.emc.ocopea.crb.RepositoryStats;
import com.emc.ocopea.dsb.BindingPort;
import com.emc.ocopea.dsb.CopyServiceInstance;
import com.emc.ocopea.dsb.CopyServiceInstanceResponse;
import com.emc.ocopea.dsb.CreateServiceInstance;
import com.emc.ocopea.dsb.DsbInfo;
import com.emc.ocopea.dsb.DsbPlan;
import com.emc.ocopea.dsb.DsbSupportedCopyProtocol;
import com.emc.ocopea.dsb.DsbSupportedProtocol;
import com.emc.ocopea.dsb.DsbWebApi;
import com.emc.ocopea.dsb.ServiceInstance;
import com.emc.ocopea.dsb.ServiceInstanceDetails;
import com.emc.ocopea.protection.ProtectApplicationInstanceInfoDTO;
import com.emc.ocopea.protection.ProtectionAppCopyDTO;
import com.emc.ocopea.protection.ProtectionWebAPI;
import com.emc.ocopea.psb.DeployAppServiceManifestDTO;
import com.emc.ocopea.psb.DeployAppServiceResponseDTO;
import com.emc.ocopea.psb.PSBAppServiceInstanceDTO;
import com.emc.ocopea.psb.PSBAppServiceStatusEnumDTO;
import com.emc.ocopea.psb.PSBInfoDTO;
import com.emc.ocopea.psb.PSBLogsWebSocketDTO;
import com.emc.ocopea.psb.PSBSpaceDTO;
import com.emc.ocopea.psb.PSBWebAPI;
import com.emc.ocopea.site.app.DeployedApplicationState;
import com.emc.ocopea.site.artifact.SiteArtifactRegistry;
import com.emc.ocopea.site.repository.SiteRepositorySchema;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TestSiteMicroService {

    private MicroServiceTestHelper testHelper;
    private static final String DSB_URN = "mock-dsb";
    private static final String DSB_URL = "http://mock-dsb-url";
    private static final String PSB_URN = "mock-psb";
    private static final String PSB_URL = "http://mock-psb";
    private static final String CRB_URN = "mock-copy-repo";
    private static final String CRB_URL = "http://mock-copy-repo";

    @Before
    public void init() throws IOException, SQLException {
        testHelper = new MicroServiceTestHelper(new SiteMicroService(), "siteDB" + System.nanoTime());

        // Setting the site property as site-name is required
        System.setProperty("site_site-name", "testing site");

        // Setting the timeout to be short
        System.setProperty("site_deploy-app-timeout-in-seconds", "4");

        // Creating the site persistence schema
        testHelper.createOrUpgrdaeSchema(new SiteRepositorySchema("site"));

        testHelper.getResourceProvider().getServiceRegistryApi().registerExternalResource(
                RegistryClientDescriptor.REGISTRY_CLIENT_RESOURCE_NAME, Collections.emptyMap());

        // Starting the site service
        testHelper.startServiceInTestMode();
    }

    @After
    public void tearDown() {
        testHelper.stopTestMode();
    }

    @Test
    public void testSiteInfo() throws IOException {
        final SiteInfoDto siteInfo = getSiteAPI().getSiteInfo();
        System.out.println(siteInfo);
        Assert.assertNotNull("Site info must not be null", siteInfo);
        Assert.assertEquals("testing site", siteInfo.getName());
    }

    @Test
    public void testEmptyAppInstances() throws IOException {
        final Collection<AppInstanceInfoDTO> appInstances = getSiteAPI().listAppInstanceInfo();
        System.out.println(appInstances);
        Assert.assertNotNull(appInstances);
        Assert.assertTrue(appInstances.isEmpty());
    }

    @Test
    public void testRegisterInvalidDsb() throws IOException {
        final DsbCatalogResource serviceResource = getDSBCatalogAPI();
        final Collection<SupportedServiceDto> catalog = serviceResource.getCatalog();
        Assert.assertTrue(catalog.isEmpty());
        try {
            getSiteAPI().registerDsb(new RegisterDsbToSiteCommandArgs("bad-urn", "http://go.test.yourself"));
            Assert.fail("Should miserably fail");
        } catch (InternalServerErrorException e) {
            printResponseText(e.getResponse());
        }
    }

    @Test
    public void testAddRemoveDSB() throws IOException {

        final DsbCatalogResource serviceResource = getDSBCatalogAPI();
        Collection<SupportedServiceDto> catalog = serviceResource.getCatalog();
        Assert.assertTrue(catalog.isEmpty());

        addDsbMockToSite();
        catalog = serviceResource.getCatalog();
        Assert.assertEquals(1, catalog.size());

        // verify idempotency
        addDsbMockToSite();
        catalog = serviceResource.getCatalog();
        Assert.assertEquals(1, catalog.size());

        // verify remove works
        getSiteAPI().removeDsb(new RemoveDsbFromSiteCommandArgs(DSB_URN));
        catalog = serviceResource.getCatalog();
        Assert.assertEquals(0, catalog.size());

        // idempotent service should allow removing non-existent entries
        getSiteAPI().removeDsb(new RemoveDsbFromSiteCommandArgs(DSB_URN));
        catalog = serviceResource.getCatalog();
        Assert.assertEquals(0, catalog.size());
    }

    @Test
    public void testIllegalArgs() throws Exception {
        expectBadRequestException(() -> getSiteAPI().registerDsb(new RegisterDsbToSiteCommandArgs(null, null)));
        expectBadRequestException(() -> getSiteAPI().registerPsb(new RegisterPsbToSiteCommandArgs(null, null)));
        expectBadRequestException(() -> getSiteAPI().registerPsb(new RegisterPsbToSiteCommandArgs(null, null)));
        expectBadRequestException(() -> getSiteAPI().removeDsb(new RemoveDsbFromSiteCommandArgs(null)));

        expectBadRequestException(() ->
                getSiteAPI().addCustomRestArtifactRegistry(new AddCustomArtifactRegistryToSiteCommandArgs("a", null)));
        expectBadRequestException(() ->
                getSiteAPI().addCustomRestArtifactRegistry(new AddCustomArtifactRegistryToSiteCommandArgs(null, "b")));
        expectBadRequestException(() ->
                getSiteAPI().addCustomRestArtifactRegistry(new AddCustomArtifactRegistryToSiteCommandArgs("c", "")));
        expectBadRequestException(() ->
                getSiteAPI().addCustomRestArtifactRegistry(new AddCustomArtifactRegistryToSiteCommandArgs("", "d")));

        expectBadRequestException(() -> getSiteAPI().addMavenArtifactRegistry(
                new AddMavenArtifactRegistryToSiteCommandArgs("stam", null, null, null)));
        expectBadRequestException(() -> getSiteAPI().addMavenArtifactRegistry(
                new AddMavenArtifactRegistryToSiteCommandArgs(null, "stam", null, null)));

        // TODO add calls to more endpoints
        // TODO need to test each argument separately when endpoint take multiple arguments
    }

    @Test
    public void testAddCrb() throws Exception {
        mockCrb();
        SiteResource siteApi = getSiteAPI();

        Assert.assertEquals(0, siteApi.listCopyRepositories().size());
        getSiteAPI().registerCrb(new RegisterCrbToSiteCommandArgs(CRB_URN, CRB_URL));
        Assert.assertEquals(1, siteApi.listCopyRepositories().size());

        // verify idempotency
        getSiteAPI().registerCrb(new RegisterCrbToSiteCommandArgs(CRB_URN, CRB_URL));
        Assert.assertEquals(1, siteApi.listCopyRepositories().size());
    }

    @Test(expected = InternalServerErrorException.class)
    public void testAddNonExistentCrb() throws Exception {
        getSiteAPI().registerCrb(new RegisterCrbToSiteCommandArgs(CRB_URN, CRB_URL));
    }

    @Test
    public void testDeployAppWithPSBNotSetup() throws IOException {

        // Mocking dsb and copy repo
        addDsbMockToSite();
        mockCrb();

        final SiteWebApi siteWebApi = getSiteAPI();

        UUID appInstanceId = UUID.randomUUID();

        boolean failed = false;
        try {
            siteWebApi.deployApplicationOnSite(createDeployAppArgs(appInstanceId, "test-instance"));
        } catch (BadRequestException ex) {
            failed = true;
            // Should fail finding psb type
        }

        Assert.assertTrue("Should fail", failed);
    }

    @Test
    public void testDeployAppWithPsbError() throws IOException, InterruptedException {

        // Mocking dsb and copy repo
        addDsbMockToSite();
        mockCrb();

        // Mocking psb with error
        mockPsb(false);

        mockArtifactRegistry();

        final SiteWebApi siteWebApi = getSiteAPI();

        UUID appInstanceId = UUID.randomUUID();
        siteWebApi.deployApplicationOnSite(createDeployAppArgs(appInstanceId, "test-instance"));

        waitForIt(5, 100, () -> {
            final AppInstanceInfoDTO appInstanceInfo = siteWebApi.getAppInstanceInfo(appInstanceId);
            Assert.assertEquals(DeployedApplicationState.error, appInstanceInfo.getState());
        });
    }

    @Test
    public void testDeployAppWithPsbTimeOut() throws IOException, InterruptedException {
        // Mocking dsb and copy repo
        addDsbMockToSite();
        mockCrb();

        // Mocking psb with error
        mockPsbToRunForever();

        mockArtifactRegistry();

        final SiteWebApi siteWebApi = getSiteAPI();

        UUID appInstanceId = UUID.randomUUID();
        siteWebApi.deployApplicationOnSite(createDeployAppArgs(appInstanceId, "test-instance"));

        waitForIt(15, 100, () -> {
            final AppInstanceInfoDTO appInstanceInfo = siteWebApi.getAppInstanceInfo(appInstanceId);
            Assert.assertEquals(DeployedApplicationState.error, appInstanceInfo.getState());
        });
    }

    @Test
    public void testDeployApp() throws IOException, InterruptedException {
        runApp("test-instance");
    }

    @Test
    public void testStopApp() throws IOException, InterruptedException {

        final UUID appInstanceId = runApp("funky-app");
        getSiteAPI().stopApp(new StopAppOnSiteCommandArgs(appInstanceId));

        final AppInstanceInfoDTO appInstanceInfo = getSiteAPI().getAppInstanceInfo(appInstanceId);

        Assert.assertEquals(DeployedApplicationState.stopped, appInstanceInfo.getState());

    }

    @Test
    public void testDeployAppWithNoDSBs() throws IOException, InterruptedException {
        mockProtection();
        mockArtifactRegistry();
        mockPsb(true);

        final SiteWebApi siteWebApi = getSiteAPI();

        UUID appInstanceId = UUID.randomUUID();

        final Map<String, DeployApplicationOnSiteCommandArgs.DeployAppServiceOnSiteManifestDTO> appServiceTemplates =
                new HashMap<>();
        appServiceTemplates.put("app-svc1", new DeployApplicationOnSiteCommandArgs.DeployAppServiceOnSiteManifestDTO(
                "app-svc1",
                "mock",
                "ShpanSpace",
                "artifactRegistry1",
                "image-a",
                "java",
                "1",
                Collections.emptyMap(),
                Collections.emptyMap(),
                "/login",
                Collections.singleton(80),
                80,
                Collections.emptySet()));
        siteWebApi.deployApplicationOnSite(
                new DeployApplicationOnSiteCommandArgs(
                        appInstanceId,
                        "test-instance",
                        "test-template",
                        "1",
                        "app-svc1",
                        appServiceTemplates,
                        Collections.emptyMap(),
                        Collections.singleton(new DeployApplicationOnSiteCommandArgs
                                .DeployAppPolicyInfoDTO("protection", "foo-policy", Collections.emptyMap()))
                ));

        AppInstanceInfoDTO appInstanceInfo = siteWebApi.getAppInstanceInfo(appInstanceId);
        Assert.assertNotNull(appInstanceInfo);
        Assert.assertEquals(appInstanceId, appInstanceInfo.getId());
        Assert.assertEquals("test-instance", appInstanceInfo.getName());

        System.out.printf(appInstanceInfo.toString());

        // Bad, but we do have a  timer, need to mock  timers for test to be sync...
        Thread.sleep(1000L);
        appInstanceInfo = siteWebApi.getAppInstanceInfo(appInstanceId);

        Assert.assertEquals(DeployedApplicationState.running, appInstanceInfo.getState());
    }

    @Test
    public void testAppInstanceNotFound() throws IOException {
        final SiteWebApi siteWebApi = getSiteAPI();
        try {
            siteWebApi.getAppInstanceInfo(UUID.randomUUID());
            Assert.fail("should throw an exception");
        } catch (NotFoundException ex) {
            // good boy
        }
    }

    @Test
    public void testDeployAppFailDSBCreateError() throws IOException {

        // Mocking dsb and copy repo
        final DsbMock dsbMock = addDsbMockToSite();

        // Mocking psb
        mockPsb(true);
        mockArtifactRegistry();

        // Making create throw an exception
        dsbMock.setCreateException(true);

        final SiteWebApi siteWebApi = getSiteAPI();

        UUID appInstanceId = UUID.randomUUID();
        siteWebApi.deployApplicationOnSite(createDeployAppArgs(
                appInstanceId,
                "test-instance"));

        final AppInstanceInfoDTO appInstanceInfo = siteWebApi.getAppInstanceInfo(appInstanceId);
        Assert.assertEquals(DeployedApplicationState.error, appInstanceInfo.getState());

    }

    @Test
    public void testDeployAppFailInvalidDsbUrn() throws IOException {

        // Mocking dsb and copy repo
        addDsbMockToSite();
        mockCrb();

        // Mocking psb
        mockPsb(true);
        mockArtifactRegistry();

        final SiteWebApi siteWebApi = getSiteAPI();

        UUID appInstanceId = UUID.randomUUID();
        try {
            siteWebApi.deployApplicationOnSite(createDeployAppArgsWithInvalidDSB(
                    appInstanceId,
                    "test-instance"));
        } catch (BadRequestException ex) {
            // Should throw an exception... good for us
        }

        try {
            siteWebApi.getAppInstanceInfo(appInstanceId);
            Assert.fail("Should throw an exception and not be here..");
        } catch (Exception ex) {
            Assert.assertTrue(WebApplicationException.class.isAssignableFrom(ex.getClass()));
            Assert.assertEquals(404, ((WebApplicationException) ex).getResponse().getStatus());
        }
    }

    @Test
    public void testDeployAppFailDsbBindError() throws IOException, InterruptedException {

        // Mocking dsb and copy repo
        final DsbMock dsbMock = addDsbMockToSite();
        mockCrb();

        // Mocking psb
        mockPsb(true);
        mockArtifactRegistry();

        // Making create throw an exception
        dsbMock.setBindException(true);

        final SiteWebApi siteWebApi = getSiteAPI();

        UUID appInstanceId = UUID.randomUUID();
        siteWebApi.deployApplicationOnSite(createDeployAppArgs(
                appInstanceId,
                "test-instance"));

        waitForIt(5, 100, () -> {
            final AppInstanceInfoDTO appInstanceInfo = siteWebApi.getAppInstanceInfo(appInstanceId);
            Assert.assertEquals(DeployedApplicationState.error, appInstanceInfo.getState());
        });

    }

    @Test(expected = NotFoundException.class)
    public void testGetNonExistentAppInstanceLogsWebSockets() throws Exception {
        getSiteAPI().getAppInstanceLogsWebSockets(UUID.randomUUID());
    }

    @Test
    public void testGetAppInstanceLogsWebSockets() throws Exception {
        UUID appIntanceId = runApp("test-instance");
        List<ServiceLogsWebSocketDTO> sockets = getSiteAPI().getAppInstanceLogsWebSockets(appIntanceId);
        Assert.assertTrue(sockets.size() > 0);

        sockets.forEach(socket -> {
            Assert.assertFalse(socket.getAddress().isEmpty());
            Assert.assertFalse(socket.getTags().isEmpty());
            Assert.assertEquals("json", socket.getSerialization());
        });
    }

    @Test
    public void testGetAppInstanceStatistics() throws Exception {
        UUID id = runApp("test-instance");
        AppInstanceStatisticsDTO statistics = getSiteAPI().getAppInstanceStatistics(id);
        Assert.assertEquals(statistics.getAppCopies().size(), 0);
        Assert.assertEquals(statistics.getAppInstanceId(), id);

        UUID copyId = getSiteAPI().createAppCopy(new CreateAppCopyCommandArgs(id));
        statistics = getSiteAPI().getAppInstanceStatistics(id);
        Assert.assertEquals(statistics.getAppInstanceId(), id);
    }

    @Test
    public void testListPsbs() throws Exception {
        Collection<SitePsbInfoDto> psbs = getSiteAPI().listPsbs();
        Assert.assertEquals(0, psbs.size());
        Collection<SitePsbDetailedInfoDto> detailedPsbs = getSiteAPI().listPsbsDetailed();
        Assert.assertEquals(0, detailedPsbs.size());

        mockPsb(true);
        psbs = getSiteAPI().listPsbs();
        Assert.assertEquals(1, psbs.size());
        Assert.assertEquals(psbs.iterator().next().getUrn(), PSB_URN);

        detailedPsbs = getSiteAPI().listPsbsDetailed();
        Assert.assertEquals(1, detailedPsbs.size());
        detailedPsbs = getSiteAPI().listPsbsDetailed();
        Assert.assertEquals(detailedPsbs.iterator().next().getUrn(), PSB_URN);
    }

    @Test
    public void testAddMavenArtifactRegistry() throws Exception {
        String registryName = "Maven";
        String registryUrl = "stam_url";
        SiteResource siteAPI = getSiteAPI();
        siteAPI.addMavenArtifactRegistry(
                new AddMavenArtifactRegistryToSiteCommandArgs(registryName, registryUrl, null, null));

        Collection<SiteArtifactRegistryInfoDTO> registries = siteAPI.listArtifactRegistries();
        Assert.assertEquals(registries.size(), 1);
        SiteArtifactRegistryInfoDTO retrievedMavenRegistry = registries.iterator().next();
        Assert.assertEquals(retrievedMavenRegistry.getName(), registryName);
        Assert.assertEquals(
                retrievedMavenRegistry.getType(),
                SiteArtifactRegistry.ArtifactRegistryType.mavenRepository.toString());
        Assert.assertEquals(retrievedMavenRegistry.getParameters().get("url"), registryUrl);

        // verify idempotency
        siteAPI.addMavenArtifactRegistry(
                new AddMavenArtifactRegistryToSiteCommandArgs(registryName, registryUrl, null, null));
        Assert.assertEquals(siteAPI.listArtifactRegistries().size(), 1);

        // check removing
        siteAPI.removeArtifactRegistry(new RemoveArtifactRegistryFromSiteCommandArgs(registryName));
        Assert.assertEquals(siteAPI.listArtifactRegistries().size(), 0);
    }

    @Test
    public void testAddDockerArtifactRegistry() throws Exception {
        String registryName = "Docker hub";
        String registryUrl = "https://registry.hub.docker.com";
        SiteResource siteAPI = getSiteAPI();
        siteAPI.addDockerArtifactRegistry(
                new AddDockerArtifactRegistryToSiteCommandArgs(registryName, registryUrl, null, null));

        Collection<SiteArtifactRegistryInfoDTO> registries = siteAPI.listArtifactRegistries();
        Assert.assertEquals(registries.size(), 1);
        SiteArtifactRegistryInfoDTO retrievedDockerRegistry = registries.iterator().next();
        Assert.assertEquals(retrievedDockerRegistry.getName(), registryName);
        Assert.assertEquals(
                retrievedDockerRegistry.getType(),
                SiteArtifactRegistry.ArtifactRegistryType.dockerRegistry.toString());
        Assert.assertEquals(retrievedDockerRegistry.getParameters().get("url"), registryUrl);

        // verify idempotency
        siteAPI.addDockerArtifactRegistry(
                new AddDockerArtifactRegistryToSiteCommandArgs(registryName, registryUrl, null, null));
        Assert.assertEquals(siteAPI.listArtifactRegistries().size(), 1);

        // check removing
        siteAPI.removeArtifactRegistry(new RemoveArtifactRegistryFromSiteCommandArgs(registryName));
        Assert.assertEquals(siteAPI.listArtifactRegistries().size(), 0);
    }

    @Test
    public void testRemoveNonExistentArtifactRegistry() {
        getSiteAPI().removeArtifactRegistry(new RemoveArtifactRegistryFromSiteCommandArgs("my artifact registry"));
        // should not throw because it's idempotent
    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    // Helper methods

    private void printResponseText(Response response) {
        if (response.getEntity() != null) {
            System.out.println(response.getEntity().toString());
        }
    }

    private void mockPsb(boolean behave) {
        final PsbMock psbMock = new PsbMock();
        if (!behave) {
            psbMock.setErrorOnDeploy(true);
        }

        testHelper.mockDependentServiceRestResourceByUrl(PSB_URL, PSBWebAPI.class, psbMock);

        getSiteAPI().registerPsb(new RegisterPsbToSiteCommandArgs(PSB_URN, PSB_URL));
    }

    private void mockPsbToRunForever() {
        final PsbMock psbMock = new PsbMock();
        psbMock.setRunForever(true);
        testHelper.mockDependentServiceRestResourceByUrl(PSB_URL, PSBWebAPI.class, psbMock);

        getSiteAPI().registerPsb(new RegisterPsbToSiteCommandArgs(PSB_URN, PSB_URL));
    }

    private void mockProtection() {
        final ProtectionMock protectionMock = new ProtectionMock();

        testHelper.mockDependentServiceRestResource("protection", ProtectionWebAPI.class, protectionMock);
    }

    private DeployApplicationOnSiteCommandArgs createDeployAppArgs(
            UUID appInstanceId,
            String appInstanceName) {

        return new DeployApplicationOnSiteCommandArgs(
                appInstanceId,
                appInstanceName,
                "test-template",
                "1",
                "app-svc1",
                createAppServices(),
                createDataServices(DSB_URN),
                Collections.emptyList()
        );
    }

    private DeployApplicationOnSiteCommandArgs createDeployAppArgsWithInvalidDSB(
            UUID appInstanceId,
            String appInstanceName) {

        return new DeployApplicationOnSiteCommandArgs(appInstanceId,
                appInstanceName, "test-template", "1", "app-svc1",
                createAppServices(),
                createDataServices("non-existing-dsb"),
                Collections.emptyList()
        );
    }

    private Map<String, DeployApplicationOnSiteCommandArgs.DeployDataServiceOnSiteManifestDTO> createDataServices(
            String dsbUrn) {
        Map<String, DeployApplicationOnSiteCommandArgs.DeployDataServiceOnSiteManifestDTO> dataServices =
                new HashMap<>();

        dataServices.put(
                "data-svc1", new DeployApplicationOnSiteCommandArgs.DeployDataServiceOnSiteManifestDTO(
                        "data-svc1",
                        dsbUrn,
                        "default",
                        Collections.emptyMap(),
                        null));
        return dataServices;
    }

    private HashMap<String, DeployApplicationOnSiteCommandArgs.DeployAppServiceOnSiteManifestDTO> createAppServices() {
        final HashMap<String, DeployApplicationOnSiteCommandArgs.DeployAppServiceOnSiteManifestDTO> appServices =
                new HashMap<>();
        appServices.put("app-svc1", new DeployApplicationOnSiteCommandArgs.DeployAppServiceOnSiteManifestDTO(
                "app-svc1",
                "mock",
                "ShpanSpace",
                "artifactRegistry1",
                "image-a",
                "java",
                "1",
                Collections.emptyMap(),
                Collections.emptyMap(),
                "/login",
                Collections.singleton(80),
                80,
                Collections.singleton("data-svc1")
        ));
        return appServices;
    }

    private SiteResource getSiteAPI() {
        return testHelper.getServiceResource(SiteResource.class);
    }

    private DsbMock mockDsb() {
        DsbMock dsbMock = new DsbMock();
        testHelper.mockDependentServiceRestResourceByUrl(DSB_URL, DsbWebApi.class, dsbMock);
        return dsbMock;
    }

    private DsbMock addDsbMockToSite() {
        DsbMock mockDsb = mockDsb();
        getSiteAPI().registerDsb(new RegisterDsbToSiteCommandArgs(DSB_URN, DSB_URL));
        return mockDsb;
    }

    private MockCopyRepo mockCrb() {
        MockCopyRepo mock = new MockCopyRepo();
        testHelper.mockDependentServiceRestResourceByUrl(CRB_URL, CrbWebApi.class, mock);
        return mock;
    }

    private void mockArtifactRegistry() {
        getSiteAPI().addCustomRestArtifactRegistry(
                new AddCustomArtifactRegistryToSiteCommandArgs("artifactRegistry1", "http://tipesh.com"));
    }

    private static class DsbMock implements DsbWebApi {
        private boolean createException = false;
        private boolean bindException = false;
        Map<String, ServiceInstanceDetails> serviceInstances = new ConcurrentHashMap<>();

        private DsbMock() {
        }

        public void setCreateException(boolean createException) {
            this.createException = createException;
        }

        public void setBindException(boolean bindException) {
            this.bindException = bindException;
        }

        @Override
        public DsbInfo getDSBInfo() {
            return new DsbInfo(
                    "dsb",
                    "db",
                    "hahaha",
                    Collections.singletonList(
                            new DsbPlan(
                                    "default",
                                    "default",
                                    "default plan description",
                                    "10$",
                                    Collections.singletonList(new DsbSupportedProtocol(
                                            "shpanCP",
                                            "1",
                                            Collections.emptyMap())),
                                    Collections.singletonList(new DsbSupportedCopyProtocol(
                                            "shpanCP",
                                            "1")),
                                    Collections.emptyMap())
                    )
            );
        }

        @Override
        public Response getDSBIcon() {
            return Response.noContent().build();
        }

        @Override
        public ServiceInstanceDetails getServiceInstance(@PathParam("instanceId") String instanceId) {
            if (bindException) {
                throwExWithNoStackTrace("error binding");
            }

            final ServiceInstanceDetails serviceInstanceDetails = serviceInstances.get(instanceId);
            if (serviceInstanceDetails == null) {
                throw new NotFoundException();
            }
            return serviceInstanceDetails;
        }

        @Override
        public List<ServiceInstance> getServiceInstances() {
            return null;
        }

        @Override
        public ServiceInstance createServiceInstance(CreateServiceInstance serviceSettings) {
            if (createException) {
                throwExWithNoStackTrace("Ha ha take this one");
            }

            final Map<String, String> bindInfo = new HashMap<>();
            bindInfo.put("take", "this");
            serviceInstances.put(
                    serviceSettings.getInstanceId(),
                    new ServiceInstanceDetails(
                            serviceSettings.getInstanceId(),
                            bindInfo,
                            Collections.singletonList(new BindingPort("http", "test", 666)),
                            "EBS",
                            66L,
                            ServiceInstanceDetails.StateEnum.RUNNING));

            return new ServiceInstance(serviceSettings.getInstanceId());
        }

        @Override
        public ServiceInstance deleteServiceInstance(@PathParam("instanceId") String instanceId) {
            return new ServiceInstance(instanceId);
        }

        @Override
        public CopyServiceInstanceResponse copyServiceInstance(
                @PathParam("instanceId") String instanceId,
                CopyServiceInstance copyDetails) {
            return null;
        }
    }

    private static class MockCopyRepo implements CrbWebApi {
        @Override
        public Info getInfo() {
            return new Info("mocky", "fake", "1", "foo");
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
            return null;
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

    private class PsbMock implements PSBWebAPI {
        private boolean errorOnDeploy = false;
        private boolean runForever = false;
        private Map<String, Map<String, PSBAppServiceInstanceDTO>> deployedAppServiceInstances = new HashMap<>();

        @Override
        public PSBInfoDTO getPSBInfo() {
            return new PSBInfoDTO("psb-mock", "1", "mock", "hahahaha", 50);
        }

        public void setErrorOnDeploy(boolean errorOnDeploy) {
            this.errorOnDeploy = errorOnDeploy;
        }

        @Override
        public DeployAppServiceResponseDTO stopApp(
                @PathParam("space") String space,
                @PathParam("appServiceId") String appServiceId) {
            final Map<String, PSBAppServiceInstanceDTO> bySpace = deployedAppServiceInstances.get(space);
            if (bySpace != null) {
                bySpace.remove(appServiceId);
            }
            return new DeployAppServiceResponseDTO(0, "removed app");
        }

        @Override
        public List<PSBSpaceDTO> listSpaces() {
            return Collections.singletonList(new PSBSpaceDTO("ShpanSpace", Collections.emptyMap()));
        }

        @Override
        public PSBAppServiceInstanceDTO getAppService(
                @PathParam("space") String space,
                @PathParam("appServiceId") String appServiceId) {

            final Map<String, PSBAppServiceInstanceDTO> bySpace = deployedAppServiceInstances.get(space);
            if (bySpace != null) {
                return bySpace.get(appServiceId);
            }
            return null;
        }

        @Override
        public PSBLogsWebSocketDTO getAppServiceLogsWebSocket(
                @PathParam("space") String space, @PathParam("appServiceId") String appServiceId) {
            return null;
        }

        @Override
        public DeployAppServiceResponseDTO deployApplicationService(
                DeployAppServiceManifestDTO deployAppServiceManifestDTO) {
            if (errorOnDeploy) {
                final InternalServerErrorException internalServerErrorException =
                        new InternalServerErrorException("ha ha ha PSB Mock misbehaving");
                internalServerErrorException.setStackTrace(new StackTraceElement[0]);
                throw internalServerErrorException;
            }

            final PSBAppServiceStatusEnumDTO state = runForever ?
                    PSBAppServiceStatusEnumDTO.starting : PSBAppServiceStatusEnumDTO.running;

            deployedAppServiceInstances.computeIfAbsent(
                    deployAppServiceManifestDTO.getSpace(),
                    space -> new HashMap<>())
                    .put(
                            deployAppServiceManifestDTO.getAppServiceId(),
                            new PSBAppServiceInstanceDTO(
                                    deployAppServiceManifestDTO.getImageName(),
                                    state,
                                    "all good, trust me",
                                    1,
                                    Collections.emptyMap(),
                                    "http://go.to.test.com"));

            return new DeployAppServiceResponseDTO(0, "up and running - yeah right, I'm a  mock - hahaha");
        }

        public void setRunForever(boolean runForever) {
            this.runForever = runForever;
        }
    }

    private class ProtectionMock implements ProtectionWebAPI {
        @Override
        public void protectApplication(ProtectApplicationInstanceInfoDTO protectApplicationInstanceInfo) {
        }

        @Override
        public UUID createAppCopy(ProtectApplicationInstanceInfoDTO protectApplicationInstanceInfo) {
            return null;
        }

        @Override
        public Collection<ProtectionAppCopyDTO> listAppInstanceCopies(
                @PathParam("appInstanceId") UUID appInstanceId) {
            return Collections.emptyList();
        }

        @Override
        public Collection<ProtectionAppCopyDTO> listAppInstanceCopies(
                @PathParam("appInstanceId") UUID appInstanceId,
                @QueryParam("intervalStart") Long intervalStart,
                @QueryParam("intervalEnd") Long intervalEnd) {
            return Collections.emptyList();
        }

        @Override
        public ProtectionAppCopyDTO getCopy(@PathParam("copyId") UUID copyId) {
            throw new NotFoundException();
        }

    }

    private DsbCatalogResource getDSBCatalogAPI() {
        return testHelper.getServiceResource(DsbCatalogResource.class);
    }

    private void expectBadRequestException(Runnable runnable) {
        try {
            runnable.run();
            Assert.fail("should've thrown a BadRequestException");
        } catch (BadRequestException ex) {
            // pass
        }
    }

    private UUID runApp(String appInstanceName) throws InterruptedException {
        addDsbMockToSite();
        mockArtifactRegistry();
        mockPsb(true);
        mockProtection();

        final SiteWebApi siteWebApi = getSiteAPI();
        siteWebApi.registerDsb(new RegisterDsbToSiteCommandArgs(DSB_URN, DSB_URL));

        UUID appInstanceId = UUID.randomUUID();
        siteWebApi.deployApplicationOnSite(createDeployAppArgs(appInstanceId, appInstanceName));

        AppInstanceInfoDTO appInstanceInfo = siteWebApi.getAppInstanceInfo(appInstanceId);
        Assert.assertNotNull(appInstanceInfo);
        Assert.assertEquals(appInstanceId, appInstanceInfo.getId());
        Assert.assertEquals(appInstanceName, appInstanceInfo.getName());

        System.out.printf(appInstanceInfo.toString());

        // Bad, but we do have a  timer, need to mock  timers for test to be sync...
        Thread.sleep(1000L);
        appInstanceInfo = siteWebApi.getAppInstanceInfo(appInstanceId);

        Assert.assertEquals(DeployedApplicationState.running, appInstanceInfo.getState());

        return appInstanceId;
    }

    private static void throwExWithNoStackTrace(String message) {
        final RuntimeException exception = new InternalServerErrorException(message);
        exception.setStackTrace(new StackTraceElement[0]);
        throw exception;
    }

    private void waitForIt(int maxSecondsToWait, long millisBetween, Runnable r) throws
            InterruptedException {
        final long started = System.currentTimeMillis();
        while (true) {

            try {
                r.run();
                break;
            } catch (Throwable th) {
                if (System.currentTimeMillis() - started > maxSecondsToWait * 1000) {
                    throw th;
                }
            }
            Thread.sleep(millisBetween);
        }
    }

}
