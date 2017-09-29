// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.artifact;

import com.emc.microservice.Context;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.microservice.webclient.WebApiResolverBuilder;

import java.util.Map;

/**
 * Created by liebea on 1/15/17.
 * Drink responsibly
 */
public class ArtifactRegistryFactoryImpl implements ServiceLifecycle, ArtifactRegistryFactory {
    private WebAPIResolver webAPIResolver;

    @Override
    public ArtifactRegistryApi create(
            SiteArtifactRegistry.ArtifactRegistryType artifactRegistryType,
            Map<String, String> parameters,
            WebAPIResolver resolver) {
        switch (artifactRegistryType) {
            case customRest:
                return new CustomRestArtifactRegistryImpl(webAPIResolver, parameters.get("url"));
            case mavenRepository:
                return buildMavenArtifactRegistry(parameters);
            case dockerRegistry:
                return buildDockerArtifactRegistry(parameters);
            default:
                throw new UnsupportedOperationException("Not supporting artifact registry of type " +
                        artifactRegistryType + " yet, bye");
        }
    }

    private ArtifactRegistryApi buildMavenArtifactRegistry(Map<String, String> parameters) {
        final String username = parameters.get("username");
        final String password = parameters.get("password");
        WebApiResolverBuilder mavenResolverBuilder = new WebApiResolverBuilder().withVerifySsl(false);
        if (username != null && !username.isEmpty() && password != null) {
            mavenResolverBuilder.withBasicAuthentication(username, password);
        }
        return new MavenArtifactRegistryImpl(
                webAPIResolver.buildResolver(mavenResolverBuilder),
                parameters.get("url")
        );
    }

    private ArtifactRegistryApi buildDockerArtifactRegistry(Map<String, String> parameters) {
        final String username = parameters.get("username");
        final String password = parameters.get("password");
        WebApiResolverBuilder builder = new WebApiResolverBuilder().withVerifySsl(false);
        return new DockerArtifactRegistryImpl(
                webAPIResolver.buildResolver(builder),
                parameters.get("url"),
                username,
                password
        );
    }

    @Override
    public void init(Context context) {

        this.webAPIResolver = context.getWebAPIResolver();
    }

    @Override
    public void shutDown() {

    }
}
