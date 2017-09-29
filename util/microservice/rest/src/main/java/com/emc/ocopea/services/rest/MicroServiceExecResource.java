// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.services.rest;

import com.emc.microservice.ContextImpl;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liebea on 5/10/15.
 * Drink responsibly
 */
public class MicroServiceExecResource extends MicroServiceResource implements MicroServiceExecAPI {

    @Override
    public Response execute(final InputStream serviceInput, @Context final HttpHeaders headers) {
        final ContextImpl context = (ContextImpl) getMicroServiceApplication().getMicroServiceContext();
        StreamingOutput streamingOutput = output -> context
                .getSyncExecutor()
                .execute(serviceInput, output, getRequestHeaders(headers), Collections.emptyMap());
        return Response.ok(streamingOutput).build();
    }

    @Override
    public Response execute2(final InputStream serviceInput, @HeaderParam("headers") final String headers) {
        final ContextImpl context = (ContextImpl) getMicroServiceApplication().getMicroServiceContext();
        StreamingOutput streamingOutput = output -> context
                .getSyncExecutor()
                .execute(serviceInput, output, getRequestHeaders(headers), Collections.<String, String>emptyMap());
        return Response.ok(streamingOutput).build();
    }

    private Map<String, String> getRequestHeaders(HttpHeaders headers) {
        MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
        Map<String, String> stringStringHashMap = new HashMap<>(requestHeaders.size());
        for (Map.Entry<String, List<String>> currEntry : requestHeaders.entrySet()) {
            List<String> value = currEntry.getValue();
            if (value != null && !value.isEmpty()) {
                stringStringHashMap.put(currEntry.getKey(), value.get(0));
            }
        }
        return stringStringHashMap;
    }

    private Map<String, String> getRequestHeaders(String headers) {
        if (headers == null || headers.isEmpty()) {
            return Collections.emptyMap();
        }
        String[] split = headers.split("\\|");

        Map<String, String> stringStringHashMap = new HashMap<>(split.length / 2);
        for (int i = 0; i < split.length; i += 2) {
            stringStringHashMap.put(split[i], split[i + 1]);
        }
        return stringStringHashMap;
    }
}
