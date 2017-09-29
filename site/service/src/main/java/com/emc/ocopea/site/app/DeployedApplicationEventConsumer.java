// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import com.emc.microservice.discovery.ServiceDiscoveryManager;
import com.emc.microservice.schedule.ManagedScheduler;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.site.SiteRepository;

import java.util.function.Consumer;

/**
 * Created by liebea on 10/19/16.
 * Drink responsibly
 */
public class DeployedApplicationEventConsumer implements Consumer<DeployedApplicationEvent> {

    private final AppServiceDeployer appServiceDeployer;
    private final DataServiceDeployer dataServiceDeployer;
    private final PolicyDeployer policyDeployer;

    public DeployedApplicationEventConsumer(
            DeployedApplicationLoader deployedApplicationLoader,
            PolicyEngineConnector policyEngineConnector,
            WebAPIResolver webAPIResolver,
            ManagedScheduler scheduler,
            ServiceDiscoveryManager serviceDiscoveryManager,
            SiteRepository siteRepository,
            DeployedApplicationPersisterService deployedApplicationPersisterService) {

        this.policyDeployer = new PolicyDeployer(deployedApplicationLoader, policyEngineConnector,
                deployedApplicationPersisterService);

        this.appServiceDeployer = new AppServiceDeployer(
                deployedApplicationLoader,
                webAPIResolver, scheduler,
                siteRepository, deployedApplicationPersisterService);

        this.dataServiceDeployer = new DataServiceDeployer(
                deployedApplicationLoader,
                webAPIResolver,
                siteRepository, deployedApplicationPersisterService,
                scheduler
        );
    }

    @Override
    public void accept(DeployedApplicationEvent event) {

        if (event instanceof DataServiceStateChangeEvent) {
            dataServiceDeployer.process((DataServiceStateChangeEvent) event);
        } else if (event instanceof AppServiceStateChangeEvent) {
            appServiceDeployer.process((AppServiceStateChangeEvent) event);
        } else if (event instanceof PolicyStateChangeEvent) {
            policyDeployer.process((PolicyStateChangeEvent) event);
        }
    }
}
