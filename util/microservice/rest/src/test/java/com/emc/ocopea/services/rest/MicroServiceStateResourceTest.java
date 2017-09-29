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

public class MicroServiceStateResourceTest extends MockResourceTest {

    public MicroServiceStateResourceTest() {
        super(MicroServiceStateResource.class);
    }

    @Test
    public void getServiceState() throws URISyntaxException, IOException {
        final MockHttpResponse response = get(MicroServiceStateAPI.BASE_URI, MediaType.APPLICATION_JSON_TYPE);
        assertEquals(HttpStatus.SC_OK, response.getStatus());

        final Map<String, ?> json = json(response);

        assertEquals("Mock Micro Service", json.get("name"));
        assertEquals("RUNNING", json.get("state"));
    }
}
