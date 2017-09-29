// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.dpa.dev.manager;

import com.emc.dpa.dev.DevResourceProvider;
import com.emc.dpa.dev.datasource.DevDataSource;
import com.emc.microservice.datasource.DatasourceConfiguration;
import com.emc.microservice.resource.ResourceProviderManager;
import com.emc.ocopea.util.database.BasicNativeQueryService;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.sql.ResultSetMetaData;

/**
 * Created by liebea on 2/23/15.
 * Drink responsibly
 */
public class DevModeServiceManagerResource implements DevModeServiceManagerAPI {

    private DevResourceProvider devResourceProvider;
    private static final Logger logger = LoggerFactory.getLogger(DevModeServiceManagerResource.class);

    @Context
    public void setApplication(Application application) {
        devResourceProvider = (DevResourceProvider) ResourceProviderManager.getResourceProvider();
    }

    @Override
    public void startService(@PathParam("serviceURI") String serviceURI) {
        devResourceProvider.scaleService(serviceURI);
    }

    @Override
    public void pauseDB(@PathParam("dsName") String dsName) {
        DevDataSource datastore = getDevDataSourceByName(dsName);
        datastore.pauseDataSource();
    }

    @Override
    public void resumeDB(@PathParam("dsName") String dsName) {
        DevDataSource datastore = getDevDataSourceByName(dsName);
        datastore.resumeDataSource();
    }

    @Override
    public Response executeQuery(@PathParam("dsName") String dsName, final String sql) {
        final DataSource ds = getDevDataSourceByName(dsName);
        StreamingOutput so = output -> {
            BasicNativeQueryService basicNativeQueryService = new BasicNativeQueryService(ds);

            try (JsonGenerator generator = new JsonFactory().createGenerator(output)) {
                generator.writeStartArray();
                try {

                    basicNativeQueryService.handleResultSet(sql, (resultSet, i) -> {

                        try {
                            generator.writeStartObject();
                            try {
                                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                                int numColumns = resultSetMetaData.getColumnCount();
                                for (int columnIdx = 1; columnIdx < numColumns + 1; columnIdx++) {
                                    String columnName = resultSetMetaData.getColumnName(columnIdx);
                                    generator.writeStringField(columnName, resultSet.getString(columnIdx));
                                }
                            } finally {
                                generator.writeEndObject();
                            }
                        } catch (IOException e) {
                            throw new IllegalStateException("Failed formatting result for sql: " + sql, e);
                        }
                        return null;
                    });
                } finally {
                    generator.writeEndArray();
                }
            } catch (Exception ex) {
                logger.error("Failed executing sql: " + sql, ex);
                throw new WebApplicationException(ex);
            }
        };
        return Response.ok(so, MediaType.APPLICATION_JSON_TYPE).build();

    }

    private DevDataSource getDevDataSourceByName(String dsName) {
        DatasourceConfiguration dsConfiguration = devResourceProvider
                .getServiceRegistryApi()
                .getDataSourceConfiguration(devResourceProvider.getDatasourceConfigurationClass(), dsName);
        if (dsConfiguration == null) {
            throw new IllegalArgumentException("Invalid datastore name: " + dsName);
        }

        return (DevDataSource) devResourceProvider.getDataSource(dsConfiguration);
    }
}
