// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.services.rest;

import org.apache.http.HttpStatus;
import org.dom4j.DocumentException;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class APIMetaResourceTest extends MockResourceTest {

    @Path("/sample/")
    public static class SampleResource {

        public static class SampleJsonObject {

        }

        @GET
        @Path("/duplicate//slashes/")
        @Produces(MediaType.APPLICATION_JSON)
        public SampleJsonObject getWithDuplicateSlash() {
            return null;
        }

    }

    public APIMetaResourceTest() {
        super(APIMetaResource.class, SampleResource.class);
    }

    @Test
    public void getAvailableResourcesAsJSON() throws URISyntaxException, DocumentException, IOException {

        final MockHttpResponse response = get("/meta", MediaType.APPLICATION_JSON_TYPE);
        assertEquals(HttpStatus.SC_OK, response.getStatus());

        final Map<String, ?> json = json(response);
        assertEquals("Unexpected number of resources registered", 2, ((Collection) json.get("resources")).size());
    }

    @Test
    public void getAvailableResourcesAsHTML() throws URISyntaxException, DocumentException, InterruptedException {

        final MockHttpResponse response = get("/meta", MediaType.TEXT_HTML_TYPE);
        assertEquals(HttpStatus.SC_NOT_ACCEPTABLE, response.getStatus());
        // allow scheduled tasks to run for a while
        Thread.sleep(1000);
    }

    @Test
    public void getAvailableResourcesAsWildcard() throws URISyntaxException, DocumentException {

        final MockHttpResponse response = get("/meta", MediaType.WILDCARD_TYPE);
        assertEquals(HttpStatus.SC_OK, response.getStatus());

        assertEquals(
                "The default type used to be json!!!",
                MediaType.APPLICATION_JSON,
                response.getOutputHeaders().getFirst("Content-Type").toString());
    }

    @Test
    public void getResourcesWithSlashNormalization() throws URISyntaxException, IOException {
        final MockHttpResponse response = get("/meta", MediaType.APPLICATION_JSON_TYPE);
        assertEquals(HttpStatus.SC_OK, response.getStatus());

        final Map<String, ?> json = json(response);

        final Collection<String> paths = collectPathsFromJson(new HashSet<>(), json);

        assertThat(
                paths,
                allOf(hasItem("/meta"),
                        hasItem("/sample/duplicate/slashes"),
                        not(hasItem("/sample///duplicate//slashes/"))));

        System.out.println(paths);
    }

    private Collection<String> collectPathsFromJson(Collection<String> paths, Object json) {
        if (json instanceof Map) {
            final Map map = (Map) json;
            if (map.containsKey("path")) {
                paths.add((String) map.get("path"));
            }
            collectPathsFromJson(paths, map.values());
        } else if (json instanceof Collection) {
            final Collection<?> collection = (Collection) json;
            for (Object value : collection) {
                collectPathsFromJson(paths, value);
            }
        }
        return paths;
    }
}
