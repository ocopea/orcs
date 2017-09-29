// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.commands;

import com.emc.ocopea.hub.application.AppInstanceManagerService;
import com.emc.ocopea.hub.application.ApplicationServiceTemplate;
import com.emc.ocopea.hub.application.ApplicationTemplate;
import com.emc.ocopea.hub.application.ApplicationTemplateManagerService;
import com.emc.ocopea.hub.application.DeployAppCommandArgs;
import com.emc.ocopea.hub.application.RepurposeAppCommandArgs;
import com.emc.ocopea.hub.policy.AppPolicy;
import com.emc.ocopea.hub.repository.DBAppInstanceConfig;
import com.emc.ocopea.hub.site.Site;
import com.emc.ocopea.hub.site.SiteManagerService;
import com.emc.ocopea.site.AppInstanceCopyStatisticsDTO;
import com.emc.ocopea.site.AppInstanceStatisticsDTO;
import com.emc.ocopea.site.DsbCatalogWebApi;
import com.emc.ocopea.site.SiteArtifactRegistryInfoDTO;
import com.emc.ocopea.site.SiteWebApi;
import com.emc.ocopea.site.SupportedServiceDto;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by liebea on 5/17/16.
 * Drink responsibly
 */
public class RePurposeAppCommand extends HubCommand<RepurposeAppCommandArgs, UUID> {
    private final SiteManagerService siteManagerService;
    private final AppInstanceManagerService appInstanceManagerService;
    private final ApplicationTemplateManagerService applicationTemplateManagerService;

    public RePurposeAppCommand(
            SiteManagerService siteManagerService,
            AppInstanceManagerService appInstanceManagerService,
            ApplicationTemplateManagerService applicationTemplateManagerService) {
        this.siteManagerService = siteManagerService;
        this.appInstanceManagerService = appInstanceManagerService;
        this.applicationTemplateManagerService = applicationTemplateManagerService;
    }

    @Override
    protected UUID run(RepurposeAppCommandArgs repurposeAppCommandArgs) {

        validateEmptyField("appInstanceName", repurposeAppCommandArgs.getAppInstanceName());
        validateEmptyField("copyId", repurposeAppCommandArgs.getCopyId());

        // Getting origin instance Id
        final UUID originAppInstanceId = validateEmptyField(
                "originAppInstanceId",
                repurposeAppCommandArgs.getOriginAppInstanceId());

        DBAppInstanceConfig originAppInstance = appInstanceManagerService.getInstanceById(originAppInstanceId);
        if (originAppInstance == null) {
            throw new BadRequestException("origin appInstanceId " + originAppInstanceId + " does not exist");
        }


        // taking same site as origin...
        //todo:enable setting different site/space
        Site site = siteManagerService.getSiteById(originAppInstance.getSiteId());
        // todo:random space.. this is the production module need to support space selection in ui

        final String spaceName = CreateAppDeploymentPlanCommand.findSpace(site);

        // Setting deployment type
        String purpose = repurposeAppCommandArgs.getPurpose() == null ? "other" : repurposeAppCommandArgs.getPurpose();

        SiteWebApi originSiteWebApi = site.getWebAPIConnection().resolve(SiteWebApi.class);

        Map<String, DeployAppCommandArgs.DataServiceDeploymentPlanDTO> dataServices =
                buildDataServices(site, repurposeAppCommandArgs.getCopyId());

        final ApplicationTemplate appTemplate =
                applicationTemplateManagerService.getAppTemplateById(originAppInstance.getAppTemplateId(), true);
        List<AppPolicy> applyPolicies = Collections.emptyList();
        if (repurposeAppCommandArgs.getAppPolicies() != null) {
            applyPolicies = new ArrayList<>(repurposeAppCommandArgs.getAppPolicies().size());

            for (DeployAppCommandArgs.ApplicationPolicyInfoDTO curr : repurposeAppCommandArgs.getAppPolicies()) {
                Map<String, String> policySettings = Collections.emptyMap();
                if (curr.getPolicySettings() != null) {
                    policySettings = new HashMap<>(curr.getPolicySettings());
                }
                applyPolicies.add(new AppPolicy(curr.getPolicyType(), curr.getPolicyName(), policySettings));
            }
        }

        //todo: assuming one
        final Collection<SiteArtifactRegistryInfoDTO> artifactRegistries =
                site.getWebAPIConnection().resolve(SiteWebApi.class).listArtifactRegistries();

        if (artifactRegistries.isEmpty()) {
            throw new InternalServerErrorException("No artifact registries found on site " + site.getName());
        }
        final String artifactRegistryName = artifactRegistries.iterator().next().getName();

        Map<String, DeployAppCommandArgs.AppServiceDeploymentPlanDTO> appServices = appTemplate.getAppServiceTemplates()
                .stream()
                .collect(Collectors.toMap(
                        ApplicationServiceTemplate::getAppServiceName,
                        ast -> new DeployAppCommandArgs.AppServiceDeploymentPlanDTO(
                                true,
                                spaceName,
                                artifactRegistryName,
                                ast.getImageVersion())));

        final DeployAppCommandArgs.AppTemplateDeploymentPlanDTO deploymentPlan =
                new DeployAppCommandArgs.AppTemplateDeploymentPlanDTO(
                        appServices,
                        dataServices
                );

        try {
            return appInstanceManagerService.runApp(
                    appTemplate,
                    deploymentPlan,
                    new AppInstanceManagerService.RestoreAppInfo(
                            originAppInstance.getSiteId(),
                            repurposeAppCommandArgs.getCopyId()),
                    repurposeAppCommandArgs.getAppInstanceName(),
                    originAppInstance.getId(),
                    null,
                    repurposeAppCommandArgs.getUserId(),
                    site,
                    URLEncoder.encode(repurposeAppCommandArgs.getAppInstanceName(), "UTF-8"),
                    purpose,
                    applyPolicies);
        } catch (UnsupportedEncodingException e) {
            throw new InternalServerErrorException(e);
        }
    }

