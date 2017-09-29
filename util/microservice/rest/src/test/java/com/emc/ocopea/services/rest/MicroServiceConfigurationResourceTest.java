// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.services.rest;

import org.apache.http.HttpStatus;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MicroServiceConfigurationResourceTest extends MockResourceTest {

    private static final String VERSION_NOT_AVAILABLE = "Version Not Available";

    public MicroServiceConfigurationResourceTest() {
        super(MicroServiceConfigurationResource.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getServiceConfiguration() throws URISyntaxException, IOException {

        final MockHttpResponse response = get(MicroServiceConfigurationAPI.BASE_URI, MediaType.APPLICATION_JSON_TYPE);
        assertEquals(HttpStatus.SC_OK, response.getStatus());

        final Map<String, ?> json = json(response);

        assertEquals("Mock Micro Service", json.get("name"));
        assertEquals(VERSION_NOT_AVAILABLE, json.get("version"));

        final List<Map<String, ?>> parameters = (List<Map<String, ?>>) json.get("parameters");
        assertEquals("Expected 2 parameters, boo-timeout and health-check-period-in-seconds", 3, parameters.size());
    }
}
