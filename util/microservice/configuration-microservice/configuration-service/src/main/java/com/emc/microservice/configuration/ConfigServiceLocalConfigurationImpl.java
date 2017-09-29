// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.configuration;

import com.emc.microservice.config.ConfigurationAPI;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by liebea on 1/2/17.
 * Drink responsibly
 */
public class ConfigServiceLocalConfigurationImpl implements ConfigurationAPI {

    private final ConfigService configService;

    public ConfigServiceLocalConfigurationImpl(DataSource ds) {
        this.configService = new ConfigService(ds);
    }

    @Override
    public Collection<String> list(String path) {
        String content = configService.read(path);
        return parse(content);
    }

    @Override
    public String readData(String path) {
        String content = configService.read(path);
        if ("[]".equals(content)) {
            return null;
        }
        return content;
    }

    @Override
    public boolean isDirectory(String path) {
        String content = configService.read(path);
        return "[]".equals(content);
    }

    @Override
    public boolean exists(String path) {
        String content = configService.read(path);
        return !"[]".equals(content);
    }

    @Override
    public void writeData(String path, String data) {
        configService.overwrite(path, data);
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
