// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.demo;

import com.emc.microservice.bootstrap.AbstractSchemaBootstrap;
import com.emc.ocopea.demo.docker.DockerRunner;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.hub.HubMicroService;
import com.emc.ocopea.hub.copy.ShpanCopyRepositoryMicroService;
import com.emc.ocopea.hub.repository.HubRepositorySchema;
import com.emc.ocopea.hub.webapp.HubWebAppMicroService;
import com.emc.ocopea.protection.ProtectionMicroService;
import com.emc.ocopea.protection.ProtectionRepositorySchema;
import com.emc.ocopea.site.SiteMicroService;
import com.emc.ocopea.site.repository.SiteRepositorySchema;
import com.emc.ocopea.util.MapBuilder;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by liebea on 7/21/15.
 * Drink responsibly
 */
public class OrcsDemoK8SDevRunner {

    @NoJavadoc
    public static void main(String[] args) throws IOException, SQLException {

        // Reading environment variables for setting site configuration
        String publicDNS = System.getenv("SITE_PUBLIC_DNS");

        if (publicDNS != null && !publicDNS.isEmpty()) {
            System.setProperty("site_public-load-balancer", publicDNS);
        }

        DockerRunner.runServices(
                "orcs",
                MapBuilder.<String, AbstractSchemaBootstrap>newHashMap()
                        .with("protection-db", new ProtectionRepositorySchema())
                        .with("site-db", new SiteRepositorySchema())
                        .with("hub-db", new HubRepositorySchema())
                        .build(),
                new HubMicroService(),
                new HubWebAppMicroService(),
                new ProtectionMicroService(),
                new SiteMicroService(),
                new ShpanCopyRepositoryMicroService()
        );
    }

}
