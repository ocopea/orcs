// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.webapp;

import com.emc.microservice.testing.MicroServiceTestHelper;
import com.emc.ocopea.hub.application.ApplicationTemplateDTO;
import com.emc.ocopea.hub.application.CreateDeploymentPlanCommandArgs;
import com.emc.ocopea.hub.application.CreateSavedImageCommandArgs;
import com.emc.ocopea.hub.application.DeployAppCommandArgs;
import com.emc.ocopea.hub.application.DeploySavedImageCommandArgs;
import com.emc.ocopea.hub.application.HubAppInstanceConfigurationDTO;
import com.emc.ocopea.hub.application.HubAppInstanceDownStreamTreeDTO;
import com.emc.ocopea.hub.application.HubAppInstanceWithStateDTO;
import com.emc.ocopea.hub.application.HubWebApi;
import com.emc.ocopea.hub.application.RepurposeAppCommandArgs;
import com.emc.ocopea.hub.application.StopAppCommandArgs;
import com.emc.ocopea.hub.auth.AuthUser;
import com.emc.ocopea.hub.auth.HubWebAppSecurityContext;
import com.emc.ocopea.hub.site.AddSiteToHubCommandArgs;
import com.emc.ocopea.hub.site.SiteDto;
import com.emc.ocopea.hub.testdev.SavedImageDTO;
import com.emc.ocopea.site.AddCrToSiteCommandArgs;
import com.emc.ocopea.site.AddCustomArtifactRegistryToSiteCommandArgs;
import com.emc.ocopea.site.AddDockerArtifactRegistryToSiteCommandArgs;
import com.emc.ocopea.site.AddMavenArtifactRegistryToSiteCommandArgs;
import com.emc.ocopea.site.AppInstanceCopyStatisticsDTO;
import com.emc.ocopea.site.AppInstanceInfoDTO;
import com.emc.ocopea.site.AppInstanceStatisticsDTO;
import com.emc.ocopea.site.DeployApplicationOnSiteCommandArgs;
import com.emc.ocopea.site.DsbCatalogWebApi;
import com.emc.ocopea.site.RegisterCrbToSiteCommandArgs;
import com.emc.ocopea.site.RegisterDsbToSiteCommandArgs;
import com.emc.ocopea.site.RegisterPsbToSiteCommandArgs;
import com.emc.ocopea.site.RemoveArtifactRegistryFromSiteCommandArgs;
import com.emc.ocopea.site.RemoveCrbFromSiteCommandArgs;
import com.emc.ocopea.site.RemoveDsbFromSiteCommandArgs;
import com.emc.ocopea.site.ServiceInstanceInfo;
import com.emc.ocopea.site.ServiceLogsWebSocketDTO;
import com.emc.ocopea.site.SiteArtifactRegistryInfoDTO;
import com.emc.ocopea.site.SiteCopyRepoInfoDTO;
import com.emc.ocopea.site.SiteInfoDto;
import com.emc.ocopea.site.SiteLocationDTO;
import com.emc.ocopea.site.SitePsbDetailedInfoDto;
import com.emc.ocopea.site.SitePsbInfoDto;
import com.emc.ocopea.site.SiteWebApi;
import com.emc.ocopea.site.StopAppOnSiteCommandArgs;
import com.emc.ocopea.site.SupportedServiceDto;
import com.emc.ocopea.site.app.DeployedAppServiceState;
import com.emc.ocopea.site.app.DeployedApplicationState;
import com.emc.ocopea.site.app.DeployedDataServiceState;
import org.apache.commons.io.input.ReaderInputStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liebea on 3/22/17.
 * Drink responsibly
 */
public class TestHubWebAppMicroService {
    private MicroServiceTestHelper testHelper;
    private static final UUID SAVED_IMAGE_UUID = UUID.randomUUID();
    private static final UUID APP_COPY_UUID = UUID.randomUUID();
    private static final UUID DEPLOY_SAVED_IMAGE_APP_INSTANCE_ID = UUID.randomUUID();
    private static final UUID DEPLOY_APP_INSTANCE_ID = UUID.randomUUID();
    private static final UUID REPURPOSE_APP_INSTANCE_ID = UUID.randomUUID();
    private static final UUID APP_TEMPLATE_ID = UUID.randomUUID();

    @Before
    public void init() throws IOException, SQLException, NoSuchFieldException {
        testHelper = new MicroServiceTestHelper(new HubWebAppMicroService());

        // Creating the hub persistence schema
        testHelper.startServiceInTestMode();
    }

