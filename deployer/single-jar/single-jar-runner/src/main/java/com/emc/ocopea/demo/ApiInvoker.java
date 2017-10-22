// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.demo;

import com.emc.microservice.resource.DefaultWebApiResolver;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.microservice.webclient.WebApiResolverBuilder;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.hub.application.HubWebApi;
import com.emc.ocopea.hub.site.AddSiteToHubCommandArgs;
import com.emc.ocopea.hub.webapp.HubWebAppImageStoreWebApi;
import com.emc.ocopea.hub.webapp.HubWebAppUserWebApi;
import com.emc.ocopea.hub.webapp.HubWebAppWebApi;
import com.emc.ocopea.hub.webapp.UIApplicationTemplate;
import com.emc.ocopea.hub.webapp.UICommandAddJiraIntegration;
import com.emc.ocopea.hub.webapp.UICommandCreateAppTemplate;
import com.emc.ocopea.hub.webapp.UIUser;
import com.emc.ocopea.site.AddCustomArtifactRegistryToSiteCommandArgs;
import com.emc.ocopea.site.AddMavenArtifactRegistryToSiteCommandArgs;
import com.emc.ocopea.site.RegisterCrbToSiteCommandArgs;
import com.emc.ocopea.site.RegisterDsbToSiteCommandArgs;
import com.emc.ocopea.site.RegisterPsbToSiteCommandArgs;
import com.emc.ocopea.site.SiteWebApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Created by liebea on 10/15/17.
 * Drink responsibly
 */
public class ApiInvoker {
    private static Logger log = LoggerFactory.getLogger(ApiInvoker.class);
    private final ResourceProvider resourceProvider;
    private final WebAPIResolver webAPIResolver;

    public ApiInvoker(ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
        this.webAPIResolver = getResolver();
    }

    protected WebAPIResolver getResolver() {
        return new DefaultWebApiResolver().buildResolver(
                new WebApiResolverBuilder()
                        .withBasicAuthentication("admin", "nazgul")
                        .withVerifySsl(false));
    }

    private HubWebAppWebApi getHubWebAppWebApi() {
        return proxy("hub-web", HubWebAppWebApi.class);
    }

    public void createAppTemplate(UICommandCreateAppTemplate createAppTemplate) {
        createAppTemplate(createAppTemplate, null);
    }

    /**
     * Creates an application template on the hub
     */
    public void createAppTemplate(
            UICommandCreateAppTemplate createAppTemplate,
            InputStream appTemplateImageStream) {
        final List<UIApplicationTemplate> allTemplates = proxy("hub-web", HubWebAppWebApi.class).listAppTemplates();
        if (allTemplates.stream().anyMatch(t -> t.getName().equals(createAppTemplate.getName()))) {
            log.info("App template for " + createAppTemplate.getName() + " already exist, skipping creation");
        } else {
            UUID appInstanceId = proxy("hub-web", HubWebAppWebApi.class).createAppTemplate(createAppTemplate);
            if (appTemplateImageStream != null) {
                uploadAppTemplateIcon(appInstanceId, appTemplateImageStream);
            }
        }
    }

    @NoJavadoc
    // TODO add javadoc
    public void addJiraIntegration(String jiraUrl, String jiraProjectId, String jiraIssueTypeId) {
        getHubWebAppWebApi().addJiraIntegration(
                new UICommandAddJiraIntegration(
                        jiraUrl,
                        jiraProjectId,
                        jiraIssueTypeId
                )
        );
    }

    public Collection<UIUser> listUsers() {
        return proxy("hub-web", HubWebAppUserWebApi.class).listUsers();
    }

    private void uploadAppTemplateIcon(UUID appTemplateId, InputStream appTemplateImageStream) {
        proxy("hub-web", HubWebAppImageStoreWebApi.class)
                .uploadIcon(appTemplateImageStream, "app-template", appTemplateId.toString());
    }

    @NoJavadoc
    // TODO add javadoc
    public void addSiteToHub(String siteUrn, String siteUrl) {
        if (proxy("hub", HubWebApi.class)
                .listSites()
                .stream()
                .anyMatch(siteDTO -> siteDTO.getUrn().equals(siteUrn))) {
            log.info("Site " + siteUrn + " is already registered on hub. skipping registration");
        } else {
            proxy("hub", HubWebApi.class).addSite(new AddSiteToHubCommandArgs(siteUrn, siteUrl));
        }
    }

    public void addPsb(String siteUrl, String psbUrn, String psbUrl) {
        proxyViaUrl(siteUrl, SiteWebApi.class).registerPsb(new RegisterPsbToSiteCommandArgs(psbUrn, psbUrl));
    }

    public void addMavenArtifactRegistry(String siteUrl, String name, String url) {
        proxyViaUrl(siteUrl, SiteWebApi.class).addMavenArtifactRegistry(
                new AddMavenArtifactRegistryToSiteCommandArgs(name, url, null, null));
    }

    public void addCustomRestArtifactRegistry(String siteUrl, String name, String url) {
        proxyViaUrl(siteUrl, SiteWebApi.class).addCustomRestArtifactRegistry(
                new AddCustomArtifactRegistryToSiteCommandArgs(name, url));
    }

    public void addDsb(String siteUrl, String dsbUrn, String dsbUrl) {
        proxyViaUrl(siteUrl, SiteWebApi.class).registerDsb(new RegisterDsbToSiteCommandArgs(dsbUrn, dsbUrl));
    }

    public void addCrb(String siteUrl, String crbUrn, String crbUrl) {
        proxyViaUrl(siteUrl, SiteWebApi.class).registerCrb(new RegisterCrbToSiteCommandArgs(crbUrn, crbUrl));
    }

    public <T> T proxy(String urn, Class<T> resourceWebAPI) {
        String route = resourceProvider.getServiceRegistryApi().getServiceConfig(urn).getRoute();
        return webAPIResolver.getWebAPI(route, resourceWebAPI);
    }

    public <T> T proxyViaUrl(String url, Class<T> resourceWebAPI) {
        return webAPIResolver.getWebAPI(url, resourceWebAPI);
    }

}
