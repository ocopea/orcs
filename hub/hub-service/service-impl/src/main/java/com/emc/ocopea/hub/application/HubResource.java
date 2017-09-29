// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.schedule.ManagedScheduler;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.hub.AppServiceExternalDependencyProtocol;
import com.emc.ocopea.hub.HubWebAppUtil;
import com.emc.ocopea.hub.commands.CreateAppCopyCommand;
import com.emc.ocopea.hub.commands.CreateAppDeploymentPlanCommand;
import com.emc.ocopea.hub.commands.CreateAppTemplateCommand;
import com.emc.ocopea.hub.commands.CreateSavedImageCommand;
import com.emc.ocopea.hub.commands.DeployAppCommand;
import com.emc.ocopea.hub.commands.DeploySavedImageCommand;
import com.emc.ocopea.hub.commands.RePurposeAppCommand;
import com.emc.ocopea.hub.commands.StopAppCommand;
import com.emc.ocopea.hub.repository.DBAppInstanceConfig;
import com.emc.ocopea.hub.repository.DBAppInstanceState;
import com.emc.ocopea.hub.repository.DBSavedImage;
import com.emc.ocopea.hub.repository.HubConfigRepositoryImpl;
import com.emc.ocopea.hub.site.AddSiteToHubCommand;
import com.emc.ocopea.hub.site.AddSiteToHubCommandArgs;
import com.emc.ocopea.hub.site.Site;
import com.emc.ocopea.hub.site.SiteDto;
import com.emc.ocopea.hub.site.SiteManagerService;
import com.emc.ocopea.hub.testdev.SavedImageDTO;
import com.emc.ocopea.util.Pair;
import com.emc.ocopea.util.database.BasicNativeQueryService;
import com.emc.ocopea.hub.repository.AppInstanceRepository;
import com.emc.ocopea.hub.repository.HubConfigRepository;
import com.emc.ocopea.hub.repository.SavedImageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by liebea on 12/13/15.
 * Drink responsibly
 */
public class HubResource implements HubWebApi {
    private static final String NAZGUL_LOCATION_HEADER_FILTER_KEY = "NAZGUL-LOCATION-HEADER-FILTER";
    private SiteManagerService siteManagerService;
    private ApplicationTemplateManagerService applicationTemplateManagerService;
    private AppInstanceManagerService instanceManagerService;
    private AppInstanceRepository appInstanceRepository;
    private DeployAppCommand deployAppCommand;
    private CreateAppDeploymentPlanCommand createAppDeploymentPlanCommand;
    private RePurposeAppCommand rePurposeAppCommand;
    private CreateAppCopyCommand createAppCopyCommand;
    private AddSiteToHubCommand addSiteToHubCommand;
    private CreateAppTemplateCommand createAppTemplateCommand;
    private CreateSavedImageCommand createSavedImageCommand;
    private SavedImageRepository savedImageRepository;
    private DeploySavedImageCommand deploySavedImageCommand;
    private StopAppCommand stopAppCommand;
    private HubConfigRepository hubConfigRepository;

    @javax.ws.rs.core.Context
    private HttpServletRequest servletRequest;