    @After
    public void tearDown() {
        testHelper.stopTestMode();
    }

    private HubWebAppWebApi getApi() {
        return testHelper.getServiceResource(HubWebAppResource.class);
    }

    private HubWebAppTestDevWebApi getTestDevApi() {
        return testHelper.getServiceResource(TestDevHubWebAppResource.class);
    }

    private HubWebAppImageStoreWebApi getImageStoreApi() {
        return testHelper.getServiceResource(HubWebAppImageStoreResource.class);
    }

    private HubWebAppUserWebApi getUserApi() {
        return testHelper.getServiceResource(UserHubWebAppResource.class);
    }

    private HubWebAppHtmlResource getHtmlResource() {
        return testHelper.getServiceResource(HubWebAppHtmlResource.class);
    }

    @Test
    public void testAddIntegration() {
        mockStuff();
        getApi().addJiraIntegration(new UICommandAddJiraIntegration("https://test.com", "12", "1002"));
        getApi().addPivotalTrackerIntegration(new UICommandAddPivotalTrackerIntegration(
                "https://bells.com",
                "bug15",
                "bug",
                "7f8c4afb99678a471ca82cb6c8df7f28"));
        final List<UIIntegrationDetails> integrations = getApi().listIntegrations();
        Assert.assertEquals(2, integrations.size());

        for (UIIntegrationDetails integration : integrations) {
            String name = integration.getIntegrationName();
            switch (name) {
                case "jira":
                    Assert.assertEquals("https://test.com", integration.getConnectionDetails().get("url"));
                    break;
                case "pivotal-tracker":
                    Assert.assertEquals("https://bells.com", integration.getConnectionDetails().get("url"));
                    break;
                default:
                    Assert.fail("Integration name did not match expected values");
                    break;
            }
        }
    }

    @Test
    public void testGetAppInstance() {
        mockStuff();
        final UUID id = UUID.randomUUID();
        final UIAppInstance instance = getApi().getAppInstance(id);
        Assert.assertEquals(id, instance.getId());
    }

    @Test
    public void testGetAppInstanceCopy() {
        mockStuff();
        final UUID id = UUID.randomUUID();
        final UIAppInstanceCopy copy = getApi().getAppInstanceCopy(UUID.randomUUID(), id);
        Assert.assertEquals(id, copy.getCopyId());
    }

    @Test
    public void testGetAppInstanceState() {
        mockStuff();
        final UUID id = UUID.randomUUID();
        final UIAppInstanceState instanceState = getApi().getAppInstanceState(id);
        Assert.assertEquals(id, instanceState.getId());
    }

    @Test
    public void testGetAppTemplate() {
        mockStuff();
        final UIApplicationTemplate appTemplate = getApi().getAppTemplate(APP_TEMPLATE_ID);
        Assert.assertEquals(APP_TEMPLATE_ID, appTemplate.getId());
        System.out.printf(appTemplate.toString());
    }

    @Test
    public void testGetCopyHistory() {
        mockStuff();
        UICopyHistoryData copyHistory = getApi().getCopyHistory(UUID.randomUUID(), "day", -1);
        Assert.assertNotNull(copyHistory);
        Assert.assertEquals(1, copyHistory.getCopies().size());
        System.out.printf(copyHistory.toString());
        copyHistory = getApi().getCopyHistory(UUID.randomUUID(), "day", -1);
        Assert.assertNotNull(copyHistory);
        Assert.assertEquals(1, copyHistory.getCopies().size());
        copyHistory = getApi().getCopyHistory(UUID.randomUUID(), "week", -1);
        Assert.assertNotNull(copyHistory);
        Assert.assertEquals(1, copyHistory.getCopies().size());
        copyHistory = getApi().getCopyHistory(UUID.randomUUID(), "month", -1);
        Assert.assertNotNull(copyHistory);
        Assert.assertEquals(1, copyHistory.getCopies().size());
        copyHistory = getApi().getCopyHistory(UUID.randomUUID(), "year", -1);
        Assert.assertNotNull(copyHistory);
        Assert.assertEquals(1, copyHistory.getCopies().size());

        try {
            getApi().getCopyHistory(UUID.randomUUID(), "shana", -1);
            Assert.fail("should fail miserably");
        } catch (Exception ex) {
            // should be here
        }

    }

