// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import com.emc.microservice.Context;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.ocopea.hub.AppServiceExternalDependencyProtocol;
import com.emc.ocopea.hub.repository.DBAppServiceExternalDependency;
import com.emc.ocopea.hub.repository.DBApplicationServiceTemplate;
import com.emc.ocopea.hub.repository.DBApplicationTemplate;
import com.emc.ocopea.hub.repository.DuplicateResourceException;
import com.emc.ocopea.hub.repository.ApplicationTemplateRepository;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by liebea on 11/29/15.
 * Drink responsibly
 */
public class ApplicationTemplateManagerService implements ServiceLifecycle {

    private ApplicationTemplateRepository applicationTemplateRepository;

    @Override
    public void init(Context context) {
        applicationTemplateRepository = context.getDynamicJavaServicesManager()
                .getManagedResourceByName(ApplicationTemplateRepository.class.getSimpleName()).getInstance();
    }

    @Override
    public void shutDown() {
    }

    /**
     * Lists all non-deleted templates.
     */
    public Collection<ApplicationTemplate> list() {
        return applicationTemplateRepository
                .list()
                .stream()
                .filter(template -> !template.isDeleted())
                .map(this::convert)
                .collect(Collectors.toList());
    }

    private ApplicationTemplate convert(DBApplicationTemplate dbApplicationTemplate) {
        return new ApplicationTemplate(
                dbApplicationTemplate.getId(),
                dbApplicationTemplate.getName(),
                dbApplicationTemplate.getVersion(),
                dbApplicationTemplate.getDescription(),
                dbApplicationTemplate.getAppServiceTemplates().stream().map(dbApplicationServiceTemplate ->
                        new ApplicationServiceTemplate(
                                dbApplicationServiceTemplate.getAppServiceName(),
                                dbApplicationServiceTemplate.getPsbType(),
                                dbApplicationServiceTemplate.getImageName(),
                                dbApplicationServiceTemplate.getImageType(),
                                dbApplicationServiceTemplate.getImageVersion(),
                                dbApplicationServiceTemplate.getPsbSettings(),
                                dbApplicationServiceTemplate.getEnvironmentVariables(),
                                dbApplicationServiceTemplate.getDependencies()
                                        .stream()
                                        .map(dbAppServiceExternalDependency -> new AppServiceExternalDependency(
                                                DataServiceTypeEnumDTO.valueOf(
                                                        dbAppServiceExternalDependency.getType()),
                                                dbAppServiceExternalDependency.getName(),
                                                dbAppServiceExternalDependency.getProtocols()
                                                        .stream()
                                                        .map(protocol -> new AppServiceExternalDependencyProtocol(
                                                                protocol.getProtocol(),
                                                                protocol.getVersion(),
                                                                getNonNullMap(protocol.getConditions()),
                                                                getNonNullMap(protocol.getSettings())))
                                                        .collect(Collectors.toList()),
                                                dbAppServiceExternalDependency.getDescription()))
                                        .collect(Collectors.toList()),
                                new ArrayList<>(dbApplicationServiceTemplate.getExposedPorts()),
                                dbApplicationServiceTemplate.getHttpPort(),
                                dbApplicationServiceTemplate.getEntryPointUrl()))
                        .collect(Collectors.toList()),
                dbApplicationTemplate.getEntryPointServiceName(),
                dbApplicationTemplate.getCreatedByUserId());
    }

    private static Map<String, String> getNonNullMap(Map<String, String> map) {
        return map == null ? Collections.emptyMap() : map;
    }

    public ApplicationTemplate getAppTemplateById(UUID id) {
        return getAppTemplateById(id, false);
    }

    public ApplicationTemplate getAppTemplateById(UUID id, boolean allowDeleted) {
        final DBApplicationTemplate byId = applicationTemplateRepository.getById(id);
        if (byId == null || (!allowDeleted && byId.isDeleted())) {
            return null;
        }
        return convert(byId);
    }

    /**
     * Adds an application template to the repository.
     *
     * @param applicationTemplate the template to add
     */
    public void addApp(ApplicationTemplate applicationTemplate) {
        final Date dateCreated = new Date();
        try {
            applicationTemplateRepository.createApplicationTemplate(
                    new DBApplicationTemplate(
                            applicationTemplate.getId(),
                            applicationTemplate.getName(),
                            dateCreated,
                            dateCreated,
                            applicationTemplate.getVersion(),
                            applicationTemplate.getDescription(),
                            applicationTemplate.getAppServiceTemplates()
                                    .stream()
                                    .map(applicationServiceTemplate -> new DBApplicationServiceTemplate(
                                            applicationServiceTemplate.getAppServiceName(),
                                            applicationServiceTemplate.getPsbType(),
                                            applicationServiceTemplate.getImageName(),
                                            applicationServiceTemplate.getImageType(),
                                            applicationServiceTemplate.getImageVersion(),
                                            applicationServiceTemplate.getPsbSettings(),
                                            applicationServiceTemplate.getEnvironmentVariables(),
                                            applicationServiceTemplate.getDependencies()
                                                    .stream()
                                                    .map(appServiceExternalDependency ->
                                                            new DBAppServiceExternalDependency(
                                                                    appServiceExternalDependency
                                                                            .getType()
                                                                            .name(),
                                                                    appServiceExternalDependency.getName(),
                                                                    appServiceExternalDependency.getProtocols()
                                                                            .stream()
                                                                            .map(ApplicationTemplateManagerService
                                                                                    ::convertProtocol)
                                                                            .collect(Collectors.toList()),
                                                                    appServiceExternalDependency.getDescription()))
                                                    .collect(Collectors.toList()),
                                            new HashSet<Integer>(applicationServiceTemplate.getExposedPorts()),
                                            applicationServiceTemplate.getHttpPort(),
                                            applicationServiceTemplate.getEntryPointUrl()))
                                    .collect(Collectors.toList()),
                            applicationTemplate.getEntryPointServiceName(),
                            applicationTemplate.getCreatedByUserId())
            );
        } catch (DuplicateResourceException e) {
            throw new ClientErrorException("Template already exist " + applicationTemplate.getName(), 409, e);
        }
    }

    private static DBAppServiceExternalDependency.DBAppServiceExternalDependencyProtocol convertProtocol(
            AppServiceExternalDependencyProtocol protocol) {

        return new DBAppServiceExternalDependency.DBAppServiceExternalDependencyProtocol(
                protocol.getProtocol(),
                protocol.getVersion(),
                protocol.getConditions(),
                protocol.getSettings());
    }

    public void deleteAppTemplate(UUID id) {
        try {
            applicationTemplateRepository.markAppTemplateAsDeleted(id);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("no application template with id " + id.toString(), e);
        }
    }
}