    @NoJavadoc
    @Context
    public void setApplication(Application application) {
        com.emc.microservice.Context context = ((MicroServiceApplication) application).getMicroServiceContext();
        final ManagedScheduler scheduler =
                context.getSchedulerManager().getManagedResourceByName("default");
        siteManagerService = context.getSingletonManager().getManagedResourceByName("site-manager").getInstance();
        applicationTemplateManagerService = context.getSingletonManager()
                .getManagedResourceByName("app-template-manager").getInstance();
        instanceManagerService = context.getSingletonManager()
                .getManagedResourceByName("app-instance-manager").getInstance();
        deployAppCommand = new DeployAppCommand(
                siteManagerService,
                applicationTemplateManagerService,
                instanceManagerService);
        rePurposeAppCommand = new RePurposeAppCommand(
                siteManagerService,
                instanceManagerService,
                applicationTemplateManagerService);
        createAppCopyCommand = new CreateAppCopyCommand(siteManagerService, instanceManagerService);
        addSiteToHubCommand = new AddSiteToHubCommand(siteManagerService);
        appInstanceRepository = context.getDynamicJavaServicesManager()
                .getManagedResourceByName(AppInstanceRepository.class.getSimpleName()).getInstance();
        createAppTemplateCommand = new CreateAppTemplateCommand(applicationTemplateManagerService);
        createAppDeploymentPlanCommand = new CreateAppDeploymentPlanCommand(
                siteManagerService,
                context.getWebAPIResolver(),
                applicationTemplateManagerService);
        savedImageRepository = context.getDynamicJavaServicesManager()
                .getManagedResourceByName(SavedImageRepository.class.getSimpleName()).getInstance();
        createSavedImageCommand = new CreateSavedImageCommand(
                siteManagerService,
                instanceManagerService,
                savedImageRepository,
                scheduler);
        deploySavedImageCommand = new DeploySavedImageCommand(
                siteManagerService,
                instanceManagerService,
                applicationTemplateManagerService,
                savedImageRepository);
        stopAppCommand = new StopAppCommand(siteManagerService, instanceManagerService, scheduler);
        hubConfigRepository = new HubConfigRepositoryImpl(new BasicNativeQueryService(
                context.getDatasourceManager().getManagedResourceByName("hub-db").getDataSource()));
    }

    @Override
    public ApplicationTemplateDTO getAppTemplate(
            @PathParam("appTemplateId") UUID appTemplateId,
            @HeaderParam("NAZGUL-INCLUDE-DELETED") @DefaultValue("false") Boolean includeDeleted) {
        return convertAppTemplate(HubWebAppUtil.wrapMandatory(
                "application template with id " + appTemplateId,
                () -> applicationTemplateManagerService.getAppTemplateById(appTemplateId, includeDeleted)));
    }

    @Override
    public void deleteAppTemplate(@PathParam("appTemplateId") UUID appTemplateId) {
        applicationTemplateManagerService.deleteAppTemplate(appTemplateId);
    }

    @Override
    public Collection<ApplicationTemplateDTO> listAppCatalog() {
        return applicationTemplateManagerService.list()
                .stream()
                .map(this::convertAppTemplate)
                .collect(Collectors.toList());
    }

    @Override
    public SiteDto getSite(@PathParam("siteId") UUID siteId) {
        return convertSite(HubWebAppUtil.wrapMandatory(
                "fetching site with id " + siteId,
                () -> siteManagerService.getSiteById(siteId)));
    }

    private SiteDto convertSite(Site site) {
        return new SiteDto(
                site.getId(),
                site.getUrn(),
                site.getUrl(),
                site.getName(),
                site.getVersion(),
                site.getLocation(),
                site.getPublicDns());
    }

    @Override
    public Collection<SiteDto> listSites() {
        return siteManagerService.list()
                .stream()
                .map(this::convertSite)
                .collect(Collectors.toList());
    }

    private ApplicationTemplateDTO convertAppTemplate(ApplicationTemplate curr) {
        return new ApplicationTemplateDTO(
                curr.getId(),
                curr.getName(),
                curr.getVersion(),
                curr.getDescription(),
                curr.getAppServiceTemplates()
                        .stream()
                        .map(currST -> new ApplicationServiceTemplateDTO(
                                currST.getAppServiceName(),
                                currST.getPsbType(),
                                currST.getImageName(),
                                currST.getImageType(),
                                currST.getImageVersion(),
                                currST.getPsbSettings(),
                                currST.getEnvironmentVariables(),
                                currST.getDependencies()
                                        .stream()
                                        .map(currDep -> new AppServiceExternalDependencyDTO(
                                                currDep.getType(),
                                                currDep.getName(),
                                                currDep.getProtocols()
                                                        .stream()
                                                        .map(HubResource::convertExternalDependency)
                                                        .collect(Collectors.toList()),
                                                currDep.getDescription()))
                                        .collect(Collectors.toList()),
                                currST.getExposedPorts(),
                                currST.getHttpPort(),
                                currST.getEntryPointUrl()))
                        .collect(Collectors.toList()),
                curr.getEntryPointServiceName(),
                curr.getCreatedByUserId());
    }