    @Test
    public void testAddMavenArtifactRegistry() {
        mockStuff();
        final UUID siteId = getApi().listSites().iterator().next().getId();
        getApi().addMavenArtifactRegistry(new UICommandAddMavenArtifactRegistry(
                siteId,
                "ubu",
                "http://maven-registry.com",
                "Sergey",
                "Shilpi"));

        Assert.assertEquals(1, getApi().listSiteConfigurations().size());
    }

    @Test
    public void testAddDockerArtifactRegistry() {
        mockStuff();
        final UUID siteId = getApi().listSites().iterator().next().getId();
        getApi().addDockerArtifactRegistry(new UICommandAddDockerArtifactRegistry(
                siteId,
                "docker hub",
                "https://registry.hub.docker.com",
                null,
                null));

        Assert.assertEquals(1, getApi().listSiteConfigurations().size());
    }

    @Test
    public void testAddCustomArtifactRegistry() {
        mockStuff();
        final UUID siteId = getApi().listSites().iterator().next().getId();
        getApi().addCustomArtifactRegistry(new UICommandAddCustomArtifactRegistry(
                siteId,
                "ubu",
                "http://custom-registry.com"));

        final List<UISiteConfig> siteConfigs = getApi().listSiteConfigurations();
        Assert.assertEquals(1, siteConfigs.size());
    }

    @Test
    public void testAddDsb() {
        mockStuff();
        final UUID siteId = getApi().listSites().iterator().next().getId();
        getApi().addDsb(new UICommandAddDsb(
                siteId,
                "dsb1",
                "http://dsb.com"));

        final List<UISiteConfig> siteConfigs = getApi().listSiteConfigurations();
        Assert.assertEquals(1, siteConfigs.size());
    }

    @Test
    public void testRemoveArtifactRegistry() {
        mockStuff();
        final UUID siteId = getApi().listSites().iterator().next().getId();
        getApi().removeArtifactRegistry(new UICommandRemoveArtifactRegistry(
                siteId,
                "ubu"));
    }

    @Test
    public void testCreateAppCopy() {
        mockStuff();
        getApi().createAppCopy(new UICommandCreateAppCopy(UUID.randomUUID()));

        // Simple pass-through to the hub
    }

    @Test
    public void testCreateAppTemplate() {

        // The mock stuff creates a template...
        mockStuff();

        final UIApplicationTemplate template = getApi().getAppTemplate(APP_TEMPLATE_ID);
        Assert.assertEquals("lame twitter", template.getName());
        Assert.assertEquals("1.0", template.getVersion());
        Assert.assertEquals("Lame Twitter app", template.getDescription());
        Assert.assertEquals(APP_TEMPLATE_ID, template.getId());
        Assert.assertEquals(1, template.getAppServiceTemplates().size());
        expectNotFoundException(() -> getApi().getAppTemplate(UUID.randomUUID()));
    }

    private void createTemplate() {
        getApi().createAppTemplate(
                new UICommandCreateAppTemplate(
                        "lame twitter",
                        "1.0",
                        "Lame Twitter app",
                        "svc1",
                        Collections.singletonList(
                                new UICreateApplicationServiceTemplate(
                                        "svc1",
                                        "cf",
                                        "com.emc.nazgul.demo:lame-cf-app:zip:cfzip",
                                        "java",
                                        "0.89",
                                        Collections.emptyMap(),
                                        Collections.emptyMap(),
                                        Collections.singletonList(
                                                new UICreateAppServiceExternalDependency(
                                                        UICreateAppServiceExternalDependency.TypeEnum.OBJECTSTORE,
                                                        "ds1",
                                                        "Lame document store",
                                                        Collections.singletonList(
                                                                new UICreateAppServiceExternalDependencyProtocols(
                                                                        "s3",
                                                                        null,
                                                                        null,
                                                                        null
                                                                        ))
                                                )),
                                        Collections.singletonList(8080),
                                        8080,
                                        "lame-api/index.html"))));
    }

    @Test
    public void testListAppTemplates() {
        mockStuff();

        final List<UIApplicationTemplate> templates = getApi().listAppTemplates();
        Assert.assertEquals(1, templates.size());
    }

    @Test
    public void testCreateSavedImage() {
        mockStuff();

        final UUID imageId = getApi().createSavedImage(new UICommandCreateSavedImage(
                "my image",
                UUID.randomUUID(),
                Collections.emptyList(),
                "some comment"));

        Assert.assertEquals(SAVED_IMAGE_UUID, imageId);
    }

