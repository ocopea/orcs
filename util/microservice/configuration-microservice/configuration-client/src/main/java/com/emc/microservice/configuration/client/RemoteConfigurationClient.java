// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.configuration.client;

import com.emc.dpa.microsevice.configuration.ConfigurationWebApi;
import com.emc.microservice.config.ConfigurationAPI;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ashish-kebab on 04/08/15.
 */
public class RemoteConfigurationClient implements ConfigurationAPI {

    private final ConfigurationWebApi api;

    public static interface RestClientResolver {
        public <T> T resolve(Class<T> webInterface, URI remoteService, boolean verifySSL);
    }

    public RemoteConfigurationClient(RestClientResolver resolver, URI remoteService, boolean verifySSL) {
        api = resolver.resolve(ConfigurationWebApi.class, remoteService, verifySSL);
    }

    @Override
    public Collection<String> list(String path) {
        String content = api.read(path);
        return parse(content);
    }

    @Override
    public String readData(String path) {
        String content = api.read(path);
        if ("[]".equals(content)) {
            return null;
        }
        return content;
    }

    @Override
    public boolean isDirectory(String path) {
        String content = api.read(path);
        return "[]".equals(content);
    }

    @Override
    public boolean exists(String path) {
        String content = api.read(path);
        return !"[]".equals(content);
    }

    @Override
    public void writeData(String path, String data) {
        api.overwrite(path, data);
    }

    @Override
    public void mkdir(String path) {
        // do nothing
    }

    private Collection<String> parse(String content) {
        // being lazy and not including JSON library
        content = content
                .replace('[', ' ')
                .replace(']', ' ')
                .replace('\'', ' ')
                .replace('"', ' ')
                .trim();

        List<String> result = new ArrayList<>();
        if (!content.isEmpty()) {
            for (String part : content.split(",")) {
                result.add(part.trim());
            }
        }
        return result;
    }
}
