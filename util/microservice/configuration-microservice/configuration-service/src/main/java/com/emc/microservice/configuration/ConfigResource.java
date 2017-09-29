// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.configuration;

import com.emc.dpa.microsevice.configuration.ConfigurationWebApi;
import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.datasource.ManagedDatasource;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Application;

/**
 * Created by ashish-kebab on 31/07/15.
 */
public class ConfigResource implements ConfigurationWebApi {
    private ConfigService configService;

    @Override
    public String read(@PathParam("path") String path) {
        return configService.read(path);
    }

    @Override
    public void write(@PathParam("path") String path, String data) {
        configService.write(path, data);
    }

    @Override
    public void overwrite(@PathParam("path") String path, String data) {
        configService.overwrite(path, data);
    }

    @Override
    public void delete(@PathParam("path") String path) {
        configService.delete(path);
    }

    @javax.ws.rs.core.Context
    public void setApplication(Application app) {
        final ManagedDatasource managedDatasource = ((MicroServiceApplication) app)
                .getMicroServiceContext()
                .getDatasourceManager()
                .getManagedResourceByName(ConfigurationMicroservice.CONFIG_DB);
        configService = new ConfigService(managedDatasource.getDataSource());
    }
}
