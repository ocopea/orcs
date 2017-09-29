// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.testing;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceController;
import com.emc.microservice.bootstrap.SchemaBootstrap;
import com.emc.microservice.bootstrap.SchemaBootstrapRunner;
import com.emc.microservice.runner.MicroServiceRunner;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.util.database.BasicNativeQueryService;
import com.emc.ocopea.util.database.NativeQueryService;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with true love by liebea on 10/21/2014.
 * One helper to rule them all,
 * one helper to integrate them,
 * One helper bring them all, and in the darkness test them!
 * (Ash Nazg)
 */
public class MicroServiceIntegrationTestHelper {
    private final Map<String, MicroService> serviceDescriptors;
    private Map<String, MicroServiceController> serviceControllers;
    private final MockTestingResourceProvider resourceProvider;
    private final List<MicroService> sortedServiceDescriptors;
    private final DataSource dataSource;
    private final NativeQueryService nativeQueryService;

    public MicroServiceIntegrationTestHelper(List<MicroService> serviceDescriptors) {
        this(serviceDescriptors, "integration" + System.currentTimeMillis() + "-TestDB-" + System.currentTimeMillis());
    }

    public MicroServiceIntegrationTestHelper(List<MicroService> serviceDescriptors, String dataSourceName) {
        sortedServiceDescriptors = new ArrayList<>(serviceDescriptors);
        this.serviceDescriptors = new HashMap<>();
        for (MicroService currMS : serviceDescriptors) {
            this.serviceDescriptors.put(currMS.getIdentifier().getShortName(), currMS);
        }

        MockTestingResourceProvider.MockDatasourceConfiguration mockDatasourceConfiguration =
                new MockTestingResourceProvider.MockDatasourceConfiguration(dataSourceName);
        dataSource = MicroServiceTestDataSourceProvider.getDataSource(mockDatasourceConfiguration);
        this.resourceProvider = new MockTestingResourceProvider(dataSource);
        this.nativeQueryService = new BasicNativeQueryService(dataSource);
    }

    /**
     * @deprecated Please use alternate constructors moving forward, which are closer to production (they use resource
     *             providers as opposed to creating a NativeQueryService impl first)
     */
    public MicroServiceIntegrationTestHelper(
            List<MicroService> serviceDescriptors,
            MicroServiceTestNativeQueryServiceImpl nativeQueryService) {
        sortedServiceDescriptors = new ArrayList<>(serviceDescriptors);
        this.serviceDescriptors = new HashMap<>();
        for (MicroService currMS : serviceDescriptors) {
            this.serviceDescriptors.put(currMS.getIdentifier().getShortName(), currMS);
        }

        if (nativeQueryService == null) {
            this.nativeQueryService = new MicroServiceTestNativeQueryServiceImpl(
                    "integration" + System.currentTimeMillis() + "-TestDB-" + System.currentTimeMillis());
        } else {
            this.nativeQueryService = nativeQueryService;
        }
        this.dataSource = nativeQueryService.getDataSource();
        this.resourceProvider = new MockTestingResourceProvider(dataSource);
    }

    @NoJavadoc
    public void startServiceInTestMode() {
        Map<String, MicroServiceController> controllers = new MicroServiceRunner().run(
                resourceProvider,
                sortedServiceDescriptors.toArray(new MicroService[sortedServiceDescriptors.size()]));
        serviceControllers = new HashMap<>(controllers);
        controllers
                .values()
                .stream()
                .filter(currService -> dataSource instanceof HikariDataSource)
                .forEach(currService -> {
                    HikariDataSource hds = ((HikariDataSource) dataSource);
                    if (hds.getMetricRegistry() == null) {
                        hds.setMetricRegistry(currService.getMetricRegistry().getRegistry());
                    }
                });
    }

    public void stopTestMode() {
        serviceControllers.values().forEach(MicroServiceController::stop);
    }

    @NoJavadoc
    public <T> T getServiceResource(Class<T> resourceClass, String serviceURI) {
        MicroServiceController serviceController = serviceControllers.get(serviceURI);
        return serviceController
                .getContext()
                .getServiceDiscoveryManager()
                .discoverServiceConnection(serviceController.getBaseURI())
                .resolve(resourceClass);
    }

    public MockTestingResourceProvider getResourceProvider() {
        return resourceProvider;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public NativeQueryService getNativeQueryService() {
        return nativeQueryService;
    }

    @NoJavadoc
    public <T> void executeService(String msURI, T objectToSend, Class<T> format) throws IOException {
        MicroServiceTestHelper.executeService(
                serviceDescriptors.get(msURI),
                resourceProvider,
                objectToSend,
                format,
                null,
                serviceControllers.get(msURI).getContext());
    }

    @NoJavadoc
    public void createOrUpgradeSchema(SchemaBootstrap schemaBootstrap) throws IOException, SQLException {
        SchemaBootstrapRunner.runBootstrap(
                dataSource,
                schemaBootstrap,
                MockTestingResourceProvider.makeSchemaSafe(schemaBootstrap.getSchemaName()),
                "some_role");
    }

}
