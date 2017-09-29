// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.copy;

import com.emc.ocopea.crb.CopyMetaData;
import com.emc.ocopea.crb.CrbWebApi;
import com.emc.ocopea.crb.CrbWebDataApi;
import com.emc.ocopea.crb.Info;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by liebea on 3/6/16.
 * Drink responsibly
 */
public class ShpanCopyTester {

    private final CrbWebApi publicAPI;
    private final CrbWebDataApi publicDataAPI;

    public ShpanCopyTester(CrbWebApi crbAPI, CrbWebDataApi crbDataApi) {
        publicAPI = crbAPI;
        publicDataAPI = crbDataApi;
    }

    @NoJavadoc
    // TODO add javadoc
    public void test() throws IOException {
        Info crbInfo = publicAPI.getInfo();
        Objects.requireNonNull(crbInfo);
        System.out.println("getInfo Seems not to crash at least...");
        System.out.println(crbInfo);

        String str = "Hi, This is a small cute string";
        String copyId = UUID.randomUUID().toString();
        final String dsbName = "amit";
        final String facility = "recoverpoint";
        try (InputStream is = new ByteArrayInputStream(str.getBytes("UTF-8"))) {
            String response = publicDataAPI.createCopyInRepo("repoId", copyId, is);
            // TODO(maloni) need to verify response...
        }
        CopyMetaData copyMetadata = publicAPI.getCopyMetaData(copyId);
        Assert.assertNotNull("metadata null", copyMetadata);
        System.out.println(copyMetadata);

        Assert.assertEquals("Wrong copyId", copyId, copyMetadata.getCopyId());
        Assert.assertEquals("Wrong dsb name", dsbName, copyMetadata.getProtocol());
        Assert.assertEquals("Wrong facility", facility, copyMetadata.getRepoId());
        Assert.assertEquals("Wrong size", str.length(),
                Long.parseLong(copyMetadata.getCopyAdditionalInfo().getOrDefault("size", "0")));

        Response download = publicDataAPI.retrieveCopy(copyId);
        try {
            Assert.assertNotNull("data is null", download);

            InputStream inputStream = download.readEntity(InputStream.class);
            String downloadedData = IOUtils.toString(inputStream, Charset.defaultCharset());
            Assert.assertEquals(str, downloadedData);
            //      }
        } finally {
            download.close();
        }
    }
}
