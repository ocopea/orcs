// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.commands;

import com.emc.microservice.dependency.ManagedDependency;
import com.emc.ocopea.protection.ProtectApplicationInstanceInfoDTO;
import com.emc.ocopea.protection.ProtectionWebAPI;
import com.emc.ocopea.site.CreateAppCopyCommandArgs;
import com.emc.ocopea.site.app.DeployedApplication;
import com.emc.ocopea.site.app.DeployedApplicationEventRepository;
import com.emc.ocopea.site.app.PolicyDeployer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liebea on 1/19/16.
 * Drink responsibly
 */
public class CreateAppCopyCommand extends SiteCommand<CreateAppCopyCommandArgs, UUID> {
    private final ManagedDependency protectionPolicyDependency;
    private final DeployedApplicationEventRepository deployedApplicationEventRepository;

    public CreateAppCopyCommand(
            ManagedDependency protectionPolicyDependency,
            DeployedApplicationEventRepository deployedApplicationEventRepository) {
        this.protectionPolicyDependency = protectionPolicyDependency;
        this.deployedApplicationEventRepository = deployedApplicationEventRepository;
    }

    @Override
    protected UUID run(CreateAppCopyCommandArgs args) {

        // Input validation
        final UUID appInstanceId = validateEmptyField("appInstanceId", args.getAppInstanceId());

        DeployedApplication app = new DeployedApplication(
                deployedApplicationEventRepository.listSortedEvents(appInstanceId));

        //todo: this is foolish, according to which policy are we taking the copy. bah..
        final Map<String, String> settings = Collections.emptyMap();

        final List<ProtectApplicationInstanceInfoDTO.ProtectDataServiceBinding> protectDataServiceBindings =
                PolicyDeployer.buildProtectDataServiceBindings(app, settings);

        return protectionPolicyDependency.getWebAPI(ProtectionWebAPI.class)
                .createAppCopy(
                        new ProtectApplicationInstanceInfoDTO(
                                protectDataServiceBindings,
                                -1,
                                app.getId().toString(),
                                PolicyDeployer.buildProtectAppConfigurations(app)
                        ));
    }
}