    @Test
    public void testDeploySavedImage() {
        mockStuff();

        final UUID appInstanceId = getApi().deploySavedImage(new UIDeploySavedImageCommandArgs(
                SAVED_IMAGE_UUID,
                "appInstanceName",
                UUID.randomUUID(),
                new UIAppTemplateDeploymentPlan(Collections.emptyMap(), Collections.emptyMap())));

        Assert.assertEquals(DEPLOY_SAVED_IMAGE_APP_INSTANCE_ID, appInstanceId);
    }

    @Test
    public void testDeployTestDevApp() {
        mockStuff();

        final UUID appInstanceId = getApi().deployTestDevAppCommand(new UICommandDeployTestDevAppArgs(
                "appInstanceName",
                UUID.randomUUID(),
                UUID.randomUUID(),
                new UIAppTemplateDeploymentPlan(Collections.emptyMap(), Collections.emptyMap())));

        Assert.assertEquals(DEPLOY_APP_INSTANCE_ID, appInstanceId);
    }

    @Test
    public void testListTestDevImages() {
        mockStuff();

        final UUID imageId = getApi().createSavedImage(new UICommandCreateSavedImage(
                "my image",
                UUID.randomUUID(),
                Collections.emptyList(),
                "some comment"));

        final List<UISavedAppImage> images = getTestDevApi().getSavedIAppImages(APP_TEMPLATE_ID);
        Assert.assertEquals(1, images.size());
        Assert.assertEquals(imageId, images.iterator().next().getId());

    }

    @Test
    public void testGetSavedImageInfo() {
        mockStuff();

        final UUID imageId = getApi().createSavedImage(new UICommandCreateSavedImage(
                "my image",
                UUID.randomUUID(),
                Collections.emptyList(),
                "some comment"));

        final UISavedAppImage image = getTestDevApi().getSavedIAppImage(imageId);
        final UISavedAppImageDetailed imageDetailed = getTestDevApi().getSavedIAppImageDetailed(imageId);
        Assert.assertEquals(imageId, image.getId());
        Assert.assertEquals(imageId, imageDetailed.getId());
        Assert.assertNotNull(imageDetailed.toString());
        Assert.assertNotNull(image.toString());

        // Invalid UUID
        try {
            getTestDevApi().getSavedIAppImageDetailed(UUID.randomUUID());
            Assert.fail("Should throw not found exception");
        } catch (NotFoundException e) {
            // This is valid
        }
    }

    @Test
    public void testGetTestDevInstances() {
        mockStuff();
        final List<UITestDevAppInstance> instances = getTestDevApi().getTestDevInstances();
        Assert.assertEquals(1, instances.size());
    }

    @Test
    public void testGetTestDevDashboardData() {
        mockStuff();

        final UUID appInstanceId = getApi().deployTestDevAppCommand(new UICommandDeployTestDevAppArgs(
                "appInstanceName",
                UUID.randomUUID(),
                UUID.randomUUID(),
                new UIAppTemplateDeploymentPlan(
                        Collections.singletonMap(
                                "svc1",
                                new UIAppServiceDeploymentPlan(
                                        true,
                                        "shpanSpace",
                                        "shpanRegistry",
                                        "1")),
                        Collections.singletonMap(
                                "ds1",
                                new UIDataServiceDeploymentPlan(
                                        "dsb1",
                                        true,
                                        "default",
                                        "s3"
                                )
                        ))));

        final UITestDevDashboardData dashboardData = getTestDevApi().getAppInstanceDashboardData(appInstanceId);
        Assert.assertNotNull(dashboardData.toString());
    }

    @Test
    public void testGetAppTemplateConfigurationBySite() {
        mockStuff();
        final UUID siteId = getApi().listSites().iterator().next().getId();
        final UIAppTemplateConfigurationForSite plan =
                getTestDevApi().getAppTemplateConfigurationBySite(siteId, APP_TEMPLATE_ID);
        Assert.assertEquals(1, plan.getAppServiceConfigurations().size());
        Assert.assertEquals(1, plan.getDataServiceConfigurations().size());

    }

    @Test
    public void testRePurposeApp() {
        mockStuff();

        final UUID appInstanceId = getApi().rePurposeApp(new UICommandRePurposeApp(
                UUID.randomUUID(),
                "repAppName",
                UUID.randomUUID(),
                "fun"));

        Assert.assertEquals(REPURPOSE_APP_INSTANCE_ID, appInstanceId);
    }

