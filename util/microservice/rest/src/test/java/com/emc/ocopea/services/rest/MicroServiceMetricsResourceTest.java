// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.services.rest;

import org.apache.http.HttpStatus;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MicroServiceMetricsResourceTest extends MockResourceTest {

    public MicroServiceMetricsResourceTest() {
        super(MicroServiceMetricsResource.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getMetricsOutput() throws URISyntaxException, IOException {

        final MockHttpResponse response = get(MicroServiceMetricsAPI.BASE_URI, MediaType.APPLICATION_JSON_TYPE);
        assertEquals(HttpStatus.SC_OK, response.getStatus());

        final Map<String, ?> json = json(response);

        System.out.println(json);

        assertEquals("mock-api", json.get("name"));

        final Map<String, ?> metrics = (Map<String, ?>) json.get("metrics");
        assertEquals("3.0.0", metrics.get("version"));
    }

}
