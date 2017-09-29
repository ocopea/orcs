// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub;

import com.emc.microservice.testing.MicroServiceTestHelper;
import com.emc.ocopea.hub.application.AppServiceExternalDependencyDTO;
import com.emc.ocopea.hub.application.ApplicationServiceTemplateDTO;
import com.emc.ocopea.hub.application.ApplicationTemplateDTO;
import com.emc.ocopea.hub.application.CreateDeploymentPlanCommandArgs;
import com.emc.ocopea.hub.application.CreateAppCopyCommandArgs;
import com.emc.ocopea.hub.application.CreateSavedImageCommandArgs;
import com.emc.ocopea.hub.application.DataServiceTypeEnumDTO;
import com.emc.ocopea.hub.application.DeployAppCommandArgs;
import com.emc.ocopea.hub.application.DeploySavedImageCommandArgs;
import com.emc.ocopea.hub.application.HubAppInstanceConfigurationDTO;
import com.emc.ocopea.hub.application.HubAppInstanceWithStateDTO;
import com.emc.ocopea.hub.application.HubResource;
import com.emc.ocopea.hub.application.HubWebApi;
import com.emc.ocopea.hub.application.RepurposeAppCommandArgs;
import com.emc.ocopea.hub.application.StopAppCommandArgs;
import com.emc.ocopea.hub.repository.HubRepositorySchema;
import com.emc.ocopea.hub.site.AddSiteToHubCommandArgs;
import com.emc.ocopea.hub.site.SiteDto;
import com.emc.ocopea.hub.testdev.SavedImageDTO;
import com.emc.ocopea.site.AddCustomArtifactRegistryToSiteCommandArgs;
import com.emc.ocopea.site.DsbCatalogWebApi;
import com.emc.ocopea.site.RegisterCrbToSiteCommandArgs;
import com.emc.ocopea.site.RegisterDsbToSiteCommandArgs;
import com.emc.ocopea.site.RegisterPsbToSiteCommandArgs;
import com.emc.ocopea.site.ServiceInstanceInfo;
import com.emc.ocopea.site.SiteWebApi;
import com.emc.ocopea.site.SupportedServiceDto;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by liebea on 10/13/16.
 * Drink responsibly
 */
public class TestHubMicroService {

    private static final UUID USER_ID = UUID.randomUUID();
    private MicroServiceTestHelper testHelper;
    private final Field servletRequestField;
    private static final String SITE_URN = "site1";
    private static final String SITE_URL = "http://site1";

    public TestHubMicroService() {
        try {
            servletRequestField = HubResource.class.getDeclaredField("servletRequest");
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("can't find injection field of HubResource", e);
        }
        servletRequestField.setAccessible(true);
    }

    @Before
    public void init() throws IOException, SQLException {
        testHelper = new MicroServiceTestHelper(new HubMicroService());

        // Creating the hub persistence schema
        testHelper.createOrUpgrdaeSchema(new HubRepositorySchema("hub"));
        testHelper.startServiceInTestMode();
    }

    private HubWebApi getHubWebAPI() {
        return testHelper.getServiceResource(HubResource.class);
    }

    @After
    public void tearDown() {
        testHelper.stopTestMode();
    }

    @Test
    public void testSiteManagement() throws IOException {
        Assert.assertTrue("No sites should be registered by default", getHubWebAPI().listSites().isEmpty());
        addSite(SITE_URN, SITE_URL);
        addSite("site2", "http://site2");

        final Collection<SiteDto> sites = getHubWebAPI().listSites();
        Assert.assertEquals(2, sites.size());

        getHubWebAPI().getSite(sites.iterator().next().getId());
        expectNotFoundException(() -> getHubWebAPI().getSite(UUID.randomUUID()));
    }

