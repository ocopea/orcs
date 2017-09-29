// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.artifact;

import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.microservice.webclient.WebApiResolverBuilder;
import com.emc.ocopea.util.rest.RestClientUtil;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.WebTarget;
import java.util.Collection;

/**
 * Created by liebea on 8/3/17.
 * Drink responsibly
 */
public class DockerArtifactRegistryImplTest {

    @Test
    public void testMe() {
        new ResteasyJackson2Provider();

        final DockerArtifactRegistryImpl dockerArtifactRegistry =
                new DockerArtifactRegistryImpl(new TestResolver(), "https://registry.hub.docker.com", null, null);
        final Collection<String> mysql = dockerArtifactRegistry.listVersions("mysql");

        mysql.forEach(System.out::println);
        Assert.assertTrue(mysql.contains("latest"));
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