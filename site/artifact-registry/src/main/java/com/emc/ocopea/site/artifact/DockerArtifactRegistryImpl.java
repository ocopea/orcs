// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.artifact;

import com.emc.microservice.webclient.WebAPIResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An implementation of the docker artifact registry api
 */
public class DockerArtifactRegistryImpl implements ArtifactRegistryApi {
    private static final Logger log = LoggerFactory.getLogger(DockerArtifactRegistryImpl.class);
    private final WebAPIResolver webAPIResolver;
    private final String url;
    private final String username;
    private final String password;

    public DockerArtifactRegistryImpl(WebAPIResolver webAPIResolver, String url, String username, String password) {
        this.webAPIResolver = webAPIResolver;
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        this.url = url;

        this.username = username;
        this.password = password;
    }

    /**
     * @param artifactId must be of the form valid docker image name
     *
     * @return A list of available versions for the given docker image.
     */
    @Override
    public Collection<String> listVersions(String artifactId) {

        //todo: authentication - get the token etc...
        /*
        final WebTarget webTarget =
                webAPIResolver.getWebTarget("https://auth.docker.io/token?service=registry.docker.io");

        final Map auth = webTarget.request().buildGet().invoke(Map.class);
        final Object token = auth.get("token");
        */

        try {
            if (!artifactId.contains("/")) {
                artifactId = "library/" + artifactId;
            }
            final String registryUrl = this.url + "/v2/repositories/" + artifactId + "/tags/";
            log.info("fetching tags from {}", registryUrl);
            final Map res = webAPIResolver
                    .getWebTarget(registryUrl)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .buildGet()
                    .invoke(Map.class);

            return ((List<Map>) res.get("results"))
                    .stream()
                    .map(o -> (String) o.get("name"))
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.warn("oh well.. couldn't fetch from registry" + ex.getMessage(), ex);
            return Collections.singleton("latest");
        }
    }
}