    private Map<String, DeployAppCommandArgs.DataServiceDeploymentPlanDTO> buildDataServices(
            Site site,
            UUID copyId) {

        final AppInstanceCopyStatisticsDTO appCopyMetadata =
                site.getWebAPIConnection().resolve(SiteWebApi.class).getCopyMetadata(copyId);

        final Map<String, SupportedServiceDto> catalog =
                site.getWebAPIConnection().resolve(DsbCatalogWebApi.class).getCatalog()
                    .stream()
                    .collect(Collectors.toMap(
                            SupportedServiceDto::getUrn,
                            Function.identity()
                    ));


        return appCopyMetadata.getDataServiceCopies()
                .stream()
                .collect(Collectors.toMap(
                        AppInstanceStatisticsDTO.DataServiceCopyStatisticsDTO::getBindName,
                        c -> {

                            final SupportedServiceDto dsb = catalog.get(c.getDsbUrn());
                            if (dsb == null) {
                                throw new InternalServerErrorException("Could not match dsb with urn " + c.getDsbUrn()
                                        + " on site " + site.getName());
                            }

                            // Todo: delegate selection of plan and protocol ot user
                            final SupportedServiceDto.SupportedServicePlanDto randomPlan = dsb.getPlans()
                                    .stream()
                                    .findFirst()
                                    .orElseThrow(() -> new InternalServerErrorException(
                                            "no plans found for dsb " + dsb.getName() + " on site " +
                                                    site.getName()));

                            final String protocol = randomPlan.getSupportedProtocols()
                                    .stream()
                                    .findFirst()
                                    .orElseThrow(() -> new InternalServerErrorException(
                                            "no protocols found for dsb " + dsb.getName() + " with plan " +
                                                    randomPlan.getName() + " on site " +
                                                    site.getName()))
                                    .getProtocolName();



                            return new DeployAppCommandArgs.DataServiceDeploymentPlanDTO(
                                    c.getDsbUrn(),
                                    randomPlan.getId(),
                                    protocol,
                                    true,
                                    Collections.emptyMap());
                        }
                ));

    }
}
