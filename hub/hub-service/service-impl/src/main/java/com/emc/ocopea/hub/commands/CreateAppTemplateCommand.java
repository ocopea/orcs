// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.commands;

import com.emc.ocopea.hub.AppServiceExternalDependencyProtocol;
import com.emc.ocopea.hub.application.AppServiceExternalDependency;
import com.emc.ocopea.hub.application.AppServiceExternalDependencyDTO;
import com.emc.ocopea.hub.application.ApplicationServiceTemplate;
import com.emc.ocopea.hub.application.ApplicationTemplate;
import com.emc.ocopea.hub.application.ApplicationTemplateDTO;
import com.emc.ocopea.hub.application.ApplicationTemplateManagerService;

import javax.ws.rs.BadRequestException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by liebea on 10/5/16.
 * Drink responsibly
 */
public class CreateAppTemplateCommand extends HubCommand<ApplicationTemplateDTO, UUID> {

    private final ApplicationTemplateManagerService applicationTemplateManagerService;

    public CreateAppTemplateCommand(ApplicationTemplateManagerService applicationTemplateManagerService) {
        this.applicationTemplateManagerService = applicationTemplateManagerService;
    }

    @Override
    protected UUID run(ApplicationTemplateDTO applicationTemplateDTO) {
        validateEmptyField("name", applicationTemplateDTO.getName());
        validateEmptyField("createdByUserId", applicationTemplateDTO.getCreatedByUserId());
        validateEmptyField("version", applicationTemplateDTO.getVersion());
        validateEmptyField("appServiceTemplates", applicationTemplateDTO.getAppServiceTemplates());

        if (applicationTemplateDTO.getId() != null) {
            throw new BadRequestException(
                    "populating applicationTemplate id field is not supported in create operation");
        }

        // Validating entryPointServiceName when supplied
        final String entryPointServiceName = applicationTemplateDTO.getEntryPointServiceName();
        if (entryPointServiceName != null) {
            if (!applicationTemplateDTO.getAppServiceTemplates()
                    .stream()
                    .anyMatch(
                            applicationServiceTemplateDTO ->
                                    applicationServiceTemplateDTO.getAppServiceName()
                                            .equals(entryPointServiceName))) {
                throw new BadRequestException(
                        "entryPointServiceName was set to " + entryPointServiceName +
                                " however no such service exist");
            }
        }

        final UUID id = UUID.randomUUID();
        applicationTemplateManagerService.addApp(
                new ApplicationTemplate(
                        id,
                        applicationTemplateDTO.getName(),
                        applicationTemplateDTO.getVersion(),
                        applicationTemplateDTO.getDescription(),
                        applicationTemplateDTO.getAppServiceTemplates()
                                .stream()
                                .map(ast -> new ApplicationServiceTemplate(
                                        ast.getAppServiceName(),
                                        ast.getPsbType(),
                                        ast.getImageName(),
                                        ast.getImageType(),
                                        ast.getImageVersion(),
                                        ast.getPsbSettings(),
                                        ast.getEnvironmentVariables(),
                                        ast.getDependencies()
                                                .stream()
                                                .map(this::convertExternalDependency)
                                                .collect(Collectors.toList()),
                                        ast.getExposedPorts(),
                                        ast.getHttpPort(),
                                        ast.getEntryPointURL()))
                                .collect(Collectors.toList()),
                        entryPointServiceName,
                        applicationTemplateDTO.getCreatedByUserId()));

        return id;
    }

    private AppServiceExternalDependency convertExternalDependency(AppServiceExternalDependencyDTO currDepDTO) {
        return new AppServiceExternalDependency(
                currDepDTO.getType(),
                currDepDTO.getName(),
                currDepDTO.getProtocols()
                        .stream()
                        .map(p -> new AppServiceExternalDependencyProtocol(
                                p.getProtocolName(),
                                p.getVersion(),
                                p.getConditions(),
                                p.getSettings()))
                        .collect(Collectors.toList()),
                currDepDTO.getDescription());
    }

}