    @Test
    public void testAppTemplateManagement() throws IOException {
        Assert.assertTrue("shouldn't be any templates when starting", getHubWebAPI().listAppCatalog().isEmpty());
        final UUID templateId = createTemplate("template1");
        Assert.assertEquals("should be one template", 1, getHubWebAPI().listAppCatalog().size());
        final ApplicationTemplateDTO template = getHubWebAPI().getAppTemplate(templateId, false);
        Assert.assertEquals("template1", template.getName());
        expectNotFoundException(() -> getHubWebAPI().getAppTemplate(UUID.randomUUID(), false));
    }

    @Test
    public void testCreateAppTemplateWithWrongMainService() {
        expectWebApplicationException(
                () -> getHubWebAPI().createApplicationTemplate(new ApplicationTemplateDTO(
                        null,
                        "bad",
                        "1.3",
                        "Fancy app template",
                        Collections.singletonList(createAppServiceTemplate("ms1")),
                        "ms2",
                        USER_ID)),
                400);

    }

    @Test
    public void testCreateAppTemplateWithPreDefinedId() {
        expectWebApplicationException(
                () -> getHubWebAPI().createApplicationTemplate(new ApplicationTemplateDTO(
                        UUID.randomUUID(),
                        "bad",
                        "1.3",
                        "Fancy app template",
                        Collections.singletonList(createAppServiceTemplate("ms1")),
                        "ms1",
                        USER_ID)),
                400);

    }

    @Test
    public void testCreateDuplicateAppTemplate() {
        createTemplate("t1");
        expectWebApplicationException(
                () -> createTemplate("t1"),
                409);

    }

    private ApplicationServiceTemplateDTO createAppServiceTemplate(String appServiceName) {
        return new ApplicationServiceTemplateDTO(
                appServiceName,
                "psb1",
                "img1",
                "java",
                "1.1",
                Collections.emptyMap(),
                Collections.emptyMap(),
                Arrays.asList(
                        new AppServiceExternalDependencyDTO(
                                DataServiceTypeEnumDTO.database,
                                "db1",
                                Collections.singletonList(
                                        new AppServiceExternalDependencyDTO
                                                .AppServiceExternalDependencyProtocolDTO(
                                                "postgres",
                                                "9.0",
                                                null,
                                                null)),
                                "Configuration db"),
                        new AppServiceExternalDependencyDTO(
                                DataServiceTypeEnumDTO.objectStore,
                                "db32",
                                Collections.singletonList(
                                        new AppServiceExternalDependencyDTO
                                                .AppServiceExternalDependencyProtocolDTO(
                                                "s3",
                                                null,
                                                null,
                                                null)),
                                "object store")),
                Collections.singletonList(8080),
                8080,
                "entry-url/index.html");
    }

    @Test
    public void testAppDeployment() throws IOException, InterruptedException {

        Assert.assertTrue("No apps should be deployed", getHubWebAPI().listAppInstances().isEmpty());

        addSite(SITE_URN, SITE_URL);
        createTemplate("template1");
        UUID appInstanceId = deployApp();
        final HubAppInstanceWithStateDTO appInstanceState = waitForAppState(appInstanceId, "running");
        Assert.assertEquals("running", appInstanceState.getState());
        Assert.assertEquals("1 app should be deployed", 1, getHubWebAPI().listAppInstances().size());

        final HubAppInstanceConfigurationDTO appInstance = getHubWebAPI().getAppInstance(appInstanceId);
        Assert.assertEquals(appInstanceId, appInstance.getId());
        expectNotFoundException(() -> getHubWebAPI().getAppInstance(UUID.randomUUID()));
        expectNotFoundException(() -> getHubWebAPI().getAppInstanceState(UUID.randomUUID()));
    }

