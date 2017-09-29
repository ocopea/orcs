// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.webapp;

import com.emc.microservice.Context;
import com.emc.microservice.dependency.ManagedDependency;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.ocopea.hub.HubWebAppUtil;
import com.emc.ocopea.hub.application.ApplicationTemplateDTO;
import com.emc.ocopea.hub.application.HubWebApi;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by liebea on 7/26/16.
 * Drink responsibly
 */
public class AppTemplateCache implements ServiceLifecycle {

    private ManagedDependency hubServiceDependency;
    private Map<UUID, ApplicationTemplateDTO> cache;

    @Override
    public void init(Context context) {
        hubServiceDependency = context.getDependencyManager().getManagedResourceByName("hub");
        cache = new ConcurrentHashMap<>();
    }

    Collection<ApplicationTemplateDTO> listAppTemplates() {
        final Collection<ApplicationTemplateDTO> applicationTemplateDTOs =
                hubServiceDependency.getWebAPI(HubWebApi.class).listAppCatalog();
        this.cache.putAll(applicationTemplateDTOs
                .stream()
                .collect(Collectors.toMap(
                        ApplicationTemplateDTO::getId,
                        applicationTemplateDTO -> applicationTemplateDTO)));
        return applicationTemplateDTOs;
    }

    ApplicationTemplateDTO getAppTemplateById(UUID appTemplateId, boolean allowDeleted) {
        final ApplicationTemplateDTO applicationTemplateDTO = this.cache.get(appTemplateId);
        final ApplicationTemplateDTO templateDTO = (applicationTemplateDTO != null) ?
                applicationTemplateDTO :
                HubWebAppUtil.wrapMandatory(
                        "loading appTemplate with id " + appTemplateId,
                        () -> hubServiceDependency
                                .getWebAPI(HubWebApi.class)
                                .getAppTemplate(appTemplateId, allowDeleted));
        if (!allowDeleted) {
            cache.put(templateDTO.getId(), templateDTO);
        }
        return templateDTO;
    }

    @Override
    public void shutDown() {
        // Clearing cache
        cache = new ConcurrentHashMap<>();
    }

    public void clear() {
        cache.clear();
    }
}
