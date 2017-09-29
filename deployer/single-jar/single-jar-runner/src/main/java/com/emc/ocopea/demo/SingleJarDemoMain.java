// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.demo;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceController;
import com.emc.microservice.MicroServiceInitializationHelper;
import com.emc.microservice.bootstrap.AbstractSchemaBootstrap;
import com.emc.microservice.config.ConfigurationAPI;
import com.emc.microservice.configuration.client.RemoteConfigurationClient;
import com.emc.microservice.inspector.InspectorMicroService;
import com.emc.microservice.registry.RegistryClientDescriptor;
import com.emc.microservice.registry.ServiceRegistryApi;
import com.emc.microservice.resource.DefaultWebApiResolver;
import com.emc.microservice.restapi.MicroServiceWebServer;
import com.emc.microservice.runner.MicroServiceRunner;
import com.emc.microservice.standalone.web.UndertowWebServerConfiguration;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.demo.dsb.h2.H2DSBMicroService;
import com.emc.ocopea.demo.dsb.shpanblob.DuplicateShpanBlobDSBMicroService;
import com.emc.ocopea.demo.dsb.shpanblob.ShpanBlobDSBMicroService;
import com.emc.ocopea.hub.HubMicroService;
import com.emc.ocopea.hub.ShpanPaaSResourceProvider;
import com.emc.ocopea.hub.auth.AuthFilter;
import com.emc.ocopea.hub.copy.ShpanCopyRepositoryMicroService;
import com.emc.ocopea.hub.psb.shpanpaas.ShpanPaasPsbMicroService;
import com.emc.ocopea.hub.repository.HubRepositorySchema;
import com.emc.ocopea.hub.webapp.HubWebAppMicroService;
import com.emc.ocopea.hub.webapp.HubWebAppTestDevWebApi;
import com.emc.ocopea.hub.webapp.HubWebAppWebApi;
import com.emc.ocopea.hub.webapp.UIAppServiceConfiguration;
import com.emc.ocopea.hub.webapp.UIAppServiceDeploymentPlan;
import com.emc.ocopea.hub.webapp.UIAppTemplateConfigurationForSite;
import com.emc.ocopea.hub.webapp.UIAppTemplateDeploymentPlan;
import com.emc.ocopea.hub.webapp.UIApplicationTemplate;
import com.emc.ocopea.hub.webapp.UICommandCreateAppTemplate;
import com.emc.ocopea.hub.webapp.UICommandDeployTestDevAppArgs;
import com.emc.ocopea.hub.webapp.UICreateAppServiceExternalDependency;
import com.emc.ocopea.hub.webapp.UICreateAppServiceExternalDependencyProtocols;
import com.emc.ocopea.hub.webapp.UICreateApplicationServiceTemplate;
import com.emc.ocopea.hub.webapp.UIDSBConfiguration;
import com.emc.ocopea.hub.webapp.UIDSBPlanConfiguration;
import com.emc.ocopea.hub.webapp.UIDataServiceConfiguration;
import com.emc.ocopea.hub.webapp.UIDataServiceDeploymentPlan;
import com.emc.ocopea.hub.webapp.UISiteTopology;
import com.emc.ocopea.protection.ProtectionMicroService;
import com.emc.ocopea.protection.ProtectionRepositorySchema;
import com.emc.ocopea.site.SiteLocationDTO;
import com.emc.ocopea.site.SiteMicroService;
import com.emc.ocopea.site.repository.SiteRepositorySchema;
import com.emc.ocopea.util.MapBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SingleJarDemoMain {

    private static ShpanPaaSResourceProvider rp;
    private static final String shpanCopyRepoUrn = "shpan-copy-store";
    private static final String h2ServiceURN = "h2-dsb";
    private static final String shpanBlobServiceUrn = "shpanblob-dsb";
    private static final String dupShpanBlobServiceUrn = "shpanblob-dup-dsb";
    private static final String siteUrn = "site";
    private static final String shpanPaasPsbUrn = "shpanpaas-psb";

    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
        run();
    }

    static String run() throws IOException, SQLException {
        initRP();

        // Setting configuration variables to load as service configurations
        String siteName = System.getenv("SITE_NAME");
        if (siteName == null || siteName.isEmpty()) {
            siteName = getHostName();
        }
        System.setProperty("site_site-name", siteName);

        String publicDns = System.getenv("SITE_PUBLIC_DNS");
        if (publicDns != null && publicDns.length() > 0) {
            System.setProperty("site_public-load-balancer", publicDns);
        }

        String location = System.getenv("SITE_LOCATION");
        if (location == null || location.length() <= 0) {
            location = new ObjectMapper().writeValueAsString(new SiteLocationDTO(
                    32.1792126,
                    34.9005128,
                    "Israel",
                    Collections.emptyMap()));
        }
        System.setProperty("site_location", location);

        //rp.setWebAuthentication("shpandrak", "1234");

        ServiceRegistryApi serviceRegistryApi = rp.getServiceRegistryApi();

        serviceRegistryApi.registerExternalResource(
                RegistryClientDescriptor.REGISTRY_CLIENT_RESOURCE_NAME,
                Collections.emptyMap());

        final MicroService access = Boolean.getBoolean("allowUnauthenticatedAccess") ?
                new HubWebAppMicroService() {
                    @Override
                    public MicroServiceInitializationHelper getInitializationHelper() {
                        MicroServiceInitializationHelper initializationHelper =
                                super.getInitializationHelper();
                        initializationHelper
                                .getManagedRestProviders()
                                .removeIf(descriptor -> descriptor.getResourceClass() ==
                                        AuthFilter.class);
                        if (initializationHelper
                                .getManagedRestProviders()
                                .stream()
                                .noneMatch(descriptor ->
                                        descriptor.getResourceClass() == MockAuthFilter.class)) {
                            initializationHelper.withRestProvider(
                                    MockAuthFilter.class,
                                    "mock authentication filter");
                        }
                        return initializationHelper;
                    }
                }
                :
                new HubWebAppMicroService();
        final ArrayList<MicroServiceRunner.ServiceDeploymentDescriptor> serviceDeploymentDescriptors =
                Stream.of(
                        new InspectorMicroService(),
                        new ProtectionMicroService(),
                        new SiteMicroService(),
                        new H2DSBMicroService(),
                        new ShpanCopyRepositoryMicroService(),
                        new ShpanPaasPsbMicroService(),
                        new ShpanBlobDSBMicroService(),
                        new DuplicateShpanBlobDSBMicroService(),
                        new HubMicroService(), access
                )
                        .peek(microService ->
                                System.setProperty(
                                        microService.getIdentifier().getShortName() + "_print-all-json-requests",
                                        Boolean.toString(true)))
                        .map(microService ->
                                new MicroServiceRunner.ServiceDeploymentDescriptor(
                                        microService.getIdentifier().getShortName(),
                                        microService)
                        )
                        .collect(Collectors.toCollection(ArrayList::new));

        final Map<String, MicroServiceController> controllers = new MicroServiceRunner().run(
                rp,
                serviceDeploymentDescriptors
        );
        UndertowWebServerConfiguration webServerConfiguration =
                serviceRegistryApi.getWebServerConfiguration(UndertowWebServerConfiguration.class);
        MicroServiceWebServer webServer = rp.getWebServer(webServerConfiguration);
        System.out.println("Single Jar demo running on port: " + webServer.getPort());
        ApiInvoker api = new ApiInvoker(rp);
        final String urlPrefix = "http://localhost:" + webServer.getPort();
        final String siteUrl = urlPrefix + "/site-api";

        prepareSite(
                webServer.getPort(),
                shpanCopyRepoUrn,
                h2ServiceURN,
                shpanBlobServiceUrn,
                dupShpanBlobServiceUrn,
                siteUrn,
                shpanPaasPsbUrn,
                api);
        api.addCustomRestArtifactRegistry(
                siteUrl,
                "shpanRegistry",
                "http://localhost:" + webServer.getPort() + "/" + shpanPaasPsbUrn + "-api/artifact-registry");

        api.addJiraIntegration(
                "https://jira.cec.lab.emc.com:8443",
                "10305",
                "10004"
        );

        createAppTemplates(api);

        runDevApp(api);

        String hubWebEntryPoint = "http://localhost:" + webServer.getPort() + "/hub-web-api";
        System.out.println(hubWebEntryPoint + "/html/nui/index.html");
        return hubWebEntryPoint;
    }

    private static void runDevApp(ApiInvoker api) {
        if (Boolean.getBoolean("devApp")) {
            final Collection<UIApplicationTemplate> uiApplicationTemplates =
                    api.proxy("hub-web", HubWebAppWebApi.class).listAppTemplates();

            final UUID hackathonAppTemplateId = uiApplicationTemplates
                    .stream()
                    .filter(uiApplicationTemplate -> uiApplicationTemplate.getName().equalsIgnoreCase("hackathon"))
                    .findFirst().get().getId();

            final UISiteTopology siteTopology =
                    api.proxy("hub-web", HubWebAppWebApi.class).listSiteTopologies().iterator().next();
            final UUID siteId = siteTopology.getId();
            final String space = siteTopology.getSpaces().iterator().next();

            final UIAppTemplateConfigurationForSite appTemplateConfigurationBySite =
                    api
                            .proxy("hub-web", HubWebAppTestDevWebApi.class)
                            .getAppTemplateConfigurationBySite(siteId, hackathonAppTemplateId);

            api.proxy("hub-web", HubWebAppWebApi.class).deployTestDevAppCommand(new UICommandDeployTestDevAppArgs(
                    "dev1",
                    hackathonAppTemplateId,
                    siteId,
                    new UIAppTemplateDeploymentPlan(
                            appTemplateConfigurationBySite
                                    .getAppServiceConfigurations()
                                    .stream()
                                    .collect(Collectors.toMap(
                                            UIAppServiceConfiguration::getAppServiceName,
                                            as -> new UIAppServiceDeploymentPlan(
                                                    true,
                                                    space,
                                                    as.getSupportedVersions().keySet().iterator().next(),
                                                    as
                                                            .getSupportedVersions()
                                                            .values()
                                                            .iterator()
                                                            .next()
                                                            .iterator()
                                                            .next())
                                    )),
                            appTemplateConfigurationBySite
                                    .getDataServiceConfigurations()
                                    .stream()
                                    .collect(Collectors.toMap(
                                            UIDataServiceConfiguration::getDataServiceName,
                                            ds -> {
                                                final Map.Entry<String, UIDSBConfiguration> dsbPlans =
                                                        ds.getDsbPlans().entrySet().iterator().next();
                                                final UIDSBPlanConfiguration plan = dsbPlans
                                                        .getValue()
                                                        .getPlans()
                                                        .stream()
                                                        .filter(p -> p.getName().equals("default"))
                                                        .findFirst()
                                                        .orElseGet(() ->
                                                                dsbPlans
                                                                        .getValue()
                                                                        .getPlans()
                                                                        .iterator()
                                                                        .next());

                                                return new UIDataServiceDeploymentPlan(
                                                        dsbPlans.getKey(),
                                                        true,
                                                        plan.getId(),
                                                        plan.getProtocols().iterator().next());
                                            }
                                    ))
                    )
            ));
        }
    }

    private static void createAppTemplates(ApiInvoker api) {
        api.createAppTemplate(
                new UICommandCreateAppTemplate(
                        "hackathon",
                        "1.0",
                        "Hackathon Registration App",
                        "hackathon-svc",
                        Collections.singletonList(
                                new UICreateApplicationServiceTemplate(
                                        "hackathon-svc",
                                        "ShpanPaaS",
                                        "hackathon",
                                        "java",
                                        "1.0",
                                        Collections.emptyMap(),
                                        Collections.emptyMap(),
                                        Arrays.asList(
                                                new UICreateAppServiceExternalDependency(
                                                        UICreateAppServiceExternalDependency.TypeEnum.DATABASE,
                                                        "hackathon-db",
                                                        "Hackathon ideas DB",
                                                        Collections.singletonList(
                                                                new UICreateAppServiceExternalDependencyProtocols(
                                                                        "postgres",
                                                                        "9.0",
                                                                        null,
                                                                        null))
                                                ),
                                                new UICreateAppServiceExternalDependency(
                                                        UICreateAppServiceExternalDependency.TypeEnum.OBJECTSTORE,
                                                        "hack-docs",
                                                        "Hackathon document store",
                                                        Collections.singletonList(
                                                                new UICreateAppServiceExternalDependencyProtocols(
                                                                        "s3",
                                                                        null,
                                                                        null,
                                                                        null)))),
                                        Collections.singletonList(8080),
                                        8080,
                                        "hackathon-api/html/nui/index.html"
                                ))),
                SingleJarDemoMain.class.getResourceAsStream("/hackathon.png"));

        api.createAppTemplate(
                new UICommandCreateAppTemplate(
                        "hackathon-pro",
                        "1.0",
                        "Hackathon Pro Registration App",
                        "hackathon-svc",
                        Arrays.asList(
                                new UICreateApplicationServiceTemplate(
                                        "hackathon-svc",
                                        "ShpanPaaS",
                                        "hackathon",
                                        "java",
                                        "1.0",
                                        Collections.emptyMap(),
                                        MapBuilder.<String, String>newHashMap()
                                                .with("hackathon-pro-mode", Boolean.toString(true))
                                                .build(),
                                        Arrays.asList(
                                                new UICreateAppServiceExternalDependency(
                                                        UICreateAppServiceExternalDependency.TypeEnum.DATABASE,
                                                        "hackathon-db",
                                                        "Hackathon ideas DB",
                                                        Collections.singletonList(
                                                                new UICreateAppServiceExternalDependencyProtocols(
                                                                        "postgres",
                                                                        "9.0",
                                                                        null,
                                                                        null))
                                                ),
                                                new UICreateAppServiceExternalDependency(
                                                        UICreateAppServiceExternalDependency.TypeEnum.OBJECTSTORE,
                                                        "hack-docs",
                                                        "Hackathon document store",
                                                        Collections.singletonList(
                                                                new UICreateAppServiceExternalDependencyProtocols(
                                                                        "s3",
                                                                        null,
                                                                        null,
                                                                        null))
                                                )),
                                        Collections.singletonList(8080),
                                        8080,
                                        "hackathon-api/html/nui/index.html"),
                                new UICreateApplicationServiceTemplate(
                                        "committee-svc",
                                        "ShpanPaaS",
                                        "committee",
                                        "java",
                                        "2.1",
                                        Collections.emptyMap(),
                                        Collections.emptyMap(),
                                        Collections.singletonList(
                                                new UICreateAppServiceExternalDependency(
                                                        UICreateAppServiceExternalDependency.TypeEnum.DATABASE,
                                                        "hackathon-db",
                                                        "Hackathon ideas DB",
                                                        Collections.singletonList(
                                                                new UICreateAppServiceExternalDependencyProtocols(
                                                                        "postgres",
                                                                        "9.0",
                                                                        null,
                                                                        null))
                                                )),
                                        Collections.singletonList(8080),
                                        8080,
                                        "committee-api/html/cui/index.html"))),
                SingleJarDemoMain.class.getResourceAsStream("/hackathon-pro.png"));

        // Supporting Additional apps flag
        if (Boolean.getBoolean("additionalApps")) {
            api.createAppTemplate(
                    new UICommandCreateAppTemplate(
                            "wordpress",
                            "1.0",
                            "Wordpress content management",
                            "wordpress",
                            Collections.singletonList(
                                    new UICreateApplicationServiceTemplate(
                                            "wordpress",
                                            "ShpanPaaS",
                                            "wordpress",
                                            "apache-webserver",
                                            "1.0",
                                            Collections.emptyMap(),
                                            Collections.emptyMap(),
                                            Arrays.asList(
                                                    new UICreateAppServiceExternalDependency(
                                                            UICreateAppServiceExternalDependency.TypeEnum.DATABASE,
                                                            "configuration",
                                                            "wordpress configuration db",
                                                            Collections.singletonList(
                                                                    new UICreateAppServiceExternalDependencyProtocols(
                                                                            "mysql",
                                                                            null,
                                                                            null,
                                                                            null))
                                                    ),
                                                    new UICreateAppServiceExternalDependency(
                                                            UICreateAppServiceExternalDependency.TypeEnum.OTHER,
                                                            "documents",
                                                            "Wordpress documents",
                                                            Collections.singletonList(
                                                                    new UICreateAppServiceExternalDependencyProtocols(
                                                                            "docker-volume",
                                                                            null,
                                                                            null,
                                                                            null))
                                                    )),
                                            Collections.singletonList(8080),
                                            8080,
                                            ""))),
                    SingleJarDemoMain.class.getResourceAsStream("/wordpress.png"));

            api.createAppTemplate(
                    new UICommandCreateAppTemplate(
                            "hackathon-bad",
                            "1.0",
                            "Bad Hackathon Registration App",
                            "hackathon-bad-svc",
                            Collections.singletonList(
                                    new UICreateApplicationServiceTemplate(
                                            "hackathon-bad-svc",
                                            "k8s",
                                            "hackathon",
                                            "java",
                                            "1.0",
                                            Collections.emptyMap(),
                                            Collections.emptyMap(),
                                            Arrays.asList(
                                                    new UICreateAppServiceExternalDependency(
                                                            UICreateAppServiceExternalDependency.TypeEnum.DATABASE,
                                                            "hackathon-db",
                                                            "Hackathon ideas DB",
                                                            Collections.singletonList(
                                                                    new UICreateAppServiceExternalDependencyProtocols(
                                                                            "postgres",
                                                                            "9.0",
                                                                            null,
                                                                            null))
                                                    ),
                                                    new UICreateAppServiceExternalDependency(
                                                            UICreateAppServiceExternalDependency.TypeEnum.OBJECTSTORE,
                                                            "hack-docs",
                                                            "Hackathon document store",
                                                            Collections.singletonList(
                                                                    new UICreateAppServiceExternalDependencyProtocols(
                                                                            "s3",
                                                                            null,
                                                                            null,
                                                                            null))
                                                    )),
                                            Collections.singletonList(8080),
                                            8080,
                                            "hackathon-api/html")
                            )),
                    SingleJarDemoMain.class.getResourceAsStream("/hackathon.png"));

        }

        // Supporting complex app
        if (Boolean.getBoolean("complexApp")) {
            api.createAppTemplate(
                    new UICommandCreateAppTemplate(
                            "complex-app",
                            "1.0",
                            "Complex app that looks like the hackathon",
                            "hackathon-svc",
                            Arrays.asList(
                                    new UICreateApplicationServiceTemplate(
                                            "hackathon-svc",
                                            "k8s",
                                            "hackathon",
                                            "java",
                                            "1.0",
                                            Collections.emptyMap(),
                                            Collections.emptyMap(),
                                            Arrays.asList(
                                                    new UICreateAppServiceExternalDependency(
                                                            UICreateAppServiceExternalDependency.TypeEnum.DATABASE,
                                                            "hackathon-db",
                                                            "Hackathon ideas DB",
                                                            Collections.singletonList(
                                                                    new UICreateAppServiceExternalDependencyProtocols(
                                                                            "postgres",
                                                                            "9.0",
                                                                            null,
                                                                            null))
                                                    ),
                                                    new UICreateAppServiceExternalDependency(
                                                            UICreateAppServiceExternalDependency.TypeEnum.OBJECTSTORE,
                                                            "hack-docs",
                                                            "Hackathon document store",
                                                            Collections.singletonList(
                                                                    new UICreateAppServiceExternalDependencyProtocols(
                                                                            "s3",
                                                                            null,
                                                                            null,
                                                                            null))
                                                    ),
                                                    new UICreateAppServiceExternalDependency(
                                                            UICreateAppServiceExternalDependency.TypeEnum.MESSAGING,
                                                            "messaging",
                                                            "Messaging",
                                                            Collections.singletonList(
                                                                    new UICreateAppServiceExternalDependencyProtocols(
                                                                            "rabbitmq",
                                                                            null,
                                                                            null,
                                                                            null))
                                                    )),
                                            Collections.singletonList(8080),
                                            8080,
                                            "hackathon-api/html"),
                                    new UICreateApplicationServiceTemplate(
                                            "submission-evaluator",
                                            "k8s",
                                            "hackathon-submission-eval",
                                            "java",
                                            "1.0",
                                            Collections.emptyMap(),
                                            Collections.emptyMap(),
                                            Collections.singletonList(
                                                    new UICreateAppServiceExternalDependency(
                                                            UICreateAppServiceExternalDependency.TypeEnum.MESSAGING,
                                                            "messaging",
                                                            "Messaging",
                                                            Collections.singletonList(
                                                                    new UICreateAppServiceExternalDependencyProtocols(
                                                                            "rabbitmq",
                                                                            null,
                                                                            null,
                                                                            null))
                                                    )),
                                            Collections.singletonList(8080),
                                            8080,
                                            null)
                            )),
                    SingleJarDemoMain.class.getResourceAsStream("/hackathon.png"));

            api.createAppTemplate(new UICommandCreateAppTemplate(
                    "complex-app2",
                    "1.0",
                    "Complex app2 that looks like the hackathon",
                    "hackathon-svc",
                    Arrays.asList(
                            new UICreateApplicationServiceTemplate(
                                    "hackathon-svc",
                                    "k8s",
                                    "hackathon",
                                    "java",
                                    "1.0",
                                    Collections.emptyMap(),
                                    Collections.emptyMap(),
                                    Arrays.asList(
                                            new UICreateAppServiceExternalDependency(
                                                    UICreateAppServiceExternalDependency.TypeEnum.DATABASE,
                                                    "hackathon-db",
                                                    "Hackathon ideas DB",
                                                    Collections.singletonList(
                                                            new UICreateAppServiceExternalDependencyProtocols(
                                                                    "postgres",
                                                                    "9.0",
                                                                    null,
                                                                    null))
                                            ),
                                            new UICreateAppServiceExternalDependency(
                                                    UICreateAppServiceExternalDependency.TypeEnum.DATABASE,
                                                    "configuration-db",
                                                    "Hackathon configuration",
                                                    Collections.singletonList(
                                                            new UICreateAppServiceExternalDependencyProtocols(
                                                                    "postgres",
                                                                    "9.0",
                                                                    null,
                                                                    null))
                                            ),
                                            new UICreateAppServiceExternalDependency(
                                                    UICreateAppServiceExternalDependency.TypeEnum.OBJECTSTORE,
                                                    "hack-docs",
                                                    "Hackathon document store",
                                                    Collections.singletonList(
                                                            new UICreateAppServiceExternalDependencyProtocols(
                                                                    "s3",
                                                                    null,
                                                                    null,
                                                                    null))
                                            ),
                                            new UICreateAppServiceExternalDependency(
                                                    UICreateAppServiceExternalDependency.TypeEnum.MESSAGING,
                                                    "messaging",
                                                    "Messaging",
                                                    Collections.singletonList(
                                                            new UICreateAppServiceExternalDependencyProtocols(
                                                                    "rabbitmq",
                                                                    null,
                                                                    null,
                                                                    null))
                                            )),
                                    Collections.singletonList(8080),
                                    8080,
                                    "hackathon-api/html"),
                            new UICreateApplicationServiceTemplate(
                                    "hackathon-submission-eval",
                                    "k8s",
                                    "submission evaluator",
                                    "java",
                                    "1.0",
                                    Collections.emptyMap(),
                                    Collections.emptyMap(),
                                    Arrays.asList(
                                            new UICreateAppServiceExternalDependency(
                                                    UICreateAppServiceExternalDependency.TypeEnum.MESSAGING,
                                                    "messaging",
                                                    "Messaging",
                                                    Collections.singletonList(
                                                            new UICreateAppServiceExternalDependencyProtocols(
                                                                    "rabbitmq",
                                                                    null,
                                                                    null,
                                                                    null))
                                            ),
                                            new UICreateAppServiceExternalDependency(
                                                    UICreateAppServiceExternalDependency.TypeEnum.DATABASE,
                                                    "profile-db",
                                                    "Mongo db",
                                                    Collections.singletonList(
                                                            new UICreateAppServiceExternalDependencyProtocols(
                                                                    "mongo",
                                                                    null,
                                                                    null,
                                                                    null))
                                            )
                                    ),
                                    Collections.singletonList(8080),
                                    8080,
                                    "hackathon-eval-api/html"),
                            new UICreateApplicationServiceTemplate(
                                    "gals-nightmare",
                                    "k8s",
                                    "gals-nightmare",
                                    "java",
                                    "1.1",
                                    Collections.emptyMap(),
                                    Collections.emptyMap(),
                                    Arrays.asList(
                                            new UICreateAppServiceExternalDependency(
                                                    UICreateAppServiceExternalDependency.TypeEnum.MESSAGING,
                                                    "messaging",
                                                    "Messaging",
                                                    Collections.singletonList(
                                                            new UICreateAppServiceExternalDependencyProtocols(
                                                                    "rabbitmq",
                                                                    null,
                                                                    null,
                                                                    null))
                                            ),
                                            new UICreateAppServiceExternalDependency(
                                                    UICreateAppServiceExternalDependency.TypeEnum.DATABASE,
                                                    "configuration-db",
                                                    "Hackathon configuration",
                                                    Collections.singletonList(
                                                            new UICreateAppServiceExternalDependencyProtocols(
                                                                    "postgres",
                                                                    "9.0",
                                                                    null,
                                                                    null))
                                            ),
                                            new UICreateAppServiceExternalDependency(
                                                    UICreateAppServiceExternalDependency.TypeEnum.DATABASE,
                                                    "customers-db",
                                                    "Customers database",
                                                    Collections.singletonList(
                                                            new UICreateAppServiceExternalDependencyProtocols(
                                                                    "postgres",
                                                                    "9.0",
                                                                    null,
                                                                    null))
                                            ),
                                            new UICreateAppServiceExternalDependency(
                                                    UICreateAppServiceExternalDependency.TypeEnum.DATABASE,
                                                    "profile-db",
                                                    "Mongo db",
                                                    Collections.singletonList(
                                                            new UICreateAppServiceExternalDependencyProtocols(
                                                                    "mongo",
                                                                    null,
                                                                    null,
                                                                    null))
                                            )
                                    ),
                                    Collections.singletonList(8080),
                                    8080,
                                    "hackathon-api/html")
                    )));
        }
    }

    private static void prepareSite(
            int serverPort,
            String shpanCopyRepoUrn,
            String h2ServiceUrn,
            String shpanBlobServiceUrn,
            String dupShpanBlobServiceUrn,
            String siteUrn,
            String psbUrn,
            ApiInvoker api) {

        final String urlPrefix = "http://localhost:" + serverPort;
        final String siteUrl = urlPrefix + "/site-api";

        // Add h2 DSB
        api.addDsb(siteUrl, h2ServiceUrn, urlPrefix + "/h2-dsb-api");
        api.addCrb(siteUrl, shpanCopyRepoUrn, urlPrefix + "/shpan-copy-store-api");

        // Adding shpanblob dsb to site
        api.addDsb(siteUrl, shpanBlobServiceUrn, urlPrefix + "/shpanblob-dsb-api");
        api.addDsb(siteUrl, dupShpanBlobServiceUrn, urlPrefix + "/shpanblob-dup-dsb-api");

        // Adding ShpanPaaSPSB to site
        api.addPsb(siteUrl, psbUrn, urlPrefix + "/shpanpaas-psb-api");

        api.addSiteToHub(siteUrn, siteUrl);
    }

    private static void initRP() throws IOException, SQLException {

        final Map<String, AbstractSchemaBootstrap> schemaBootstrapMap = new HashMap<>();
        schemaBootstrapMap.put("hub-db", new HubRepositorySchema());
        schemaBootstrapMap.put("protection-db", new ProtectionRepositorySchema());
        schemaBootstrapMap.put("site-db", new SiteRepositorySchema());
        WebAPIResolver apiResolver = new DefaultWebApiResolver();
        String remoteConfPort = System.getProperty("remoteConfPort", null);
        if (remoteConfPort != null) {
            ConfigurationAPI remoteConfigurationClient = new RemoteConfigurationClient(
                    new RemoteConfigurationClient.RestClientResolver() {
                        @Override
                        public <T> T resolve(Class<T> webInterface, URI remoteService, boolean verifySSL) {
                            return apiResolver.getWebAPI(remoteService.toString(), webInterface);
                        }
                    },
                    URI.create("http://localhost:" + remoteConfPort + "/configuration-api"),
                    false);
            rp = new ShpanPaaSResourceProvider(schemaBootstrapMap, remoteConfigurationClient);
        } else {
            rp = new ShpanPaaSResourceProvider(schemaBootstrapMap);
        }
    }

    private static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "localhost";
        }
    }

    @Provider
    @PreMatching
    public static class MockAuthFilter extends AuthFilter {
        @Override
        protected String[] getLap(ContainerRequestContext containerRequest) {
            return new String[]{"shpandrak", "1234"};
        }
    }
}