    @Test
    public void testAppDeploymentSameName() throws IOException, InterruptedException {
        addSite(SITE_URN, SITE_URL);
        createTemplate("template1");
        UUID appInstanceId = deployApp();
        waitForAppState(appInstanceId, "running");

        expectWebApplicationException(() -> {
            try {
                deployApp();
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }, 409);
    }

    @Test
    public void testCreateAppDeploymentPlan() throws IOException, InterruptedException {
        final UUID templateId = createTemplate("template1");
        addSite(SITE_URN, SITE_URL);

        getHubWebAPI().createAppDeploymentPlan(new CreateDeploymentPlanCommandArgs(
                templateId, Collections.singletonList(new DeployAppCommandArgs.ApplicationPolicyInfoDTO(
                "dataProtection", "bronze", Collections.emptyMap()
        ))));
    }

    @Test
    public void testCreateSavedImage() throws IOException, InterruptedException {
        addSite(SITE_URN, SITE_URL);

        createTemplate("template1");
        UUID appInstanceId = deployApp();
        waitForAppState(appInstanceId, "running");

        final UUID savedImageId = createSavedImage(appInstanceId);

        final SavedImageDTO image = getHubWebAPI().getSavedImage(savedImageId);
        Assert.assertNotNull(image);
        Assert.assertEquals(savedImageId, image.getId());
        Assert.assertEquals("test  comment", image.getComment());
        Assert.assertEquals("tag1", image.getTags().get(0));
        System.out.println(image.toString());

        // Invalid UUID
        try {
            getHubWebAPI().getSavedImage(UUID.randomUUID());
            Assert.fail("Should throw not found exception");
        } catch (NotFoundException e) {
            // This is valid
        }
    }

    @Test
    public void testCreateSavedImageBadRequest() throws IOException, InterruptedException {
        try {
            createSavedImage(null);
            Assert.fail("should fail");
        } catch (BadRequestException bre) {
            // this is actually good, yey
        }
    }

    @Test
    public void testRepurposeApp() throws IOException, InterruptedException {

        addSite(SITE_URN, SITE_URL);

        createTemplate("template1");
        UUID appInstanceId = deployApp();
        waitForAppState(appInstanceId, "running");
        final UUID appCopyId = createAppCopy(appInstanceId);
        getHubWebAPI().repurposeApp(new RepurposeAppCommandArgs(
                "rep",
                appInstanceId,
                appCopyId,
                UUID.randomUUID(),
                "test-dev",
                Collections.singletonList(
                        new DeployAppCommandArgs.ApplicationPolicyInfoDTO(
                                "dataProtection",
                                "silver",
                                Collections.emptyMap()
                        ))
        ));
    }

    private UUID createSavedImage(UUID appInstanceId) {
        return getHubWebAPI().createSavedImage(
                new CreateSavedImageCommandArgs(
                        "img1",
                        appInstanceId,
                        UUID.randomUUID(),
                        Collections.singletonList("tag1"),
                        "test  comment"));
    }

    private UUID createAppCopy(UUID appInstanceId) {
        return getHubWebAPI().createAppCopy(
                new CreateAppCopyCommandArgs(appInstanceId));
    }

    @Test
    public void testDeploySavedImage() throws IOException, InterruptedException {
        addSite(SITE_URN, SITE_URL);

        createTemplate("template1");
        UUID appInstanceId = deployApp();
        waitForAppState(appInstanceId, "running");
        final UUID savedImageId = createSavedImage(appInstanceId);

        final HubAppInstanceConfigurationDTO appInstance = getHubWebAPI().getAppInstance(appInstanceId);
        final ApplicationTemplateDTO appTemplate = getHubWebAPI().getAppTemplate(appInstance.getAppTemplateId(), false);
        getHubWebAPI().deploySavedImage(new DeploySavedImageCommandArgs(
                "fun-new-app",
                savedImageId,
                UUID.randomUUID(),
                "test-dev",
                appInstance.getSiteId(),
                buildDeploymentPlan(appTemplate)));
    }

    @Test
    public void testAppStop() throws IOException, InterruptedException {

        Assert.assertTrue("No apps should be deployed", getHubWebAPI().listAppInstances().isEmpty());

        addSite(SITE_URN, SITE_URL);

        createTemplate("template1");
        UUID appInstanceId = deployApp();
        final HubAppInstanceWithStateDTO appInstanceState = waitForAppState(appInstanceId, "running");
        Assert.assertEquals("running", appInstanceState.getState());
        Assert.assertEquals("1 app should be deployed", 1, getHubWebAPI().listAppInstances().size());

        getHubWebAPI().stopApp(new StopAppCommandArgs(appInstanceId, UUID.randomUUID()));
        final HubAppInstanceWithStateDTO stoppedState = waitForAppState(appInstanceId, "stopped");
        Assert.assertEquals("stopped", stoppedState.getState());
    }

    private HubAppInstanceWithStateDTO waitForAppState(UUID appInstanceId, String state) throws InterruptedException {
        final long started = System.currentTimeMillis();
        HubAppInstanceWithStateDTO appInstanceState = getHubWebAPI().getAppInstanceState(appInstanceId);
        final long maxSeconds = 10L;
        while (!state.equals(appInstanceState.getState())
                && System.currentTimeMillis() - started < maxSeconds * 1000
                ) {
            Thread.sleep(100L);
            appInstanceState = getHubWebAPI().getAppInstanceState(appInstanceId);
        }

        return appInstanceState;
    }

    private UUID deployApp() throws IOException {

        final UUID siteId = getHubWebAPI().listSites().iterator().next().getId();
        final UUID templateId = getHubWebAPI().listAppCatalog().iterator().next().getId();
        final ApplicationTemplateDTO appTemplate = getHubWebAPI().getAppTemplate(templateId, false);
        return getHubWebAPI()
                .deployApp(new DeployAppCommandArgs(
                        "appInstance1",
                        templateId,
                        siteId,
                        UUID.randomUUID(),
                        "production",
                        Collections.singletonList(new DeployAppCommandArgs.ApplicationPolicyInfoDTO
                                ("dataProtection", "silver", Collections.emptyMap())),
                        buildDeploymentPlan(appTemplate)));

    }

    private DeployAppCommandArgs.AppTemplateDeploymentPlanDTO buildDeploymentPlan(ApplicationTemplateDTO appTemplate) {
        // Converting app services to plans from templates
        Map<String, DeployAppCommandArgs.AppServiceDeploymentPlanDTO> appServices =
                appTemplate.getAppServiceTemplates()
                        .stream()
                        .collect(Collectors.toMap(
                                ApplicationServiceTemplateDTO::getAppServiceName,
                                ast -> new DeployAppCommandArgs.AppServiceDeploymentPlanDTO(
                                        true,
                                        "ShpanSpace",
                                        "artifactRegistry1",
                                        ast.getImageVersion())));

        // Resolving data services
        Map<String, DeployAppCommandArgs.DataServiceDeploymentPlanDTO> dataServices = new HashMap<>();
        for (ApplicationServiceTemplateDTO currAST : appTemplate.getAppServiceTemplates()) {
            for (AppServiceExternalDependencyDTO currDep : currAST.getDependencies()) {
                final DeployAppCommandArgs.DataServiceDeploymentPlanDTO dataServiceDeploymentPlan =
                        dataServices.get(currDep.getName());
                if (dataServiceDeploymentPlan == null) {
                    final String protocolName = currDep.getProtocols().iterator().next().getProtocolName();
                    dataServices.put(
                            currDep.getName(),
                            new DeployAppCommandArgs.DataServiceDeploymentPlanDTO(
                                    "dsb-for-" + protocolName,
                                    "default",
                                    protocolName,
                                    true,
                                    Collections.emptyMap()));
                }
            }
        }

        return new DeployAppCommandArgs.AppTemplateDeploymentPlanDTO(
                appServices,
                dataServices
        );
    }

    private UUID createTemplate(String templateName) {
        return getHubWebAPI().createApplicationTemplate(new ApplicationTemplateDTO(
                null, templateName,
                "1.3",
                "Fancy app template",
                Collections.singletonList(
                        createAppServiceTemplate("ms1")),
                "ms1",
                USER_ID)).getId();
    }

    private SiteDto addSite(String siteUrn, String siteUrl) {
        final String siteName = "my " + siteUrn;
        final int sitesCountBeforeAdding = getHubWebAPI().listSites().size();
        final SiteApiMock impl = new SiteApiMock(siteName);
        impl.registerDsb(new RegisterDsbToSiteCommandArgs("dsb1", "http://dsb1"));
        impl.registerCrb(new RegisterCrbToSiteCommandArgs("crb1", "http://crb1"));
        impl.registerPsb(new RegisterPsbToSiteCommandArgs("psb1", "http://psb1"));
        impl.addCustomRestArtifactRegistry(new AddCustomArtifactRegistryToSiteCommandArgs("art1", "http://go.away"));
        testHelper.mockDependentServiceRestResourceByUrl(siteUrl, SiteWebApi.class, impl);
        testHelper.mockDependentServiceRestResourceByUrl(siteUrl, DsbCatalogWebApi.class, new MyDsbCatalogWebApi());
        getHubWebAPI().addSite(new AddSiteToHubCommandArgs(siteUrn, siteUrl));
        Assert.assertEquals(
                "Should have increased by 1 site only",
                sitesCountBeforeAdding + 1,
                getHubWebAPI().listSites().size());

        final Optional<SiteDto> siteFromList = getHubWebAPI().listSites()
                .stream()
                .filter(siteDTO -> siteDTO.getUrn().equals(siteUrn)).findAny();

        Assert.assertTrue(siteFromList.isPresent());
        Assert.assertTrue(siteFromList.get().getName().equals(siteName) && siteFromList.get().getUrn().equals(siteUrn));

        final SiteDto site = getHubWebAPI().getSite(siteFromList.get().getId());
        Assert.assertTrue(site.getName().equals(siteName) && site.getUrn().equals(siteUrn));

        return site;
    }

    private static class MyDsbCatalogWebApi implements DsbCatalogWebApi {
        @Override
        public Collection<SupportedServiceDto> getCatalog() {
            return Arrays.asList(
                    new SupportedServiceDto(
                            "dsb-for-postgres",
                            "pg1",
                            "postgres",
                            "postgres Service",
                            Collections.singletonList(
                                    new SupportedServiceDto.SupportedServicePlanDto(
                                            "default",
                                            "default",
                                            "default",
                                            null,
                                            Collections.singletonList(
                                                    new SupportedServiceDto.SupportedServiceProtocolDto(
                                                            "postgres",
                                                            "9.0",
                                                            Collections.emptyMap())),
                                            Collections.emptyMap()))),
                    new SupportedServiceDto(
                            "dsb-for-s3",
                            "s31",
                            "s3",
                            "S3 service",
                            Collections.singletonList(
                                    new SupportedServiceDto.SupportedServicePlanDto(
                                            "default",
                                            "default",
                                            "default",
                                            null,
                                            Collections.singletonList(
                                                    new SupportedServiceDto.SupportedServiceProtocolDto(
                                                            "s3",
                                                            "1.0",
                                                            Collections.emptyMap())),
                                            Collections.emptyMap())
                            )));
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

    private void expectNotFoundException(Runnable r) {
        try {
            r.run();
            Assert.fail("Should have raised not found exception");
        } catch (NotFoundException ex) {
            // This is valid, yey
        }
    }

    private void expectWebApplicationException(Runnable r, int statusCode) {
        try {
            r.run();
            Assert.fail("Should have raised web app exception with code " + statusCode);
        } catch (WebApplicationException ex) {
            Assert.assertEquals(statusCode, ex.getResponse().getStatus());
        }
    }

}