    @Test
    public void testImageStore() throws IOException {
        try (final ReaderInputStream inputStream = new ReaderInputStream(new StringReader("hello"))) {
            getImageStoreApi().uploadIcon(inputStream, "t1", "i1");
        }
        Response image = getImageStoreApi().getImage("t1", "i1");
        Assert.assertEquals(200, image.getStatus());
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ((StreamingOutput) image.getEntity()).write(output);
            Assert.assertEquals("hello", output.toString());
        }
        image = getImageStoreApi().getImage("t1", "i2");
        Assert.assertEquals(204, image.getStatus());
    }

    @Test
    public void testGetLogsWebSockets() throws IOException {
        mockStuff();

        final UUID appInstanceId = getApi().deployTestDevAppCommand(new UICommandDeployTestDevAppArgs(
                "appInstanceName",
                UUID.randomUUID(),
                UUID.randomUUID(),
                new UIAppTemplateDeploymentPlan(Collections.emptyMap(), Collections.emptyMap())));

        final List<UILogsWebSocketInfo> sockets = getApi().getLogsWebSockets(appInstanceId);
        Assert.assertEquals(1, sockets.size());
    }

    @Test
    public void testUserResource() throws IOException {
        mockStuff();
        getUserApi().listUsers();
        UIUser user = getUserApi().getLoggedInUser();
        getUserApi().getUser(user.getId());
        try (OutputStream outputStream = new ByteArrayOutputStream()) {
            ((StreamingOutput) getUserApi().getUserImage(user.getId()).getEntity()).write(outputStream);
        }
    }

    @Test
    public void testHtmlResource() throws IOException {
        mockStuff();
        getHtmlResource().welcome().getEntity();
        try (OutputStream outputStream = new ByteArrayOutputStream()) {
            ((StreamingOutput) getHtmlResource().get(".").getEntity()).write(outputStream);
        }
        getHtmlResource().get("Mustafa.piz");
    }

    private void mockStuff() {

        final UIUser user = testHelper.getServiceResource(UserHubWebAppResource.class).listUsers().get(0);
        testHelper.setSecurityContext(new HubWebAppSecurityContext(
                new AuthUser(
                        UUID.fromString(user.getId()),
                        user.getName(),
                        "1234",
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail()),
                "http"));
        final MyHubWebApi hub = new MyHubWebApi();
        testHelper.mockDependentServiceRestResource("hub", HubWebApi.class, hub);
        testHelper.mockDependentServiceRestResourceByUrl("http://site1", SiteWebApi.class, new MySiteWebApi());
        testHelper.mockDependentServiceRestResourceByUrl("http://site1", DsbCatalogWebApi.class, new MyDsbCatalogWebApi());
        hub.addSite(new AddSiteToHubCommandArgs("site1", "http://site1"));
        createTemplate();

    }

    private static class MyHubWebApi implements HubWebApi {
        private final Map<String, String> hubConfig = new HashMap<>();
        Map<String, SiteDto> siteByUrn = new HashMap<>();
        private final Map<UUID, SavedImageDTO> savedImages = new HashMap<>();
        private final Map<UUID, ApplicationTemplateDTO> appTemplates = new HashMap<>();

        @Override
        public Collection<ApplicationTemplateDTO> listAppCatalog() {
            return Collections.singletonList(
                    getAppTemplate(APP_TEMPLATE_ID, false)
            );
        }

        @Override
        public ApplicationTemplateDTO getAppTemplate(
                @PathParam("appTemplateId") UUID appTemplateId,
                @HeaderParam("NAZGUL-INCLUDE-DELETED") @DefaultValue("false") Boolean includeDeleted) {
            return appTemplates.get(appTemplateId);
        }

        @Override
        public Collection<HubAppInstanceConfigurationDTO> listAppInstances() {
            return null;
        }

        @Override
        public HubAppInstanceWithStateDTO getAppInstanceState(@PathParam("appInstanceId") UUID appInstanceId) {
            return new HubAppInstanceWithStateDTO(
                    appInstanceId,
                    "app instance",
                    APP_TEMPLATE_ID,
                    siteByUrn.values().iterator().next().getId(),
                    null,
                    UUID.randomUUID(),
                    "prod",
                    new Date(),
                    "a",
                    "running",
                    null);
        }

        @Override
        public Collection<HubAppInstanceWithStateDTO> listAppInstanceStates() {
            return Collections.singletonList(
                    new HubAppInstanceWithStateDTO(
                            DEPLOY_APP_INSTANCE_ID,
                            "app",
                            APP_TEMPLATE_ID,
                            siteByUrn.values().iterator().next().getId(),
                            null,
                            UUID.randomUUID(),
                            "test-dev",
                            new Date(),
                            "http://dead.end",
                            "running",
                            null));
        }

        @Override
        public HubAppInstanceConfigurationDTO getAppInstance(@PathParam("appInstanceId") UUID appInstanceId) {
            return getAppInstanceState(appInstanceId);
        }

        @Override
        public HubAppInstanceDownStreamTreeDTO listDownStreamInstances(@PathParam("appInstanceId") UUID appInstanceId) {
            final UUID siteId = siteByUrn.values().iterator().next().getId();
            return new HubAppInstanceDownStreamTreeDTO(
                    siteId,
                    appInstanceId,
                    "prod",
                    Collections.singletonList(new HubAppInstanceDownStreamTreeDTO(
                            siteId, UUID.randomUUID(), "test-dev", Collections.emptyList()
                    )));
        }

        @Override
        public Collection<SiteDto> listSites() {
            return new ArrayList<>(siteByUrn.values());
        }

        @Override
        public SiteDto getSite(@PathParam("siteId") UUID siteId) {
            return siteByUrn.values().stream().filter(siteDTO -> siteDTO.getId().equals(siteId)).findFirst()
                    .orElseGet(() -> {
                        throw new NotFoundException();
                    });
        }

        @Override
        public Collection<SavedImageDTO> listSavedImages(@QueryParam("appTemplateId") UUID appTemplateId) {
            return savedImages.values();
        }

        @Override
        public SavedImageDTO getSavedImage(@PathParam("savedImageId") UUID savedImageId) {
            return savedImages.get(savedImageId);
        }

        @Override
        public String readHubConfig(@PathParam("key") String key) {
            return hubConfig.get(key);
        }

        @Override
        public void writeHubConfig(@PathParam("key") String key, String value) {
            hubConfig.put(key, value);
        }

        @Override
        public ApplicationTemplateDTO createApplicationTemplate(ApplicationTemplateDTO applicationTemplateDTO) {
            final ApplicationTemplateDTO t = new ApplicationTemplateDTO(
                    APP_TEMPLATE_ID,
                    applicationTemplateDTO.getName(),
                    applicationTemplateDTO.getVersion(),
                    applicationTemplateDTO.getDescription(),
                    applicationTemplateDTO.getAppServiceTemplates(),
                    applicationTemplateDTO.getEntryPointServiceName(),
                    applicationTemplateDTO.getCreatedByUserId());

            appTemplates.put(APP_TEMPLATE_ID, t);
            return t;
        }

        @Override
        public void deleteAppTemplate(@PathParam("appTemplateId") UUID appTemplateId) {
            appTemplates.remove(APP_TEMPLATE_ID);
        }

        @Override
        public UUID deployApp(DeployAppCommandArgs applicationTemplateDTO) {
            return DEPLOY_APP_INSTANCE_ID;
        }

        @Override
        public DeployAppCommandArgs.AppTemplateDeploymentPlanDTO createAppDeploymentPlan(CreateDeploymentPlanCommandArgs applicationTemplateDTO) {
            return null;
        }

        @Override
        public UUID repurposeApp(RepurposeAppCommandArgs applicationTemplateDTO) {
            return REPURPOSE_APP_INSTANCE_ID;
        }

        @Override
        public UUID createAppCopy(com.emc.ocopea.hub.application.CreateAppCopyCommandArgs args) {
            return APP_COPY_UUID;
        }

        @Override
        public UUID createSavedImage(CreateSavedImageCommandArgs args) {
            savedImages.put(
                    SAVED_IMAGE_UUID,
                    new SavedImageDTO(
                            SAVED_IMAGE_UUID,
                            APP_TEMPLATE_ID,
                            "img1",
                            null,
                            UUID.randomUUID(),
                            new Date(),
                            Collections.singletonList("tag1"),
                            "hi",
                            siteByUrn.values().iterator().next().getId(),
                            UUID.randomUUID(),
                            "created"
                    ));

            return SAVED_IMAGE_UUID;
        }

        @Override
        public UUID deploySavedImage(DeploySavedImageCommandArgs args) {
            return DEPLOY_SAVED_IMAGE_APP_INSTANCE_ID;
        }

        @Override
        public String addSite(AddSiteToHubCommandArgs addSiteToHubCommandArgs) {
            siteByUrn.put(
                    addSiteToHubCommandArgs.getUrn(),
                    new SiteDto(
                            UUID.randomUUID(),
                            addSiteToHubCommandArgs.getUrn(),
                            addSiteToHubCommandArgs.getUrl(),
                            addSiteToHubCommandArgs.getUrn(),
                            "1",
                            new SiteLocationDTO(32.1792126, 34.9005128, "Israel", Collections.emptyMap()),
                            null));
            return null;
        }

        @Override
        public void stopApp(StopAppCommandArgs stopAppCommandArgs) {

        }
    }

    private static class MySiteWebApi implements SiteWebApi {
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
            return Collections.singletonList(new SitePsbInfoDto("psb1", "psb1", "mock", "1"));
        }

        @Override
        public Collection<SitePsbDetailedInfoDto> listPsbsDetailed() {
            return null;
        }

        @Override
        public Collection<SiteArtifactRegistryInfoDTO> listArtifactRegistries() {
            return Collections.singletonList(new SiteArtifactRegistryInfoDTO("art1", "mock", Collections.emptyMap()));
        }

        @Override
        public Collection<String> listArtifactVersions(
                @PathParam("artifactRegistryName") String s, @PathParam("artifactId") String s1) {
            return null;
        }

        @Override
        public Collection<SiteCopyRepoInfoDTO> listCopyRepositories() {
            return Collections.singletonList(new SiteCopyRepoInfoDTO("crb1", "http://crb1", "crb1", "mock", "1"));
        }

        @Override
        public AppInstanceInfoDTO getAppInstanceInfo(@PathParam("appInstanceId") UUID uuid) {
            return new AppInstanceInfoDTO(uuid, "app", "template", "1", DeployedApplicationState.running, null, new
                    Date(), new Date(), "http://hi.com", Collections.singletonList(new AppInstanceInfoDTO
                    .AppServiceInfoDTO("svc1", "i", "j", "1", "http://hi.com", DeployedAppServiceState.deployed, null,
                    new Date(), Collections.singleton("ds1"))),
                    Collections.singletonList(new AppInstanceInfoDTO.DataServiceInfoDTO("ds1", "dsb1", "jobo",
                            DeployedDataServiceState.bound, null, new Date())
                    ));
        }

        @Override
        public List<ServiceLogsWebSocketDTO> getAppInstanceLogsWebSockets(@PathParam("appInstanceId") UUID uuid) {
            return Collections.singletonList(new ServiceLogsWebSocketDTO(
                    "ws://test",
                    "json",
                    Collections.singletonList("a")));
        }

        @Override
        public Collection<AppInstanceCopyStatisticsDTO> getCopyHistory(
                @PathParam("appInstanceId") UUID uuid,
                @QueryParam("intervalStart") Long aLong,
                @QueryParam("intervalEnd") Long aLong1) {
            return Collections.singletonList(
                    new AppInstanceCopyStatisticsDTO(
                            UUID.randomUUID(),
                            new Date(),
                            3L,
                            AppInstanceCopyStatisticsDTO.SiteAppCopyState.created,
                            Collections.singletonList(
                                    new AppInstanceStatisticsDTO.DataServiceCopyStatisticsDTO(
                                            UUID.randomUUID(),
                                            "dsb",
                                            "a",
                                            new Date(),
                                            "repoId1",
                                            "facility1",
                                            "crb",
                                            "moshe",
                                            "fs",
                                            "1",
                                            12L,
                                            "bye")
                            ),
                            Collections.singletonList(
                                    new AppInstanceStatisticsDTO.AppServiceCopyStatisticsDTO(
                                            "a",
                                            "i",
                                            "j",
                                            "1",
                                            new Date(),
                                            Collections.emptyMap(),
                                            "hi"))
                    ));
        }

        @Override
        public AppInstanceStatisticsDTO getAppInstanceStatistics(@PathParam("appInstanceId") UUID uuid) {
            return new AppInstanceStatisticsDTO(uuid, Collections.emptyList(), Collections.singletonList(new
                    AppInstanceStatisticsDTO.DataServiceProductionCopyStatisticsDTO("dsb1", "ds1", "EBS", 222)));
        }

        @Override
        public AppInstanceCopyStatisticsDTO getCopyMetadata(@PathParam("copyId") UUID uuid) {
            final Date timeStamp = new Date();
            return new AppInstanceCopyStatisticsDTO(
                    uuid,
                    timeStamp,
                    3L,
                    AppInstanceCopyStatisticsDTO.SiteAppCopyState.created,
                    Collections.singleton(new AppInstanceStatisticsDTO.DataServiceCopyStatisticsDTO(
                            UUID.randomUUID(),
                            "dsb1",
                            "ds1",
                            timeStamp,
                            "repoIdB",
                            "facilityA",
                            "crb1",
                            "Crb 1",
                            "shpanRest",
                            "1",
                            12L,
                            "nice"
                    )),
                    Collections.singletonList(
                            new AppInstanceStatisticsDTO.AppServiceCopyStatisticsDTO(
                                    "svc1",
                                    "img1",
                                    "java",
                                    "1",
                                    timeStamp,
                                    Collections.emptyMap(),
                                    "not bad"
                            )
                    ));
        }

        @Override
        public Response downloadCopy(@PathParam("copyRepoURN") String s, @PathParam("copyId") UUID uuid) {
            return null;
        }

        @Override
        public void addCr(AddCrToSiteCommandArgs addCrToSiteCommandArgs) {
        }

        @Override
        public void registerCrb(RegisterCrbToSiteCommandArgs args) {
        }

        @Override
        public void registerPsb(RegisterPsbToSiteCommandArgs registerPsbToSiteCommandArgs) {
        }

        @Override
        public SupportedServiceDto registerDsb(RegisterDsbToSiteCommandArgs args) {
            return createPgService(args.getDsbUrn());
        }

        @Override
        public void addCustomRestArtifactRegistry(AddCustomArtifactRegistryToSiteCommandArgs args) {
        }

        @Override
        public void addMavenArtifactRegistry(AddMavenArtifactRegistryToSiteCommandArgs args) {
        }

        @Override
        public void addDockerArtifactRegistry(AddDockerArtifactRegistryToSiteCommandArgs addDockerArtifactRegistryToSiteCommandArgs) {
        }

        @Override
        public void removeArtifactRegistry(RemoveArtifactRegistryFromSiteCommandArgs args) {

        }

        @Override
        public void removeDsb(RemoveDsbFromSiteCommandArgs args) {

        }

        @Override
        public void removeCrb(RemoveCrbFromSiteCommandArgs args) {
        }

        @Override
        public void deployApplicationOnSite(DeployApplicationOnSiteCommandArgs args) {

        }

        @Override
        public void stageCopy(
                InputStream stream,
                @HeaderParam("crbUrn") String s,
                @HeaderParam("dsb") String s1,
                @HeaderParam("copyTimestamp") Long aLong,
                @HeaderParam("facility") String s2,
                @HeaderParam("meta") String s3,
                @HeaderParam("copyId") UUID uuid) {

        }

        @Override
        public UUID createAppCopy(com.emc.ocopea.site.CreateAppCopyCommandArgs args) {
            return null;
        }

        @Override
        public void stopApp(StopAppOnSiteCommandArgs args) {

        }
    }

    private static class MyDsbCatalogWebApi implements DsbCatalogWebApi {
        @Override
        public Collection<SupportedServiceDto> getCatalog() {
            return Arrays.asList(
                    createPgService("pg1Urn"),
                    createSupportedService("s31Urn", "s31", "s3", "s3", Collections.singletonList(
                            new SupportedServiceDto.SupportedServicePlanDto(
                                    "default",
                                    "default",
                                    "default",
                                    "1$",
                                    Collections.singletonList(
                                            new SupportedServiceDto.SupportedServiceProtocolDto(
                                                    "s3",
                                                    "1.0",
                                                    Collections.emptyMap())),
                                    Collections.emptyMap()

                            ))));
        }

        @Override
        public Collection<ServiceInstanceInfo> getInstancesByDsb() {
            return null;
        }

        @Override
        public Collection<ServiceInstanceInfo> getInstancesByDsb(@PathParam("dsbUrn") String dsbUrn) {
            return null;
        }
    }

    private static SupportedServiceDto createPgService(String urn) {
        return createSupportedService(urn, "pg1", "postgres", "Postgres DB", Collections.singletonList(
                new SupportedServiceDto.SupportedServicePlanDto(
                        "default",
                        "default",
                        "default",
                        "1$",
                        Collections.singletonList(
                                new SupportedServiceDto.SupportedServiceProtocolDto(
                                        "postgres",
                                        "9.0",
                                        Collections.emptyMap())),
                        Collections.emptyMap()

                )

        ));
    }

    private static SupportedServiceDto createSupportedService(
            String urn,
            String pg1,
            String postgres,
            String description,
            List<SupportedServiceDto.SupportedServicePlanDto> plans) {
        return new SupportedServiceDto(
                urn,
                pg1,
                postgres,
                description,
                plans);
    }

    private void expectNotFoundException(Runnable r) {
        try {
            r.run();
            Assert.fail("Should have raised not found exception");
        } catch (NotFoundException ex) {
            // This is valid, yey
        }
    }

}
