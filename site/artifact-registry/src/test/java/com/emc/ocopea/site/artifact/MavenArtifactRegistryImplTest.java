// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.artifact;

import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.microservice.webclient.WebApiResolverBuilder;
import com.emc.ocopea.util.rest.RestClientUtil;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.client.WebTarget;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class MavenArtifactRegistryImplTest {

    private String readFile(String fileName) {
        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            return convertStreamToString(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Test failed, bye", e);
        }
    }

    // TODO extract to future module TestUtils
    private static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private static class TestResolver implements WebAPIResolver {

        @Override
        public WebAPIResolver buildResolver(WebApiResolverBuilder builder) {
            throw new UnsupportedOperationException("No!");
        }

        @Override
        public <T> T getWebAPI(String url, Class<T> resourceWebAPI) {
            ResteasyWebTarget target = getResteasyWebTarget(url);
            return target.proxy(resourceWebAPI);
        }

        private ResteasyWebTarget getResteasyWebTarget(String url) {
            return RestClientUtil.getWebTarget(url);
        }

        @Override
        public WebTarget getWebTarget(String url) {
            return getResteasyWebTarget(url);
        }
    }

}
