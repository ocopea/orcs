// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.commands;

import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.hub.DsbResolver;
import com.emc.ocopea.hub.application.AppServiceExternalDependency;
import com.emc.ocopea.hub.application.ApplicationServiceTemplate;
import com.emc.ocopea.hub.application.ApplicationTemplate;
import com.emc.ocopea.hub.application.ApplicationTemplateManagerService;
import com.emc.ocopea.hub.application.CreateDeploymentPlanCommandArgs;
import com.emc.ocopea.hub.application.DeployAppCommandArgs;
import com.emc.ocopea.hub.policy.AppPolicy;
import com.emc.ocopea.hub.site.Site;
import com.emc.ocopea.hub.site.SiteManagerService;
import com.emc.ocopea.site.DsbCatalogWebApi;
import com.emc.ocopea.site.SitePsbDetailedInfoDto;
import com.emc.ocopea.site.SiteWebApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by liebea on 5/17/16.
 * Drink responsibly
 */
public class CreateAppDeploymentPlanCommand extends HubCommand<
        CreateDeploymentPlanCommandArgs,
        DeployAppCommandArgs.AppTemplateDeploymentPlanDTO> {
    private final SiteManagerService siteManagerService;
    private final WebAPIResolver webAPIResolver;
    private final ApplicationTemplateManagerService applicationTemplateManagerService;
    private static final Random random = new Random();
    private static final Logger logger = LoggerFactory.getLogger(CreateAppDeploymentPlanCommand.class);

    public CreateAppDeploymentPlanCommand(
            SiteManagerService siteManagerService,
            WebAPIResolver webAPIResolver, ApplicationTemplateManagerService applicationTemplateManagerService) {
        this.siteManagerService = siteManagerService;
        this.webAPIResolver = webAPIResolver;
        this.applicationTemplateManagerService = applicationTemplateManagerService;
    }

    @Override
    protected DeployAppCommandArgs.AppTemplateDeploymentPlanDTO run(
            CreateDeploymentPlanCommandArgs createDeploymentPlanCommandArgs) {

        validateEmptyField("appTemplateId", createDeploymentPlanCommandArgs.getAppTemplateId());

        // Getting the application template
        ApplicationTemplate appTemplate = getTemplate(createDeploymentPlanCommandArgs);

        //todo:amit:multi-site
        // Deciding on which site to run
        Site site = findSite();

        // Find space to run on
        final String spaceName = findSpace(site);

        logger.info("Selected site {} space {}", site.getUrn(), spaceName);

        // Use DSB Resolver to find DSBs matching the requested app protocols
        DsbResolver dsbResolver = new DsbResolver(
                () -> webAPIResolver.getWebAPI(site.getUrl(), DsbCatalogWebApi.class).getCatalog(),
                site.getName());

        //todo: assuming one, need to find one that has the version!
        final String artifactRegistryName = site.getWebAPIConnection().resolve(SiteWebApi.class)
                .listArtifactRegistries().iterator().next().getName();

        return new DeployAppCommandArgs.AppTemplateDeploymentPlanDTO(
                appTemplate.getAppServiceTemplates()
                        .stream()
                        .collect(Collectors.toMap(
                                ApplicationServiceTemplate::getAppServiceName,
                                s -> new DeployAppCommandArgs.AppServiceDeploymentPlanDTO(
                                        true,
                                        spaceName,
                                        artifactRegistryName,
                                        s.getImageVersion()))),

                appTemplate.getAppServiceTemplates()
                        .stream()
                        .flatMap(st -> st.getDependencies().stream())
                        .collect(Collectors.toMap(AppServiceExternalDependency::getName, d -> {

                            final DsbResolver.ProtocolDsbMatch dsbMatch = dsbResolver.listDSBs(d.getProtocols())
                                    .stream()
                                    .findFirst()
                                    .orElseThrow(() ->
                                            new InternalServerErrorException(
                                                    "Could not match dsb for " + d.getName()));

                            return new DeployAppCommandArgs.DataServiceDeploymentPlanDTO(
                                    dsbMatch.getService().getUrn(),
                                    dsbMatch.getPlan().getId(),
                                    dsbMatch.getProtocol().getProtocolName(),
                                    true,
                                    Collections.emptyMap());
                        }, (first, dup) -> first))
        );
    }

    static String findSpace(Site site) {

        final Collection<SitePsbDetailedInfoDto> psbs =
                site.getWebAPIConnection().resolve(SiteWebApi.class).listPsbsDetailed();

        // todo: orcs hack
        Optional<SitePsbDetailedInfoDto.SitePSBSpaceInfo> space = psbs
                .stream()
                .flatMap(psb -> psb.getSpaces().stream())
                .filter(s -> s.getName().equalsIgnoreCase("Orcs"))
                .findFirst();

        if (!space.isPresent()) {
            space = psbs
                    .stream()
                    .flatMap(psb -> psb.getSpaces().stream())
                    .findAny();
        }

        if (!space.isPresent()) {
            throw new InternalServerErrorException("No space found on site " + site.getName());
        }

        return space.get().getName();
    }

    private ApplicationTemplate getTemplate(CreateDeploymentPlanCommandArgs deployAppCommandArgs) {
        // Verifying template is supported
        ApplicationTemplate appTemplate =
                applicationTemplateManagerService.getAppTemplateById(deployAppCommandArgs.getAppTemplateId(), true);
        if (appTemplate == null) {
            throw new BadRequestException("Unsupported app template " + deployAppCommandArgs.getAppTemplateId());
        }
        return appTemplate;
    }

    static List<AppPolicy> buildPolicies(DeployAppCommandArgs deployAppCommandArgs) {
        List<AppPolicy> applyPolicies = Collections.emptyList();
        if (deployAppCommandArgs.getAppPolicies() != null) {
            applyPolicies = deployAppCommandArgs.getAppPolicies()
                    .stream()
                    .map(policyDTO -> new AppPolicy(
                            policyDTO.getPolicyType(),
                            policyDTO.getPolicyName(),
                            policyDTO.getPolicySettings() == null ?
                                    Collections.emptyMap() :
                                    new HashMap<String, String>(policyDTO.getPolicySettings())))
                    .collect(Collectors.toList());
        }
        return applyPolicies;
    }

    private Site findSite() {
        List<Site> sitesList = siteManagerService.list().stream().collect(Collectors.toList());
        if (sitesList.isEmpty()) {
            throw new InternalServerErrorException("Could not find any attached sites to run the app");
        }

        //todo: just choosing random - hahahaha
        return sitesList.get(random.nextInt(sitesList.size()));
    }
}
