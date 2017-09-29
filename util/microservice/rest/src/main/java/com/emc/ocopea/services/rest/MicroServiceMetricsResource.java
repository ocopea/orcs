// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id$
 * 
 * This computer code is copyright 2014 EMC Corporation
 * All rights reserved
 */

package com.emc.ocopea.services.rest;

import com.codahale.metrics.json.MetricsModule;
import com.emc.microservice.datasource.DatasourceConfiguration;
import com.emc.microservice.datasource.DatasourceDescriptor;
import com.emc.microservice.datasource.ManagedDatasource;
import com.emc.microservice.datasource.MicroServiceDataSource;
import com.emc.microservice.metrics.MetricRegistryModule;
import com.emc.microservice.resource.ResourceManager;
import com.emc.ocopea.util.MapBuilder;
import com.emc.ocopea.util.database.BasicNativeQueryService;
import com.emc.ocopea.util.database.NativeQueryService;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PathParam;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MicroServiceMetricsResource extends MicroServiceResource implements MicroServiceMetricsAPI {
    private final ObjectMapper mapper;

    public MicroServiceMetricsResource() {
        final TimeUnit rateUnit = TimeUnit.SECONDS;
        final TimeUnit durationUnit = TimeUnit.SECONDS;
        final boolean showSamples = false;
        this.mapper = new ObjectMapper().registerModule(new MetricsModule(
                rateUnit,
                durationUnit,
                showSamples))
                .registerModule(new MetricRegistryModule());

    }

    @Override
    public Response getMetricsOutput() {
        StreamingOutput stream = outputStream -> mapper
                .writerWithDefaultPrettyPrinter()
                .writeValue(outputStream, getMicroServiceApplication().getMicroServiceContext().getMetricsRegistry());

        return Response
                .ok(stream, MediaType.APPLICATION_JSON_TYPE)
                .header("Cache-Control", "must-revalidate,no-cache,no-store")
                .build();
    }

    @Override
    public Map<String, List> getDsMetrics(@PathParam("ds") String ds) {
        ResourceManager<DatasourceDescriptor, DatasourceConfiguration, ManagedDatasource> datasourceManager =
                getMicroServiceApplication()
                        .getMicroServiceContext()
                        .getDatasourceManager();
        if (!datasourceManager.hasResource(ds)) {
            throw new NotFoundException("no DS named " + ds);
        }
        MicroServiceDataSource dataSource = datasourceManager
                .getManagedResourceByName(ds)
                .getDataSource();
        DsType dsType;
        try (Connection connection = dataSource.getConnection();) {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            if ("PostgreSQL".equals(databaseProductName)) {
                dsType = DsType.POSTGRESQL;
            } else {
                throw new InternalServerErrorException("Can't query stats on ds of type " + databaseProductName);
            }
        } catch (SQLException e) {
            throw new ServiceUnavailableException("failed to connect to ds: " + e.getMessage(), 60L, e);
        }
        NativeQueryService nativeQueryService = new BasicNativeQueryService(dataSource);
        switch (dsType) {
            case POSTGRESQL:
                List<PostgresStats> processes = nativeQueryService.getList(
                        "SELECT * FROM pg_stat_activity",
                        (rs, index) -> new PostgresStats(
                                rs.getInt("pid"),
                                rs.getString("usename"),
                                rs.getString("application_name"),
                                rs.getString("client_addr"),
                                rs.getString("client_hostname"),
                                rs.getInt("client_port"),
                                rs.getDate("backend_start"),
                                rs.getDate("xact_start"),
                                rs.getDate("query_start"),
                                rs.getDate("state_change"),
                                rs.getBoolean("waiting"),
                                rs.getString("state"),
                                rs.getString("query"))

                );
                List<PostgresLock> locks = nativeQueryService.getList(
                        "SELECT * FROM pg_locks",
                        (rs, index) -> new PostgresLock(
                                rs.getString("locktype"),
                                getNullableInt(rs, "database"),
                                getNullableInt(rs, "relation"),
                                getNullableInt(rs, "page"),
                                getNullableInt(rs, "tuple"),
                                rs.getString("virtualxid"),
                                rs.getString("transactionid"),
                                getNullableInt(rs, "classid"),
                                getNullableInt(rs, "objid"),
                                getNullableInt(rs, "objsubid"),
                                rs.getString("virtualtransaction"),
                                getNullableInt(rs, "pid"),
                                rs.getString("mode"),
                                rs.getBoolean("granted"),
                                rs.getBoolean("fastpath"))

                );
                return MapBuilder.<String, List>newHashMap()
                        .with("processes", processes)
                        .with("locks", locks)
                        .build();
            default:
                throw new InternalServerErrorException("How did we get here?");
        }
    }

    private Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        int retVal = rs.getInt(column);
        return rs.wasNull() ? null : retVal;
    }

    private enum DsType {
        POSTGRESQL
    }

    public static class PostgresStats {
        public final int pid;
        public final String usename;
        public final String applicationName;
        public final String clientAddr;
        public final String clientHostname;
        public final int clientPort;
        public final Date backendStart;
        public final Date xactStart;
        public final Date queryStart;
        public final Date stateChange;
        public final boolean waiting;
        public final String state;
        public final String query;

        private PostgresStats(
                int pid,
                String usename,
                String applicationName,
                String clientAddr,
                String clientHostname,
                int clientPort,
                Date backendStart,
                Date xactStart,
                Date queryStart,
                Date stateChange,
                boolean waiting,
                String state,
                String query) {
            this.pid = pid;
            this.usename = usename;
            this.applicationName = applicationName;
            this.clientAddr = clientAddr;
            this.clientHostname = clientHostname;
            this.clientPort = clientPort;
            this.backendStart = backendStart;
            this.xactStart = xactStart;
            this.queryStart = queryStart;
            this.stateChange = stateChange;
            this.waiting = waiting;
            this.state = state;
            this.query = query;
        }
    }

    public static class PostgresLock {
        public final String locktype;
        public final Integer database;
        public final Integer relation;
        public final Integer page;
        public final Integer tuple;
        public final String virtualxid;
        public final String transactionid;
        public final Integer classid;
        public final Integer objid;
        public final Integer objsubid;
        public final String virtualtransaction;
        public final Integer pid;
        public final String mode;
        public final boolean granted;
        public final boolean fastpath;

        private PostgresLock(
                String locktype,
                Integer database,
                Integer relation,
                Integer page,
                Integer tuple,
                String virtualxid,
                String transactionid,
                Integer classid,
                Integer objid,
                Integer objsubid,
                String virtualtransaction,
                Integer pid,
                String mode,
                boolean granted,
                boolean fastpath) {
            this.locktype = locktype;
            this.database = database;
            this.relation = relation;
            this.page = page;
            this.tuple = tuple;
            this.virtualxid = virtualxid;
            this.transactionid = transactionid;
            this.classid = classid;
            this.objid = objid;
            this.objsubid = objsubid;
            this.virtualtransaction = virtualtransaction;
            this.pid = pid;
            this.mode = mode;
            this.granted = granted;
            this.fastpath = fastpath;
        }
    }
}