    private static AppServiceExternalDependencyDTO.AppServiceExternalDependencyProtocolDTO convertExternalDependency(
            AppServiceExternalDependencyProtocol protocol) {
        return new AppServiceExternalDependencyDTO.AppServiceExternalDependencyProtocolDTO(
                protocol.getProtocol(),
                protocol.getVersion(),
                protocol.getConditions(),
                protocol.getSettings());
    }

    @Override
    public UUID createAppCopy(CreateAppCopyCommandArgs args) {
        return createAppCopyCommand.execute(args);
    }

    @Override
    public UUID createSavedImage(CreateSavedImageCommandArgs args) {
        return createSavedImageCommand.execute(args);
    }

    @Override
    public Collection<HubAppInstanceConfigurationDTO> listAppInstances() {
        return appInstanceRepository.listConfig()
                .stream()
                .map(this::convertInstanceConfig)
                .collect(Collectors.toList());
    }

    @Override
    public HubAppInstanceWithStateDTO getAppInstanceState(@PathParam("appInstanceId") UUID appInstanceId) {
        final Pair<DBAppInstanceConfig, DBAppInstanceState> configWithState =
                appInstanceRepository.getConfigWithState(appInstanceId);
        if (configWithState == null) {
            throw new NotFoundException("app instance with id " + appInstanceId + " not found");
        }
        return convertAppInstance(configWithState);
    }

    private HubAppInstanceWithStateDTO convertAppInstance(Pair<DBAppInstanceConfig, DBAppInstanceState> pair) {
        return new HubAppInstanceWithStateDTO(
                pair.getObject1().getId(),
                pair.getObject1().getName(),
                pair.getObject1().getAppTemplateId(),
                pair.getObject1().getSiteId(),
                pair.getObject1().getBaseSavedImageId(),
                pair.getObject1().getCreatorUserId(),
                pair.getObject1().getDeploymentType(),
                pair.getObject1().getCreatedDate(),
                pair.getObject2().getUrl() == null ? null : pair.getObject2().getUrl().toString(),
                pair.getObject2().getState(),
                null);
    }

    @Override
    public Collection<HubAppInstanceWithStateDTO> listAppInstanceStates() {
        return HubWebAppUtil.wrap(
                "listing app instance states",
                () -> appInstanceRepository.listConfigWithState()
                        .stream()
                        .map(this::convertAppInstance)
                        .collect(Collectors.toList()));
    }

    @Override
    public HubAppInstanceConfigurationDTO getAppInstance(@PathParam("appInstanceId") UUID appInstanceId) {
        return convertInstanceConfig(HubWebAppUtil.wrapMandatory(
                "loading app Instance " + appInstanceId,
                () -> appInstanceRepository.getConfig(appInstanceId)));
    }

    private HubAppInstanceConfigurationDTO convertInstanceConfig(DBAppInstanceConfig currInstance) {
        return new HubAppInstanceConfigurationDTO(
                currInstance.getId(),
                currInstance.getName(),
                currInstance.getAppTemplateId(),
                currInstance.getSiteId(),
                currInstance.getBaseSavedImageId(),
                currInstance.getCreatorUserId(),
                currInstance.getDeploymentType(),
                currInstance.getCreatedDate());
    }

    @Override
    public HubAppInstanceDownStreamTreeDTO listDownStreamInstances(@PathParam("appInstanceId") UUID appInstanceId) {
        DBAppInstanceConfig appInstance = appInstanceRepository.getConfig(appInstanceId);
        if (appInstance == null) {
            throw new NotFoundException("No app instance found for " + appInstanceId);
        }
        Collection<DBAppInstanceConfig> downStreamAppInstances =
                instanceManagerService.getDownStreamAppInstances(appInstanceId);
        List<HubAppInstanceDownStreamTreeDTO> downStream = downStreamAppInstances
                .stream()
                .map(currDownStreamInstance -> listDownStreamInstances(currDownStreamInstance.getId()))
                .collect(Collectors.toList());
        return new HubAppInstanceDownStreamTreeDTO(
                appInstance.getSiteId(),
                appInstanceId,
                appInstance.getDeploymentType(),
                downStream);
    }

