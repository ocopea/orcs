// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import com.emc.jmsl.dev.RemoteDevResourceProvider;
import com.emc.microservice.runner.MicroServiceRunner;
import com.emc.ocopea.hub.copy.ShpanCopyRepositoryMicroService;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by liebea on 7/21/15.
 * Drink responsibly
 */
public class ShpanCopyRepoRemoteDevRunner {
    public static void main(String[] args) throws IOException, SQLException {
        RemoteDevResourceProvider devResourceProvider = new RemoteDevResourceProvider("http://localhost:8081");
        new MicroServiceRunner().run(
                devResourceProvider,
                new ShpanCopyRepositoryMicroService()
        );
    }
}
