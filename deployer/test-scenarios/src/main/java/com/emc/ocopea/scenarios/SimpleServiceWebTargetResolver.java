// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scenarios;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liebea on 6/20/16.
 * Drink responsibly
 */
public class SimpleServiceWebTargetResolver implements ServiceWebTargetResolver, AutoCloseable {
    private final Map<String, String> mappings;
    private Client client = ClientBuilder.newBuilder().build();
    private final List<Object> registeredComponents;

    public static SimpleServiceURLResolverBuilder builder() {
        return new SimpleServiceURLResolverBuilder();
    }

    public static class SimpleServiceURLResolverBuilder {
        private final Map<String, String> mappings = new HashMap<>();
        private final List<Object> registeredComponents = new ArrayList<>();

        public SimpleServiceURLResolverBuilder withService(String serviceURN, String url) {
            mappings.put(serviceURN, url);
            return this;
        }

        public SimpleServiceWebTargetResolver build() {
            return new SimpleServiceWebTargetResolver(mappings, registeredComponents);
        }

        public SimpleServiceURLResolverBuilder withTargetRegister(Object registeredComponent) {
            registeredComponents.add(registeredComponent);
            return this;
        }
    }

    private SimpleServiceWebTargetResolver(Map<String, String> mappings, List<Object> registeredComponents) {
        this.mappings = mappings;
        this.registeredComponents = registeredComponents;
    }

    @Override
    public WebTarget resolveWebTarget(String serviceURN, String path) {
        if (!mappings.containsKey(serviceURN)) {
            throw new IllegalStateException("Could not resolve service " + serviceURN);
        }
        String fullPath = mappings.get(serviceURN) + "/" + path;
        WebTarget target = client.target(fullPath);
        registeredComponents.forEach(target::register);
        return target;
    }

    @Override
    public void close() {
        client.close();
    }
}