    @Override
    public ApplicationTemplateDTO createApplicationTemplate(ApplicationTemplateDTO applicationTemplateDTO) {
        UUID templateId = createAppTemplateCommand.execute(applicationTemplateDTO);
        servletRequest.setAttribute(NAZGUL_LOCATION_HEADER_FILTER_KEY, "./" + templateId);
        return convertAppTemplate(applicationTemplateManagerService.getAppTemplateById(templateId));
    }

    @Override
    public UUID deployApp(DeployAppCommandArgs deployAppCommandArgs) {
        return deployAppCommand.execute(deployAppCommandArgs);
    }

    @Override
    public void stopApp(StopAppCommandArgs stopAppCommandArgs) {
        stopAppCommand.execute(stopAppCommandArgs);
    }

    @Override
    public DeployAppCommandArgs.AppTemplateDeploymentPlanDTO createAppDeploymentPlan(
            CreateDeploymentPlanCommandArgs createDeploymentPlanCommandArgs) {
        return createAppDeploymentPlanCommand.execute(createDeploymentPlanCommandArgs);
    }

    @Override
    public UUID repurposeApp(RepurposeAppCommandArgs repurposeAppCommandArgs) {
        UUID appId = rePurposeAppCommand.execute(repurposeAppCommandArgs);
        servletRequest.setAttribute(NAZGUL_LOCATION_HEADER_FILTER_KEY, "app-instance/" + appId);
        return appId;
    }

    @Override
    public String addSite(AddSiteToHubCommandArgs addSiteToHubCommandArgs) {
        try {
            URI uri = addSiteToHubCommand.execute(addSiteToHubCommandArgs);
            servletRequest.setAttribute(NAZGUL_LOCATION_HEADER_FILTER_KEY, uri);
            return new ObjectMapper().writeValueAsString(uri.toString());
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException("failed to write site uri", e);
        }
    }

    private SavedImageDTO convertSavedImage(DBSavedImage savedImage) {
        return new SavedImageDTO(
                savedImage.getId(),
                savedImage.getAppTemplateId(),
                savedImage.getName(),
                savedImage.getBaseImageId(),
                savedImage.getCreatorUserId(),
                savedImage.getDateCreated(),
                new ArrayList<>(savedImage.getTags()),
                savedImage.getDescription(),
                savedImage.getSiteId(),
                savedImage.getAppCopyId(),
                savedImage.getState().toString());
    }

    @Override
    public SavedImageDTO getSavedImage(@PathParam("savedImageId") UUID savedImageId) {
        final DBSavedImage image = savedImageRepository.get(savedImageId);
        if (image == null) {
            throw new NotFoundException("Could not find saved image with id " + savedImageId);
        }
        return convertSavedImage(image);
    }

    @Override
    public Collection<SavedImageDTO> listSavedImages(@QueryParam("appTemplateId") UUID appTemplateId) {
        if (appTemplateId != null) {
            return savedImageRepository.findByAppTemplateId(appTemplateId)
                    .stream()
                    .map(this::convertSavedImage)
                    .collect(Collectors.toList());
        } else {
            return savedImageRepository.list()
                    .stream()
                    .map(this::convertSavedImage)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public UUID deploySavedImage(DeploySavedImageCommandArgs args) {
        UUID imageId = deploySavedImageCommand.execute(args);
        servletRequest.setAttribute(NAZGUL_LOCATION_HEADER_FILTER_KEY, imageId);
        return imageId;
    }

    @Override
    public String readHubConfig(@PathParam("key") String key) {
        return HubWebAppUtil.wrap(
                "reading Hub config for key=" + key,
                () -> hubConfigRepository.readKey(key)
        );
    }

    @Override
    public void writeHubConfig(@PathParam("key") String key, String value) {
        HubWebAppUtil.wrap(
                "writing Hub config for key=" + key,
                () -> hubConfigRepository.storeKey(key, value)
        );
    }
}
