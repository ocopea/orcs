// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

/**
 * Created by liebea on 4/25/17.
 * Drink responsibly
 */
public class DeployedApplicationCreatorService {
    private final DeployedApplicationEventRepository deployedApplicationEventRepository;
    private final DeployedApplicationPersisterService deployedApplicationPersisterService;

    public DeployedApplicationCreatorService(
            DeployedApplicationEventRepository deployedApplicationEventRepository,
            DeployedApplicationPersisterService deployedApplicationPersisterService) {
        this.deployedApplicationEventRepository = deployedApplicationEventRepository;
        this.deployedApplicationPersisterService = deployedApplicationPersisterService;
    }

    public DeployedApplication create(DeployedApplicationBuilder builder) {
        DeployedApplication deployedApplication = new DeployedApplication().create(builder);
        deployedApplicationEventRepository.store(deployedApplication.getExecutionEvent());
        deployedApplicationPersisterService.persist(deployedApplication);
        deployedApplication.markAsClean();
        return deployedApplication;
    }
}
