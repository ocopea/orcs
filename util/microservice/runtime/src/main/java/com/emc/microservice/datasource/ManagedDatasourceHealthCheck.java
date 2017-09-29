// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.datasource;

import com.emc.microservice.Context;
import com.emc.microservice.health.HealthCheck;
import com.emc.microservice.health.HealthCheckResult;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Objects;

/**
 * Created by liebea on 7/31/2014. Enjoy it
 */
public class ManagedDatasourceHealthCheck implements HealthCheck {
    private final ManagedDatasource managedDatasource;
    private final String name;

    public ManagedDatasourceHealthCheck(ManagedDatasource managedDatasource) {
        this.name = "Data Source " + managedDatasource.getDescriptor().getName();
        this.managedDatasource = Objects.requireNonNull(managedDatasource);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public HealthCheckResult check(Context context) {
        DataSource dataSource = context
                .getDatasourceManager()
                .getManagedResourceByName(managedDatasource.getDescriptor().getName())
                .getDataSource();

        // Trying to get a connection, validating whether datasource is working (this will throw an exception if not)
        // Check that getConnection does not throw exception and returns a connection
        try (Connection connection = dataSource.getConnection()) {
            //todo: not sure we need the isValid.. since getConnection should be fine (really?)
            //connection.isValid(30);
            if (connection == null) {
                return new HealthCheckResult(false, "Failed to check connection for " + getName());
            }
            return HealthCheckResult.healthy();
        } catch (Exception e) {
            return new HealthCheckResult(false, "Failed to check connection for " + getName(), e);
        }
    }
}
