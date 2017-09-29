// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.copy;

import com.emc.microservice.testing.MicroServiceTestHelper;
import com.emc.ocopea.crb.CopyMetaData;
import com.emc.ocopea.crb.CrbWebApi;
import com.emc.ocopea.crb.CrbWebDataApi;
import com.emc.ocopea.crb.Info;
import com.emc.ocopea.crb.RepositoryInfo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 3/3/16.
 * Drink responsibly
 */
public class TestShpanCopyRepositoryMicroService {

    private MicroServiceTestHelper microServiceTestHelper;

    @Before
    public void init() throws SQLException, IOException {
        microServiceTestHelper = new MicroServiceTestHelper(new ShpanCopyRepositoryMicroService());

        // Creating the bank db schema
        //microServiceTestHelper.createSchema(new ());

        // Starting the service in test mode
        microServiceTestHelper.startServiceInTestMode();
    }

    @After
    public void tearDown() {
        microServiceTestHelper.stopTestMode();
    }

    @Test
    public void testBasic() throws Exception {
        CrbWebApi copyRepoWebApi = microServiceTestHelper.getServiceResource(ShpanCopyRepositoryResource.class);
        CrbWebDataApi copyRepoWebDataApi =
                microServiceTestHelper.getServiceResource(ShpanCopyRepositoryDataResource.class);
        Info crbInfo = copyRepoWebApi.getInfo();

        Assert.assertNotNull(crbInfo);
        System.out.println("getInfo Seems to work...");
        System.out.println(crbInfo);

        String str = "Hi, This is a small cute string";
        String copyId = UUID.randomUUID().toString();
        try (InputStream is = new ByteArrayInputStream(str.getBytes("UTF-8"))) {
            copyRepoWebApi.storeRepositoryInfo("test-repo", new RepositoryInfo(
                    "test-repo",
                    "test-repo",
                    Collections.emptyList(),
                    Collections.emptyMap()));

            copyRepoWebApi.storeCopyMetaData(copyId, new CopyMetaData(
                    copyId,
                    "test-repo",
                    new Date(),
                    "shpanRest",
                    "1.0",
                    "f1",
                    null,
                    Collections.emptyMap()
                    ));

            String response = copyRepoWebDataApi.createCopyInRepo("test-repo", copyId, is);
            // TODO(maloni) need to assert success
        }

        CopyMetaData copyMetadata = copyRepoWebApi.getCopyMetaData(copyId);
        Assert.assertNotNull("metadata null", copyMetadata);
        System.out.println(copyMetadata);

        Assert.assertEquals("Wrong copyId", copyId, copyMetadata.getCopyId());
        Assert.assertEquals("Wrong size",
                str.length(),
                Integer.parseInt(copyMetadata.getCopyAdditionalInfo().get(ShpanCopyRepositoryResource.SIZE_INFO_KEY)));

        Response download = copyRepoWebDataApi.retrieveCopy(copyId);
        try {
            InputStream inputStream = download.readEntity(InputStream.class);
            String returnedString = new BufferedReader(new InputStreamReader(inputStream)).readLine();
            Assert.assertEquals("data content changed", str, returnedString);
        } finally {
            download.close();
        }
    }
}
